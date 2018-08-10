package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.BonusSymbol.APPLE;
import static de.amr.games.pacman.model.BonusSymbol.BELL;
import static de.amr.games.pacman.model.BonusSymbol.CHERRIES;
import static de.amr.games.pacman.model.BonusSymbol.GALAXIAN;
import static de.amr.games.pacman.model.BonusSymbol.GRAPES;
import static de.amr.games.pacman.model.BonusSymbol.KEY;
import static de.amr.games.pacman.model.BonusSymbol.PEACH;
import static de.amr.games.pacman.model.BonusSymbol.STRAWBERRY;

import java.util.function.IntSupplier;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.view.PacManGameUI;

public class Game {

	public static final int FOOD_EATEN_BONUS_1 = 70;
	public static final int FOOD_EATEN_BONUS_2 = 170;
	public static final int EXTRALIFE_SCORE = 10_000;
	public static final int[] GHOST_POINTS = new int[] { 200, 400, 800, 1600 };

	enum DataColumn {
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
	 * @see <a href="http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Gamasutra</a>
	 */
	private static final Object[][] DATA = {
	/*@formatter:off*/
		{}, 
		{ CHERRIES,    100,  .80f, .71f, .75f, .40f,  20, .8f, 10,  .85f, .90f, .79f, .50f, 6, 5 },
		{ STRAWBERRY,  300,  .90f, .79f, .85f, .45f,  30, .8f, 15,  .95f, .95f, .83f, .55f, 5, 5 },
		{ PEACH,       500,  .90f, .79f, .85f, .45f,  40, .8f, 20,  .95f, .95f, .83f, .55f, 4, 5 },
		{ PEACH,       500,  .90f, .79f, .85f, .50f,  40, .8f, 20,  .95f, .95f, .83f, .55f, 3, 5 },
		{ APPLE,       700,    1f, .87f, .95f, .50f,  40, .8f, 20, .105f,   1f, .87f, .60f, 2, 5 },
		{ APPLE,       700,    1f, .87f, .95f, .50f,  50, .8f, 25, .105f,   1f, .87f, .60f, 5, 5 },
		{ GRAPES,     1000,    1f, .87f, .95f, .50f,  50, .8f, 25, .105f,   1f, .87f, .60f, 2, 5 },
		{ GRAPES,     1000,    1f, .87f, .95f, .50f,  50, .8f, 25, .105f,   1f, .87f, .60f, 2, 5 },
		{ GALAXIAN,   2000,    1f, .87f, .95f, .50f,  60, .8f, 30, .105f,   1f, .87f, .60f, 1, 3 },
		{ GALAXIAN,   2000,    1f, .87f, .95f, .50f,  60, .8f, 30, .105f,   1f, .87f, .60f, 5, 5 },
		{ BELL,       3000,    1f, .87f, .95f, .50f,  60, .8f, 30, .105f,   1f, .87f, .60f, 2, 5 },
		{ BELL,       3000,    1f, .87f, .95f, .50f,  80, .8f, 40, .105f,   1f, .87f, .60f, 1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f,  80, .8f, 40, .105f,   1f, .87f, .60f, 1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f,  80, .8f, 40, .105f,   1f, .87f, .60f, 3, 5 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f, 50, .105f,   1f, .87f, .60f, 1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f, 50, .105f,   0f,   0f,   0f, 0, 0 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f, 50, .105f,   1f, .87f, .60f, 1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f, 50, .105f,   0f,   0f,   0f, 0, 0 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 120, .8f, 60, .105f,   0f,   0f,   0f, 0, 0 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 120, .8f, 60, .105f,   0f,   0f,   0f, 0, 0 },
		{ KEY,        5000,  .90f, .79f, .95f, .50f, 120, .8f, 60, .105f,   0f,   0f,   0f, 0, 0 },
		/*@formatter:on*/
	};

	@SuppressWarnings("unchecked")
	private <T> T levelData(DataColumn column) {
		return (T) DATA[level][column.ordinal()];
	}

	/** Tiles per second. */
	private float tps(float value) {
		return (value * PacManGameUI.TS) / fnTicksPerSecond.getAsInt();
	}

	/** Ticks representing the given seconds. */
	public int sec(float seconds) {
		return Math.round(fnTicksPerSecond.getAsInt() * seconds);
	}

	public BonusSymbol getBonusSymbol() {
		return levelData(DataColumn.BonusSymbol);
	}

	public int getBonusValue() {
		return levelData(DataColumn.BonusValue);
	}

	public int getBonusTime() {
		return sec(9);
	}

	public int getFoodValue(char food) {
		if (food == Content.PELLET) {
			return 10;
		}
		if (food == Content.ENERGIZER) {
			return 50;
		}
		return 0;
	}

	public int getGhostValue() {
		return GHOST_POINTS[ghostsKilledInSeries];
	}

	public float getGhostSpeed(MazeMover<Ghost.State> ghost) {
		if (maze.getContent(ghost.getTile()) == Content.TUNNEL) {
			return baseSpeed * (float) levelData(DataColumn.GhostTunnelSpeed);
		}
		switch (ghost.getState()) {
		case AGGRO:
			return baseSpeed * (float) levelData(DataColumn.GhostSpeed);
		case DYING:
			return 0;
		case DEAD:
			return baseSpeed * 1.5f;
		case AFRAID:
			return baseSpeed * (float) levelData(DataColumn.GhostAfraidSpeed);
		case SAFE:
			return baseSpeed * 0.75f;
		case SCATTERING:
			return baseSpeed * (float) levelData(DataColumn.GhostSpeed);
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
			return baseSpeed * (float) levelData(DataColumn.PacManSpeed);
		case STEROIDS:
			return baseSpeed * (float) levelData(DataColumn.PacManSteroidSpeed);
		case DYING:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public int getPacManSteroidTime() {
		int seconds = levelData(DataColumn.PacManSteroidSeconds); 
		return sec(seconds);
	}

	public int getPacManDyingTime() {
		return sec(2);
	}

	public int getLevelChangingTime() {
		int numFlashes = levelData(DataColumn.NumFlashes);
		return sec(numFlashes);
	}

	public int getReadyTime() {
		return sec(2);
	}

	public final IntSupplier fnTicksPerSecond;
	public final Maze maze;
	public int level;
	public int livesRemaining;
	public int score;
	public long foodTotal;
	public int foodEaten;
	public int ghostsKilledInSeries;

	private float baseSpeed;

	public Game(Maze maze, IntSupplier fnTicksPerSecond) {
		this.fnTicksPerSecond = fnTicksPerSecond;
		this.maze = maze;
		baseSpeed = tps(8f);
	}

	public void init() {
		maze.resetFood();
		foodTotal = maze.getFoodCount();
		level = 1;
		livesRemaining = 3;
		score = 0;
		foodEaten = 0;
		ghostsKilledInSeries = 0;
	}
}