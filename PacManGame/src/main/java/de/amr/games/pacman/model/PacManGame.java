package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.jumpingUpAndDown;
import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.movingRandomly;
import static de.amr.games.pacman.model.BonusSymbol.APPLE;
import static de.amr.games.pacman.model.BonusSymbol.BELL;
import static de.amr.games.pacman.model.BonusSymbol.CHERRIES;
import static de.amr.games.pacman.model.BonusSymbol.GALAXIAN;
import static de.amr.games.pacman.model.BonusSymbol.GRAPES;
import static de.amr.games.pacman.model.BonusSymbol.KEY;
import static de.amr.games.pacman.model.BonusSymbol.PEACH;
import static de.amr.games.pacman.model.BonusSymbol.STRAWBERRY;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.LevelData.BONUS_SYMBOL;
import static de.amr.games.pacman.model.PacManGame.LevelData.BONUS_VALUE;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.Actor;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.graph.grid.impl.Top4;

/**
 * The "model" (in MVC speak) of the Pac-Man game. Contains the current game
 * state and defines the "business logic" for playing the game. Also serves as
 * factory and container for the actors.
 * 
 * @author Armin Reichert
 */
public class PacManGame {

	/** The tile size (8px). */
	public static final int TS = 8;

	/** Base speed (11 tiles/second) in pixel/tick. */
	public static final float PIXELS_PER_TICK = (float) 11 * TS / 60;

	/**
	 * @param fraction fraction of seconds
	 * @return ticks corresponding to given fraction of seconds at 60Hz
	 */
	public static int sec(float fraction) {
		return (int) (60 * fraction);
	}

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float relSpeed(float fraction) {
		return fraction * PIXELS_PER_TICK;
	}

	/**
	 * Level data.
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Gamasutra</a>
	 */
	public enum LevelData {

		BONUS_SYMBOL, BONUS_VALUE, PACMAN_SPEED, PACMAN_DOTS_SPEED, GHOST_SPEED, GHOST_TUNNEL_SPEED, ELROY1_DOTS_LEFT,
		ELROY1_SPEED, ELROY2_DOTS_LEFT, ELROY2_SPEED, PACMAN_POWER_SPEED, PACMAN_POWER_DOTS_SPEED, GHOST_FRIGHTENED_SPEED,
		PACMAN_POWER_SECONDS, MAZE_NUM_FLASHES;

		private static final Object[][] LEVEL_DATA = {
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

		@SuppressWarnings("unchecked")
		public <T> T value(int level) {
			if (level < 1) {
				throw new IllegalArgumentException("Level must be >= 1, is " + level);
			}
			level = Math.min(LEVEL_DATA.length - 1, level);
			return (T) LEVEL_DATA[level][ordinal()];
		}

		public float $float(int level) {
			return value(level);
		}

		public int $int(int level) {
			return value(level);
		}
	}

	public float speed(LevelData column) {
		return relSpeed(column.$float(level));
	}

	public final Maze maze;
	public final PacManTheme theme;
	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;

	/** The currently active actors. Actors can be toggled during the game. */
	private final Set<Actor<?>> activeActors = new HashSet<>();

	/**
	 * If ghosts use classic flight behavior (random direction at each intersection)
	 * or path based flight into a "safe" corner.
	 */
	public boolean classicFlightBehavior = true;

	/** The game score including highscore management. */
	public final Score score;

	/** Pac-Man's remaining lives. */
	private int lives;

	private int foodTotal;

	/** Pellets + energizers eaten in current level. */
	private int eaten;

	/** Global food counter. */
	private int globalFoodCounter;

	/** If global food counter is enabled. */
	private boolean globalFoodCounterEnabled = false;

	/** Ghosts killed using current energizer. */
	private int numGhostsKilled;

	/** Current level. */
	public int level;

	/** The currently active bonus. */
	private Bonus bonus;

	/** Level counter symbols displayed at the bottom right corner. */
	private final List<BonusSymbol> levelCounter = new LinkedList<>();

	/**
	 * Creates the game using the classic Pac-Man theme.
	 */
	public PacManGame() {
		this(new ClassicPacManTheme());
	}

