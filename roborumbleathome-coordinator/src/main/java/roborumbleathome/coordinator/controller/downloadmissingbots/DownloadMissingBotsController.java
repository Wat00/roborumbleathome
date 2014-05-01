package roborumbleathome.coordinator.controller.downloadmissingbots;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.File;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import roborumbleathome.coordinator.integration.BotProxy;
import roborumbleathome.coordinator.integration.BotProxy.CouldNotDownloadException;
import roborumbleathome.coordinator.integration.BotProxy.CouldNotFindException;
import roborumbleathome.coordinator.model.participant.ParticipantTO;
import roborumbleathome.coordinator.persistence.BotDAO;
import roborumbleathome.coordinator.persistence.BotDAO.DownloadedFileWrongCorruptedException;
import roborumbleathome.coordinator.persistence.BotDAO.NotAbleReadPropertiesException;

public class DownloadMissingBotsController {

    private static final Logger LOGGER = Logger.getLogger(DownloadMissingBotsController.class.getName());

    private final BotDAO botDAO;

    private final BotProxy botProxy;

    public DownloadMissingBotsController(BotDAO botDAO, BotProxy botProxy) {
	this.botDAO = botDAO;
	this.botProxy = botProxy;
    }

    String download(ParticipantTO participant) throws CouldNotFindException, CouldNotDownloadException, NotAbleReadPropertiesException, DownloadedFileWrongCorruptedException {
	try {
	    LOGGER.fine("Downloading " + participant.getRobot() + " ...");
	    File tempFile = botProxy.downloadBot(participant);
	    botDAO.checkJarFile(tempFile, participant);
	    File botsRepFile = botDAO.moveFile(tempFile, participant);
	    LOGGER.info("Downloaded " + participant.getRobot() + " into " + botsRepFile);
	    return participant.getRobot();

	} catch (CouldNotFindException e) {
	    LOGGER.warning("Could not find " + e.getRobot() + " from " + e.getUrl());
	    throw e;

	} catch (CouldNotDownloadException e) {
	    LOGGER.warning("Could not download " + e.getFile().getName());
	    throw e;

	} catch (NotAbleReadPropertiesException e) {
	    LOGGER.warning("Not able to read properties");
	    LOGGER.warning("Downloaded file is wrong or corrupted:" + e.getFile().getName());
	    throw e;

	} catch (DownloadedFileWrongCorruptedException e) {
	    Throwable cause = e.getCause();
	    if (cause != null) {
		if (cause instanceof ZipException) {
		    LOGGER.log(WARNING, "" + cause, cause);
		} else {
		    LOGGER.log(SEVERE, "" + cause, cause);
		}
	    }
	    LOGGER.warning("Downloaded file is wrong or corrupted:" + e.getFile().getName());
	    throw e;
	}
    }

}