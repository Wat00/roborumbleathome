package roborumbleathome.coordinator.model.participant;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ParticipantFactory {

    private final Map<String, Participant> participants;

    private final Map<String, Participant> participantsByRumbleName;

    public ParticipantFactory() {
	participants = new HashMap<String, Participant>();
	participantsByRumbleName = new HashMap<String, Participant>();
    }

    public Participant getParticipant(String name) {
	Participant participant = participants.get(name);
	if (participant == null) {
	    participant = new Participant(name);
	    participants.put(name, participant);
	    participantsByRumbleName.put(name.replaceAll(" ", "_"), participant);
	}
	return participant;
    }

    public Participant findParticipantByRumbleName(String rumbleName) {
	return participantsByRumbleName.get(rumbleName);
    }

    public Collection<Participant> getAllParticipants() {
	return participants.values();
    }

}