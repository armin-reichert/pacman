package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.BonusSymbol.APPLE;
import static de.amr.games.pacman.model.BonusSymbol.BELL;
import static de.amr.games.pacman.model.BonusSymbol.CHERRIES;
import static de.amr.games.pacman.model.BonusSymbol.GALAXIAN;
import static de.amr.games.pacman.model.BonusSymbol.GRAPES;
import static de.amr.games.pacman.model.BonusSymbol.KEY;
import static de.amr.games.pacman.model.BonusSymbol.PEACH;
import static de.amr.games.pacman.model.BonusSymbol.STRAWBERRY;

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
import de.amr.games.pacman.actor.MazeEntity;
import de.amr.games.pacman.theme.GhostColor;

/**
 * The "model" (in MVC speak) of the Pac-Man game. Contains the current game state and defines the
 * "business logic" for playing the game. Also serves as factory and container for the actors.
 * 
 * @author Armin Reichert
 */
public class PacManGame {

	/** The tile size (8px). */
	public static final int TS = 8;

	/**
	 * @see <a href= "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Gamasutra</a>
	 */
	private enum Param {
		BONUS_SYMBOL,
		BONUS_VALUE,
		PACMAN_SPEED,
		PACMAN_DOTS_SPEED,
		GHOST_SPEED,
		GHOST_TUNNEL_SPEED,
		ELROY1_DOTS_LEFT,
		ELROY1_SPEED,
		ELROY2_DOTS_LEFT,
		ELROY2_SPEED,
		PACMAN_POWER_SPEED,
		PACMAN_POWER_DOTS_SPEED,
		GHOST_FRIGHTENED_SPEED,
		PACMAN_POWER_SECONDS,
		MAZE_NUM_FLASHES;

		private static final Object[][] VALUES_BY_LEVEL = {
			/*@formatter:off*/
			{ /* this row is not used */ },
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

		public float asFloat(int level) {
			level = Math.max(VALUES_BY_LEVEL.length - 1, level);
			return (float) VALUES_BY_LEVEL[level][ordinal()];
		}

		public int asInt(int level) {
			level = Math.max(VALUES_BY_LEVEL.length - 1, level);
			return (int) VALUES_BY_LEVEL[level][ordinal()];
		}

		@SuppressWarnings("unchecked")
		public <T> T asObject(int level) {
			level = Math.max(VALUES_BY_LEVEL.length - 1, level);
			return (T) VALUES_BY_LEVEL[level][ordinal()];
		}
	};

	private final Maze maze;

	private final PacMan pacMan;

	private final Ghost blinky, pinky, inky, clyde;

	/** The currently active actors. Actors can be toggled during the game. */
	private final Map<MazeEntity, Boolean> activeActors = new HashMap<>();

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

		Arrays.asList(pacMan, blinky, pinky, inky, clyde).forEach(actor -> activeActors.put(actor, true));

		// Define the ghost behavior ("AI")

		getAllGhosts().forEach(ghost -> {
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

	public Stream<Ghost> getAllGhosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> getGhosts() {
		return getAllGhosts().filter(this::isActorActive);
	}

	public void initActiveActors() {
		activeActors.entrySet().forEach(e -> {
			if (e.getValue()) {
				e.getKey().init();
			}
		});
	}

	public boolean isActorActive(MazeEntity actor) {
		return activeActors.get(actor);
	}

	public void setActorActive(MazeEntity actor, boolean active) {
		if (activeActors.containsKey(actor) && active == activeActors.get(actor)) {
			return; // no change
		}
		activeActors.put(actor, active);
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

	private int sec(float seconds) {
		return app().clock.sec(seconds);
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
		return Param.BONUS_SYMBOL.asObject(level);
	}

	public int getBonusValue() {
		return Param.BONUS_VALUE.asInt(level);
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
		float slowSpeed = speed(Param.GHOST_TUNNEL_SPEED.asFloat(level));
		switch (ghost.getState()) {
		case CHASING:
			return slow ? slowSpeed : speed(Param.GHOST_SPEED.asFloat(level));
		case DYING:
			return 0;
		case DEAD:
			return speed(1.5f);
		case FRIGHTENED:
			return slow ? slowSpeed : speed(Param.GHOST_FRIGHTENED_SPEED.asFloat(level));
		case LOCKED:
			return speed(0.75f);
		case SCATTERING:
			return slow ? slowSpeed : speed(Param.GHOST_SPEED.asFloat(level));
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
		return Param.MAZE_NUM_FLASHES.asInt(level);
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
			return speed(Param.PACMAN_SPEED.asFloat(level));
		case POWER:
			return speed(Param.PACMAN_POWER_SPEED.asFloat(level));
		default:
			return 0;
		}
	}

	public int getPacManPowerTime() {
		return sec(Param.PACMAN_POWER_SECONDS.asInt(level));
	}

	public int getPacManLosingPowerTicks() {
		return sec(getGhostNumFlashes() * 400 / 1000);
	}

	public int getPacManDyingTime() {
		return sec(2);
	}

}