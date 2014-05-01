package roborumbleathome.coordinator.persistence;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import net.sf.roborumbleathome.util.codesize.Codesize;
import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.participant.Participant;
import roborumbleathome.coordinator.model.participant.ParticipantTO;

public class BotDAO {

    public static class DownloadedFileWrongCorruptedException extends Exception {

	private final File file;

	public DownloadedFileWrongCorruptedException(File file) {
	    this.file = file;
	}

	public DownloadedFileWrongCorruptedException(File file, Throwable cause) {
	    super(cause);
	    this.file = file;
	}

	public File getFile() {
	    return file;
	}

    }

    public static class NotAbleReadPropertiesException extends Exception {

	private final File file;

	public NotAbleReadPropertiesException(File file) {
	    this.file = file;
	}

	public File getFile() {
	    return file;
	}

    }

    private final Configuration configuration;

    public BotDAO(Configuration configuration) {
	this.configuration = configuration;
    }

    // private File getBotsRepFile(ParticipantTO participant) {
    private File getBotsRepFile(String name) {
	String filename = name.replaceAll(" ", "_") + ".jar";
	File botsRep = configuration.getBotsRep();
	return new File(botsRep, filename);
    }

    public boolean fileExists(ParticipantTO participant) {
	File botsRepFile = getBotsRepFile(participant.getRobot());
	return botsRepFile.exists();
    }

    public void checkJarFile(File file, ParticipantTO participant) throws DownloadedFileWrongCorruptedException, NotAbleReadPropertiesException {
	String nameVersion = participant.getRobot();
	int index = nameVersion.indexOf(" ");
	String name = nameVersion.substring(0, index);
	String version = nameVersion.substring(index + 1);

	String propertiesFileName = name.replace('.', '/');

	boolean teams = configuration.isTeams();
	if (teams) {
	    propertiesFileName += ".team";
	} else {
	    propertiesFileName += ".properties";
	}

	Properties properties = new Properties();
	try {
	    JarFile jarFile = new JarFile(file);
	    try {
		ZipEntry zipEntry = jarFile.getJarEntry(propertiesFileName);

		if (zipEntry == null) {
		    throw new NotAbleReadPropertiesException(file);
		}

		InputStream inputStream = jarFile.getInputStream(zipEntry);
		try {
		    properties.load(inputStream);
		} finally {
		    inputStream.close();
		}
	    } finally {
		jarFile.close();
	    }
	} catch (IOException e) {
	    throw new DownloadedFileWrongCorruptedException(file, e);
	}

	if (teams) {
	    String teamVersion = properties.getProperty("team.version");
	    if (!version.equals(teamVersion)) {
		throw new DownloadedFileWrongCorruptedException(file);
	    }

	} else {
	    String robotClassName = properties.getProperty("robot.classname");
	    if (!name.equals(robotClassName)) {
		throw new DownloadedFileWrongCorruptedException(file);
	    }

	    String robotVersion = properties.getProperty("robot.version");
	    if (!version.equals(robotVersion)) {
		throw new DownloadedFileWrongCorruptedException(file);
	    }
	}

    }

    public File moveFile(File tempFile, ParticipantTO participant) {
	File botsRepFile = getBotsRepFile(participant.getRobot());

	tempFile.renameTo(botsRepFile);
	return botsRepFile;
    }

    public byte[] loadRobot(ParticipantTO robot) {
	try {

	    File botsRepFile = getBotsRepFile(robot.getRobot());
	    byte[] buffer = new byte[(int) botsRepFile.length()];
	    DataInputStream dataInputStream = new DataInputStream(new FileInputStream(botsRepFile));
	    try {
		dataInputStream.readFully(buffer);
	    } finally {
		dataInputStream.close();
	    }
	    return buffer;

	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    public int calculateCodesize(Participant robot) {
	try {

	    int codesize;
	    File botsRepFile = getBotsRepFile(robot.getRobot());
	    InputStream inputStream = new BufferedInputStream(new FileInputStream(botsRepFile));
	    try {
		codesize = Codesize.processJar(inputStream);
	    } finally {
		inputStream.close();
	    }
	    return codesize;

	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }
}