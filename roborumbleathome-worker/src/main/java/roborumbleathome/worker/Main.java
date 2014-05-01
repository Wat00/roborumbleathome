package roborumbleathome.worker;

import static java.util.logging.Level.SEVERE;

import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import robocode.control.RobocodeEngine;
import roborumbleathome.worker.controller.DownloadRobotsController;
import roborumbleathome.worker.controller.MainController;
import roborumbleathome.worker.controller.RunBattleController;
import roborumbleathome.worker.controller.ShutdownController;
import roborumbleathome.worker.integration.CoordinatorProxy;
import roborumbleathome.worker.integration.CoordinatorProxy.CannotCommunicateWithCoordinatorProcessException;
import roborumbleathome.worker.persistence.WorkerBotDAO;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * @param args
     * @throws RemoteException
     * @throws NotBoundException
     */
    public static void main(String[] args) {
	try {

	    LOGGER.fine("Starting ...");

	    ExecutorService mainExecutor = Executors.newSingleThreadExecutor();

	    ShutdownController shutdownController = new ShutdownController(mainExecutor);
	    Runtime.getRuntime().addShutdownHook(new Thread(shutdownController));
	    Window window = new Frame();
	    window.addWindowListener(shutdownController);

	    CoordinatorProxy coordinatorProxy = new CoordinatorProxy(args[0]);

	    File robocodeHome = new File(".");
	    RobocodeEngine robocodeEngine = new RobocodeEngine(robocodeHome);
	    File botsRep = RobocodeEngine.getRobotsDir();
	    WorkerBotDAO workerBotDAO = new WorkerBotDAO(botsRep);
	    DownloadRobotsController downloadRobotsController = new DownloadRobotsController(coordinatorProxy, workerBotDAO);
	    RunBattleController runBattleController = new RunBattleController(robocodeEngine, robocodeHome);

	    MainController mainController = new MainController(coordinatorProxy, downloadRobotsController, runBattleController, mainExecutor, shutdownController);

	    workerBotDAO.initializeDirectories();

	    mainExecutor.submit(mainController);

	} catch (CannotCommunicateWithCoordinatorProcessException e) {
	    LOGGER.severe("Cannot communicate with coordinator process");
	    LOGGER.log(SEVERE, "" + e.getCause(), e.getCause());
	}
    }
}