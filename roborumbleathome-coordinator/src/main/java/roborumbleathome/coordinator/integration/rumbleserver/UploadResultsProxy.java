package roborumbleathome.coordinator.integration.rumbleserver;

import java.util.Collection;
import java.util.List;

import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.Game;
import roborumbleathome.coordinator.model.participant.ParticipantTO;
import roborumbleathome.coordinator.service.BattleResultTO;
import roborumbleathome.coordinator.service.BattleResultsTO;

public class UploadResultsProxy {

    private final Configuration configuration;

    // private final Executor uploadResultsExecutor;

    private final AsyncRumbleServerProxy rumbleServerProxy;

    public UploadResultsProxy(Configuration configuration, // Executor
							   // uploadResultsExecutor,
	    AsyncRumbleServerProxy rumbleServerProxy) {
	this.configuration = configuration;
	// this.uploadResultsExecutor = uploadResultsExecutor;
	this.rumbleServerProxy = rumbleServerProxy;
    }

    public void uploadBattle(BattleResultsTO battleResultsTO) {
	Collection<Game> games = configuration.getGames();
	List<BattleResultTO> battleResultList = battleResultsTO.getBattleResultList();
	for (int i = 0; i < battleResultList.size() - 1; ++i) {
	    for (int j = i + 1; j < battleResultList.size(); ++j) {
		BattleResultTO battleResult1 = battleResultList.get(i);
		ParticipantTO participant1 = battleResult1.getParticipantTO();
		BattleResultTO battleResult2 = battleResultList.get(j);
		ParticipantTO participant2 = battleResult2.getParticipantTO();
		for (Game game : games) {
		    if (game.isCodesizeAllowed(participant1.getCodesize()) && game.isCodesizeAllowed(participant2.getCodesize())) {
			UploadResultsTO uploadResultsTO = new UploadResultsTO(game, battleResultsTO.getVersion(), configuration.isTeams(), configuration.getCompetitors(),
				configuration.getRounds(), configuration.getWidth(), configuration.getHeight(), battleResultsTO.getTime(), battleResult1, battleResult2);
			rumbleServerProxy.uploadResults(uploadResultsTO);
		    }
		}
	    }
	}
    }

}

// class UploadResultsRunnable implements Runnable {
//
// private static final Logger LOGGER =
// Logger.getLogger(UploadResultsRunnable.class.getName());
//
// private final RumbleServerProxy rumbleServerProxy;
//
// private final Executor uploadResultsExecutor;
//
// private final UploadResultsTO uploadResultsTO;
//
// public UploadResultsRunnable(RumbleServerProxy rumbleServerProxy, Executor
// uploadResultsExecutor, UploadResultsTO uploadResultsTO) {
// this.rumbleServerProxy = rumbleServerProxy;
// this.uploadResultsExecutor = uploadResultsExecutor;
// this.uploadResultsTO = uploadResultsTO;
// }
//
// public void run() {
// try {
//
// try {
// rumbleServerProxy.uploadResults(uploadResultsTO);
//
// } catch (ErrorsFoundException e) {
// if (e.getCause() != null) {
// LOGGER.log(WARNING, "" + e.getCause(), e.getCause());
// }
// LOGGER.warning("Unable to upload results " +
// uploadResultsTO.getBattleResult1().getParticipantTO().getRobot() + "," +
// uploadResultsTO.getBattleResult1().getScore() + ","
// + uploadResultsTO.getBattleResult1().getBulletDamage() + "," +
// uploadResultsTO.getBattleResult1().getFirsts() + " "
// + uploadResultsTO.getBattleResult2().getParticipantTO().getRobot() + "," +
// uploadResultsTO.getBattleResult2().getScore() + ","
// + uploadResultsTO.getBattleResult2().getBulletDamage() + "," +
// uploadResultsTO.getBattleResult2().getFirsts());// TODO
//
// Thread.sleep(10000);
// uploadResultsExecutor.execute(this);
//
// }
//
// } catch (InterruptedException e) {
// throw new RuntimeException(e);
// }
// }
//
// }

// class UploadResultsTO {
//
// private final Game game;
//
// private final String version;
//
// private final boolean teams;
//
// private final int competitors;
//
// private final int rounds;
//
// private final int width;
//
// private final int height;
//
// private final long time;
//
// private final BattleResultTO battleResult1;
//
// private final BattleResultTO battleResult2;
//
// public UploadResultsTO(Game game, String version, boolean teams, int
// competitors, int rounds, int width, int height, long time, BattleResultTO
// battleResult1, BattleResultTO battleResult2) {
// this.game = game;
// this.version = version;
// this.teams = teams;
// this.competitors = competitors;
// this.rounds = rounds;
// this.width = width;
// this.height = height;
// this.time = time;
// this.battleResult1 = battleResult1;
// this.battleResult2 = battleResult2;
// }
//
// public Game getGame() {
// return game;
// }
//
// public String getVersion() {
// return version;
// }
//
// public boolean isTeams() {
// return teams;
// }
//
// public int getCompetitors() {
// return competitors;
// }
//
// public int getRounds() {
// return rounds;
// }
//
// public int getWidth() {
// return width;
// }
//
// public int getHeight() {
// return height;
// }
//
// public long getTime() {
// return time;
// }
//
// public BattleResultTO getBattleResult1() {
// return battleResult1;
// }
//
// public BattleResultTO getBattleResult2() {
// return battleResult2;
// }
//
// }