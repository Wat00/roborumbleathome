package roborumbleathome.worker.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import roborumbleathome.coordinator.model.participant.ParticipantTO;

public class WorkerBotDAO {

    private final File botsRep;

    public WorkerBotDAO(File botsRep) {
	this.botsRep = botsRep;
    }

    public void initializeDirectories() {
	botsRep.mkdirs();
    }

    private File getBotsRepFile(String name) {
	String filename = name.replaceAll(" ", "_") + ".jar";
	return new File(botsRep, filename);
    }

    public boolean exists(ParticipantTO robot) {
	File botsRepFile = getBotsRepFile(robot.getRobot());
	return botsRepFile.exists();
    }

    public File saveRobot(ParticipantTO robot, byte[] content) {
	try {

	    File botsRepFile = getBotsRepFile(robot.getRobot());
	    OutputStream outputStream = new FileOutputStream(botsRepFile);
	    try {
		outputStream.write(content);
	    } finally {
		outputStream.close();
	    }
	    return botsRepFile;

	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

}
