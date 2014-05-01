package roborumbleathome.worker.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import robocode.BattleResults;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;
import robocode.control.events.BattleAdaptor;
import robocode.control.events.BattleCompletedEvent;
import robocode.control.events.BattleErrorEvent;
import robocode.control.events.IBattleListener;
import roborumbleathome.coordinator.model.participant.ParticipantTO;
import roborumbleathome.coordinator.service.BattleResultTO;
import roborumbleathome.coordinator.service.BattleResultsTO;
import roborumbleathome.coordinator.service.BattleTO;

public class RunBattleController {

    private static final Logger LOGGER = Logger.getLogger(RunBattleController.class.getName());

    static class SkippingBattleException extends Exception {

	private final StringBuffer namesBuffer;

	SkippingBattleException(StringBuffer namesBuffer) {
	    this.namesBuffer = namesBuffer;
	}

	StringBuffer getNamesBuffer() {
	    return namesBuffer;
	}

    }

    private final BlockingQueue<BattleCompletedEvent> eventQueue;

    private final RobocodeEngine robocodeEngine;

    private final Map<String, RobotSpecification> localRepository;

    private String version;

    public RunBattleController(RobocodeEngine robocodeEngine, File robocodeHome) {
	this.robocodeEngine = robocodeEngine;

	eventQueue = new LinkedBlockingQueue<BattleCompletedEvent>();
	IBattleListener listener = new BattleCompletedListener(eventQueue);
	robocodeEngine.addBattleListener(listener);
	localRepository = new HashMap<String, RobotSpecification>();

	version = robocodeEngine.getVersion();
    }

    void refreshLocalRepository(BattleTO battleTO) {
	boolean missingBots = false;
	for (Iterator<ParticipantTO> i = battleTO.getRobots().iterator(); i.hasNext() && !missingBots;) {
	    ParticipantTO participant = i.next();
	    if (localRepository.get(participant.getRobot()) == null) {
		missingBots = true;
	    }
	}

	if (missingBots) {
	    localRepository.clear();
	    for (RobotSpecification robot : robocodeEngine.getLocalRepository()) {
		localRepository.put(robot.getNameAndVersion(), robot);
	    }
	}
    }

    BattleResultsTO runBattle(BattleTO battleTO) throws SkippingBattleException {
	try {
	    BattleSpecification battleSpecification;
	    {
		List<ParticipantTO> robotTOs = battleTO.getRobots();

		StringBuffer namesBuffer = new StringBuffer();
		for (Iterator<ParticipantTO> i = robotTOs.iterator(); i.hasNext();) {
		    ParticipantTO robot = i.next();
		    namesBuffer.append(robot.getRobot());
		    if (i.hasNext()) {
			namesBuffer.append(',');
		    }
		}
		RobotSpecification[] robots = new RobotSpecification[robotTOs.size()];
		for (int i = 0; i < robotTOs.size(); ++i) {
		    ParticipantTO robotTO = robotTOs.get(i);
		    robots[i] = localRepository.get(robotTO.getRobot());
		    if (robots[i] == null) {
			throw new SkippingBattleException(namesBuffer);
		    }
		}

		BattlefieldSpecification battlefieldSize = new BattlefieldSpecification(battleTO.getWidth(), battleTO.getHeight());
		battleSpecification = new BattleSpecification(battleTO.getNumRounds(), battleTO.getInactivityTime(), battleTO.getGunCoolingRate(), battlefieldSize, robots);

		LOGGER.info("Fighting battle " + battleTO.getId() + " ... " + namesBuffer);
	    }

	    System.gc();
	    Thread.yield();
	    robocodeEngine.runBattle(battleSpecification, true);
	    long time = System.currentTimeMillis();

	    {
		BattleCompletedEvent event = eventQueue.take();
		BattleResults[] sortedResults = event.getSortedResults();
		if (sortedResults.length == 2) {
		    LOGGER.info("RESULT = " + sortedResults[0].getTeamLeaderName() + " wins " + sortedResults[0].getScore() + " to " + sortedResults[1].getScore());
		} else {
		    LOGGER.info("RESULT = " + sortedResults[0].getTeamLeaderName() + " wins, " + sortedResults[1].getTeamLeaderName() + " is second.");
		}

		List<ParticipantTO> robotTOs = battleTO.getRobots();
		List<BattleResultTO> battleResultList = new ArrayList<BattleResultTO>();

		BattleResults[] indexedResults = event.getIndexedResults();
		for (int i = 0; i < indexedResults.length; ++i) {
		    BattleResults result = indexedResults[i];
		    ParticipantTO robotTO = robotTOs.get(i);
		    BattleResultTO battleResult = new BattleResultTO(robotTO, result.getScore(), result.getBulletDamage(), result.getFirsts());
		    battleResultList.add(battleResult);
		}
		return new BattleResultsTO(version, time, battleResultList);
	    }

	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}
    }
}

class BattleCompletedListener extends BattleAdaptor {

    private static final Logger LOGGER = Logger.getLogger(BattleCompletedListener.class.getName());

    private final BlockingQueue<BattleCompletedEvent> eventQueue;

    BattleCompletedListener(BlockingQueue<BattleCompletedEvent> eventQueue) {
	this.eventQueue = eventQueue;
    }

    @Override
    public void onBattleError(BattleErrorEvent event) {
	LOGGER.warning(event.getError());
    }

    @Override
    public void onBattleCompleted(BattleCompletedEvent event) {
	try {

	    eventQueue.put(event);

	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}
    }
}