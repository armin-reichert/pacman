package de.amr.games.pacman.model;

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
import java.util.Random;
import java.util.function.IntSupplier;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;

/**
 * The model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Game {

	/** Tile size used throughout the game. */
	public static final int TS = 16;

	public static final int FOOD_EATEN_FOR_BONUS_1 = 70;
	public static final int FOOD_EATEN_FOR_BONUS_2 = 170;
	public static final int SCORE_FOR_EXTRA_LIFE = 10_000;
	public static final int[] KILLED_GHOST_POINTS = { 200, 400, 800, 1600 };

	public final Maze maze;
	public final IntSupplier fnTicksPerSecond;
	public final Counter score = new Counter();
	public final Counter lives = new Counter();
	public final Counter foodEaten = new Counter();
	public final Counter ghostsKilledInSeries = new Counter();
	public final List<BonusSymbol> levelCounter = new LinkedList<>();
	private final long foodTotal;
	private int level;
	private final float baseSpeed;
	private final Random rnd = new Random();

	public Game(Maze maze, IntSupplier fnTicksPerSecond) {
		this.maze = maze;
		this.fnTicksPerSecond = fnTicksPerSecond;
		baseSpeed = tps(8f);
		foodTotal = maze.getFoodTotal();
	}

	public void init() {
		score.set(0);
		lives.set(3);
		levelCounter.clear();
		level = 0;
		nextLevel();
	}

	public void nextLevel() {
		maze.resetFood();
		foodEaten.set(0);
		ghostsKilledInSeries.set(0);
		level += 1;
		levelCounter.add(0, getBonusSymbol());
		if (levelCounter.size() == 8) {
			levelCounter.remove(levelCounter.size() - 1);
		}
	}

	public int getLevel() {
		return level;
	}

	public int getFoodValue(char food) {
		return food == Content.PELLET ? 10 : food == Content.ENERGIZER ? 50 : 0;
	}

	public int getKilledGhostValue() {
		return KILLED_GHOST_POINTS[ghostsKilledInSeries.get()];
	}

	public long getFoodTotal() {
		return foodTotal;
	}

	/** Ticks representing the given seconds. */
	public int sec(float seconds) {
		return Math.round(fnTicksPerSecond.getAsInt() * seconds);
	}

	/** Tiles per second. */
	private float tps(float value) {
		return (value * Game.TS) / fnTicksPerSecond.getAsInt();
	}

	private float speed(float relativeSpeed) {
		return baseSpeed * relativeSpeed;
	}

	// Level data

	private enum Field {
		BonusSymbol,
		iBonusValue,
		fPacManSpeed,
		fPacManDotsSpeed,
		fGhostSpeed,
		fGhostTunnelSpeed,
		iElroy1DotsLeft,
		fElroy1Speed,
		iElroy2DotsLeft,
		fElroy2Speed,
		fPacManSteroidSpeed,
		fPacManSteroidDotsSpeed,
		fGhostAfraidSpeed,
		iPacManSteroidSeconds,
		iNumFlashes
	};

	/**
	 * @see <a href= "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Gamasutra</a>
	 */
	private static final Object[][] DATA = {
	/*@formatter:off*/
	{ /* not used */},
	{ CHERRIES,           100,  .80f, .71f, .75f, .40f,  20, .8f,  10,  .85f, .90f, .79f, .50f,   6,   5 },
	{ STRAWBERRY,         300,  .90f, .79f, .85f, .45f,  30, .8f,  15,  .95f, .95f, .83f, .55f,   5,   5 },
	{ PEACH,              500,  .90f, .79f, .85f, .45f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   4,   5 },
	{ PEACH,              500,  .90f, .79f, .85f, .50f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   3,   5 },
	{ APPLE,              700,    1f, .87f, .95f, .50f,  40, .8f,  20, .105f,   1f, .87f, .60f,   2,   5 },
	{ APPLE,              700,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   5,   5 },
	{ GRAPES,            1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2,   5 },
	{ GRAPES,            1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2,   5 },
	{ GALAXIAN,          2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   1,   3 },
	{ GALAXIAN,          2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   5,   5 },
	{ BELL,              3000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   2,   5 },
	{ BELL,              3000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1,   3 },
	{ KEY,               5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1,   3 },
	{ KEY,               5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   3,   5 },
	{ KEY,               5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1,   3 },
	{ KEY,               5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0,   0 },
	{ KEY,               5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1,   3 },
	{ KEY,               5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0,   0 },
	{ KEY,               5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0,   0 },
	{ KEY,               5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0,   0 },
	{ KEY,               5000,  .90f, .79f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0,   0 },
	/*@formatter:on*/
	};

	private float fLevelData(Field field) {
		return (float) DATA[level][field.ordinal()];
	}

	private int iLevelData(Field field) {
		return (int) DATA[level][field.ordinal()];
	}

	@SuppressWarnings("unchecked")
	private <T> T oLevelData(Field field) {
		return (T) DATA[level][field.ordinal()];
	}

	public BonusSymbol getBonusSymbol() {
		return oLevelData(Field.BonusSymbol);
	}

	public int getBonusValue() {
		return iLevelData(Field.iBonusValue);
	}

	public int getBonusTime() {
		return sec(9f + rnd.nextFloat());
	}

	public float getGhostSpeed(MazeMover<Ghost.State> ghost) {
		if (maze.getContent(ghost.getTile()) == Content.TUNNEL) {
			return speed(fLevelData(Field.fGhostTunnelSpeed));
		}
		switch (ghost.getState()) {
		case AGGRO:
			return speed(fLevelData(Field.fGhostSpeed));
		case DYING:
			return 0;
		case DEAD:
			return speed(1.5f);
		case AFRAID:
			return speed(fLevelData(Field.fGhostAfraidSpeed));
		case SAFE:
			return speed(0.75f);
		case SCATTERING:
			return speed(fLevelData(Field.fGhostSpeed));
		default:
			throw new IllegalStateException();
		}
	}

	public int getGhostDyingTime() {
		return sec(0.5f);
	}

	public float getPacManSpeed(MazeMover<PacMan.State> pacMan) {
		switch (pacMan.getState()) {
		case SAFE:
			return 0;
		case VULNERABLE:
			return speed(fLevelData(Field.fPacManSpeed));
		case STEROIDS:
			return speed(fLevelData(Field.fPacManSteroidSpeed));
		case DYING:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public int getPacManSteroidTime() {
		return sec(iLevelData(Field.iPacManSteroidSeconds));
	}

	public int getPacManDyingTime() {
		return sec(2);
	}

	public int getLevelChangingTime() {
		return sec(2);
	}

	public int getReadyTime() {
		return sec(2);
	}
}