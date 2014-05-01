package roborumbleathome.coordinator.integration.rumbleserver;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import roborumbleathome.coordinator.integration.rumbleserver.RumbleServerProxy.ErrorsFoundException;
import roborumbleathome.coordinator.model.Configuration;

public final class AsyncRumbleServerProxy {

    private static final Logger LOGGER = Logger.getLogger(AsyncRumbleServerProxy.class.getName());

    private final ThreadPoolExecutor uploadResultsExecutor;

    // private final ExecutorService errorExecutor;

//    private final BlockingQueue<UploadResultsTO> uploadResultsQueue;

    private final RumbleServerProxy rumbleServerProxy;

    private final Configuration configuration;
    
    public AsyncRumbleServerProxy(final RumbleServerProxy rumbleServerProxy, ThreadPoolExecutor uploadResultsExecutor, Configuration configuration) {// ,
														     // ExecutorService
														     // errorExecutor)
														     // {
	this.uploadResultsExecutor = uploadResultsExecutor;
	this.rumbleServerProxy = rumbleServerProxy;
	this.configuration = configuration;
	// this.errorExecutor = errorExecutor;
//	uploadResultsQueue = new LinkedBlockingQueue<UploadResultsTO>(32);

//	startReceivingResults();
    }

    void uploadResults(final UploadResultsTO uploadResultsTO) {
//    private void startReceivingResults() {
	uploadResultsExecutor.execute(new Runnable() {
	    public void run() {
		try {

//		    final UploadResultsTO uploadResultsTO = uploadResultsQueue.take();
//		    boolean errorsFound;
//		    do {
			try {
			    rumbleServerProxy.uploadResults(uploadResultsTO);

			    int uploadPoolSize = configuration.getUploadPoolSize();
			    uploadResultsExecutor.setCorePoolSize(uploadPoolSize);
			    uploadResultsExecutor.setMaximumPoolSize(uploadPoolSize);
//			    errorsFound = false;


			} catch (ErrorsFoundException e) {
			    if (e.getCause() != null) {
				LOGGER.log(WARNING, "" + e.getCause(), e.getCause());
			    }
			    LOGGER.warning("Unable to upload results " + uploadResultsTO.getBattleResult1().getParticipantTO().getRobot() + ","
				    + uploadResultsTO.getBattleResult1().getScore() + "," + uploadResultsTO.getBattleResult1().getBulletDamage() + ","
				    + uploadResultsTO.getBattleResult1().getFirsts() + " " + uploadResultsTO.getBattleResult2().getParticipantTO().getRobot() + ","
				    + uploadResultsTO.getBattleResult2().getScore() + "," + uploadResultsTO.getBattleResult2().getBulletDamage() + ","
				    + uploadResultsTO.getBattleResult2().getFirsts());

			    uploadResultsExecutor.setCorePoolSize(1);
			    uploadResultsExecutor.setMaximumPoolSize(1);

			    uploadResultsExecutor.execute(this);
			    
			    Thread.sleep(10000);
//			    errorsFound = true;

			    // retry(uploadResultsTO);
			}
//		    } while (errorsFound);
//		    uploadResultsExecutor.execute(this);

		} catch (InterruptedException e) {
		    LOGGER.log(FINE, "" + e, e);
		}
	    }
	});
    }

    // private void retry(final UploadResultsTO uploadResultsTO) {
    // errorExecutor.execute(new Runnable() {
    // public void run() {
    // try {
    //
    // uploadResultsQueue.put(uploadResultsTO);
    //
    // } catch (InterruptedException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // });
    // }

//    void uploadResults(final UploadResultsTO uploadResultsTO) {
//	try {
//
//	    uploadResultsQueue.put(uploadResultsTO);
//
//	} catch (InterruptedException e) {
//	    throw new RuntimeException(e);
//	}
//
//    }

}

// final class UploadResultsTO {
//
// private final Game game;
//
// private final String version;
//
// private final boolean teams;
//
// private final int competitors;
//
// private final int rounds;
//
// private final int width;
//
// private final int height;
//
// private final long time;
//
// private final BattleResultTO battleResult1;
//
// private final BattleResultTO battleResult2;
//
// public UploadResultsTO(Game game, String version, boolean teams, int
// competitors, int rounds, int width, int height, long time, BattleResultTO
// battleResult1, BattleResultTO battleResult2) {
// this.game = game;
// this.version = version;
// this.teams = teams;
// this.competitors = competitors;
// this.rounds = rounds;
// this.width = width;
// this.height = height;
// this.time = time;
// this.battleResult1 = battleResult1;
// this.battleResult2 = battleResult2;
// }
//
// public Game getGame() {
// return game;
// }
//
// public String getVersion() {
// return version;
// }
//
// public boolean isTeams() {
// return teams;
// }
//
// public int getCompetitors() {
// return competitors;
// }
//
// public int getRounds() {
// return rounds;
// }
//
// public int getWidth() {
// return width;
// }
//
// public int getHeight() {
// return height;
// }
//
// public long getTime() {
// return time;
// }
//
// public BattleResultTO getBattleResult1() {
// return battleResult1;
// }
//
// public BattleResultTO getBattleResult2() {
// return battleResult2;
// }
//
// }
