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

import java.util.LinkedList;
import java.util.List;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame {

	/** The tile size (8px). */
	public static final int TS = 8;

	/** Base speed (11 tiles/second) in pixel/tick. */
	public static final float BASE_SPEED = (float) 11 * TS / 60;

	/** Idle time after eating normal pellet. */
	public static final int DIGEST_TICKS = 1;

	/** Idle time after eating energizer. */
	public static final int DIGEST_TICKS_ENERGIZER = 1;

	public static final int PELLET_VALUE = 10;

	public static final int ENERGIZER_VALUE = 50;

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	/**
	 * @param fraction fraction of seconds
	 * @return ticks corresponding to given fraction of seconds at 60Hz
	 */
	public static int sec(float fraction) {
		return (int) (60 * fraction);
	}

	/** Ticks for given minutes at 60 Hz */
	public static int min(float min) {
		return (int) (3600 * min);
	}

	/**
	 * @see <a href=
	 *      "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Gamasutra</a>
	 */
	final PacManGameLevel[] levels = PacManGameLevel.parse(new Object[][] {
		/*@formatter:off*/
		{ CHERRIES,    100,  .80f, .71f, .75f, .40f,  20, .8f,  10,  .85f, .90f, .79f, .50f,   6, 5 },
		{ STRAWBERRY,  300,  .90f, .79f, .85f, .45f,  30, .8f,  15,  .95f, .95f, .83f, .55f,   5, 5 },
		{ PEACH,       500,  .90f, .79f, .85f, .45f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   4, 5 },
		{ PEACH,       500,  .90f, .79f, .85f, .50f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   3, 5 },
		{ APPLE,       700,    1f, .87f, .95f, .50f,  40, .8f,  20, .105f,   1f, .87f, .60f,   2, 5 },
		{ APPLE,       700,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   5, 5 },
		{ GRAPES,     1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 },
		{ GRAPES,     1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 },
		{ GALAXIAN,   2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   1, 3 },
		{ GALAXIAN,   2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   5, 5 },
		{ BELL,       3000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   2, 5 },
		{ BELL,       3000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   3, 5 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 },
		{ KEY,        5000,  .90f, .79f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 },
		/*@formatter:on*/
	});

	/** The maze (model). */
	public final Maze maze;

	/** The game score including highscore management. */
	public final Score score;

	/** Level counter symbols displayed at the bottom right corner. */
	public final List<BonusSymbol> levelCounter = new LinkedList<>();

	/** Pac-Man lives. */
	public int lives;

	/** Pellets + energizers eaten in current level. */
	private int numPelletsEaten;

	/** Global food counter. */
	public int globalFoodCount;

	/** If global food counter is enabled. */
	public boolean globalFoodCounterEnabled = false;

	/** Ghosts killed using current energizer. */
	public int numGhostsKilledByCurrentEnergizer;

	/** Current level number. */
	public int levelNumber;

	public PacManGame() {
		maze = new Maze();
		score = new Score();
	}

	public void init() {
		lives = 3;
		levelNumber = 1;
		maze.restoreFood();
		levelCounter.clear();
		score.loadHiscore();
	}

	public void startLevel() {
		LOGGER.info("Start game level " + levelNumber);
		maze.restoreFood();
		numPelletsEaten = 0;
		numGhostsKilledByCurrentEnergizer = 0;
		levelCounter.add(0, level().bonusSymbol);
		if (levelCounter.size() > 8) {
			levelCounter.remove(levelCounter.size() - 1);
		}
		globalFoodCounterEnabled = false;
		globalFoodCount = 0;
	}

	/**
	 * @return the current level parameters
	 */
	public PacManGameLevel level() {
		// Note: levelNumber counts from 1!
		if (levelNumber - 1 < levels.length) {
			return levels[levelNumber - 1];
		}
		return levels[levels.length - 1];
	}

	public int eat(Tile tile) {
		numPelletsEaten += 1;
		if (maze.containsEnergizer(tile)) {
			numGhostsKilledByCurrentEnergizer = 0;
			maze.removeFood(tile);
			return ENERGIZER_VALUE;
		} else {
			maze.removeFood(tile);
			return PELLET_VALUE;
		}
	}

	public void enableGlobalFoodCounter() {
		globalFoodCounterEnabled = true;
		globalFoodCount = 0;
	}

	public int numPelletsRemaining() {
		return maze.totalNumPellets - numPelletsEaten;
	}

	/**
	 * @param points points scored
	 * @return <code>true</code> if new life has been granted
	 */
	public boolean scorePoints(int points) {
		int oldScore = score.getPoints();
		int newScore = oldScore + points;
		score.set(levelNumber, newScore);
		if (oldScore < 10_000 && 10_000 <= newScore) {
			lives += 1;
			return true;
		}
		return false;
	}

	public boolean isBonusReached() {
		return numPelletsEaten == 70 || numPelletsEaten == 170;
	}
}