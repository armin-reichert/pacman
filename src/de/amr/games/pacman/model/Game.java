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
	private int level;
	private long foodTotal;
	public final List<BonusSymbol> levelCounter = new LinkedList<>();
	
	private float baseSpeed;
	private final Random rnd = new Random();
	
	public Game(Maze maze, IntSupplier fnTicksPerSecond) {
		this.maze = maze;
		this.fnTicksPerSecond = fnTicksPerSecond;
		foodTotal = maze.getFoodTotal();
		baseSpeed = tps(8f);
	}

	public void init() {
		lives.set(3);
		score.set(0);
		foodEaten.set(0);
		ghostsKilledInSeries.set(0);
		level = 1;
		levelCounter.clear();
		levelCounter.add(getBonusSymbol());
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

	// Level data

	private enum Field {
		BonusSymbol,
		BonusValue,
		PacManSpeed,
		PacManDotsSpeed,
		GhostSpeed,
		GhostTunnelSpeed,
		Elroy1DotsLeft,
		Elroy1Speed,
		Elroy2DotsLeft,
		Elroy2Speed,
		PacManSteroidSpeed,
		PacManSteroidDotsSpeed,
		GhostAfraidSpeed,
		PacManSteroidSeconds,
		NumFlashes
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

	@SuppressWarnings("unchecked")
	private <T> T levelData(Field column) {
		return (T) DATA[level][column.ordinal()];
	}

	public BonusSymbol getBonusSymbol() {
		return levelData(Field.BonusSymbol);
	}

	public int getBonusValue() {
		return levelData(Field.BonusValue);
	}

	public int getBonusTime() {
		return sec(9f + rnd.nextFloat());
	}

	public float getGhostSpeed(MazeMover<Ghost.State> ghost) {
		if (maze.getContent(ghost.getTile()) == Content.TUNNEL) {
			return baseSpeed * (float) levelData(Field.GhostTunnelSpeed);
		}
		switch (ghost.getState()) {
		case AGGRO:
			return baseSpeed * (float) levelData(Field.GhostSpeed);
		case DYING:
			return 0;
		case DEAD:
			return baseSpeed * 1.5f;
		case AFRAID:
			return baseSpeed * (float) levelData(Field.GhostAfraidSpeed);
		case SAFE:
			return baseSpeed * 0.75f;
		case SCATTERING:
			return baseSpeed * (float) levelData(Field.GhostSpeed);
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
			return baseSpeed * (float) levelData(Field.PacManSpeed);
		case STEROIDS:
			return baseSpeed * (float) levelData(Field.PacManSteroidSpeed);
		case DYING:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public int getPacManSteroidTime() {
		int value = levelData(Field.PacManSteroidSeconds);
		return sec(value);
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