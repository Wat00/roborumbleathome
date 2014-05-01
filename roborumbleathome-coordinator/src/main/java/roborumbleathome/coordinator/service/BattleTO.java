package roborumbleathome.coordinator.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roborumbleathome.coordinator.model.participant.ParticipantTO;

public final class BattleTO implements Serializable {

    private final int id;

    private final int numRounds;
    private final long inactivityTime;
    private final double gunCoolingRate;
    private final boolean hideEnemyNames;

    private final int width;
    private final int height;

    private final List<ParticipantTO> robots;

    public BattleTO(int id, int numRounds, long inactivityTime, double gunCoolingRate, boolean hideEnemyNames, int width, int height, List<ParticipantTO> robots) {
	this.id = id;
	this.numRounds = numRounds;
	this.inactivityTime = inactivityTime;
	this.gunCoolingRate = gunCoolingRate;
	this.hideEnemyNames = hideEnemyNames;
	this.width = width;
	this.height = height;
	this.robots = Collections.unmodifiableList(new ArrayList<ParticipantTO>(robots));
    }

    public int getId() {
	return id;
    }

    public int getNumRounds() {
	return numRounds;
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

    public List<ParticipantTO> getRobots() {
	return robots;
    }

    @Override
    public String toString() {
	return "BattleTO [id=" + id + ", numRounds=" + numRounds + ", inactivityTime=" + inactivityTime + ", gunCoolingRate=" + gunCoolingRate + ", hideEnemyNames=" + hideEnemyNames
		+ ", width=" + width + ", height=" + height + ", robots=" + robots + "]";
    }

}
