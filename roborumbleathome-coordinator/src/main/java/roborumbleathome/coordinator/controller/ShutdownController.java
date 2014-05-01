package roborumbleathome.coordinator.controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import roborumbleathome.coordinator.service.ICoordinatorService;

public class ShutdownController extends WindowAdapter implements Runnable {

    private final ExecutorService downloadMissingBotsExecutorService;

    private final ExecutorService battleGeneratorExecutorService;

    private final ExecutorService uploadResultsExecutor;

//    private final ExecutorService errorExecutor;

    private final ExecutorService mainScheduledExecutorService;

    private final Registry registry;

    public ShutdownController(ExecutorService downloadMissingBotsExecutorService, ExecutorService battleGeneratorExecutorService, ExecutorService uploadResultsExecutor,
	    //ExecutorService errorExecutor,
	    ExecutorService mainScheduledExecutorService, Registry registry) {
	this.downloadMissingBotsExecutorService = downloadMissingBotsExecutorService;
	this.battleGeneratorExecutorService = battleGeneratorExecutorService;
	this.uploadResultsExecutor = uploadResultsExecutor;
//	this.errorExecutor = errorExecutor;
	this.mainScheduledExecutorService = mainScheduledExecutorService;
	this.registry = registry;
    }

    @Override
    public void windowClosing(WindowEvent e) {
	run();
    }

    public void run() {
	try {

	    System.out.println("Shutting down ...");

	    downloadMissingBotsExecutorService.shutdown();
	    battleGeneratorExecutorService.shutdown();
	    uploadResultsExecutor.shutdownNow();
//	    errorExecutor.shutdown();
	    mainScheduledExecutorService.shutdown();
	    registry.unbind(ICoordinatorService.NAME);

	    while (!downloadMissingBotsExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
	    }
	    while (!battleGeneratorExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
	    }
	    while (!uploadResultsExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
	    }
//	    while (!errorExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
//	    }
	    while (!mainScheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
	    }

	} catch (RemoteException e) {
	    throw new RuntimeException(e);
	} catch (NotBoundException e) {
	    throw new RuntimeException(e);
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}
    }
}