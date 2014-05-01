package roborumbleathome.coordinator.controller.downloadmissingbots;

import java.util.concurrent.Callable;

import roborumbleathome.coordinator.integration.BotProxy.CouldNotDownloadException;
import roborumbleathome.coordinator.integration.BotProxy.CouldNotFindException;
import roborumbleathome.coordinator.model.participant.ParticipantTO;
import roborumbleathome.coordinator.persistence.BotDAO.DownloadedFileWrongCorruptedException;
import roborumbleathome.coordinator.persistence.BotDAO.NotAbleReadPropertiesException;

public class DownloadMissingBotsCallable implements Callable<String> {

    private final DownloadMissingBotsController downloadMissingBotsController;

    private final ParticipantTO participant;

    public DownloadMissingBotsCallable(DownloadMissingBotsController downloadMissingBotsController, ParticipantTO participant) {
	this.downloadMissingBotsController = downloadMissingBotsController;
	this.participant = participant;
    }

    public String call() throws CouldNotFindException, CouldNotDownloadException, NotAbleReadPropertiesException, DownloadedFileWrongCorruptedException {
	return downloadMissingBotsController.download(participant);
    }

}