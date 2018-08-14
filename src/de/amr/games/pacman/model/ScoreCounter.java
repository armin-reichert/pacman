package de.amr.games.pacman.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.amr.easy.game.Application;

public class ScoreCounter {

	private static final File FILE = new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml");

	private final Game game;
	private int score;
	private int hiscore;
	private int level;

	public ScoreCounter(Game game) {
		this.game = game;
		hiscore = 0;
		level = 1;
	}

	public void load() {
		score = 0;
		Properties prop = new Properties();
		try {
			prop.loadFromXML(new FileInputStream(FILE));
		} catch (FileNotFoundException e) {
			save(0);
		} catch (IOException e) {
			Application.LOGGER.info("Could not load hiscore file");
		}
		hiscore = Integer.valueOf(prop.getProperty("score", "0"));
		level = Integer.valueOf(prop.getProperty("level", "0"));
	}

	public void save(int level) {
		Properties prop = new Properties();
		prop.setProperty("score", String.valueOf(hiscore));
		prop.setProperty("level", String.valueOf(level));
		try {
			prop.storeToXML(new FileOutputStream(FILE), "Pac-Man Highscore");
		} catch (FileNotFoundException e) {
			Application.LOGGER.info("Could not find hiscore file");
		} catch (IOException e) {
			Application.LOGGER.info("Could not save hiscore file");
		}
	}

	public void add(int n) {
		score += n;
		checkHiscore();
	}

	private void checkHiscore() {
		if (score >= hiscore) {
			hiscore = score;
			if (game.getLevel() > level) {
				level = game.getLevel();
			}
		}
	}

	public int getScore() {
		return score;
	}

	public int getHiscore() {
		return hiscore;
	}

	public int getLevel() {
		return level;
	}
}