package roborumbleathome.worker.controller;

import static java.util.logging.Level.SEVERE;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import roborumbleathome.coordinator.service.BattleResultsTO;
import roborumbleathome.coordinator.service.BattleTO;
import roborumbleathome.worker.controller.RunBattleController.SkippingBattleException;
import roborumbleathome.worker.integration.CoordinatorProxy;
import roborumbleathome.worker.integration.CoordinatorProxy.CannotCommunicateWithCoordinatorProcessException;

public class MainController implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    private final CoordinatorProxy coordinatorProxy;

    private final DownloadRobotsController downloadRobotsController;

    private final RunBattleController runBattleController;

    private final ExecutorService mainExecutor;

    private final ShutdownController shutdownController;

    public MainController(CoordinatorProxy coordinatorProxy, DownloadRobotsController downloadRobotsController, RunBattleController runBattleController, ExecutorService mainExecutor,
	    ShutdownController shutdownController) {
	this.coordinatorProxy = coordinatorProxy;
	this.downloadRobotsController = downloadRobotsController;
	this.runBattleController = runBattleController;
	this.mainExecutor = mainExecutor;
	this.shutdownController = shutdownController;
    }

    public void run() {
	try {

	    try {
		LOGGER.fine("Receiving battle ...");
		BattleTO battleTO = coordinatorProxy.generateBattle();
		downloadRobotsController.downloadRobots(battleTO);
		runBattleController.refreshLocalRepository(battleTO);
		BattleResultsTO battleResults = runBattleController.runBattle(battleTO);
		coordinatorProxy.uploadResults(battleResults);

	    } catch (SkippingBattleException e) {
		LOGGER.severe("Skipping battle because can't load robots: " + e.getNamesBuffer());

	    }

	    mainExecutor.submit(this);

	} catch (CannotCommunicateWithCoordinatorProcessException e) {
	    LOGGER.severe("Cannot communicate with coordinator process");
	    LOGGER.log(SEVERE, "" + e.getCause(), e.getCause());
	    shutdownController.shutdown();

	}
    }
}