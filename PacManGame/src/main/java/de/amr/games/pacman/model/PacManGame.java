package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.jumpingUpAndDown;
import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.movingRandomly;
import static de.amr.games.pacman.actor.behavior.pacman.PacManSteerings.steeredByKeys;
import static de.amr.games.pacman.model.BonusSymbol.APPLE;
import static de.amr.games.pacman.model.BonusSymbol.BELL;
import static de.amr.games.pacman.model.BonusSymbol.CHERRIES;
import static de.amr.games.pacman.model.BonusSymbol.GALAXIAN;
import static de.amr.games.pacman.model.BonusSymbol.GRAPES;
import static de.amr.games.pacman.model.BonusSymbol.KEY;
import static de.amr.games.pacman.model.BonusSymbol.PEACH;
import static de.amr.games.pacman.model.BonusSymbol.STRAWBERRY;
import static de.amr.games.pacman.model.PacManGame.Column.BONUS;
import static de.amr.games.pacman.model.PacManGame.Column.BONUS_VALUE;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.actor.Actor;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.graph.grid.impl.Top4;

/**
 * The "model" (in MVC speak) of the Pac-Man game. Contains the current game
 * data and defines the "business logic" for playing the game. Also serves as
 * factory and container for the actors.
 * 
 * @author Armin Reichert
 */
public class PacManGame {

	/** The tile size (8px). */
	public static final int TS = 8;

	/** Base speed (11 tiles/second) in pixel/tick. */
	static final float BASE_SPEED = (float) 11 * TS / 60;

	/**
	 * Level data.
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Gamasutra</a>
	 */
	static final Object[][] LEVEL_DATA = {
		/*@formatter:off*/
		{ /* this row intentionally empty */ },
		{ CHERRIES,    100,  .80f, .71f, .75f, .40f,  20, .8f,  10,  .85f, .90f, .79f, .50f,   6, 5 },
		{ STRAWBERRY,  300,  .90f, .79f, .85f, .45f,  30, .8f,  15,  .95f, .95f, .83f, .55f,   5, 5 },
		{ PEACH,       500,  .90f, .79f, .85f, .45f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   4, 5 },
		{ PEACH,       500,  .90f, .79f, .85f, .50f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   3, 5 },
		{ APPLE,       700,    1f, .87f, .95f, .50f,  40, .8f,  20, .105f,   1f, .87f, .60f,   2, 5 },
		{ APPLE,       700,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   5, 5 },
		{ GRAPES,     1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 },
		{ GRAPES,     1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 },
		{ GALAXIAN,   2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   1, 3 },
		{ GALAXIAN,   2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   5, 5 },
		{ BELL,       3000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   2, 5 },
		{ BELL,       3000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   3, 5 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 },
		{ KEY,        5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 },
		{ KEY,        5000,  .90f, .79f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 },
		/*@formatter:on*/
	};

	public enum Column {
		/*@formatter:off*/
		BONUS, BONUS_VALUE, PACMAN_SPEED, PACMAN_DOTS_SPEED, GHOST_SPEED,	GHOST_TUNNEL_SPEED,	
		ELROY1_DOTS_LEFT,	ELROY1_SPEED,	ELROY2_DOTS_LEFT,	ELROY2_SPEED,
		PACMAN_POWER_SPEED,	PACMAN_POWER_DOTS_SPEED, GHOST_FRIGHTENED_SPEED, 
		PACMAN_POWER_SECONDS, MAZE_NUM_FLASHES;
		/*@formatter:on*/

		@SuppressWarnings("unchecked")
		public <T> T value(int level) {
			if (level < 1) {
				throw new IllegalArgumentException("Level must be at least 1, is " + level);
			}
			return (T) LEVEL_DATA[min(level, LEVEL_DATA.length - 1)][ordinal()];
		}

		public float floatValue(int level) {
			return value(level);
		}

		public int intValue(int level) {
			return value(level);
		}
	}

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float relSpeed(float fraction) {
		return fraction * BASE_SPEED;
	}

	public float speed(Column column) {
		return relSpeed(column.floatValue(level));
	}

	/**
	 * @param fraction fraction of seconds
	 * @return ticks corresponding to given fraction of seconds at 60Hz
	 */
	public static int sec(float fraction) {
		return (int) (60 * fraction);
	}

	public int sec(Column column) {
		return sec(column.intValue(level));
	}

