package roborumbleathome.coordinator.model;

import java.io.File;

public final class Game {

    private final String competition;

    private final Integer codesize;

    private final File file;

    public Game(String competition, Integer codesize, File file) {
	this.competition = competition;
	this.codesize = codesize;
	this.file = file;
    }

    public boolean isCodesizeAllowed(Integer codesize) {
	return (this.codesize != null) ? ((codesize != null) && (codesize < this.codesize)) : true;
    }

    public String getCompetition() {
	return competition;
    }

    public File getFile() {
	return file;
    }

    @Override
    public String toString() {
	return "Game [competition=" + competition + ", codesize=" + codesize + ", file=" + file + "]";
    }

}