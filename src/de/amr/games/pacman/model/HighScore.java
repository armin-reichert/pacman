package de.amr.games.pacman.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.amr.easy.game.Application;

public class HighScore {

	private static final File FILE = new File(new File(System.getProperty("user.home")),
			"pacman.hiscore.xml");

	private static int score;
	private static int level;

	public static void load() {
		Properties prop = new Properties();
		try {
			prop.loadFromXML(new FileInputStream(FILE));
		} catch (FileNotFoundException e) {
			store();
		} catch (IOException e) {
			Application.LOGGER.info("Could not load hiscore file");
		}
		score = Integer.valueOf(prop.getProperty("score", "0"));
		level = Integer.valueOf(prop.getProperty("level", "0"));
	}

	private static void store() {
		Properties prop = new Properties();
		prop.setProperty("score", String.valueOf(score));
		prop.setProperty("level", String.valueOf(level));
		try {
			prop.storeToXML(new FileOutputStream(FILE), "Pac-Man Highscore");
		} catch (FileNotFoundException e) {
			Application.LOGGER.info("Could not find hiscore file");
		} catch (IOException e) {
			Application.LOGGER.info("Could not save hiscore file");
		}
	}

	public static void save(int newScore, int newLevel) {
		if (newScore >= score) {
			score = newScore;
			if (newLevel >= level) {
				level = newLevel;
			}
		}
		store();
	}

	public static int getScore() {
		return score;
	}

	public static int getLevel() {
		return level;
	}
}