package roborumbleathome.coordinator.controller;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import roborumbleathome.coordinator.controller.downloadmissingbots.DownloadMissingBotsCallable;
import roborumbleathome.coordinator.controller.downloadmissingbots.DownloadMissingBotsController;
import roborumbleathome.coordinator.integration.rumbleserver.RumbleServerProxy;
import roborumbleathome.coordinator.integration.rumbleserver.RumbleServerProxy.UpdateBotsUrlNotDefinedException;
import roborumbleathome.coordinator.integration.wiki.WikiProxy;
import roborumbleathome.coordinator.integration.wiki.WikiProxy.ParticipantsListEmptyException;
import roborumbleathome.coordinator.integration.wiki.WikiProxy.ResponseNotOkException;
import roborumbleathome.coordinator.integration.wiki.WikiProxy.UnableRetrieveParticipantsListException;
import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.Game;
import roborumbleathome.coordinator.model.battlegenerator.AsyncBattleGenerator;
import roborumbleathome.coordinator.model.participant.Participant;
import roborumbleathome.coordinator.model.participant.ParticipantFactory;
import roborumbleathome.coordinator.model.participant.ParticipantTO;
import roborumbleathome.coordinator.persistence.BotDAO;
import roborumbleathome.coordinator.persistence.ParticipantDAO;
import roborumbleathome.coordinator.persistence.ParticipantDAO.CantCreateDirectoryException;
import roborumbleathome.coordinator.persistence.ParticipantDAO.ParticipantsFileNotFoundException;
import roborumbleathome.coordinator.persistence.ParticipantDAO.UnableSaveParticipantsListException;

