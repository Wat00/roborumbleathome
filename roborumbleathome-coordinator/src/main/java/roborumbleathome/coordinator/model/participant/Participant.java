package roborumbleathome.coordinator.model.participant;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import roborumbleathome.coordinator.model.Game;

public class Participant {

    private final String robot;

    private URL link;

    private final Map<Game, Integer> battlesMap;

    private Integer codesize;

    private boolean unstable;

    Participant(String name) {
	this.robot = name;
	this.battlesMap = new HashMap<Game, Integer>();
    }

    @Override
    public String toString() {
	return "Participant [robot=" + robot + ", link=" + link + ", battlesMap=" + battlesMap + "]";
    }

    public String getRobot() {
	return robot;
    }

    public URL getLink() {
	return link;
    }

    public void setLink(URL link) {
	this.link = link;
    }

    public Integer getBattles(Game game) {
	return battlesMap.get(game);
    }

    public void setBattles(Game game, Integer battles) {
	this.battlesMap.put(game, battles);
    }

    public Integer getCodesize() {
	return codesize;
    }

    public void setCodesize(Integer codesize) {
	this.codesize = codesize;
    }

    public boolean isUnstable() {
	return unstable;
    }

    public void setUnstable(boolean unstable) {
	this.unstable = unstable;
    }

    public ParticipantTO createTO() {
	return new ParticipantTO(robot, link, codesize);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((robot == null) ? 0 : robot.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Participant other = (Participant) obj;
	if (robot == null) {
	    if (other.robot != null)
		return false;
	} else if (!robot.equals(other.robot))
	    return false;
	return true;
    }

}