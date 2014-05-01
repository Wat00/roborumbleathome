package roborumbleathome.coordinator.model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Configuration {

    private final URL ratingsUrl;

    private final Collection<Game> games;

    private final URL participantsUrl;

    private final File participantsFile;

    private final String startTag;

    private final File botsRep;

    private final File temp;

    private final int competitors;
    private final boolean teams;

    private final URL updateBotsUrl;

    private final int rounds;

    private final long inactivityTime;
    private final double gunCoolingRate;
    private final boolean hideEnemyNames;

    private final int width;
    private final int height;

    private final URL resultsUrl;

    private final String user;

    private final int battlesPerBot;
    
    private final int uploadPoolSize;

    public Configuration(URL ratingsUrl, Collection<Game> games, URL participantsUrl, File participantsFile, String startTag, File botsRep, File temp, int competitors, boolean teams,
	    URL updateBotsUrl, int rounds, long inactivityTime, double gunCoolingRate, boolean hideEnemyNames, int width, int height, URL resultsUrl, String user, int battlesPerBot, int uploadPoolSize) {
	this.ratingsUrl = ratingsUrl;
	this.games = Collections.unmodifiableCollection(new ArrayList<Game>(games));
	this.participantsUrl = participantsUrl;
	this.participantsFile = participantsFile;
	this.startTag = startTag;
	this.botsRep = botsRep;
	this.competitors = competitors;
	this.temp = temp;
	this.teams = teams;
	this.updateBotsUrl = updateBotsUrl;
	this.rounds = rounds;
	this.inactivityTime = inactivityTime;
	this.gunCoolingRate = gunCoolingRate;
	this.hideEnemyNames = hideEnemyNames;
	this.width = width;
	this.height = height;
	this.resultsUrl = resultsUrl;
	this.user = user;
	this.battlesPerBot = battlesPerBot;
	this.uploadPoolSize = uploadPoolSize;
    }

    public URL getRatingsUrl() {
	return ratingsUrl;
    }

    public Collection<Game> getGames() {
	return games;
    }

    public URL getParticipantsUrl() {
	return participantsUrl;
    }

    public File getParticipantsFile() {
	return participantsFile;
    }

    public String getStartTag() {
	return startTag;
    }

    public File getBotsRep() {
	return botsRep;
    }

    public File getTemp() {
	return temp;
    }

    public int getCompetitors() {
	return competitors;
    }

    public boolean isTeams() {
	return teams;
    }

    public URL getUpdateBotsUrl() {
	return updateBotsUrl;
    }

    public int getRounds() {
	return rounds;
    }

    public long getInactivityTime() {
	return inactivityTime;
    }

    public double getGunCoolingRate() {
	return gunCoolingRate;
    }

    public boolean isHideEnemyNames() {
	return hideEnemyNames;
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

    public URL getResultsUrl() {
	return resultsUrl;
    }

    public String getUser() {
	return user;
    }

    public int getBattlesPerBot() {
	return battlesPerBot;
    }

    public int getUploadPoolSize() {
        return uploadPoolSize;
    }

}