public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    static class ParticipantsListNotDownloadedException extends Exception {

	public ParticipantsListNotDownloadedException(Throwable cause) {
	    super(cause);
	}

    }

    public static class DownloadRunnable implements Runnable {

	private final MainController mainController;

	public DownloadRunnable(MainController mainController) {
	    this.mainController = mainController;
	}

	public void run() {
	    try {
		mainController.download();
	    } catch (Throwable e) {
		LOGGER.log(Level.SEVERE, "" + e, e);
	    }
	}
    }

    private final Configuration configuration;

    // private final ParticipantFactory participantFactory;

    private final WikiProxy participantsProxy;

    private final ParticipantDAO participantDAO;

    private final BotDAO botDAO;

    // private final BotProxy botProxy;

    private final ExecutorService downloadMissingBotsExecutorService;

    private final DownloadMissingBotsController downloadMissingBotsController;

    private final RumbleServerProxy rumbleServerProxy;

    // private final Random random;
    // private final CoordinatorService battleService;

    private final AsyncBattleGenerator battleGenerator;

    public MainController(Configuration configuration, WikiProxy participantsProxy, ParticipantDAO participantDAO, BotDAO botDAO, ExecutorService downloadMissingBotsExecutorService,
	    DownloadMissingBotsController downloadMissingBotsController, RumbleServerProxy rumbleServerProxy, AsyncBattleGenerator battleGenerator) {
	this.configuration = configuration;
	// this.participantFactory = participantFactory;
	this.participantsProxy = participantsProxy;
	this.participantDAO = participantDAO;
	this.botDAO = botDAO;
	// this.botProxy = botProxy;
	this.downloadMissingBotsExecutorService = downloadMissingBotsExecutorService;
	this.downloadMissingBotsController = downloadMissingBotsController;
	this.rumbleServerProxy = rumbleServerProxy;
	// this.random = random;
	this.battleGenerator = battleGenerator;
	// this.battleService = battleService;
    }

    // public void main() {
    //
    // initializeDirectories(configuration);
    //
    // // battleService.setValidParticipants(validParticipants);
    //
    // }

    public void initializeDirectories() {
	File botsRep = configuration.getBotsRep();
	if (botsRep.isFile() || !botsRep.exists() && !botsRep.mkdirs()) {
	    LOGGER.severe("Can't create directory: " + botsRep);
	}

	File temp = configuration.getTemp();
	if (temp.isFile() || !temp.exists() && !temp.mkdirs()) {
	    LOGGER.severe("Can't create directory: " + temp);
	}
    }

    private void download() {
	try {

	    initializeDirectories();

	    ParticipantFactory participantFactory = new ParticipantFactory();

	    boolean participantsListDownloaded;
	    Collection<Participant> allParticipants;
	    try {
		allParticipants = downloadParticipantsList(participantFactory);
		participantsListDownloaded = true;

	    } catch (ParticipantsListNotDownloadedException e) {
		allParticipants = participantDAO.loadParticipants(participantFactory);
		participantsListDownloaded = false;
	    }

	    List<Participant> validParticipants = downloadMissingBots(allParticipants, participantFactory);
	    updateCodesize(validParticipants);

	    LOGGER.info("Downloading rating files ...");
	    Map<Game, Collection<String>> rumbleNamesToDeleteMap = rumbleServerProxy.downloadRatingFiles(participantFactory);

	    if (participantsListDownloaded) {
		removeOldParticipantsFromServer(rumbleNamesToDeleteMap);
	    }

	    battleGenerator.setParticipants(validParticipants);

	    LOGGER.fine("Download complete");

	} catch (ParticipantsFileNotFoundException e) {
	    LOGGER.severe("Participants file not found ... Aborting");
	    LOGGER.log(SEVERE, "" + e.getCause(), e.getCause());

	} catch (InterruptedException e) {
	    throw new RuntimeException(e);

	}
    }

    private Collection<Participant> downloadParticipantsList(ParticipantFactory participantFactory) throws ParticipantsListNotDownloadedException {
	LOGGER.info("Downloading participants list ...");
	Collection<Participant> participants;

	try {
	    participants = participantsProxy.downloadParticipantsList(participantFactory);
	    try {
		participantDAO.saveParticipants(participants);

	    } catch (CantCreateDirectoryException e) {
		LOGGER.severe("Can't create directory: " + e.getDirectory());

	    } catch (UnableSaveParticipantsListException e) {
		LOGGER.severe("Unable to retrieve participants list:");
		LOGGER.log(SEVERE, "" + e.getCause(), e.getCause());

	    }
	} catch (ResponseNotOkException e) {
	    String msg = "Unable to retrieve participants list. Response is " + e.getResponseCode();
	    if (e.getMessage() != null) {
		msg += ": " + e.getMessage();
	    }
	    LOGGER.warning(msg);
	    throw new ParticipantsListNotDownloadedException(e);

	} catch (ParticipantsListEmptyException e) {
	    LOGGER.warning("The participants list is empty");
	    throw new ParticipantsListNotDownloadedException(e);

	} catch (UnableRetrieveParticipantsListException e) {
	    LOGGER.warning("Unable to retrieve participants list:");
	    LOGGER.log(WARNING, "" + e.getCause(), e.getCause());
	    throw new ParticipantsListNotDownloadedException(e);

	}

	return participants;
    }

    private List<Participant> downloadMissingBots(Collection<Participant> allParticipants, ParticipantFactory participantFactory) throws InterruptedException {
	LOGGER.info("Downloading missing bots ...");
	List<Participant> validParticipants = new ArrayList<Participant>();
	Collection<Future<String>> futures = new ArrayList<Future<String>>();
	for (Participant participant : allParticipants) {
	    ParticipantTO participantTO = participant.createTO();
	    if (!botDAO.fileExists(participantTO)) {
		Callable<String> task = new DownloadMissingBotsCallable(downloadMissingBotsController, participantTO);
		Future<String> future = downloadMissingBotsExecutorService.submit(task);
		futures.add(future);
	    } else {
		validParticipants.add(participant);
	    }
	}
	for (Future<String> future : futures) {
	    try {
		String name = future.get();
		Participant participant = participantFactory.getParticipant(name);
		validParticipants.add(participant);

	    } catch (ExecutionException e) {
		Throwable cause = e.getCause();
		if (cause instanceof RuntimeException) {
		    throw (RuntimeException) cause;
		} else if (cause instanceof Error) {
		    throw (Error) cause;
		}
		// ignore participant
	    }
	}
	return validParticipants;
    }

    private void updateCodesize(List<Participant> validParticipants) {
	LOGGER.fine("Updating codesize ...");
	for (Participant participant : validParticipants) {
	    if (participant.getCodesize() == null) {
		int codesize = botDAO.calculateCodesize(participant);
		participant.setCodesize(codesize);
	    }
	}
    }

    private void removeOldParticipantsFromServer(Map<Game, Collection<String>> rumbleNamesToDeleteMap) {
	LOGGER.info("Removing old participants from server ...");
	try {
	    rumbleServerProxy.removeOldParticipantsFromServer(rumbleNamesToDeleteMap);

	} catch (UpdateBotsUrlNotDefinedException e) {
	    LOGGER.severe("UPDATEBOTS URL not defined!");
	}
    }

    // private BattleTO generateRandomBattle() {
    // int index1 = random.nextInt(validParticipants.size());
    // int index2 = random.nextInt(validParticipants.size() - 1);
    // if (index2 >= index1) {
    // ++index2;
    // }
    // Participant participant1 = validParticipants.get(index1);
    // Participant participant2 = validParticipants.get(index2);
    // String robot1 = participant1.getRobot();
    // String robot2 = participant2.getRobot();
    // List<String> robots = new ArrayList<String>();
    // robots.add(robot1);
    // robots.add(robot2);
    //
    // return new BattleTO(configuration.getRounds(),
    // configuration.getInactivityTime(), configuration.getGunCoolingRate(),
    // configuration.isHideEnemyNames(), configuration.getWidth(),
    // configuration.getHeight(), robots);
    // }
}