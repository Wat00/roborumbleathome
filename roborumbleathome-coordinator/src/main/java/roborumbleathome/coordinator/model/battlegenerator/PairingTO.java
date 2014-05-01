package roborumbleathome.coordinator.model.battlegenerator;

public final class PairingTO {

    private final String robot1;

    private final String robot2;

    public PairingTO(String robot1, String robot2) {
	this.robot1 = robot1;
	this.robot2 = robot2;
    }

    public String getRobot1() {
	return robot1;
    }

    public String getRobot2() {
	return robot2;
    }

    @Override
    public int hashCode() {
	return robot1.hashCode() | robot2.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	return equals((PairingTO) obj);
    }

    public boolean equals(PairingTO obj) {
	return (robot1.equals(obj.robot1) && robot2.equals(obj.robot2)) || robot1.equals(obj.robot2) && robot2.equals(obj.robot1);
    }

}