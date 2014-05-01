package roborumbleathome.coordinator.model.battlegenerator;

public class Pairing {

    private int battles;

    private int random;

    // private boolean priority;

    public int getBattles() {
	return battles;
    }

    public void incrementBattle() {
	++battles;
    }

    public int getRandom() {
	return random;
    }

    public void setRandom(int random) {
	this.random = random;
    }

    // public boolean isPriority() {
    // return priority;
    // }

}