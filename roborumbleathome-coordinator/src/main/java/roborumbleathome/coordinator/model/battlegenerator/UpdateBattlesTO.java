package roborumbleathome.coordinator.model.battlegenerator;

import roborumbleathome.coordinator.model.Game;
import roborumbleathome.coordinator.model.participant.ParticipantTO;

public final class UpdateBattlesTO {

    private final ParticipantTO participant;

    private final Game game;

    private final int battles;

    public UpdateBattlesTO(ParticipantTO participant, Game game, int battles) {
	this.participant = participant;
	this.game = game;
	this.battles = battles;
    }

    public ParticipantTO getParticipant() {
	return participant;
    }

    public Game getGame() {
	return game;
    }

    public int getBattles() {
	return battles;
    }

}