	/**
	 * Creates the game using the specified theme.
	 */
	public PacManGame(PacManTheme theme) {
		this.theme = theme;
		maze = new Maze();
		foodTotal = (int) maze.tiles().filter(maze::containsFood).count();
		score = new Score(this);
		pacMan = new PacMan(this);

		blinky = new Ghost(this, maze, "Blinky");
		blinky.initialTile = maze.blinkyHome;
		blinky.initialDir = Top4.W;
		blinky.scatterTile = maze.blinkyScatter;
		blinky.fnChasingTarget = pacMan::currentTile;
		setSprites(blinky, GhostColor.RED);

		pinky = new Ghost(this, maze, "Pinky");
		pinky.initialTile = maze.pinkyHome;
		pinky.initialDir = Top4.S;
		pinky.scatterTile = maze.pinkyScatter;
		pinky.fnChasingTarget = () -> pacMan.tilesAhead(4);
		setSprites(pinky, GhostColor.PINK);

		inky = new Ghost(this, maze, "Inky");
		inky.initialTile = maze.inkyHome;
		inky.initialDir = Top4.N;
		inky.scatterTile = maze.inkyScatter;
		inky.fnChasingTarget = () -> {
			Tile b = blinky.currentTile(), p = pacMan.tilesAhead(2);
			return maze.tileAt(2 * p.col - b.col, 2 * p.row - b.row);
		};
		setSprites(inky, GhostColor.CYAN);

		clyde = new Ghost(this, maze, "Clyde");
		clyde.initialTile = maze.clydeHome;
		clyde.initialDir = Top4.N;
		clyde.scatterTile = maze.clydeScatter;
		clyde.fnChasingTarget = () -> Vector2f.euclideanDist(clyde.tf.getCenter(), pacMan.tf.getCenter()) > 8
				? pacMan.currentTile()
				: maze.clydeScatter;
		setSprites(clyde, GhostColor.ORANGE);

		ghosts().forEach(ghost -> {
			ghost.setSteering(FRIGHTENED, movingRandomly());
			ghost.setSteering(LOCKED, jumpingUpAndDown());
			ghost.fnIsUnlocked = () -> canLeaveHouse(ghost);
		});

		activeActors.addAll(Arrays.asList(pacMan, blinky, pinky, inky, clyde));
	}

	private void setSprites(Ghost ghost, GhostColor color) {
		NESW.dirs().forEach(dir -> {
			ghost.sprites.set("color-" + dir, theme.spr_ghostColored(color, dir));
			ghost.sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir));
		});
		for (int i = 0; i < 4; ++i) {
			ghost.sprites.set("value-" + i, theme.spr_greenNumber(i));
		}
		ghost.sprites.set("frightened", theme.spr_ghostFrightened());
		ghost.sprites.set("flashing", theme.spr_ghostFlashing());
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
		level += 1;
		maze.restoreFood();
		eaten = 0;
		numGhostsKilled = 0;
		levelCounter.add(0, getLevelSymbol());
		if (levelCounter.size() > 8) {
			levelCounter.remove(levelCounter.size() - 1);
		}
		ghosts().forEach(ghost -> ghost.foodCount = 0);
		globalFoodCounterEnabled = false;
		globalFoodCounter = 0;
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> activeGhosts() {
		return ghosts().filter(this::isActive);
	}

	public Stream<Actor<?>> activeActors() {
		return activeActors.stream();
	}

	public boolean isActive(Actor<?> actor) {
		return activeActors.contains(actor);
	}

	public void setActive(Actor<?> actor, boolean active) {
		if (active) {
			boolean added = activeActors.add(actor);
			if (added) {
				actor.init(); // only when not already active
				actor.show();
			}
		} else {
			activeActors.remove(actor);
			actor.hide();
		}
	}

	public BonusSymbol getLevelSymbol() {
		return BONUS_SYMBOL.value(level);
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
			numGhostsKilled = 0;
		}
		eaten += 1;
		maze.removeFood(tile);
		updateFoodCounter();
		return energizer ? 50 : 10;
	}

	public int getFoodRemaining() {
		return foodTotal - eaten;
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
		score.set(newScore);
		if (oldScore < 10_000 && 10_000 <= newScore) {
			lives += 1;
			return true;
		}
		return false;
	}

	public int getKilledGhostValue() {
		int value = 200;
		for (int i = 1; i < numGhostsKilled; ++i) {
			value *= 2;
		}
		return value;
	}

	public int numGhostsKilledByCurrentEnergizer() {
		return numGhostsKilled;
	}

	public void addGhostKilled() {
		numGhostsKilled += 1;
	}

	public int getLives() {
		return lives;
	}

	public void removeLife() {
		if (app().settings.getAsBoolean("pacMan.immortable")) {
			return;
		}
		lives -= 1;
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
		return eaten == 70 || eaten == 170;
	}

	public int getBonusValue() {
		return BONUS_VALUE.$int(level);
	}

	public int getBonusDuration() {
		return sec(9 + new Random().nextFloat());
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
		if (globalFoodCounterEnabled && globalFoodCounter >= getGlobalFoodCounterLimit(ghost)) {
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
			globalFoodCounter++;
			LOGGER.fine(() -> String.format("Global Food Counter=%d", globalFoodCounter));
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
		globalFoodCounter = 0;
	}
}