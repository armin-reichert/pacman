package de.amr.games.pacman.model.game;

import static de.amr.easy.game.Application.loginfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;

/**
 * Manages the game's highscore.
 * 
 * @author Armin Reichert
 */
public class Hiscore extends Score {

	private File file = new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml");
	private DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
	private boolean needsUpdate;

	public void load() {
		loginfo("Loading highscore from %s", file);
		Properties p = new Properties();
		try {
			p.loadFromXML(new FileInputStream(file));
			points = Integer.valueOf(p.getProperty("score"));
			level = Integer.valueOf(p.getProperty("level"));
			if (p.getProperty("time") != null) {
				time = ZonedDateTime.parse(p.getProperty("time"), formatter);
			}
		} catch (FileNotFoundException e) {
			loginfo("Hiscore file not available, creating new one");
			createHiscoreFile();
		} catch (DateTimeParseException e) {
			loginfo("Could not parse time in hiscore from file %s", file);
			e.printStackTrace();
		} catch (Exception e) {
			loginfo("Could not load hiscore from file %s", file);
		}
	}

	private void createHiscoreFile() {
		points = 0;
		level = 1;
		time = ZonedDateTime.now();
		needsUpdate = true;
		save();
	}

	public void checkNewHiscore(GameLevel level, int points) {
		if (points > this.points) {
			this.points = points;
			this.level = level.number;
			this.time = ZonedDateTime.now();
			needsUpdate = true;
		}
	}

	public void save() {
		if (needsUpdate) {
			Properties p = new Properties();
			p.setProperty("score", Integer.toString(points));
			p.setProperty("level", Integer.toString(level));
			p.setProperty("time", ZonedDateTime.now().format(formatter));
			try {
				p.storeToXML(new FileOutputStream(file), "Pac-Man Highscore");
				needsUpdate = false;
				loginfo("Saved highscore to %s", file);
			} catch (IOException e) {
				loginfo("Could not save hiscore in file %s", file);
				throw new RuntimeException(e);
			}
		}
	}
}