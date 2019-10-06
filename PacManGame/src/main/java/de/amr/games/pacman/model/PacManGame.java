package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.graph.grid.impl.Top4;

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
			level = Math.min(VALUES_BY_LEVEL.length - 1, level);
			return (float) VALUES_BY_LEVEL[level][ordinal()];
		}

		public int asInt(int level) {
			level = Math.min(VALUES_BY_LEVEL.length - 1, level);
			return (int) VALUES_BY_LEVEL[level][ordinal()];
		}

		@SuppressWarnings("unchecked")
		public <T> T asObject(int level) {
			level = Math.min(VALUES_BY_LEVEL.length - 1, level);
			return (T) VALUES_BY_LEVEL[level][ordinal()];
		}
	};

	public final Maze maze;

	public final PacManTheme theme;

	public final PacMan pacMan;

	public final Ghost blinky, pinky, inky, clyde;

	/** The currently active actors. Actors can be toggled during the game. */
	private final Set<MazeMover> activeActors = new HashSet<>();

	/** The game score including highscore management. */
	public final Score score;

	/** Pac-Man's remaining lives. */
	private int lives;

	/** Pellets + energizers eaten in current level. */
	private int eaten;

	/** Global food counter. */
	private int globalFoodCounter;

	/** If global food counter is enabled. */
	private boolean globalFoodCounterEnabled = false;

	/** Ghosts killed using current energizer. */
	private int ghostsKilled;

	/** Current level. */
	private int level;

	private Bonus bonus;

	/** Level counter symbols. */
	private final List<BonusSymbol> levelCounter = new LinkedList<>();

	/**
	 * Creates the game.
	 */
	public PacManGame(PacManTheme theme) {

		this.theme = theme;

		maze = new Maze();

		score = new Score(this);

		pacMan = new PacMan(this);

		blinky = new Ghost(this, "Blinky", GhostColor.RED, maze.getBlinkyHome(),
				maze.getPinkyHome(), maze.getBlinkyScatteringTarget(), Top4.S);

		pinky = new Ghost(this, "Pinky", GhostColor.PINK, maze.getPinkyHome(),
				maze.getPinkyHome(), maze.getPinkyScatteringTarget(), Top4.S);

		inky = new Ghost(this, "Inky", GhostColor.TURQUOISE, maze.getInkyHome(),
				maze.getInkyHome(), maze.getInkyScatteringTarget(), Top4.N);

		clyde = new Ghost(this, "Clyde", GhostColor.ORANGE, maze.getClydeHome(),
				maze.getClydeHome(), maze.getClydeScatteringTarget(), Top4.N);

		activeActors.addAll((Arrays.asList(pacMan, blinky, pinky, inky, clyde)));

		// Define the ghost behavior ("AI")

		ghosts().forEach(ghost -> {
			ghost.setBehavior(FRIGHTENED, ghost.flee(pacMan));
			ghost.setBehavior(SCATTERING, ghost.headFor(ghost::getScatteringTarget));
			ghost.setBehavior(DEAD, ghost.headFor(ghost::getRevivalTile));
			ghost.setBehavior(LOCKED, ghost.bounce());
		});

		// Individual ghost behavior
		blinky.setBehavior(CHASING, blinky.attackDirectly(pacMan));
		pinky.setBehavior(CHASING, pinky.ambush(pacMan, 4));
		inky.setBehavior(CHASING, inky.attackWith(blinky, pacMan));
		clyde.setBehavior(CHASING, clyde.attackOrReject(pacMan, 8 * TS));
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> activeGhosts() {
		return ghosts().filter(this::isActive);
	}

	public Stream<MazeMover> activeActors() {
		return activeActors.stream();
	}

	public boolean isActive(MazeMover actor) {
		return activeActors.contains(actor);
	}

	public void setActive(MazeMover actor, boolean active) {
		if (active) {
			if (activeActors.add(actor)) {
				actor.init(); // only when not already active
			}
		}
		else {
			activeActors.remove(actor);
		}
		actor.setVisible(active);
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

	public void init() {
		lives = 3;
		level = 0;
		bonus = null;
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
		ghosts().forEach(Ghost::resetFoodCounter);
		globalFoodCounterEnabled = false;
		globalFoodCounter = 0;
	}

	private int sec(float seconds) {
		return app().clock.sec(seconds);
	}

	private float speed(float relativeSpeed) {
		// TODO what is the original base speed in tiles/second at 60 Hz?
		return 9f * TS / 60 * relativeSpeed;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
	}

	public void removeBonus() {
		bonus = null;
	}

	public void setBonus(Bonus bonus) {
		this.bonus = Objects.requireNonNull(bonus);
	}

	public List<BonusSymbol> getLevelCounter() {
		return Collections.unmodifiableList(levelCounter);
	}

	public int eatFoodAtTile(Tile tile) {
		if (!maze.containsFood(tile)) {
			throw new IllegalArgumentException("No food at tile " + tile);
		}
		boolean energizer = maze.containsEnergizer(tile);
		if (energizer) {
			ghostsKilled = 0;
		}
		eaten += 1;
		maze.removeFood(tile);
		updateFoodCounter();
		return energizer ? 50 : 10;
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
			return speed(1.25f);
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

	// rules for leaving ghost house

	/**
	 * The first control used to evaluate when the ghosts leave home is a personal counter each ghost
	 * retains for tracking the number of dots Pac-Man eats. Each ghost's "dot counter" is reset to
	 * zero when a level begins and can only be active when inside the ghost house, but only one
	 * ghost's counter can be active at any given time regardless of how many ghosts are inside.
	 * 
	 * <p>
	 * The order of preference for choosing which ghost's counter to activate is: Pinky, then Inky,
	 * and then Clyde. For every dot Pac-Man eats, the preferred ghost in the house (if any) gets its
	 * dot counter increased by one. Each ghost also has a "dot limit" associated with his counter,
	 * per level.
	 * 
	 * <p>
	 * If the preferred ghost reaches or exceeds his dot limit, it immediately exits the house and its
	 * dot counter is deactivated (but not reset). The most-preferred ghost still waiting inside the
	 * house (if any) activates its timer at this point and begins counting dots.
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	public boolean canLeaveGhostHouse(Ghost ghost) {
		if (ghost == blinky) {
			return true;
		}
		Ghost next = Stream.of(pinky, inky, clyde).filter(g -> g.getState() == GhostState.LOCKED)
				.findFirst().orElse(null);
		if (ghost != next) {
			return false;
		}
		if (ghost.getFoodCounter() >= getFoodLimit(ghost)) {
			return true;
		}
		if (globalFoodCounterEnabled && globalFoodCounter >= getGlobalFoodCounterLimit(ghost)) {
			return true;
		}
		int timeout = level < 5 ? sec(4) : sec(3);
		if (pacMan.getEatTimer() > timeout) {
			LOGGER.info(String.format("Releasing ghost %s (Pac-Man eat timer expired)", ghost.getName()));
			return true;
		}
		return false;
	}

	private void updateFoodCounter() {
		if (globalFoodCounterEnabled) {
			globalFoodCounter++;
			LOGGER.info(String.format("Global Food Counter=%d", globalFoodCounter));
			if (globalFoodCounter == 32 && clyde.getState() == GhostState.LOCKED) {
				globalFoodCounterEnabled = false;
				globalFoodCounter = 0;
			}
			return;
		}
		/*@formatter:off*/
		Stream.of(pinky, inky, clyde)
			.filter(ghost -> ghost.getState() == GhostState.LOCKED)
			.findFirst()
			.ifPresent(preferredGhost -> {
				preferredGhost.incFoodCounter();
				LOGGER.info(String.format("Food Counter[%s]=%d", preferredGhost.getName(), preferredGhost.getFoodCounter()));
		});
		/*@formatter:on*/
	}

	/**
	 * Pinky's dot limit is always set to zero, causing him to leave home immediately when every level
	 * begins. For the first level, Inky has a limit of 30 dots, and Clyde has a limit of 60. This
	 * results in Pinky exiting immediately which, in turn, activates Inky's dot counter. His counter
	 * must then reach or exceed 30 dots before he can leave the house.
	 * 
	 * <p>
	 * Once Inky starts to leave, Clyde's counter (which is still at zero) is activated and starts
	 * counting dots. When his counter reaches or exceeds 60, he may exit. On the second level, Inky's
	 * dot limit is changed from 30 to zero, while Clyde's is changed from 60 to 50. Inky will exit
	 * the house as soon as the level begins from now on.
	 * 
	 * <p>
	 * Starting at level three, all the ghosts have a dot limit of zero for the remainder of the game
	 * and will leave the ghost house immediately at the start of every level.
	 * 
	 * @param ghost
	 *                a ghost
	 * @return the ghosts's current food limit
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private int getFoodLimit(Ghost ghost) {
		if (ghost == pinky) {
			return 0;
		}
		if (ghost == inky) {
			return level == 1 ? 30 : 0;
		}
		if (ghost == clyde) {
			return level == 1 ? 60 : level == 2 ? 50 : 0;
		}
		return 0;
	}

	private int getGlobalFoodCounterLimit(Ghost ghost) {
		return (ghost == pinky) ? 7 : (ghost == inky) ? 17 : (ghost == clyde) ? 32 : 0;
	}

	public void enableGlobalFoodCounter() {
		globalFoodCounterEnabled = true;
		globalFoodCounter = 0;
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

	public int getPacManDyingTime() {
		return sec(2);
	}

}