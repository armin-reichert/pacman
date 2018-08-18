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

import de.amr.games.pacman.actor.game.GhostState;
import de.amr.games.pacman.actor.game.PacManState;

/**
 * The model of the Pac-Man game. Contains the current game state, the level data and implements the
 * "business logic" for playing the game.
 * 
 * @author Armin Reichert
 */
public class Game {

	/** Tile size. */
	public static final int TS = 8;

	public final Maze maze;
	public final IntSupplier fnTicksPerSec;
	public final ScoreCounter score = new ScoreCounter(this);
	public int lives;
	public int foodEaten;
	public int ghostsKilledByEnergizer;
	private int level;
	public final List<BonusSymbol> levelCounter = new LinkedList<>();

	public Game(Maze maze, IntSupplier fnTicksPerSec) {
		this.maze = maze;
		this.fnTicksPerSec = fnTicksPerSec;
	}

	public void init() {
		score.load();
		lives = 3;
		levelCounter.clear();
		level = 0;
		nextLevel();
	}

	public void nextLevel() {
		maze.resetFood();
		foodEaten = 0;
		ghostsKilledByEnergizer = 0;
		level += 1;
		levelCounter.add(0, getBonusSymbol());
		if (levelCounter.size() == 8) {
			levelCounter.remove(levelCounter.size() - 1);
		}
	}

	/**
	 * Return the number of ticks representing the given seconds at the current pulse frequency.
	 * 
	 * @return number of ticks corresponding to given seconds
	 */
	public int sec(float seconds) {
		return Math.round(fnTicksPerSec.getAsInt() * seconds);
	}

	private float speed(float relativeSpeed) {
		// base speed = 8 tiles/second at 60 Hz
		return 8f * Game.TS / 60 * relativeSpeed;
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
		fPacManPowerSpeed,
		fPacManPowerDotsSpeed,
		fGhostAfraidSpeed,
		iPacManPowerSeconds,
		iNumFlashes
	};

	/**
	 * @see <a href= "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Gamasutra</a>
	 */
	private static final Object[][] LEVELS = {
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

	private float fValue(Field field) {
		return (float) LEVELS[level][field.ordinal()];
	}

	private int iValue(Field field) {
		return (int) LEVELS[level][field.ordinal()];
	}

	@SuppressWarnings("unchecked")
	private <T> T oValue(Field field) {
		return (T) LEVELS[level][field.ordinal()];
	}

	public int getLevel() {
		return level;
	}

	public void eatFoodAt(Tile tile) {
		if (!maze.isFood(tile)) {
			throw new IllegalArgumentException("No food at tile " + tile);
		}
		boolean energizer = maze.isEnergizer(tile);
		if (energizer) {
			ghostsKilledByEnergizer = 0;
		}
		maze.hideFood(tile);
		foodEaten += 1;
		int value = getFoodValue(energizer);
		if (checkExtraLife(score.getScore(), score.getScore() + value)) {
			lives += 1;
		}
		score.add(value);
	}

	public int getFoodValue(boolean energizer) {
		return energizer ? 50 : 10;
	}

	public boolean allFoodEaten() {
		return foodEaten == maze.getFoodTotal();
	}

	public int getDigestionTicks(Tile tile) {
		return maze.isEnergizer(tile) ? 3 : 1;
	}

	private boolean checkExtraLife(int oldScore, int newScore) {
		return oldScore < 10_000 && 10_000 <= newScore;
	}

	public boolean isBonusReached() {
		return foodEaten == 70 || foodEaten == 170;
	}

	public BonusSymbol getBonusSymbol() {
		return oValue(Field.BonusSymbol);
	}

	public int getBonusValue() {
		return iValue(Field.iBonusValue);
	}

	public int getBonusTime() {
		return sec(9f + new Random().nextFloat());
	}

	public float getGhostSpeed(GhostState ghostState, Tile tile) {
		boolean tunnel = maze.isTeleportSpace(tile) || maze.isTunnel(tile);
		float tunnelSpeed = speed(fValue(Field.fGhostTunnelSpeed));
		switch (ghostState) {
		case AGGRO:
			return tunnel ? tunnelSpeed : speed(fValue(Field.fGhostSpeed));
		case DYING:
			return 0;
		case DEAD:
			return speed(0.5f + new Random().nextFloat());
		case AFRAID:
			return tunnel ? tunnelSpeed : speed(fValue(Field.fGhostAfraidSpeed));
		case SAFE:
			return speed(0.75f);
		case SCATTERING:
			return tunnel ? tunnelSpeed : speed(fValue(Field.fGhostSpeed));
		default:
			throw new IllegalStateException();
		}
	}

	public int getGhostDyingTime() {
		return sec(1f);
	}

	public int getGhostNumFlashes() {
		return iValue(Field.iNumFlashes);
	}

	public int getKilledGhostValue() {
		int n = ghostsKilledByEnergizer, value = 200;
		while (n > 1) {
			value *= 2;
			n -= 1;
		}
		return value;
	}

	public float getPacManSpeed(PacManState pacManState) {
		switch (pacManState) {
		case HOME:
			return 0;
		case HUNGRY:
			return speed(fValue(Field.fPacManSpeed));
		case GREEDY:
			return speed(fValue(Field.fPacManPowerSpeed));
		case DYING:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public int getPacManGreedyTime() {
		return sec(iValue(Field.iPacManPowerSeconds));
	}

	public int getPacManGettingWeakerRemainingTime() {
		return sec(getGhostNumFlashes());
	}

	public int getPacManDyingTime() {
		return sec(2);
	}

	public int getLevelChangingTime() {
		return sec(3);
	}

	public int getReadyTime() {
		return sec(3);
	}
}