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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * @author Armin Reichert
 * 
 * @see <a href= "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php">Pac-Man
 *      dossier</a>
 * @see <a href= "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Pac-Man level
 *      specifications</a>
 */
public class PacManGame {

	public static final float BASE_SPEED = (float) 11 * Maze.TS / 60; // 11 tiles/second at 60Hz
	public static final int DIGEST_TICKS = 1;
	public static final int DIGEST_TICKS_ENERGIZER = 3;
	public static final int POINTS_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;

	public static final int[] BONUS_NUMBERS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	static final Object[][] LEVELS = new Object[][] {
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
	};

	static final int[][] SCATTERING_TICKS = {
		/*@formatter:off*/
		{ sec(7), sec(7), sec(5), sec(5) }, // Level 1
		{ sec(7), sec(7), sec(5), 1 },      // Level 2-4
		{ sec(5), sec(5), sec(5), 1 },      // Level >= 5
		/*@formatter:on*/
	};

	static final int[][] CHASING_TICKS = {
		/*@formatter:off*/
		{ sec(20), sec(20), sec(20),                Integer.MAX_VALUE }, // Level 1
		{ sec(20), sec(20), min(17) + sec(13) + 14, Integer.MAX_VALUE }, // Level 2-4
		{ sec(20), sec(20), min(17) + sec(17) + 14, Integer.MAX_VALUE }, // Level >= 5
		/*@formatter:on*/
	};

	public static class Level {

		public final int number;
		public final BonusSymbol bonusSymbol;
		public final int bonusValue;
		public final float pacManSpeed;
		public final float pacManDotsSpeed;
		public final float ghostSpeed;
		public final float ghostTunnelSpeed;
		public final int elroy1DotsLeft;
		public final float elroy1Speed;
		public final int elroy2DotsLeft;
		public final float elroy2Speed;
		public final float pacManPowerSpeed;
		public final float pacManPowerDotsSpeed;
		public final float ghostFrightenedSpeed;
		public final int pacManPowerSeconds;
		public final int mazeNumFlashes;

		public Level(int number, Object[] row) {
			this.number = number;
			bonusSymbol = (BonusSymbol) row[0];
			bonusValue = (int) row[1];
			pacManSpeed = (float) row[2];
			pacManDotsSpeed = (float) row[3];
			ghostSpeed = (float) row[4];
			ghostTunnelSpeed = (float) row[5];
			elroy1DotsLeft = (int) row[6];
			elroy1Speed = (float) row[7];
			elroy2DotsLeft = (int) row[8];
			elroy2Speed = (float) row[9];
			pacManPowerSpeed = (float) row[10];
			pacManPowerDotsSpeed = (float) row[11];
			ghostFrightenedSpeed = (float) row[12];
			pacManPowerSeconds = (int) row[13];
			mazeNumFlashes = (int) row[14];
		}

		/**
		 * @param round
		 *                attack round
		 * @return number of ticks ghost will scatter in this round and level
		 */
		public int scatterTicks(int round) {
			return SCATTERING_TICKS[(number == 1) ? 0 : (number <= 4) ? 1 : 2][Math.min(round, 3)];
		}

		/**
		 * @param round
		 *                attack round
		 * @return number of ticks ghost will chase in this round and level
		 */
		public int chasingTicks(int round) {
			return CHASING_TICKS[(number == 1) ? 0 : (number <= 4) ? 1 : 2][Math.min(round, 3)];
		}
	}

	/**
	 * @param fraction
	 *                   fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	/**
	 * @param fraction
	 *                   fraction of seconds
	 * @return ticks corresponding to given fraction of seconds at 60Hz
	 */
	public static int sec(float fraction) {
		return (int) (60 * fraction);
	}

	/** Ticks for given minutes at 60 Hz */
	public static int min(float min) {
		return (int) (3600 * min);
	}

	public int lives;
	public int numPelletsEaten;
	public int globalFoodCount;
	public boolean globalFoodCounterEnabled;
	public int numGhostsKilledByCurrentEnergizer;
	public int levelNumber;
	public Level level;
	public final Deque<BonusSymbol> levelCounter;
	public final Maze maze;
	public final Score score;

	public PacManGame() {
		levelCounter = new ArrayDeque<>(8);
		maze = new Maze();
		score = new Score();
	}

	public void start() {
		LOGGER.info("Start game");
		score.loadHiscore();
		lives = 3;
		levelCounter.clear();
		startLevel(1);
	}

	public void nextLevel() {
		startLevel(++levelNumber);
	}

	public void startLevel(int n) {
		LOGGER.info("Start level " + n);
		levelNumber = n;
		level = new Level(n, LEVELS[Math.min(levelNumber - 1, LEVELS.length - 1)]);
		maze.restoreFood();
		numPelletsEaten = 0;
		numGhostsKilledByCurrentEnergizer = 0;
		if (levelCounter.size() == 8) {
			levelCounter.removeLast();
		}
		levelCounter.addFirst(level.bonusSymbol);
		globalFoodCount = 0;
		globalFoodCounterEnabled = false;
	}

	/**
	 * @param tile
	 *               tile containing pellet
	 * @return points scored for eating pellet
	 */
	public int eat(Tile tile) {
		numPelletsEaten += 1;
		if (maze.containsEnergizer(tile)) {
			numGhostsKilledByCurrentEnergizer = 0;
			maze.removeFood(tile);
			return POINTS_ENERGIZER;
		}
		else {
			maze.removeFood(tile);
			return POINTS_PELLET;
		}
	}

	/**
	 * @return number of pellets not yet eaten
	 */
	public int numPelletsRemaining() {
		return maze.totalNumPellets - numPelletsEaten;
	}

	/**
	 * @param points
	 *                 points scored
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

	/**
	 * @return if bonus will become active
	 */
	public boolean isBonusScoreReached() {
		return numPelletsRemaining() == 70 || numPelletsRemaining() == 170;
	}

	public void enableGlobalFoodCounter() {
		globalFoodCounterEnabled = true;
		globalFoodCount = 0;
	}
}