package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;

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

	public File file;
	public int levelNumber = 1;
	public int points = 0;

	public Hiscore(File file) {
		this.file = file;
		load();
	}

	public void load() {
		loginfo("Loading highscore from %s", file);
		Properties p = new Properties();
		try {
			p.loadFromXML(new FileInputStream(file));
			points = Integer.valueOf(p.getProperty("score"));
			levelNumber = Integer.valueOf(p.getProperty("level"));
		} catch (FileNotFoundException e) {
			points = 0;
			levelNumber = 1;
		} catch (Exception e) {
			loginfo("Could not load hiscore from file %s", file);
			throw new RuntimeException(e);
		}
	}

	public void save() {
		loginfo("Save highscore to %s", file);
		Properties p = new Properties();
		p.setProperty("score", Integer.toString(points));
		p.setProperty("level", Integer.toString(levelNumber));
		try {
			p.storeToXML(new FileOutputStream(file), "Pac-Man Highscore");
		} catch (IOException e) {
			loginfo("Could not save hiscore in file %s", file);
			throw new RuntimeException(e);
		}
	}
}