	public final Maze maze;

	public final PacMan pacMan;

	public final Ghost blinky, pinky, inky, clyde;

	/** The game score including highscore management. */
	public final Score score;

	/** Number of pellets + energizers in maze. */
	private int totalPelletsInMaze;

	/** Pellets + energizers eaten in current level. */
	private int numPelletsEaten;

	/** Global food counter. */
	private int globalFoodCount;

	/** If global food counter is enabled. */
	private boolean globalFoodCounterEnabled = false;

	/** Ghosts killed using current energizer. */
	public int numGhostsKilledByEnergizer;

	/** Current level. */
	public int level;

	/** The currently active bonus. */
	private Bonus bonus;

	/** Level counter symbols displayed at the bottom right corner. */
	private final List<BonusSymbol> levelCounter = new LinkedList<>();

	/**
	 * Creates a game instance.
	 */
	public PacManGame() {
		maze = new Maze();
		totalPelletsInMaze = (int) maze.tiles().filter(maze::containsFood).count();

		score = new Score();

		pacMan = new PacMan(this);
		pacMan.steering = steeredByKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT);

		blinky = new Ghost(this, maze, "Blinky");
		blinky.initialDir = Top4.W;
		blinky.initialTile = maze.blinkyHome;
		blinky.scatterTile = maze.blinkyScatter;
		blinky.revivalTile = maze.pinkyHome;
		blinky.fnChasingTarget = pacMan::tile;

		pinky = new Ghost(this, maze, "Pinky");
		pinky.initialDir = Top4.S;
		pinky.initialTile = maze.pinkyHome;
		pinky.scatterTile = maze.pinkyScatter;
		pinky.revivalTile = maze.pinkyHome;
		pinky.fnChasingTarget = () -> pacMan.tilesAhead(4);

