package roborumbleathome.coordinator.model.participant;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public final class ParticipantTO implements Serializable {

    private final String robot;

    private final URL link;

    private final Integer codesize;

    public ParticipantTO(String robot, URL link) {
	this(robot, link, null);
    }

    public ParticipantTO(String robot, URL link, Integer codesize) {
	try {

	    this.robot = robot;
	    this.link = new URL(link.toString());
	    this.codesize = codesize;

	} catch (MalformedURLException e) {
	    throw new RuntimeException(e);
	}
    }

    public String getRobot() {
	return robot;
    }

    public URL getLink() {
	return link;
    }

    public Integer getCodesize() {
	return codesize;
    }

}