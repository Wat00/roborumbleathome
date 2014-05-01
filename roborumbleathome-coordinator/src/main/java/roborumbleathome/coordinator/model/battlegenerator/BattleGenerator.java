package roborumbleathome.coordinator.model.battlegenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.Game;
import roborumbleathome.coordinator.model.participant.Participant;
import roborumbleathome.coordinator.model.participant.ParticipantTO;
import roborumbleathome.coordinator.service.BattleTO;

public class BattleGenerator {

    private static final Logger LOGGER = Logger.getLogger(BattleGenerator.class.getName());

    private final Configuration configuration;

    private final Random random;

    private final Map<String, Participant> participantsByName;

    // private final List<PairingTO> allPairings;
    //
    private final Set<PairingTO> priorityPairings;

    private final Map<PairingTO, Pairing> allPairings;
    private int battleId;

    private boolean unstableRankings;

    public BattleGenerator(Configuration configuration) {
	this.configuration = configuration;
	this.random = new Random();
	participantsByName = new HashMap<String, Participant>();
	// allPairings = new ArrayList<PairingTO>();
	priorityPairings = new HashSet<PairingTO>();
	allPairings = new HashMap<PairingTO, Pairing>();
	unstableRankings = true;
    }

    void setParticipants(List<Participant> participants) {
	participantsByName.clear();
	for (Participant participant : participants) {
	    participantsByName.put(participant.getRobot(), participant);
	}
	Set<String> allNames = new HashSet<String>(participantsByName.keySet());

	// Collection<PairingTO> oldPairings = new LinkedHashSet<PairingTO>();
	// List<PairingTO> newPairings = new ArrayList<PairingTO>();

	// exclude old participants
	for (Iterator<Entry<PairingTO, Pairing>> i = allPairings.entrySet().iterator(); i.hasNext();) {
	    Entry<PairingTO, Pairing> entry = i.next();
	    PairingTO pairingTO = entry.getKey();
	    if (!allNames.contains(pairingTO.getRobot1()) || !allNames.contains(pairingTO.getRobot2())) {
		i.remove();
		unstableRankings = true;
	    }
	}

	boolean newPairings = false;
	// add new participants
	for (int i = 0; i < participants.size() - 1; ++i) {
	    Participant participantI = participants.get(i);
	    for (int j = i + 1; j < participants.size(); ++j) {
		Participant participantJ = participants.get(j);
		PairingTO newPairingTO = new PairingTO(participantI.getRobot(), participantJ.getRobot());
		Pairing pairing = allPairings.get(newPairingTO);
		if (pairing == null) {
		    pairing = new Pairing();
		    allPairings.put(newPairingTO, pairing);

		    newPairings = true;
		}

		//
		// if (!oldPairings.contains(newPairingTO)) {
		// newPairings.add(newPairingTO);
		// }
	    }
	}

	if (newPairings) {
	    List<Pairing> randomPairings = new ArrayList<Pairing>(allPairings.values());
	    Collections.shuffle(randomPairings, random);

	    for (int i = 0; i < randomPairings.size(); ++i) {
		Pairing pairing = randomPairings.get(i);
		pairing.setRandom(i);
	    }
	}

	// allPairings.clear();
	// allPairings.addAll(oldPairings);
	// allPairings.addAll(newPairings);

	// exclude old participants from priority battles
	for (Iterator<PairingTO> i = priorityPairings.iterator(); i.hasNext();) {
	    PairingTO priorityPairing = i.next();
	    if (!allPairings.containsKey(priorityPairing)) {
		i.remove();
	    }
	}

	if (unstableRankings) {
	    for (Participant participant : participantsByName.values()) {
		participant.setUnstable(true);
	    }
	    unstableRankings = false;
	}
    }

    boolean isParticipantsEmpty() {
	return participantsByName.isEmpty();
    }

