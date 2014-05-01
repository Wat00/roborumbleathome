package roborumbleathome.coordinator.integration.rumbleserver;

import roborumbleathome.coordinator.model.Game;
import roborumbleathome.coordinator.service.BattleResultTO;

public final class UploadResultsTO {

    private final Game game;

    private final String version;

    private final boolean teams;

    private final int competitors;

    private final int rounds;

    private final int width;

    private final int height;

    private final long time;

    private final BattleResultTO battleResult1;

    private final BattleResultTO battleResult2;

    public UploadResultsTO(Game game, String version, boolean teams, int competitors, int rounds, int width, int height, long time, BattleResultTO battleResult1, BattleResultTO battleResult2) {
	this.game = game;
	this.version = version;
	this.teams = teams;
	this.competitors = competitors;
	this.rounds = rounds;
	this.width = width;
	this.height = height;
	this.time = time;
	this.battleResult1 = battleResult1;
	this.battleResult2 = battleResult2;
    }

    public Game getGame() {
	return game;
    }

    public String getVersion() {
	return version;
    }

    public boolean isTeams() {
	return teams;
    }

    public int getCompetitors() {
	return competitors;
    }

    public int getRounds() {
	return rounds;
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

    public long getTime() {
	return time;
    }

    public BattleResultTO getBattleResult1() {
	return battleResult1;
    }

    public BattleResultTO getBattleResult2() {
	return battleResult2;
    }

}
