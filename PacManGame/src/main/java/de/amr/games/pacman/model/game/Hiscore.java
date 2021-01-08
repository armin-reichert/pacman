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
public class Hiscore {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_ZONED_DATE_TIME;

	public int points;
	public int level;

	private final File file;
	private final Properties data = new Properties(3);
	private ZonedDateTime time;
	private boolean needsUpdate;

	public Hiscore(File file) {
		this.file = file;
		points = 0;
		level = 1;
		time = ZonedDateTime.now();
		needsUpdate = true;
		load();
	}

	public void load() {
		loginfo("Loading highscore from file '%s'", file);
		try (FileInputStream is = new FileInputStream(file)) {
			data.loadFromXML(is);
			points = Integer.valueOf(data.getProperty("score"));
			level = Integer.valueOf(data.getProperty("level"));
			if (data.getProperty("time") != null) {
				time = ZonedDateTime.parse(data.getProperty("time"), DATE_FORMAT);
			} else {
				time = ZonedDateTime.now();
			}
		} catch (FileNotFoundException e) {
			loginfo("Hiscore file not available, creating new one");
			save();
		} catch (DateTimeParseException e) {
			loginfo("Could not parse time in hiscore file '%s'", file);
			e.printStackTrace();
		} catch (Exception e) {
			loginfo("Could not load hiscore file '%s'", file);
			e.printStackTrace();
		}
	}

	public void save() {
		if (needsUpdate) {
			data.setProperty("score", Integer.toString(points));
			data.setProperty("level", Integer.toString(level));
			if (time == null) {
				time = ZonedDateTime.now();
			}
			data.setProperty("time", time.format(DATE_FORMAT));
			try (FileOutputStream os = new FileOutputStream(file)) {
				data.storeToXML(os, "Pac-Man Highscore");
				needsUpdate = false;
				loginfo("Saved highscore file '%s'", file);
			} catch (IOException e) {
				loginfo("Could not save hiscore file '%s'", file);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Checks is the given level number and points mark a new hiscore.
	 * 
	 * @param levelNumber level number
	 * @param points      points
	 */
	public void check(int levelNumber, int points) {
		if (points > this.points) {
			this.points = points;
			this.level = levelNumber;
			time = ZonedDateTime.now();
			needsUpdate = true;
		}
	}
}