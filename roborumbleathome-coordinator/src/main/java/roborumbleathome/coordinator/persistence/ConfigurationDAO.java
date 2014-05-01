package roborumbleathome.coordinator.persistence;

import static java.util.logging.Level.SEVERE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.Game;

public class ConfigurationDAO {

    public static class CouldNotLoadPropertiesFileException extends Exception {

	public CouldNotLoadPropertiesFileException(String message, Throwable cause) {
	    super(message, cause);
	}

    }

    private static final Logger LOGGER = Logger.getLogger(ConfigurationDAO.class.getName());

    private final String filename;

    public ConfigurationDAO(String filename) {
	this.filename = filename;
    }

    public Configuration loadConfiguration() throws CouldNotLoadPropertiesFileException {
	Properties properties = new Properties();
	try {
	    InputStream inputStream = new FileInputStream(filename);
	    properties.load(inputStream);
	} catch (IOException e) {
	    throw new CouldNotLoadPropertiesFileException(filename, e);
	}

	String ratingsUrlString = properties.getProperty("RATINGS.URL");
	URL ratingsUrl;
	try {
	    ratingsUrl = ratingsUrlString != null ? new URL(ratingsUrlString) : null;
	} catch (MalformedURLException e) {
	    LOGGER.log(SEVERE, "" + e, e);
	    ratingsUrl = null;
	}

	Collection<Game> games = new ArrayList<Game>();

	String generalBotsCompetition = filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."));

	String generalBotsFileString = properties.getProperty("RATINGS.GENERAL");
	File generalBotsFile = generalBotsFileString != null ? new File(generalBotsFileString) : null;

	Game generalBots = new Game(generalBotsCompetition, null, generalBotsFile);
	games.add(generalBots);

	String miniBotsCompetition = properties.getProperty("MINIBOTS");
	if (miniBotsCompetition != null) {
	    String miniBotsFileString = properties.getProperty("RATINGS.MINIBOTS");
	    File miniBotsFile = miniBotsFileString != null ? new File(miniBotsFileString) : null;

	    Game miniBots = new Game(miniBotsCompetition, 1500, miniBotsFile);
	    games.add(miniBots);
	}

	String microBotsCompetition = properties.getProperty("MICROBOTS");
	if (microBotsCompetition != null) {
	    String microBotsFileString = properties.getProperty("RATINGS.MICROBOTS");
	    File microBotsFile = microBotsFileString != null ? new File(microBotsFileString) : null;

	    Game microBots = new Game(microBotsCompetition, 750, microBotsFile);
	    games.add(microBots);
	}

	String nanoBotsCompetition = properties.getProperty("NANOBOTS");
	if (nanoBotsCompetition != null) {
	    String nanoBotsFileString = properties.getProperty("RATINGS.NANOBOTS");
	    File nanoBotsFile = nanoBotsFileString != null ? new File(nanoBotsFileString) : null;

	    Game nanoBots = new Game(nanoBotsCompetition, 250, nanoBotsFile);
	    games.add(nanoBots);
	}

	String participantsUrlString = properties.getProperty("PARTICIPANTSURL");
	URL participantsUrl;
	try {
	    participantsUrl = participantsUrlString != null ? new URL(participantsUrlString) : null;
	} catch (MalformedURLException e) {
	    LOGGER.log(SEVERE, "" + e, e);
	    participantsUrl = null;
	}

	String participantsFileString = properties.getProperty("PARTICIPANTSFILE");
	File participantsFile = participantsFileString != null ? new File(participantsFileString) : null;

	String startTag = properties.getProperty("STARTAG");

	String botsRepString = properties.getProperty("BOTSREP");
	File botsRep = botsRepString != null ? new File(botsRepString) : null;

	String meleeString = properties.getProperty("MELEE");
	boolean melee = "YES".equals(meleeString);

	int competitors = melee ? 10 : 2;

	String tempString = properties.getProperty("TEMP");
	File temp = tempString != null ? new File(tempString) : null;

	String teamsString = properties.getProperty("TEAMS");
	boolean teams = "YES".equals(teamsString);

	String updateBotsUrlString = properties.getProperty("UPDATEBOTSURL");
	URL updateBotsUrl;
	try {
	    updateBotsUrl = updateBotsUrlString != null ? new URL(updateBotsUrlString) : null;
	} catch (MalformedURLException e) {
	    LOGGER.log(SEVERE, "" + e, e);
	    updateBotsUrl = null;
	}

	String roundsString = properties.getProperty("ROUNDS");
	int rounds = Integer.parseInt(roundsString);

	long inactivityTime = 450;
	double gunCoolingRate = .1;
	boolean hideEnemyNames = false;

	String widthString = properties.getProperty("FIELDL");
	int width = Integer.parseInt(widthString);

	String heightString = properties.getProperty("FIELDH");
	int height = Integer.parseInt(heightString);

	URL resultsUrl;
	try {
	    String resultsUrlString = properties.getProperty("RESULTSURL");
	    resultsUrl = resultsUrlString != null ? new URL(resultsUrlString) : null;
	} catch (MalformedURLException e) {
	    LOGGER.log(SEVERE, "" + e, e);
	    resultsUrl = null;
	}

	String user = properties.getProperty("USER");

	String battlesPerBotString = properties.getProperty("BATTLESPERBOT");
	int battlesPerBot = Integer.parseInt(battlesPerBotString);

	int uploadPoolSize = 9;
	
	return new Configuration(ratingsUrl, games, participantsUrl, participantsFile, startTag, botsRep, temp, competitors, teams, updateBotsUrl, rounds, inactivityTime, gunCoolingRate,
		hideEnemyNames, width, height, resultsUrl, user, battlesPerBot, uploadPoolSize);
    }
}