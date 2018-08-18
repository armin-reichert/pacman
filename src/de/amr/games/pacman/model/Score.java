package de.amr.games.pacman.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.amr.easy.game.Application;

public class Score {

	private final Game game;
	private Properties prop = new Properties();
	private File file;
	private int score;
	private int hiscore;
	private int level = 1;
	private boolean newHiscore;

	public Score(Game game) {
		this.game = game;
		file = new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml");
	}

	public void load() {
		try {
			prop.loadFromXML(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			store();
		} catch (IOException e) {
			Application.LOGGER.info("Could not load hiscore file");
		}
		hiscore = Integer.valueOf(prop.getProperty("score", "0"));
		level = Integer.valueOf(prop.getProperty("level", "1"));
	}

	public void save() {
		if (!newHiscore) {
			return;
		}
		store();
	}

	private void store() {
		prop.setProperty("score", String.valueOf(hiscore));
		prop.setProperty("level", String.valueOf(level));
		try {
			prop.storeToXML(new FileOutputStream(file), "Pac-Man Highscore");
		} catch (FileNotFoundException e) {
			Application.LOGGER.info("Could not find hiscore file");
		} catch (IOException e) {
			Application.LOGGER.info("Could not save hiscore file");
		}
	}

	public void set(int n) {
		score = n;
		checkHiscore();

	}

	public void add(int n) {
		set(score + n);
	}

	private void checkHiscore() {
		if (score > hiscore) {
			newHiscore = true;
			hiscore = score;
			level = game.getLevel();
		}
	}

	public int getScore() {
		return score;
	}

	public int getHiscore() {
		return hiscore;
	}

	public int getHiscoreLevel() {
		return level;
	}
}