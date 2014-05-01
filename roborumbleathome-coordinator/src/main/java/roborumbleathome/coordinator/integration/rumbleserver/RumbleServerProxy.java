package roborumbleathome.coordinator.integration.rumbleserver;

import static java.util.logging.Level.WARNING;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.Game;
import roborumbleathome.coordinator.model.battlegenerator.AsyncBattleGenerator;
import roborumbleathome.coordinator.model.battlegenerator.PairingTO;
import roborumbleathome.coordinator.model.battlegenerator.UpdateBattlesTO;
import roborumbleathome.coordinator.model.participant.Participant;
import roborumbleathome.coordinator.model.participant.ParticipantFactory;
import roborumbleathome.coordinator.service.BattleResultTO;

public class RumbleServerProxy {

    public static class UpdateBotsUrlNotDefinedException extends Exception {
    }

    static class ErrorsFoundException extends Exception {

	public ErrorsFoundException() {
	}

	public ErrorsFoundException(Throwable cause) {
	    super(cause);
	}

    }

    private static final int TIMEOUT = 10000;

    private static final Logger LOGGER = Logger.getLogger(RumbleServerProxy.class.getName());

    private final Configuration configuration;

    private final AsyncBattleGenerator asyncBattleGenerator;

    // private final ParticipantFactory participantFactory;

    public RumbleServerProxy(Configuration configuration, AsyncBattleGenerator asyncBattleGenerator) {
	this.configuration = configuration;
	this.asyncBattleGenerator = asyncBattleGenerator;
	// this.participantFactory = participantFactory;
    }

    public Map<Game, Collection<String>> downloadRatingFiles(ParticipantFactory participantFactory) {
	Map<Game, Collection<String>> rumbleNamesToDeleteMap = new HashMap<Game, Collection<String>>();

	URL ratingsUrl = configuration.getRatingsUrl();
	for (Game game : configuration.getGames()) {
	    Collection<String> rumbleNamesToDelete = new ArrayList<String>();
	    try {
		URL url = new URL(ratingsUrl + "?version=1&game=" + game.getCompetition());

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {

		    conn.setConnectTimeout(TIMEOUT);
		    conn.setReadTimeout(TIMEOUT);

		    conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		    conn.setRequestProperty("User-Agent", "RoboRumble@Home - gzip, deflate");

		    InputStream inputStream = conn.getInputStream();
		    try {

			String encoding = conn.getContentEncoding();

			if ("gzip".equalsIgnoreCase(encoding)) {
			    inputStream = new GZIPInputStream(inputStream);
			} else if ("deflate".equalsIgnoreCase(encoding)) {
			    inputStream = new InflaterInputStream(inputStream);
			}

			Properties properties = new Properties();
			properties.load(inputStream);
			for (Entry<Object, Object> entry : properties.entrySet()) {
			    String robot = entry.getKey().toString();
			    String[] valueArray = entry.getValue().toString().split(",");
			    int battles = Integer.parseInt(valueArray[1]);

			    Participant participant = participantFactory.findParticipantByRumbleName(robot);
			    if (participant != null) {
				if (game.isCodesizeAllowed(participant.getCodesize())) {
				    participant.setBattles(game, battles);
				} else if (participant.getCodesize() != null) {
				    rumbleNamesToDelete.add(robot);
				}
			    } else if (participant == null) {
				rumbleNamesToDelete.add(robot);
			    }
			}

		    } finally {
			inputStream.close();
		    }
		} finally {
		    conn.disconnect();
		}

		for (Participant participant : participantFactory.getAllParticipants()) {
		    if (game.isCodesizeAllowed(participant.getCodesize())) {
			Integer battles = participant.getBattles(game);
			if (battles == null) {
			    participant.setBattles(game, 0);
			    participant.setUnstable(true);
			}
		    }
		}

	    } catch (IOException e) {
		LOGGER.warning("Unable to ratings for " + game.getCompetition());
		LOGGER.log(WARNING, "" + e, e);
	    }
	    rumbleNamesToDeleteMap.put(game, rumbleNamesToDelete);
	}
	return rumbleNamesToDeleteMap;
    }

