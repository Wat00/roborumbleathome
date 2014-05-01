package roborumbleathome.coordinator.integration;

import static java.lang.Math.min;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import roborumbleathome.coordinator.controller.downloadmissingbots.DownloadMissingBotsController;
import roborumbleathome.coordinator.model.Configuration;
import roborumbleathome.coordinator.model.participant.ParticipantTO;

public class BotProxy {

    private static final Logger LOGGER = Logger.getLogger(BotProxy.class.getName());
    
    public static class CouldNotDownloadException extends Exception {

	private final File file;

	public CouldNotDownloadException(File file, Throwable cause) {
	    super(cause);
	    this.file = file;
	}

	public CouldNotDownloadException(File file) {
	    this.file = file;
	}

	public File getFile() {
	    return file;
	}

    }

    public static class CouldNotFindException extends Exception {

	private final String robot;

	private final URL url;

	public CouldNotFindException(String robot, URL url) {
	    this.robot = robot;
	    this.url = url;
	}

	public String getRobot() {
	    return robot;
	}

	public URL getUrl() {
	    return url;
	}

    }

    private static final int TIMEOUT = 10000;

    private final Configuration configuration;

    public BotProxy(Configuration configuration) {
	this.configuration = configuration;
    }

    private File getTempFile(ParticipantTO participant) {
	String filename = participant.getRobot().replaceAll(" ", "_") + ".jar";
	File temp = configuration.getTemp();
	return new File(temp, filename);
    }

    public File downloadBot(ParticipantTO participant) throws CouldNotDownloadException, CouldNotFindException {
	File tempFile = getTempFile(participant);
	try {
	    URL url = participant.getLink();

	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    try {

		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);

		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.setRequestProperty("User-Agent", "RoboRumble@Home - gzip, deflate");

		int responseCode = conn.getResponseCode();
		if (responseCode == HTTP_NOT_FOUND) {
		    throw new CouldNotFindException(participant.getRobot(), url);
		}

		if (responseCode != HTTP_OK) {
		    throw new CouldNotDownloadException(tempFile);
		}

		InputStream inputStream = conn.getInputStream();
		try {

		    String encoding = conn.getContentEncoding();

		    if ("gzip".equalsIgnoreCase(encoding)) {
			inputStream = new GZIPInputStream(inputStream);
		    } else if ("deflate".equalsIgnoreCase(encoding)) {
			inputStream = new InflaterInputStream(inputStream);
		    }

		    long remaining = conn.getContentLengthLong();
		    if (remaining == -1) {
			remaining = Long.MAX_VALUE;
		    }

		    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
		    try {

			byte[] b = new byte[8192];
			int len;
			while (remaining > 0 && (len = inputStream.read(b)) != -1) {
			    outputStream.write(b, 0, (int) min(len, remaining));
			    remaining -= len;
			}

		    } finally {
			outputStream.close();
		    }

		} finally {
		    inputStream.close();
		}
	    } finally {
		conn.disconnect();
	    }

	} catch (IOException e) {
	    throw new CouldNotDownloadException(tempFile, e);
	}
	return tempFile;
    }

}
