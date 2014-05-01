package roborumbleathome.worker.controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownController extends WindowAdapter implements Runnable {

    private final ExecutorService mainExecutor;

    public ShutdownController(ExecutorService mainExecutor) {
	this.mainExecutor = mainExecutor;
    }

    public void shutdown() {
	mainExecutor.shutdown();
    }

    public void awaitTermination() {
	try {
	    while (!mainExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
	    }
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void windowClosing(WindowEvent e) {
	run();
    }

    public void run() {
	System.out.println("Shutting down ...");

	shutdown();
	awaitTermination();

    }

}