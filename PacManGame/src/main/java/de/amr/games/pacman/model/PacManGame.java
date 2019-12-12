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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;

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

	public static final int POINTS_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;
	public static final int POINTS_BONUS[] = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	public static final int DIGEST_PELLET_TICKS = 1;
	public static final int DIGEST_ENERGIZER_TICKS = 3;

	public static final int SPEED_1_FPS = 60;
	public static final int SPEED_2_FPS = 70;
	public static final int SPEED_3_FPS = 80;

	static final float BASE_SPEED = (float) 11 * Maze.TS / 60; // 11 tiles/second at 60Hz
	static final File HISCORE_FILE = new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml");

	static final Object[][] LEVELDATA = new Object[][] {
		/*@formatter:off*/
		null, // levels start at 1
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
		{ sec(20), sec(20), sec(20),   Integer.MAX_VALUE }, // Level 1
		{ sec(20), sec(20), sec(1033), Integer.MAX_VALUE }, // Level 2-4
		{ sec(20), sec(20), sec(1037), Integer.MAX_VALUE }, // Level >= 5
		/*@formatter:on*/
	};

	public class Level {

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

		public int numPelletsEaten;
		public int ghostsKilledByEnergizer;
		public int ghostKilledInLevel;

		public Level(int n) {
			if (n < 1) {
				throw new IllegalArgumentException("Level number must be at least 1, was " + n);
			}
			number = n;
			Object[] data = LEVELDATA[Math.min(n, LEVELDATA.length - 1)];
			bonusSymbol = (BonusSymbol) data[0];
			bonusValue = (int) data[1];
			pacManSpeed = (float) data[2];
			pacManDotsSpeed = (float) data[3];
			ghostSpeed = (float) data[4];
			ghostTunnelSpeed = (float) data[5];
			elroy1DotsLeft = (int) data[6];
			elroy1Speed = (float) data[7];
			elroy2DotsLeft = (int) data[8];
			elroy2Speed = (float) data[9];
			pacManPowerSpeed = (float) data[10];
			pacManPowerDotsSpeed = (float) data[11];
			ghostFrightenedSpeed = (float) data[12];
			pacManPowerSeconds = (int) data[13];
			mazeNumFlashes = (int) data[14];
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
			return numPelletsEaten == 70 || numPelletsEaten == 170;
		}

		/**
		 * @return number of pellets not yet eaten
		 */
		public int numPelletsRemaining() {
			return maze.totalNumPellets - numPelletsEaten;
		}

		/**
		 * @param tile
		 *               tile containing food
		 * @return points scored
		 */
		public int eatFoodAt(Tile tile) {
			numPelletsEaten += 1;
			if (maze.containsEnergizer(tile)) {
				ghostsKilledByEnergizer = 0;
				maze.removeFood(tile);
				return POINTS_ENERGIZER;
			}
			else {
				maze.removeFood(tile);
				return POINTS_PELLET;
			}
		}
	}

	public static class Hiscore {

		public int levelNumber;
		public int points;
	}

	public static class LevelSymbols implements Iterable<BonusSymbol> {

		private final Deque<BonusSymbol> list = new ArrayDeque<>(7);

		public int count() {
			return list.size();
		}

		@Override
		public Iterator<BonusSymbol> iterator() {
			return list.iterator();
		}

		public void clear() {
			list.clear();
		}

		public void add(BonusSymbol symbol) {
			if (list.size() == 7) {
				list.removeLast();
			}
			list.addFirst(symbol);
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

	public final Maze maze = new Maze();
	public final LevelSymbols levelSymbols = new LevelSymbols();
	public final Hiscore hiscore = new Hiscore();

	public Level level;
	public int lives;
	public int score;
	public int globalFoodCount;
	public boolean globalFoodCounterEnabled;

	public void init() {
		lives = 3;
		score = 0;
		levelSymbols.clear();
		loadHiscore();
		enterLevel(1);
	}

	public void nextLevel() {
		enterLevel(level.number + 1);
	}

	private void enterLevel(int n) {
		LOGGER.info(() -> "Enter level " + n);
		level = new Level(n);
		maze.restoreFood();
		levelSymbols.add(level.bonusSymbol);
		globalFoodCount = 0;
		globalFoodCounterEnabled = false;
		saveHighscore();
	}

	public void enableGlobalFoodCounter() {
		globalFoodCounterEnabled = true;
		globalFoodCount = 0;
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
		p.setProperty("score", String.valueOf(hiscore.points));
		p.setProperty("level", String.valueOf(hiscore.levelNumber));
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