    private int compareTo(Entry<PairingTO, Pairing> entry1, Entry<PairingTO, Pairing> entry2) {
	PairingTO pairingTO1 = entry1.getKey();
	PairingTO pairingTO2 = entry2.getKey();

	Participant participant11 = participantsByName.get(pairingTO1.getRobot1());
	Participant participant12 = participantsByName.get(pairingTO1.getRobot2());

	Participant participant21 = participantsByName.get(pairingTO2.getRobot1());
	Participant participant22 = participantsByName.get(pairingTO2.getRobot2());

	// participants needing 1 battle
	boolean unstable1 = participant11.isUnstable() || participant12.isUnstable();
	boolean unstable2 = participant21.isUnstable() || participant22.isUnstable();
	if (unstable1 && !unstable2) {
	    return -1;
	} else if (!unstable1 && unstable2) {
	    return 1;
	}

	if (priorityPairings.isEmpty()) {
	    // low server battle count
	    int battlesPerBot = configuration.getBattlesPerBot();
	    boolean battles1 = false;
	    boolean battles2 = false;
	    for (Game game : configuration.getGames()) {
		if (!battles1) {
		    Integer battles11 = participant11.getBattles(game);
		    Integer battles12 = participant12.getBattles(game);
		    if (battles11 != null && battles12 != null && (battles11 < battlesPerBot || battles12 < battlesPerBot)) {
			battles1 = true;
		    }
		}
		if (!battles2) {
		    Integer battles21 = participant21.getBattles(game);
		    Integer battles22 = participant22.getBattles(game);
		    if (battles21 != null && battles22 != null && (battles21 < battlesPerBot || battles22 < battlesPerBot)) {
			battles2 = true;
		    }
		}
	    }
	    if (battles1 && !battles2) {
		return -1;
	    } else if (!battles1 && battles2) {
		return 1;
	    }

	} else {
	    // priority battles
	    boolean priority1 = priorityPairings.contains(pairingTO1);
	    boolean priority2 = priorityPairings.contains(pairingTO2);
	    if (priority1 && !priority2) {
		return -1;
	    } else if (!priority1 && priority2) {
		return 1;
	    }
	}

	Pairing pairing1 = entry1.getValue();
	Pairing pairing2 = entry2.getValue();

	// low local battle count
	if (pairing1.getBattles() < pairing2.getBattles()) {
	    return -1;
	} else if (pairing1.getBattles() > pairing2.getBattles()) {
	    return 1;
	}

	// random tiebreaking
	if (pairing1.getRandom() < pairing2.getRandom()) {
	    return -1;
	} else if (pairing1.getRandom() > pairing2.getRandom()) {
	    return 1;
	}

	return 0;
    }

    PairingTO calculatePriorityPairing() {
	Iterator<Entry<PairingTO, Pairing>> i = allPairings.entrySet().iterator();
	Entry<PairingTO, Pairing> bestEntry = i.next();
	while (i.hasNext()) {
	    Entry<PairingTO, Pairing> entry = i.next();
	    if (compareTo(entry, bestEntry) < 0) {
		bestEntry = entry;
	    }
	}
	// for (int i = 1; i < allPairings.size(); ++i) {
	// PairingTO pairing = allPairings.get(i);
	// if (compareTo(pairing, bestPairing) < 0) {
	// bestPairing = pairing;
	// }
	// }
	return bestEntry.getKey();
    }

    BattleTO generateBattle() {
	List<Participant> candidates = new ArrayList<Participant>(participantsByName.values());
	int competitors = configuration.getCompetitors();

	List<ParticipantTO> robots = new ArrayList<ParticipantTO>();

	// use 2 slots with priority battle
	PairingTO priorityPairing = calculatePriorityPairing();

	Participant participant1 = participantsByName.get(priorityPairing.getRobot1());
	ParticipantTO participantTO1 = participant1.createTO();
	robots.add(participantTO1);
	candidates.remove(participant1);
	participant1.setUnstable(false);

	Participant participant2 = participantsByName.get(priorityPairing.getRobot2());
	ParticipantTO participantTO2 = participant2.createTO();
	robots.add(participantTO2);
	candidates.remove(participant2);
	participant2.setUnstable(false);

	// fill remaining slots with random participants
	for (int i = competitors; i > 2; --i) {
	    int index = random.nextInt(candidates.size());
	    Participant participant = candidates.remove(index);
	    robots.add(participant.createTO());
	    participant.setUnstable(false);
	}
	Collections.shuffle(robots, random);

	for (int i = 0; i < robots.size() - 1; ++i) {
	    ParticipantTO participantToI = robots.get(i);
	    for (int j = i + 1; j < robots.size(); ++j) {
		ParticipantTO participantToJ = robots.get(j);
		PairingTO pairingTO = new PairingTO(participantToI.getRobot(), participantToJ.getRobot());
		Pairing pairing = allPairings.get(pairingTO);
		pairing.incrementBattle();
		// allPairings.remove(pairing);
		// allPairings.add(pairing); // move to end of list

		priorityPairings.remove(pairingTO);
	    }
	}

	++battleId;

	return new BattleTO(battleId, configuration.getRounds(), configuration.getInactivityTime(), configuration.getGunCoolingRate(), configuration.isHideEnemyNames(),
		configuration.getWidth(), configuration.getHeight(), robots);
    }

    void updateBattles(UpdateBattlesTO updateBattlesTO) {
	Participant participant = participantsByName.get(updateBattlesTO.getParticipant().getRobot());
	participant.setBattles(updateBattlesTO.getGame(), updateBattlesTO.getBattles());
    }

    void addPriorityPairing(PairingTO priorityPairingTO) {
	if (allPairings.containsKey(priorityPairingTO)) {
	    priorityPairings.add(priorityPairingTO);
	} else {
	    LOGGER.warning("Ignoring: " + priorityPairingTO.getRobot1() + "," + priorityPairingTO.getRobot2());
	}
    }

}