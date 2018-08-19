package de.amr.games.pacman.model;

import java.util.Collections;
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

	/** The maze tile size (8px). */
	public static final int TS = 8;

	/** The frequency of the game clock. Used for tracing. */
	public final IntSupplier fnTicksPerSec;

	/** The maze. */
	private final Maze maze;

	/** The game score including highscore management. */
	public final Score score;

	/** Pac-Man's remaining lives. */
	private int lives;

	/** Pellets + energizers eaten in current level. */
	private int eaten;

	/** Ghosts killed using current energizer. */
	private int ghostsKilled;

	/** Current level. */
	private int level;

	/** Level counter symbols. */
	private final List<BonusSymbol> levelCounter = new LinkedList<>();

	public Game(Maze maze, IntSupplier fnTicksPerSec) {
		this.maze = maze;
		this.fnTicksPerSec = fnTicksPerSec;
		score = new Score(this);
	}

	public void init() {
		lives = 3;
		level = 0;
		levelCounter.clear();
		score.load();
		nextLevel();
	}

	public void nextLevel() {
		maze.resetFood();
		eaten = 0;
		ghostsKilled = 0;
		level += 1;
		levelCounter.add(0, getBonusSymbol());
		if (levelCounter.size() == 8) {
			levelCounter.remove(levelCounter.size() - 1);
		}
	}

	/**
	 * @return number of ticks corresponding to given seconds
	 */
	public int sec(float seconds) {
		return Math.round(fnTicksPerSec.getAsInt() * seconds);
	}

	private float speed(float relativeSpeed) {
		// base speed = 8 tiles/second at 60 Hz
		return 8f * Game.TS / 60 * relativeSpeed;
	}

	public Maze getMaze() {
		return maze;
	}

	public int getLevel() {
		return level;
	}

	public List<BonusSymbol> getLevelCounter() {
		return Collections.unmodifiableList(levelCounter);
	}

	public void eatFoodAtTile(Tile tile) {
		if (!maze.isFood(tile)) {
			throw new IllegalArgumentException("No food at tile " + tile);
		}
		boolean energizer = maze.isEnergizer(tile);
		if (energizer) {
			ghostsKilled = 0;
		}
		eaten += 1;
		int value = energizer ? 50 : 10;
		if (score.getScore() < 10_000 && 10_000 < score.getScore() + value) {
			lives += 1;
		}
		score.add(value);
		maze.hideFood(tile);
	}

	public boolean allFoodEaten() {
		return eaten == maze.getFoodTotal();
	}

	public int getDigestionTicks(boolean energizer) {
		return energizer ? 3 : 1;
	}

	public int getLives() {
		return lives;
	}

	public void removeLife() {
		lives -= 1;
	}

	public boolean isBonusReached() {
		return eaten == 70 || eaten == 170;
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

	public float getGhostSpeed(GhostState state, Tile tile) {
		boolean tunnel = maze.inTeleportSpace(tile) || maze.isTunnel(tile);
		float tunnelSpeed = speed(LevelTable.floatValue(level, TableColumn.fGhostTunnelSpeed));
		switch (state) {
		case AGGRO:
			return tunnel ? tunnelSpeed : speed(LevelTable.floatValue(level, TableColumn.fGhostSpeed));
		case DYING:
			return 0;
		case DEAD:
			return speed(0.5f + new Random().nextFloat());
		case FRIGHTENED:
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
		int value = 200;
		for (int i = 1; i < ghostsKilled; ++i) {
			value *= 2;
		}
		return value;
	}

	public int getGhostsKilledByEnergizer() {
		return ghostsKilled;
	}

	public void addGhostKilled() {
		ghostsKilled += 1;
	}

	public float getPacManSpeed(PacManState state) {
		switch (state) {
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