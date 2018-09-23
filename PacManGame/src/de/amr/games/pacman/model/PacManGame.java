package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.app;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.model.Level.Property;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * <p>
 * Contains the current game state and defines the "business logic" for playing the game.
 * 
 * @author Armin Reichert
 */
public class PacManGame {

	/** The maze tile size (8px). */
	public static final int TS = 8;

	/** The maze. */
	private final Maze maze;

	/** The game score including highscore management. */
	private final Score score;

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

	public PacManGame(Maze maze) {
		this.maze = maze;
		score = new Score(this);
	}

	public int getPoints() {
		return score.getPoints();
	}

	public boolean addPoints(int points) {
		int oldScore = score.getPoints();
		int newScore = oldScore + points;
		score.set(newScore);
		if (oldScore < 10000 && 10000 <= newScore) {
			lives += 1;
			return true;
		}
		return false;
	}

	public void saveScore() {
		score.save();
	}

	public int getHiscorePoints() {
		return score.getHiscorePoints();
	}

	public int getHiscoreLevel() {
		return score.getHiscoreLevel();
	}

	public void init() {
		lives = 3;
		level = 0;
		levelCounter.clear();
		score.loadHiscore();
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

	private float speed(float relativeSpeed) {
		// base speed = 8 tiles/second at 60 Hz
		return 8f * TS / 60 * relativeSpeed;
	}

	public Maze getMaze() {
		return maze;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public List<BonusSymbol> getLevelCounter() {
		return Collections.unmodifiableList(levelCounter);
	}

	public int eatFoodAtTile(Tile tile) {
		if (!maze.isFood(tile)) {
			throw new IllegalArgumentException("No food at tile " + tile);
		}
		boolean energizer = maze.isEnergizer(tile);
		if (energizer) {
			ghostsKilled = 0;
		}
		eaten += 1;
		maze.hideFood(tile);
		return energizer ? 50 : 10;
	}

	public boolean allFoodEaten() {
		return eaten == maze.getFoodTotal();
	}

	public int getFoodRemaining() {
		return maze.getFoodTotal() - eaten;
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
		return Level.objValue(level, Property.BonusSymbol);
	}

	public int getBonusValue() {
		return Level.intValue(level, Property.iBonusValue);
	}

	public int getBonusTime() {
		return app().clock.sec(9f + new Random().nextFloat());
	}

	public float getGhostSpeed(GhostState state, Tile tile) {
		boolean tunnel = maze.inTeleportSpace(tile) || maze.inTunnel(tile);
		float tunnelSpeed = speed(Level.floatValue(level, Property.fGhostTunnelSpeed));
		switch (state) {
		case CHASING:
			return tunnel ? tunnelSpeed : speed(Level.floatValue(level, Property.fGhostSpeed));
		case DYING:
			return 0;
		case DEAD:
			return speed(1.5f);
		case FRIGHTENED:
			return tunnel ? tunnelSpeed : speed(Level.floatValue(level, Property.fGhostAfraidSpeed));
		case SAFE:
			return speed(0.75f);
		case SCATTERING:
			return tunnel ? tunnelSpeed : speed(Level.floatValue(level, Property.fGhostSpeed));
		default:
			throw new IllegalStateException();
		}
	}

	public int getGhostDyingTime() {
		return app().clock.sec(0.75f);
	}

	public int getGhostNumFlashes() {
		return Level.intValue(level, Property.iNumFlashes);
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
		case HUNGRY:
			return speed(Level.floatValue(level, Property.fPacManSpeed));
		case GREEDY:
			return speed(Level.floatValue(level, Property.fPacManPowerSpeed));
		case HOME:
		case DYING:
		case DEAD:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public int getPacManGreedyTime() {
		return app().clock.sec(Level.intValue(level, Property.iPacManPowerSeconds));
	}

	public int getPacManGettingWeakerRemainingTime() {
		return app().clock.sec(getGhostNumFlashes() * 400 / 1000);
	}

	public int getPacManDyingTime() {
		return app().clock.sec(2);
	}

	public int getLevelChangingTime() {
		return app().clock.sec(3);
	}
}