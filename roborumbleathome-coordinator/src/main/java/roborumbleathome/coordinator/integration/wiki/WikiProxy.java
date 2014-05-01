package roborumbleathome.coordinator.integration.wiki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.participant.Participant;
import roborumbleathome.coordinator.model.participant.ParticipantFactory;

public class WikiProxy {

    public static class ParticipantsListEmptyException extends Exception {
    }

    public static class ResponseNotOkException extends Exception {

	private final int responseCode;

	public ResponseNotOkException(int responseCode, String message) {
	    super(message);
	    this.responseCode = responseCode;
	}

	public int getResponseCode() {
	    return responseCode;
	}

    }

    public static class UnableRetrieveParticipantsListException extends Exception {

	public UnableRetrieveParticipantsListException(Throwable cause) {
	    super(cause);
	}

    }

    private final Configuration configuration;

//    private final ParticipantFactory participantFactory;

    public WikiProxy(Configuration configuration) {
	this.configuration = configuration;
//	this.participantFactory = participantFactory;
    }

    public List<Participant> downloadParticipantsList(ParticipantFactory participantFactory) throws ParticipantsListEmptyException, UnableRetrieveParticipantsListException, ResponseNotOkException {
	List<Participant> participants;
	try {

	    URL url = configuration.getParticipantsUrl();

	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    try {

		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);

		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.setRequestProperty("User-Agent", "RoboRumble@Home - gzip, deflate");

		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
		    throw new ResponseNotOkException(conn.getResponseCode(), conn.getResponseMessage());
		}

		InputStream inputStream = conn.getInputStream();
		try {

		    String encoding = conn.getContentEncoding();

		    if ("gzip".equalsIgnoreCase(encoding)) {
			inputStream = new GZIPInputStream(inputStream);
		    } else if ("deflate".equalsIgnoreCase(encoding)) {
			inputStream = new InflaterInputStream(inputStream);
		    }

		    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		    ParticipantsLexer lexer = new ParticipantsLexer(bufferedReader, configuration);
		    ParticipantsParser parser = new ParticipantsParser(lexer, participantFactory);
		    participants = parser.parsePage();
		    if (participants.isEmpty()) {
			throw new ParticipantsListEmptyException();
		    }

		} finally {
		    inputStream.close();
		}
	    } finally {
		conn.disconnect();
	    }

	} catch (IOException e) {
	    throw new UnableRetrieveParticipantsListException(e);
	}

	return participants;
    }

}