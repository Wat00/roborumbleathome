package roborumbleathome.coordinator.service;

import java.io.Serializable;

import roborumbleathome.coordinator.model.participant.ParticipantTO;

public final class BattleResultTO implements Serializable {

    private final ParticipantTO participantTO;

    private final int score;

    private final int bulletDamage;

    private final int firsts;

    public BattleResultTO(ParticipantTO robotTO, int score, int bulletDamage, int firsts) {
	this.participantTO = robotTO;
	this.score = score;
	this.bulletDamage = bulletDamage;
	this.firsts = firsts;
    }

    public ParticipantTO getParticipantTO() {
	return participantTO;
    }

    public int getScore() {
	return score;
    }

    public int getBulletDamage() {
	return bulletDamage;
    }

    public int getFirsts() {
	return firsts;
    }

}