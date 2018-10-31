package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManGameActor;
import de.amr.games.pacman.model.Level.Property;
import de.amr.games.pacman.theme.GhostColor;

/**
 * The "model" (in MVC speak) of the Pac-Man game. Contains the current game state and defines the
 * "business logic" for playing the game. Also serves as factory and container for the actors.
 * 
 * @author Armin Reichert
 */
public class PacManGame {

	private static int sec(float seconds) {
		return app().clock.sec(seconds);
	}

	/** The tile size (8px). */
	public static final int TS = 8;

	private final Maze maze;

	private final PacMan pacMan;

	private final Ghost blinky, pinky, inky, clyde;

	/** The currently active actors. Actors can be toggled during the game. */
	private final Map<PacManGameActor, Boolean> actorState = new HashMap<>();

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

	public PacManGame() {
		maze = new Maze();

		score = new Score(this);

		pacMan = new PacMan(this);

		blinky = new Ghost(this, "Blinky", GhostColor.RED, maze.getBlinkyHome(), maze.getPinkyHome(),
				maze.getBlinkyScatteringTarget(), Top4.S);

		pinky = new Ghost(this, "Pinky", GhostColor.PINK, maze.getPinkyHome(), maze.getPinkyHome(),
				maze.getPinkyScatteringTarget(), Top4.S);

		inky = new Ghost(this, "Inky", GhostColor.TURQUOISE, maze.getInkyHome(), maze.getInkyHome(),
				maze.getInkyScatteringTarget(), Top4.N);

		clyde = new Ghost(this, "Clyde", GhostColor.ORANGE, maze.getClydeHome(), maze.getClydeHome(),
				maze.getClydeScatteringTarget(), Top4.N);

		Arrays.asList(pacMan, blinky, pinky, inky, clyde).forEach(actor -> actorState.put(actor, true));

		// Define the ghost behavior ("AI")

		getGhosts().forEach(ghost -> {
			ghost.setBehavior(FRIGHTENED, ghost.flee(pacMan));
			ghost.setBehavior(SCATTERING, ghost.headFor(ghost::getScatteringTarget));
			ghost.setBehavior(DEAD, ghost.headFor(ghost::getRevivalTile));
			ghost.setBehavior(LOCKED, ghost.bounce());
		});

		// Individual ghost behavior
		blinky.setBehavior(CHASING, blinky.attackDirectly(pacMan));
		pinky.setBehavior(CHASING, pinky.ambush(pacMan, 4));
		inky.setBehavior(CHASING, inky.attackWith(blinky, pacMan));
		clyde.setBehavior(CHASING, clyde.attackOrReject(clyde, pacMan, 8 * TS));

		// Other game rules.
		// TODO: incomplete
		clyde.fnCanLeaveGhostHouse = () -> {
			if (!clyde.getStateObject().isTerminated()) {
				return false; // wait for timeout
			}
			return getLevel() > 1 || getFoodRemaining() < (66 * maze.getFoodTotal() / 100);
		};
	}

	public PacMan getPacMan() {
		return pacMan;
	}

	public Ghost getBlinky() {
		return blinky;
	}

	public Ghost getPinky() {
		return pinky;
	}

	public Ghost getInky() {
		return inky;
	}

	public Ghost getClyde() {
		return clyde;
	}

	public Stream<Ghost> getGhosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> getActiveGhosts() {
		return getGhosts().filter(this::isActorActive);
	}

	public void initActiveActors() {
		actorState.entrySet().forEach(e -> {
			if (e.getValue()) {
				e.getKey().init();
			}
		});
	}

	public boolean isActorActive(PacManGameActor actor) {
		return actorState.get(actor);
	}

	public void setActorActive(PacManGameActor actor, boolean active) {
		if (actorState.containsKey(actor) && active == actorState.get(actor)) {
			return; // no change
		}
		actorState.put(actor, active);
		actor.setVisible(active);
		if (active) {
			actor.init();
		}
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
		// TODO what is the original base speed in tiles/second at 60 Hz?
		return 9f * TS / 60 * relativeSpeed;
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

	public int getLevelChangingTime() {
		return sec(3);
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
		return sec(9 + new Random().nextFloat());
	}

	public int getGhostScatteringDuration(int round) {
		if (level <= 1) {
			return sec(round <= 1 ? 7 : 5);
		}
		// levels 2-4
		if (level <= 4) {
			return round <= 1 ? sec(7) : round == 2 ? sec(5) : 1;
		}
		// levels 5+
		return round <= 2 ? sec(5) : 1;
	}

	public int getGhostChasingDuration(int round) {
		if (level <= 1) {
			return round <= 2 ? sec(20) : Integer.MAX_VALUE;
		}
		// levels 2-4
		if (level <= 4) {
			return round <= 1 ? sec(20) : round == 2 ? sec(1033) : Integer.MAX_VALUE;
		}
		// levels 5+
		return round <= 1 ? sec(20) : round == 2 ? sec(1037) : Integer.MAX_VALUE;
	}

	public float getGhostSpeed(Ghost ghost) {
		Tile tile = ghost.getTile();
		boolean slow = maze.inTeleportSpace(tile) || maze.inTunnel(tile);
		float slowSpeed = speed(Level.floatValue(level, Property.fGhostTunnelSpeed));
		switch (ghost.getState()) {
		case CHASING:
			return slow ? slowSpeed : speed(Level.floatValue(level, Property.fGhostSpeed));
		case DYING:
			return 0;
		case DEAD:
			return speed(1.5f);
		case FRIGHTENED:
			return slow ? slowSpeed : speed(Level.floatValue(level, Property.fGhostAfraidSpeed));
		case LOCKED:
			return speed(0.75f);
		case SCATTERING:
			return slow ? slowSpeed : speed(Level.floatValue(level, Property.fGhostSpeed));
		default:
			throw new IllegalStateException();
		}
	}

	public int getGhostDyingTime() {
		return sec(0.75f);
	}

	// TODO implement this correctly
	public int getGhostLockedTime(Ghost ghost) {
		if (ghost == blinky) {
			return blinky.inGhostHouse() ? sec(0) : sec(1);
		} else if (ghost == pinky) {
			return sec(3);
		} else if (ghost == inky) {
			return sec(4);
		} else if (ghost == clyde) {
			return sec(5);
		}
		throw new IllegalArgumentException();
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

	public float getPacManSpeed(PacMan pacMan) {
		switch (pacMan.getState()) {
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
		return sec(Level.intValue(level, Property.iPacManPowerSeconds));
	}

	public int getPacManGettingWeakerTicks() {
		return sec(getGhostNumFlashes() * 400 / 1000);
	}

	public int getPacManDyingTime() {
		return sec(2);
	}

}