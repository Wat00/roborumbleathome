package roborumbleathome.worker.controller;

import java.io.File;
import java.util.logging.Logger;

import roborumbleathome.coordinator.model.participant.ParticipantTO;
import roborumbleathome.coordinator.service.BattleTO;
import roborumbleathome.worker.integration.CoordinatorProxy;
import roborumbleathome.worker.integration.CoordinatorProxy.CannotCommunicateWithCoordinatorProcessException;
import roborumbleathome.worker.persistence.WorkerBotDAO;

public class DownloadRobotsController {

    private static final Logger LOGGER = Logger.getLogger(DownloadRobotsController.class.getName());

    private final CoordinatorProxy coordinatorProxy;

    private final WorkerBotDAO workerBotDAO;

    public DownloadRobotsController(CoordinatorProxy coordinatorProxy, WorkerBotDAO workerBotDAO) {
	this.coordinatorProxy = coordinatorProxy;
	this.workerBotDAO = workerBotDAO;
    }

    void downloadRobots(BattleTO battleTO) throws CannotCommunicateWithCoordinatorProcessException {
	for (ParticipantTO robot : battleTO.getRobots()) {
	    if (!workerBotDAO.exists(robot)) {
		byte[] robotContent = coordinatorProxy.downloadRobot(robot);
		File botsRepFile = workerBotDAO.saveRobot(robot, robotContent);

		LOGGER.info("Downloaded " + robot.getRobot() + " into " + botsRepFile);
	    }
	}
    }

}
