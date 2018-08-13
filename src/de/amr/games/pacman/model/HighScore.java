package de.amr.games.pacman.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.amr.easy.game.Application;

public class HighScore {

	private static final File FILE = new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml");

	private final Game game;
	private int score;
	private int level;

	public HighScore(Game game) {
		this.game = game;
	}

	public void load() {
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

	private void store() {
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

	public void save() {
		if (game.score.get() >= score) {
			score = game.score.get();
			if (game.getLevel() >= level) {
				level = game.getLevel();
			}
		}
		store();
	}

	public int getScore() {
		return score;
	}

	public int getLevel() {
		return level;
	}
}