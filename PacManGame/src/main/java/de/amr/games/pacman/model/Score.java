package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Data structure storing the highscore.
 * 
 * @author Armin Reichert
 */
public class Score {

	public File file;
	public int hiscore = 0;
	public int hiscoreLevel = 1;
	public Date hiscoreTime;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	public Score(File file) {
		this.file = file;
		load();
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
				hiscoreTime = dateFormat.parse(p.getProperty("time"));
			}
		} catch (FileNotFoundException e) {
			hiscore = 0;
			hiscoreLevel = 1;
			hiscoreTime = Calendar.getInstance().getTime();
		} catch (Exception e) {
			loginfo("Could not load hiscore from file %s", file);
			throw new RuntimeException(e);
		}
	}

	public void save() {
		loginfo("Save highscore to %s", file);
		Properties p = new Properties();
		p.setProperty("score", Integer.toString(hiscore));
		p.setProperty("level", Integer.toString(hiscoreLevel));
		p.setProperty("time", dateFormat.format(Calendar.getInstance().getTime()));
		try {
			p.storeToXML(new FileOutputStream(file), "Pac-Man Highscore");
		} catch (IOException e) {
			loginfo("Could not save hiscore in file %s", file);
			throw new RuntimeException(e);
		}
	}
}