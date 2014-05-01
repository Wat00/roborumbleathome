package roborumbleathome.coordinator.persistence;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.participant.Participant;
import roborumbleathome.coordinator.model.participant.ParticipantFactory;

public class ParticipantDAO {

    public static class CantCreateDirectoryException extends Exception {

	private File directory;

	public CantCreateDirectoryException(File directory) {
	    this.directory = directory;
	}

	public File getDirectory() {
	    return directory;
	}

    }

    public static class ParticipantsFileNotFoundException extends Exception {

	public ParticipantsFileNotFoundException(Throwable cause) {
	    super(cause);
	}

    }

    public class UnableSaveParticipantsListException extends Exception {

	public UnableSaveParticipantsListException(Throwable cause) {
	    super(cause);
	}

    }

    private final Configuration configuration;

//    private ParticipantFactory participantFactory;

    public ParticipantDAO(Configuration configuration) {
	this.configuration = configuration;
//	this.participantFactory = participantFactory;
    }

    public void saveParticipants(final Collection<Participant> participants) throws UnableSaveParticipantsListException, CantCreateDirectoryException {
	try {
	    File participantsFile = configuration.getParticipantsFile();

	    File directory = participantsFile.getParentFile();
	    if (directory.isFile() || !directory.exists() && !directory.mkdirs()) {
		throw new CantCreateDirectoryException(directory);
	    }

	    PrintStream printStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(participantsFile)));
	    try {
		for (Participant participant : participants) {
		    printStream.println(participant.getRobot() + ',' + participant.getLink());
		}
	    } finally {
		printStream.close();
	    }
	} catch (IOException e) {
	    throw new UnableSaveParticipantsListException(e);
	}
    }

    public List<Participant> loadParticipants(ParticipantFactory participantFactory) throws ParticipantsFileNotFoundException {
	List<Participant> participants;
	try {

	    File participantsFile = configuration.getParticipantsFile();
	    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(participantsFile)));
	    try {
		participants = new ArrayList<Participant>();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
		    String[] lineArray = line.split(",");
		    String robot = lineArray[0];
		    URL link = new URL(lineArray[1]);
		    Participant participant = participantFactory.getParticipant(robot);
		    participant.setLink(link);

		    participants.add(participant);
		}
	    } finally {
		bufferedReader.close();
	    }

	} catch (IOException e) {
	    throw new ParticipantsFileNotFoundException(e);
	}
	return participants;
    }

}
