package roborumbleathome.coordinator.integration.wiki;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import roborumbleathome.coordinator.model.participant.Participant;
import roborumbleathome.coordinator.model.participant.ParticipantFactory;

class ParticipantsParser {

    static class InvalidLineException extends Exception {

	InvalidLineException(String message, Throwable cause) {
	    super(message, cause);
	}

	InvalidLineException(String message) {
	    super(message);
	}

    }

    private static final Logger LOGGER = Logger.getLogger(ParticipantsParser.class.getName());

    private final ParticipantFactory participantFactory;

    private final ParticipantsLexer lexer;

    ParticipantsParser(ParticipantsLexer lexer, ParticipantFactory participantFactory) {
	this.lexer = lexer;
	this.participantFactory = participantFactory;
    }

    List<Participant> parsePage() throws IOException {
	List<Participant> participants = new ArrayList<Participant>();

	int token;
	do {
	    token = lexer.nextToken();
	} while (token != ParticipantsLexer.BEGIN && token != ParticipantsLexer.EOF);

	do {
	    token = lexer.nextToken();
	    if (token == ParticipantsLexer.LINE) {
		try {
		    Participant participant = parseLine();
		    participants.add(participant);
		} catch (InvalidLineException e) {
		    LOGGER.warning("Participant ignored due to invalid line: " + e.getMessage());
		}
	    }
	} while (token != ParticipantsLexer.END && token != ParticipantsLexer.EOF);

	return participants;
    }

    private Participant parseLine() throws InvalidLineException {
	String line = lexer.getLine();

	String[] lineArray = line.split("[,]");
	if (lineArray.length != 2) {
	    throw new InvalidLineException(line);
	}

	String robot = lineArray[0].trim();
	if (!robot.matches("[\\w\\.]+[ ][\\w\\.-]+")) {
	    throw new InvalidLineException(line);
	}

	URL link;
	try {
	    String spec = lineArray[1].trim();
	    link = new URL(spec);
	} catch (MalformedURLException e) {
	    throw new InvalidLineException(line, e);
	}
	String protocol = link.getProtocol();
	if (!protocol.equals("http") && !protocol.equals("https")) {
	    throw new InvalidLineException(line);
	}

	Participant participant = participantFactory.getParticipant(robot);
	participant.setLink(link);
	return participant;
    }

}