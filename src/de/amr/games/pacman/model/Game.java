package de.amr.games.pacman.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.IntSupplier;

import de.amr.games.pacman.actor.game.GhostState;
import de.amr.games.pacman.actor.game.PacManState;
import de.amr.games.pacman.model.LevelTable.TableColumn;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * <p>
 * Contains the current game state and defines the "business logic" for playing the game.
 * 
 * @author Armin Reichert
 */
public class Game {

	/** Tile size. */
	public static final int TS = 8;

	public final Maze maze;
	public final IntSupplier fnTicksPerSec;
	public final ScoreCounter score = new ScoreCounter(this);
	private int lives;
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
	
	public int getLives() {
		return lives;
	}
	
	public void removeLife() {
		lives -= 1;
	}

	private boolean checkExtraLife(int oldScore, int newScore) {
		return oldScore < 10_000 && 10_000 <= newScore;
	}

	public boolean isBonusReached() {
		return foodEaten == 70 || foodEaten == 170;
	}

	public BonusSymbol getBonusSymbol() {
		return LevelTable.objValue(level, TableColumn.BonusSymbol);
	}

	public int getBonusValue() {
		return LevelTable.intValue(level, TableColumn.iBonusValue);
	}

	public int getBonusTime() {
		return sec(9f + new Random().nextFloat());
	}

	public float getGhostSpeed(GhostState ghostState, Tile tile) {
		boolean tunnel = maze.isTeleportSpace(tile) || maze.isTunnel(tile);
		float tunnelSpeed = speed(LevelTable.floatValue(level, TableColumn.fGhostTunnelSpeed));
		switch (ghostState) {
		case AGGRO:
			return tunnel ? tunnelSpeed : speed(LevelTable.floatValue(level, TableColumn.fGhostSpeed));
		case DYING:
			return 0;
		case DEAD:
			return speed(0.5f + new Random().nextFloat());
		case AFRAID:
			return tunnel ? tunnelSpeed
					: speed(LevelTable.floatValue(level, TableColumn.fGhostAfraidSpeed));
		case SAFE:
			return speed(0.75f);
		case SCATTERING:
			return tunnel ? tunnelSpeed : speed(LevelTable.floatValue(level, TableColumn.fGhostSpeed));
		default:
			throw new IllegalStateException();
		}
	}

	public int getGhostDyingTime() {
		return sec(1f);
	}

	public int getGhostNumFlashes() {
		return LevelTable.intValue(level, TableColumn.iNumFlashes);
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
			return speed(LevelTable.floatValue(level, TableColumn.fPacManSpeed));
		case GREEDY:
			return speed(LevelTable.floatValue(level, TableColumn.fPacManPowerSpeed));
		case DYING:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public int getPacManGreedyTime() {
		return sec(LevelTable.intValue(level, TableColumn.iPacManPowerSeconds));
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