    public void removeOldParticipantsFromServer(Map<Game, Collection<String>> rumbleNamesToDeleteMap) throws UpdateBotsUrlNotDefinedException {
	URL url = configuration.getUpdateBotsUrl();
	if (url == null) {
	    throw new UpdateBotsUrlNotDefinedException();
	}
	for (Game game : rumbleNamesToDeleteMap.keySet()) {
	    for (String name : rumbleNamesToDeleteMap.get(game)) {
		LOGGER.info("Removing entry ... " + name + " from " + game.getCompetition());
		try {

		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    try {

			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);

			conn.setDoOutput(true);
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty("User-Agent", "RoboRumble@Home - gzip, deflate");

			PrintStream out = new PrintStream(new BufferedOutputStream(conn.getOutputStream()));
			try {
			    out.print("version=1&game=");
			    out.print(game.getCompetition());

			    out.print("&name=");
			    out.print(name);

			    out.println("&dummy=NA");

			    out.flush();
			} finally {
			    out.close();
			}

			InputStream in = conn.getInputStream();
			try {

			    String encoding = conn.getContentEncoding();

			    if ("gzip".equalsIgnoreCase(encoding)) {
				in = new GZIPInputStream(in);
			    } else if ("deflate".equalsIgnoreCase(encoding)) {
				in = new InflaterInputStream(in);
			    }

			    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
			    String line;
			    while ((line = bufferedReader.readLine()) != null) {
				LOGGER.warning(line);
			    }

			} finally {
			    in.close();
			}
		    } finally {
			conn.disconnect();
		    }

		} catch (IOException e) {
		    LOGGER.log(WARNING, "" + e, e);
		}
	    }
	}
    }

    void uploadResults(UploadResultsTO uploadResultsTO) throws ErrorsFoundException {
	try {
	    URL url = configuration.getResultsUrl();
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    try {
		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);

		conn.setDoOutput(true);
		conn.setRequestMethod("POST");

		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.setRequestProperty("User-Agent", "RoboRumble@Home - gzip, deflate");

		PrintStream out = new PrintStream(new BufferedOutputStream(conn.getOutputStream()));
		try {
		    out.print("game=");
		    out.print(uploadResultsTO.getGame().getCompetition());

		    out.print("&version=1&client=");
		    out.print(uploadResultsTO.getVersion());

		    out.print("&teams=");
		    out.print(uploadResultsTO.isTeams() ? "YES" : "NO");

		    out.print("&melee=");
		    out.print(uploadResultsTO.getCompetitors() > 2 ? "YES" : "NO");

		    out.print("&rounds=");
		    out.print(uploadResultsTO.getRounds());

		    out.print("&field=");
		    out.print(uploadResultsTO.getWidth());
		    out.print('x');
		    out.print(uploadResultsTO.getHeight());

		    out.print("&user=");
		    out.print(configuration.getUser());

		    out.print("&time=");
		    out.print(uploadResultsTO.getTime());

		    BattleResultTO battleResult1 = uploadResultsTO.getBattleResult1();

		    out.print("&fname=");
		    out.print(battleResult1.getParticipantTO().getRobot());

		    out.print("&fscore=");
		    out.print(battleResult1.getScore());

		    out.print("&fbulletd=");
		    out.print(battleResult1.getBulletDamage());

		    out.print("&fsurvival=");
		    out.print(battleResult1.getFirsts());

		    BattleResultTO battleResult2 = uploadResultsTO.getBattleResult2();

		    out.print("&sname=");
		    out.print(battleResult2.getParticipantTO().getRobot());

		    out.print("&sscore=");
		    out.print(battleResult2.getScore());

		    out.print("&sbulletd=");
		    out.print(battleResult2.getBulletDamage());

		    out.print("&ssurvival=");
		    out.println(battleResult2.getFirsts());

		    out.flush();
		} finally {
		    out.close();
		}

		InputStream in = conn.getInputStream();
		try {

		    String encoding = conn.getContentEncoding();

		    if ("gzip".equalsIgnoreCase(encoding)) {
			in = new GZIPInputStream(in);
		    } else if ("deflate".equalsIgnoreCase(encoding)) {
			in = new InflaterInputStream(in);
		    }
		    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

		    boolean ok = false;

		    String line;
		    while ((line = bufferedReader.readLine()) != null) {
			if (line.contains("OK") && !line.contains("Queue full")) {
			    ok = true;
			    LOGGER.info(line);

			} else if (line.startsWith("<") && line.endsWith(">")) {
			    String[] lineArray = line.substring(1, line.length() - 1).split(" ");
			    if (lineArray.length == 2) {
				try {
				    int battles1 = Integer.parseInt(lineArray[0]);
				    int battles2 = Integer.parseInt(lineArray[1]);

				    asyncBattleGenerator.updateBattles(new UpdateBattlesTO(uploadResultsTO.getBattleResult1().getParticipantTO(), uploadResultsTO.getGame(), battles1));
				    asyncBattleGenerator.updateBattles(new UpdateBattlesTO(uploadResultsTO.getBattleResult2().getParticipantTO(), uploadResultsTO.getGame(), battles2));

				    LOGGER.fine(line);

				} catch (NumberFormatException e) {
				    LOGGER.log(WARNING, line, e);
				}
			    } else {
				LOGGER.warning(line);
			    }

			} else if (line.startsWith("[") && line.endsWith("]")) {
			    String[] lineArray = line.substring(1, line.length() - 1).split(",");
			    if (lineArray.length == 2) {
				String robot1 = toRobotName(lineArray[0]);
				String robot2 = toRobotName(lineArray[1]);

				asyncBattleGenerator.addPriorityPairing(new PairingTO(robot1, robot2));

				LOGGER.fine(line);

			    } else {
				LOGGER.warning(line);
			    }
			} else {
			    LOGGER.warning(line);
			}
		    }

		    if (!ok) {
			throw new ErrorsFoundException();
		    }

		} finally {
		    in.close();
		}

	    } finally {
		conn.disconnect();
	    }

	} catch (IOException e) {
	    throw new ErrorsFoundException(e);
	}
    }

    private String toRobotName(String rumbleName) {
	int underscoreIndex = rumbleName.lastIndexOf("_");
	int spaceIndex = rumbleName.lastIndexOf(" ");
	return ((underscoreIndex != -1) && (spaceIndex == -1)) ? (rumbleName.substring(0, underscoreIndex) + " " + rumbleName.substring(underscoreIndex + 1)) : rumbleName;
    }
}