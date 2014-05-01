package roborumbleathome.coordinator.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import roborumbleathome.coordinator.integration.rumbleserver.UploadResultsProxy;
import roborumbleathome.coordinator.model.battlegenerator.AsyncBattleGenerator;
import roborumbleathome.coordinator.model.participant.ParticipantTO;
import roborumbleathome.coordinator.persistence.BotDAO;

public class CoordinatorService extends UnicastRemoteObject implements ICoordinatorService {

    private static final Logger LOGGER = Logger.getLogger(CoordinatorService.class.getName());

    private final BotDAO botDAO;

    // private final Random random;

    // private final Executor uploadResultsExecutor;

    // private List<Participant> validParticipants;
    private final UploadResultsProxy uploadResultsProxy;
    // private MainController mainController;

    private final AsyncBattleGenerator battleGenerator;

    // private final AsyncUploadResultsThrottler uploadResultsThrottler;

    // private ScheduledExecutorService mainScheduledExecutorService;

    public CoordinatorService(BotDAO botDAO, UploadResultsProxy uploadResultsProxy, AsyncBattleGenerator battleGenerator) throws RemoteException {
	this.botDAO = botDAO;
	// this.random = random;
	this.battleGenerator = battleGenerator;
	// this.uploadResultsExecutor = battleResultsExecutor;
	this.uploadResultsProxy = uploadResultsProxy;
	// this.uploadResultsThrottler = uploadResultsThrottler;

    }

    // public void addWorker() {
    // uploadResultsThrottler.addWorker();
    // }

    // public synchronized void setValidParticipants(List<Participant>
    // validParticipants) {
    // this.validParticipants = validParticipants;
    // }

    public byte[] downloadRobot(ParticipantTO robot) {
	byte[] data = botDAO.loadRobot(robot);
	LOGGER.fine("Sendind " + robot.getRobot() + " ...");
	return data;
    }

    public BattleTO generateBattle() {
	LOGGER.fine("Preparing battle ...");
	BattleTO battle = battleGenerator.generateBattle();
	LOGGER.fine("Sendind battle ...");
	return battle;
    }

    public void uploadResults(BattleResultsTO battleResults) {
	uploadResultsProxy.uploadBattle(battleResults);
    }
}