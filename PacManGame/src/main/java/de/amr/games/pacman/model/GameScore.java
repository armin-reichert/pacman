package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Data structure storing the game (high) score.
 * 
 * @author Armin Reichert
 */
public class GameScore {

	/** High score points */
	public int hiscore = 0;

	/** High score level */
	public int hiscoreLevel = 1;

	/** High score time */
	public ZonedDateTime hiscoreTime;

	private boolean needsUpdate;
	private File file;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

	public GameScore(File file) {
		this.file = file;
	}

	public void load() {
		loginfo("Loading highscore from %s", file);
		Properties p = new Properties();
		try {
			p.loadFromXML(new FileInputStream(file));
			hiscore = Integer.valueOf(p.getProperty("score"));
			hiscoreLevel = Integer.valueOf(p.getProperty("level"));
			String time = p.getProperty("time");
			if (time != null) {
				hiscoreTime = ZonedDateTime.parse(p.getProperty("time"), formatter);
			}
		} catch (FileNotFoundException e) {
			loginfo("High score file not available, creating new one");
			hiscore = 0;
			hiscoreLevel = 0;
			hiscoreTime = ZonedDateTime.now();
			save();
		} catch (Exception e) {
			loginfo("Could not load hiscore from file %s", file);
		}
	}

	public void update(GameLevel level, int score) {
		if (score > hiscore) {
			hiscore = score;
			hiscoreLevel = level.number;
			hiscoreTime = ZonedDateTime.now();
			needsUpdate = true;
		}
	}

	public void save() {
		if (needsUpdate) {
			loginfo("Save highscore to %s", file);
			Properties p = new Properties();
			p.setProperty("score", Integer.toString(hiscore));
			p.setProperty("level", Integer.toString(hiscoreLevel));
			p.setProperty("time", ZonedDateTime.now().format(formatter));
			try {
				p.storeToXML(new FileOutputStream(file), "Pac-Man Highscore");
				needsUpdate = false;
			} catch (IOException e) {
				loginfo("Could not save hiscore in file %s", file);
				throw new RuntimeException(e);
			}
		}
	}
}