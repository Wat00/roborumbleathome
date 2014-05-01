package roborumbleathome.coordinator.integration.rumbleserver;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class AsyncUploadResultsThrottler {

    private final UploadResultsThrottler uploadResultsThrottler;

    private final Executor uploadResultsThrottlerExecutor;

    public AsyncUploadResultsThrottler(UploadResultsThrottler uploadResultsThrottler) {
	this.uploadResultsThrottler = uploadResultsThrottler;
	uploadResultsThrottlerExecutor = Executors.newSingleThreadExecutor();
    }

    void onUploadOk() {
	uploadResultsThrottlerExecutor.execute(new Runnable() {
	    public void run() {

		uploadResultsThrottler.onUploadOk();

	    }
	});
    }

    void onUploadError() {
	uploadResultsThrottlerExecutor.execute(new Runnable() {
	    public void run() {

		uploadResultsThrottler.onUploadError();

	    }
	});
    }

    public void addWorker() {
	uploadResultsThrottlerExecutor.execute(new Runnable() {
	    public void run() {

		uploadResultsThrottler.addWorker();

	    }
	});
    }
}