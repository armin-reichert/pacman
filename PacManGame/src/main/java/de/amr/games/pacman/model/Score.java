package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Score {

	private PacManGame game;
	private File file;
	private int points;
	private int hiscorePoints;
	private int hiscoreLevel;

	public Score(PacManGame game) {
		this.game = game;
		file = new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml");
	}

	public void loadHiscore() {
		points = 0;
		Properties prop = new Properties();
		try {
			prop.loadFromXML(new FileInputStream(file));
			hiscorePoints = Integer.valueOf(prop.getProperty("score"));
			hiscoreLevel = Integer.valueOf(prop.getProperty("level"));
		} catch (FileNotFoundException e) {
			// create new score file
			hiscoreLevel = 1;
			hiscorePoints = 0;
			save();
		} catch (IOException e) {
			LOGGER.info("Could not load score file");
			throw new RuntimeException(e);
		}
	}

	public void save() {
		Properties prop = new Properties();
		prop.setProperty("score", String.valueOf(hiscorePoints));
		prop.setProperty("level", String.valueOf(hiscoreLevel));
		try {
			prop.storeToXML(new FileOutputStream(file), "Pac-Man Highscore");
		} catch (IOException e) {
			LOGGER.info("Could not save score file");
			throw new RuntimeException(e);
		}
	}

	public void set(int n) {
		points = n;
		if (points > hiscorePoints) {
			hiscorePoints = points;
			hiscoreLevel = game.level;
		}
	}

	public int getPoints() {
		return points;
	}

	public int getHiscorePoints() {
		return hiscorePoints;
	}

	public int getHiscoreLevel() {
		return hiscoreLevel;
	}
}