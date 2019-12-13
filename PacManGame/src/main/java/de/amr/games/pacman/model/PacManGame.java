package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.model.BonusSymbol.APPLE;
import static de.amr.games.pacman.model.BonusSymbol.BELL;
import static de.amr.games.pacman.model.BonusSymbol.CHERRIES;
import static de.amr.games.pacman.model.BonusSymbol.GALAXIAN;
import static de.amr.games.pacman.model.BonusSymbol.GRAPES;
import static de.amr.games.pacman.model.BonusSymbol.KEY;
import static de.amr.games.pacman.model.BonusSymbol.PEACH;
import static de.amr.games.pacman.model.BonusSymbol.STRAWBERRY;
import static de.amr.games.pacman.model.Timing.sec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php">Pac-Man
 *      dossier</a>
 * @see <a href=
 *      "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Pac-Man
 *      level specifications</a>
 */
public class PacManGame {

	public static final int POINTS_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;
	public static final int POINTS_BONUS[] = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	public static final int DIGEST_PELLET_TICKS = 1;
	public static final int DIGEST_ENERGIZER_TICKS = 3;

	public static final int SPEED_1_FPS = 60;
	public static final int SPEED_2_FPS = 70;
	public static final int SPEED_3_FPS = 80;

	static final File HISCORE_FILE = new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml");

	static final Level[] LEVELS = new Level[] {
		/*@formatter:off*/
		null, // level numbering starts at 1
		new Level(CHERRIES,   100,  .80f, .71f, .75f, .40f,  20, .8f,  10,  .85f, .90f, .79f, .50f,   6, 5 ),
		new Level(STRAWBERRY, 300,  .90f, .79f, .85f, .45f,  30, .8f,  15,  .95f, .95f, .83f, .55f,   5, 5 ),
		new Level(PEACH,      500,  .90f, .79f, .85f, .45f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   4, 5 ),
		new Level(PEACH,      500,  .90f, .79f, .85f, .50f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   3, 5 ),
		new Level(APPLE,      700,    1f, .87f, .95f, .50f,  40, .8f,  20, .105f,   1f, .87f, .60f,   2, 5 ),
		new Level(APPLE,      700,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   5, 5 ),
		new Level(GRAPES,    1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 ),
		new Level(GRAPES,    1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 ),
		new Level(GALAXIAN,  2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   1, 3 ),
		new Level(GALAXIAN,  2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   5, 5 ),
		new Level(BELL,      3000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   2, 5 ),
		new Level(BELL,      3000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 ),
		new Level(KEY,       5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 ),
		new Level(KEY,       5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   3, 5 ),
		new Level(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 ),
		new Level(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 ),
		new Level(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 ),
		new Level(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 ),
		new Level(KEY,       5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 ),
		new Level(KEY,       5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 ),
		new Level(KEY,       5000,  .90f, .79f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 ),
		/*@formatter:on*/
	};

	public final Maze maze = new Maze();
	public final LevelSymbols levelSymbols = new LevelSymbols();
	public final Hiscore hiscore = new Hiscore();

	public Level level;
	public int lives;
	public int score;

	public void init() {
		lives = 3;
		score = 0;
		levelSymbols.clear();
		loadHiscore();
		enterLevel(1);
	}

	private void enterLevel(int n) {
		LOGGER.info(() -> "Enter level " + n);
		int index = n < LEVELS.length ? n : LEVELS.length - 1;
		level = LEVELS[index];
		level.number = n;
		level.numPelletsEaten = 0;
		level.ghostsKilledByEnergizer = 0;
		level.ghostKilledInLevel = 0;
		levelSymbols.add(level.bonusSymbol);
		maze.restoreFood();
		saveHighscore();
	}

	public void enterNextLevel() {
		enterLevel(level.number + 1);
	}

	/**
	 * @return number of pellets not yet eaten
	 */
	public int numPelletsRemaining() {
		return maze.totalNumPellets - level.numPelletsEaten;
	}

	/**
	 * @param tile tile containing food
	 * @return points scored
	 */
	public int eatFoodAt(Tile tile) {
		level.numPelletsEaten += 1;
		if (maze.containsEnergizer(tile)) {
			level.ghostsKilledByEnergizer = 0;
			maze.removeFood(tile);
			return POINTS_ENERGIZER;
		} else {
			maze.removeFood(tile);
			return POINTS_PELLET;
		}
	}

	/**
	 * @return number of ticks the bonus is active
	 */
	public int bonusActiveTicks() {
		return sec(9 + new Random().nextInt(1));
	}

	/**
	 * @return number of ticks the consumed bonus is active
	 */
	public int bonusConsumedTicks() {
		return sec(3);
	}

	/**
	 * @return if bonus will become active
	 */
	public boolean isBonusScoreReached() {
		return level.numPelletsEaten == 70 || level.numPelletsEaten == 170;
	}

	// Score management

	public void loadHiscore() {
		LOGGER.info("Loading highscore from " + HISCORE_FILE);
		Properties p = new Properties();
		try {
			p.loadFromXML(new FileInputStream(HISCORE_FILE));
			hiscore.points = Integer.valueOf(p.getProperty("score"));
			hiscore.levelNumber = Integer.valueOf(p.getProperty("level"));
		} catch (FileNotFoundException e) {
			hiscore.points = 0;
			hiscore.levelNumber = 1;
		} catch (Exception e) {
			LOGGER.info("Could not load hiscore from file " + HISCORE_FILE);
			throw new RuntimeException(e);
		}
	}

	public void saveHighscore() {
		LOGGER.info("Save highscore to " + HISCORE_FILE);
		Properties p = new Properties();
		p.setProperty("score", Integer.toString(hiscore.points));
		p.setProperty("level", Integer.toString(hiscore.levelNumber));
		try {
			p.storeToXML(new FileOutputStream(HISCORE_FILE), "Pac-Man Highscore");
		} catch (IOException e) {
			LOGGER.info("Could not save hiscore in file " + HISCORE_FILE);
			throw new RuntimeException(e);
		}
	}

	public void score(int points) {
		int oldScore = score;
		score += points;
		if (score > hiscore.points) {
			hiscore.points = score;
			hiscore.levelNumber = level.number;
		}
		if (oldScore < 10_000 && 10_000 <= score) {
			lives += 1;
		}
	}
}