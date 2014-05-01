package roborumbleathome.coordinator.controller;

import java.awt.Frame;
import java.awt.Window;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import roborumbleathome.coordinator.controller.downloadmissingbots.DownloadMissingBotsController;
import roborumbleathome.coordinator.integration.BotProxy;
import roborumbleathome.coordinator.integration.rumbleserver.AsyncRumbleServerProxy;
import roborumbleathome.coordinator.integration.rumbleserver.RumbleServerProxy;
import roborumbleathome.coordinator.integration.rumbleserver.UploadResultsProxy;
import roborumbleathome.coordinator.integration.wiki.WikiProxy;
import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.battlegenerator.AsyncBattleGenerator;
import roborumbleathome.coordinator.model.battlegenerator.BattleGenerator;
import roborumbleathome.coordinator.persistence.BotDAO;
import roborumbleathome.coordinator.persistence.ConfigurationDAO;
import roborumbleathome.coordinator.persistence.ConfigurationDAO.CouldNotLoadPropertiesFileException;
import roborumbleathome.coordinator.persistence.ParticipantDAO;
import roborumbleathome.coordinator.service.CoordinatorService;
import roborumbleathome.coordinator.service.ICoordinatorService;

public class Main {

    private static final int DOWNLOAD_THREADS = 15;

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
	try {

	    LOGGER.fine("Starting ...");

	    Configuration configuration = loadConfiguration(args);

	    // ParticipantFactory participantFactory = new ParticipantFactory();

	    BattleGenerator battleGenerator = new BattleGenerator(configuration);
	    ExecutorService battleGeneratorExecutorService = Executors.newSingleThreadExecutor();
	    AsyncBattleGenerator asyncBattleGenerator = new AsyncBattleGenerator(battleGenerator, battleGeneratorExecutorService);

	    RumbleServerProxy rumbleServerProxy = new RumbleServerProxy(configuration, asyncBattleGenerator);
	    BotProxy botProxy = new BotProxy(configuration);
	    BotDAO botDAO = new BotDAO(configuration);
	    WikiProxy participantsProxy = new WikiProxy(configuration);
	    ParticipantDAO participantDAO = new ParticipantDAO(configuration);
	    DownloadMissingBotsController downloadMissingBotsController = new DownloadMissingBotsController(botDAO, botProxy);
	    ExecutorService downloadMissingBotsExecutorService = Executors.newFixedThreadPool(DOWNLOAD_THREADS);

	    MainController mainController = new MainController(configuration, participantsProxy, participantDAO, botDAO, downloadMissingBotsExecutorService, downloadMissingBotsController,
		    rumbleServerProxy, asyncBattleGenerator);

	    // ThreadPoolExecutor uploadResultsExecutor = new
	    // ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new
	    // LinkedBlockingQueue<Runnable>());

	    // UploadResultsThrottler uploadResultsThrottler = new
	    // UploadResultsThrottler(uploadResultsExecutor, configuration);
	    // AsyncUploadResultsThrottler asyncUploadResultsThrottler = new
	    // AsyncUploadResultsThrottler(uploadResultsThrottler);
	    ThreadPoolExecutor uploadResultsExecutor = new ThreadPoolExecutor(configuration.getUploadPoolSize(), configuration.getUploadPoolSize(), 0, TimeUnit.MILLISECONDS,
		    new LinkedBlockingQueue<Runnable>());
	    // ExecutorService errorExecutor =
	    // Executors.newSingleThreadExecutor();
	    AsyncRumbleServerProxy asyncRumbleServerProxy = new AsyncRumbleServerProxy(rumbleServerProxy, uploadResultsExecutor, configuration);// ,
																		// errorExecutor);
	    UploadResultsProxy uploadResultsProxy = new UploadResultsProxy(configuration, asyncRumbleServerProxy);
	    Runnable downloadRunnable = new MainController.DownloadRunnable(mainController);

	    ScheduledExecutorService mainScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	    CoordinatorService coordinatorService = new CoordinatorService(botDAO, uploadResultsProxy, asyncBattleGenerator);

	    mainScheduledExecutorService.scheduleAtFixedRate(downloadRunnable, 0, 10, TimeUnit.MINUTES);
	    Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
	    registry.bind(ICoordinatorService.NAME, coordinatorService);

	    ShutdownController shutdownController = new ShutdownController(downloadMissingBotsExecutorService, battleGeneratorExecutorService, uploadResultsExecutor,// errorExecutor,
		    mainScheduledExecutorService, registry);
	    Runtime.getRuntime().addShutdownHook(new Thread(shutdownController));
	    Window window = new Frame();
	    window.addWindowListener(shutdownController);

	} catch (CouldNotLoadPropertiesFileException e) {
	    LOGGER.severe("Could not load properties file: " + e.getMessage());

	} catch (RemoteException e) {
	    throw new RuntimeException(e);

	} catch (AlreadyBoundException e) {
	    throw new RuntimeException(e);
	}
    }

    private static Configuration loadConfiguration(String[] args) throws CouldNotLoadPropertiesFileException {
	String filename;
	if (args.length > 0) {
	    filename = args[0];

	} else {
	    filename = "./roborumble/roborumble.txt";
	    LOGGER.warning("No argument found specifying properties file. \"roborumble.txt\" assumed.");
	}

	ConfigurationDAO configurationDAO = new ConfigurationDAO(filename);
	Configuration configuration = configurationDAO.loadConfiguration();
	return configuration;
    }

}