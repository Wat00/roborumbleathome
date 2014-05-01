package roborumbleathome.coordinator.integration.rumbleserver;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import roborumbleathome.coordinator.model.Configuration;

public class UploadResultsThrottler {

    private static final Logger LOGGER = Logger.getLogger(UploadResultsThrottler.class.getName());

    private final ThreadPoolExecutor uploadResultsExecutor;

    private final Configuration configuration;

    private int maximumPoolSize;

    private int ignoredQueueSize;

    private boolean ignoreNextQueueSize;

    private int workers;

    public UploadResultsThrottler(ThreadPoolExecutor uploadResultsExecutor, Configuration configuration) {
	this.uploadResultsExecutor = uploadResultsExecutor;
	this.configuration = configuration;
	maximumPoolSize = 1;
    }

    void onUploadOk() {
	int queueSize = uploadResultsExecutor.getQueue().size();
	if (ignoreNextQueueSize || queueSize < ignoredQueueSize) {
	    ignoredQueueSize = queueSize;
	    ignoreNextQueueSize = false;
	}

	int competitors = configuration.getCompetitors();
	int uploadsPerBattle = (competitors * (competitors - 1)) / 2 * configuration.getGames().size();
	int maximumQueueSize = uploadsPerBattle * Math.max(workers, 1) * 2;
	int threshold = ignoredQueueSize + maximumQueueSize;

	if (queueSize > threshold) {
	    ++maximumPoolSize;
	    ignoredQueueSize = queueSize;
	}

	setPoolSize(maximumPoolSize);
	LOGGER.fine("Pool size: " + uploadResultsExecutor.getMaximumPoolSize() + " Queue size: " + queueSize + " Threshold: " + threshold);
    }

    void onUploadError() {
	setPoolSize(1);
	ignoreNextQueueSize = true;
    }

    public void addWorker() {
	++workers;
	ignoreNextQueueSize = true;
    }

    private void setPoolSize(int poolSize) {
	if (uploadResultsExecutor.getMaximumPoolSize() != poolSize) {
	    LOGGER.info("Setting upload pool size to " + poolSize);
	    uploadResultsExecutor.setCorePoolSize(poolSize);
	    uploadResultsExecutor.setMaximumPoolSize(poolSize);
	}
    }
}