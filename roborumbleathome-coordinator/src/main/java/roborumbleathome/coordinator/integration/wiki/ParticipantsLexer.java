package roborumbleathome.coordinator.integration.wiki;

import java.io.BufferedReader;
import java.io.IOException;

import roborumbleathome.coordinator.model.Configuration;

class ParticipantsLexer {

    public static final int BEGIN = 1;
    public static final int END = 2;
    public static final int LINE = 3;
    public static final int EOF = 4;

    private final Configuration configuration;

    private final BufferedReader bufferedReader;

    private String line;

    ParticipantsLexer(BufferedReader bufferedReader, Configuration configuration) {
	this.bufferedReader = bufferedReader;
	this.configuration = configuration;
    }

    int nextToken() throws IOException {
	do {
	    line = bufferedReader.readLine();
	    if (line == null) {
		return EOF;
	    }

	    line = line.trim();
	} while (line.isEmpty());

	String startTag = '<' + configuration.getStartTag() + '>';
	if (startTag.equals(line)) {
	    return BEGIN;
	}

	String endTag = "</" + configuration.getStartTag() + '>';
	if (endTag.equals(line)) {
	    return END;
	}

	return LINE;
    }

    String getLine() {
	return line;
    }

}