		inky = new Ghost(this, maze, "Inky");
		inky.initialDir = Top4.N;
		inky.initialTile = maze.inkyHome;
		inky.scatterTile = maze.inkyScatter;
		inky.revivalTile = maze.inkyHome;
		inky.fnChasingTarget = () -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return maze.tileAt(2 * p.col - b.col, 2 * p.row - b.row);
		};

		clyde = new Ghost(this, maze, "Clyde");
		clyde.initialDir = Top4.N;
		clyde.initialTile = maze.clydeHome;
		clyde.scatterTile = maze.clydeScatter;
		clyde.revivalTile = maze.clydeHome;
		clyde.fnChasingTarget = () -> clyde.tileDistanceSq(pacMan) > 8 * 8 ? pacMan.tile() : maze.clydeScatter;

		ghosts().forEach(ghost -> {
			ghost.setSteering(FRIGHTENED, movingRandomly());
			ghost.setSteering(LOCKED, jumpingUpAndDown());
		});
	}

	public void init() {
		pacMan.lives = 3;
		level = 1;
		bonus = null;
		maze.restoreFood();
		levelCounter.clear();
		score.loadHiscore();
		actors().forEach(Actor::activate);
	}

	public void startLevel() {
		LOGGER.info("Start game level " + level);
		maze.restoreFood();
		numPelletsEaten = 0;
		numGhostsKilledByEnergizer = 0;
		levelCounter.add(0, getLevelSymbol());
		if (levelCounter.size() > 8) {
			levelCounter.remove(levelCounter.size() - 1);
		}
		ghosts().forEach(ghost -> ghost.foodCount = 0);
		globalFoodCounterEnabled = false;
		globalFoodCount = 0;
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> activeGhosts() {
		return ghosts().filter(Ghost::isActive);
	}

	public Stream<Actor<?>> actors() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<Actor<?>> activeActors() {
		return actors().filter(Actor::isActive);
	}

	public BonusSymbol getLevelSymbol() {
		return BONUS.value(level);
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
			numGhostsKilledByEnergizer = 0;
		}
		numPelletsEaten += 1;
		maze.removeFood(tile);
		updateFoodCounter();
		return energizer ? 50 : 10;
	}

	public int numPelletsRemaining() {
		return totalPelletsInMaze - numPelletsEaten;
	}

	public int getDigestionTicks(boolean energizer) {
		return energizer ? 3 : 1;
	}

	/**
	 * @param points points scored
	 * @return <code>true</code> if new life has been granted
	 */
	public boolean scorePoints(int points) {
		int oldScore = score.getPoints();
		int newScore = oldScore + points;
		score.set(level, newScore);
		if (oldScore < 10_000 && 10_000 <= newScore) {
			pacMan.lives += 1;
			return true;
		}
		return false;
	}

	// Bonus handling

	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
	}

	public void removeBonus() {
		bonus = null;
	}

	public void setBonus(Bonus bonus) {
		this.bonus = requireNonNull(bonus);
	}

	public boolean isBonusReached() {
		return numPelletsEaten == 70 || numPelletsEaten == 170;
	}

	public int getBonusValue() {
		return BONUS_VALUE.intValue(level);
	}

	// rules for leaving the ghost house

	/**
	 * The first control used to evaluate when the ghosts leave home is a personal
	 * counter each ghost retains for tracking the number of dots Pac-Man eats. Each
	 * ghost's "dot counter" is reset to zero when a level begins and can only be
	 * active when inside the ghost house, but only one ghost's counter can be
	 * active at any given time regardless of how many ghosts are inside.
	 * 
	 * <p>
	 * The order of preference for choosing which ghost's counter to activate is:
	 * Pinky, then Inky, and then Clyde. For every dot Pac-Man eats, the preferred
	 * ghost in the house (if any) gets its dot counter increased by one. Each ghost
	 * also has a "dot limit" associated with his counter, per level.
	 * 
	 * <p>
	 * If the preferred ghost reaches or exceeds his dot limit, it immediately exits
	 * the house and its dot counter is deactivated (but not reset). The
	 * most-preferred ghost still waiting inside the house (if any) activates its
	 * timer at this point and begins counting dots.
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	public boolean canLeaveHouse(Ghost ghost) {
		if (ghost == blinky) {
			return true;
		}
		Ghost next = Stream.of(pinky, inky, clyde).filter(g -> g.getState() == GhostState.LOCKED).findFirst().orElse(null);
		if (ghost != next) {
			return false;
		}
		if (ghost.foodCount >= getFoodLimit(ghost)) {
			return true;
		}
		if (globalFoodCounterEnabled && globalFoodCount >= getGlobalFoodCounterLimit(ghost)) {
			return true;
		}
		int timeout = level < 5 ? sec(4) : sec(3);
		if (pacMan.ticksSinceLastMeal > timeout) {
			LOGGER.info(() -> String.format("Releasing ghost %s (Pac-Man eat timer expired)", ghost.name));
			return true;
		}
		return false;
	}

	private void updateFoodCounter() {
		if (globalFoodCounterEnabled) {
			globalFoodCount++;
			LOGGER.fine(() -> String.format("Global Food Counter=%d", globalFoodCount));
			if (globalFoodCount == 32 && clyde.getState() == GhostState.LOCKED) {
				globalFoodCounterEnabled = false;
				globalFoodCount = 0;
			}
			return;
		}
		/*@formatter:off*/
		Stream.of(pinky, inky, clyde)
			.filter(ghost -> ghost.getState() == GhostState.LOCKED)
			.findFirst()
			.ifPresent(preferredGhost -> {
				preferredGhost.foodCount += 1;
				LOGGER.fine(() -> String.format("Food Counter[%s]=%d", preferredGhost.name, preferredGhost.foodCount));
		});
		/*@formatter:on*/
	}

	/**
	 * Pinky's dot limit is always set to zero, causing him to leave home
	 * immediately when every level begins. For the first level, Inky has a limit of
	 * 30 dots, and Clyde has a limit of 60. This results in Pinky exiting
	 * immediately which, in turn, activates Inky's dot counter. His counter must
	 * then reach or exceed 30 dots before he can leave the house.
	 * 
	 * <p>
	 * Once Inky starts to leave, Clyde's counter (which is still at zero) is
	 * activated and starts counting dots. When his counter reaches or exceeds 60,
	 * he may exit. On the second level, Inky's dot limit is changed from 30 to
	 * zero, while Clyde's is changed from 60 to 50. Inky will exit the house as
	 * soon as the level begins from now on.
	 * 
	 * <p>
	 * Starting at level three, all the ghosts have a dot limit of zero for the
	 * remainder of the game and will leave the ghost house immediately at the start
	 * of every level.
	 * 
	 * @param ghost a ghost
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
		globalFoodCount = 0;
	}
}