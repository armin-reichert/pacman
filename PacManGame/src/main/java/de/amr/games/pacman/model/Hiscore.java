package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Data structure storing the highscore.
 * 
 * @author Armin Reichert
 */
public class Hiscore {

	public int levelNumber = 1;
	public int points = 0;

	public void load(File file) {
		LOGGER.info("Loading highscore from " + file);
		Properties p = new Properties();
		try {
			p.loadFromXML(new FileInputStream(file));
			points = Integer.valueOf(p.getProperty("score"));
			levelNumber = Integer.valueOf(p.getProperty("level"));
		} catch (FileNotFoundException e) {
			points = 0;
			levelNumber = 1;
		} catch (Exception e) {
			LOGGER.info("Could not load hiscore from file " + file);
			throw new RuntimeException(e);
		}
	}

	public void save(File file) {
		LOGGER.info("Save highscore to " + file);
		Properties p = new Properties();
		p.setProperty("score", Integer.toString(points));
		p.setProperty("level", Integer.toString(levelNumber));
		try {
			p.storeToXML(new FileOutputStream(file), "Pac-Man Highscore");
		} catch (IOException e) {
			LOGGER.info("Could not save hiscore in file " + file);
			throw new RuntimeException(e);
		}
	}

}