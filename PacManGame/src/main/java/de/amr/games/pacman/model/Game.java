package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * @author Armin Reichert
 * 
 * @see <a href= "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php">Pac-Man
 *      dossier</a>
 * @see <a href= "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Pac-Man level
 *      specifications</a>
 */
public class Game {

	/**
	 * <img src="http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">
	 */
	public static final String[] LEVELS = {
		/*@formatter:off*/
		"CHERRIES,   100,  80%,  71%,  75%,  40%,  20,  80%,  10,  85%,  90%, 79%, 50%, 6, 5",
		"STRAWBERRY, 300,  90%,  79%,  85%,  45%,  30,  90%,  15,  95%,  95%, 83%, 55%, 5, 5",
		"PEACH,      500,  90%,  79%,  85%,  45%,  40,  90%,  20,  95%,  95%, 83%, 55%, 4, 5",
		"PEACH,      500,  90%,  79%,  85%,  50%,  40, 100%,  20,  95%,  95%, 83%, 55%, 3, 5",
		"APPLE,      700, 100%,  87%,  95%,  50%,  40, 100%,  20, 105%, 100%, 87%, 60%, 2, 5",
		"APPLE,      700, 100%,  87%,  95%,  50%,  50, 100%,  25, 105%, 100%, 87%, 60%, 5, 5",
		"GRAPES,    1000, 100%,  87%,  95%,  50%,  50, 100%,  25, 105%, 100%, 87%, 60%, 2, 5",
		"GRAPES,    1000, 100%,  87%,  95%,  50%,  50, 100%,  25, 105%, 100%, 87%, 60%, 2, 5",
		"GALAXIAN,  2000, 100%,  87%,  95%,  50%,  60, 100%,  30, 105%, 100%, 87%, 60%, 1, 3",
		"GALAXIAN,  2000, 100%,  87%,  95%,  50%,  60, 100%,  30, 105%, 100%, 87%, 60%, 5, 5",
		"BELL,      3000, 100%,  87%,  95%,  50%,  60, 100%,  30, 105%, 100%, 87%, 60%, 2, 5",
		"BELL,      3000, 100%,  87%,  95%,  50%,  80, 100%,  40, 105%, 100%, 87%, 60%, 1, 3",
		"KEY,       5000, 100%,  87%,  95%,  50%,  80, 100%,  40, 105%, 100%, 87%, 60%, 1, 3",
		"KEY,       5000, 100%,  87%,  95%,  50%,  80, 100%,  40, 105%, 100%, 87%, 60%, 3, 5",
		"KEY,       5000, 100%,  87%,  95%,  50%, 100, 100%,  50, 105%, 100%, 87%, 60%, 1, 3",
		"KEY,       5000, 100%,  87%,  95%,  50%, 100, 100%,  50, 105%,   0%,  0%,  0%, 1, 3",
		"KEY,       5000, 100%,  87%,  95%,  50%, 100, 100%,  50, 105%, 100%, 87%, 60%, 0, 0",
		"KEY,       5000, 100%,  87%,  95%,  50%, 100, 100%,  50, 105%,   0%,   0%, 0%, 1, 0",
		"KEY,       5000, 100%,  87%,  95%,  50%, 120, 100%,  60, 105%,   0%,   0%, 0%, 0, 0",
		"KEY,       5000, 100%,  87%,  95%,  50%, 120, 100%,  60, 105%,   0%,   0%, 0%, 0, 0",
		"KEY,       5000,  90%,  79%,  95%,  50%, 120, 100%,  60, 105%,   0%,   0%, 0%, 0, 0",
		/*@formatter:on*/
	};

	public static final int POINTS_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;
	public static final int POINTS_EXTRA_LIFE = 10_000;
	public static final int POINTS_KILLED_ALL_GHOSTS = 12_000;
	public static final int POINTS_BONUS[] = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
	public static final int POINTS_GHOST[] = { 200, 400, 800, 1600 };
	public static final int DIGEST_PELLET_TICKS = 1;
	public static final int DIGEST_ENERGIZER_TICKS = 3;
	public static final int BONUS_ACTIVATION[] = { 70, 170 };

	/**
	 * In Shaun William's <a href="https://github.com/masonicGIT/pacman">Pac-Man remake</a> there is a
	 * speed table giving the number of steps (=pixels?) which Pac-Man is moving in 16 frames. In level
	 * 5, he uses 4 * 2 + 12 = 20 steps in 16 frames, which is 1.25 pixels/frame. The table from
	 * Gamasutra ({@link Game#LEVELS}) states that this corresponds to 100% base speed for Pac-Man at
	 * level 5. Therefore I use 1.25 pixel/frame for 100% speed.
	 */
	public static final float BASE_SPEED = 1.25f;

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	/**
	 * Returns the number of ticks corresponding to the given time (in seconds) for a framerate of 60
	 * ticks/sec.
	 * 
	 * @param seconds seconds
	 * @return ticks corresponding to given number of seconds
	 */
	public static int sec(float seconds) {
		return Math.round(60 * seconds);
	}

	public PacMan pacMan;
	public Ghost blinky, pinky, inky, clyde;
	public Bonus bonus;
	public Maze maze;
	public List<Symbol> levelCounter;
	public GameScore gameScore;
	public GameLevel level;
	public int lives;
	public int score;

	private Set<Creature<?>> stage = new HashSet<>();

	/**
	 * Creates a game starting with the given level.
	 * 
	 * @param startLevel start level number (1-...)
	 */
	public Game(int startLevel) {
		lives = 3;
		score = 0;
		levelCounter = new ArrayList<>();
		gameScore = new GameScore(new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml"));
		maze = new Maze();
		createActors();
		enterLevel(startLevel);
	}

	/**
	 * Creates a game starting with the first level.
	 */
	public Game() {
		this(1);
	}

	/**
	 * Enters level with given number (starting at 1).
	 * 
	 * @param n level number (1-...)
	 */
	public void enterLevel(int n) {
		loginfo("Enter level %d", n);
		level = level(n);
		levelCounter.add(level.bonusSymbol);
		maze.restoreAllFood();
		gameScore.load();
	}

	private GameLevel level(int n) {
		if (n < 1) {
			throw new IllegalArgumentException("Level numbering starts at 1");
		}
		if (n > LEVELS.length) {
			n = LEVELS.length;
		}
		try {
			GameLevel level = GameLevel.parse(LEVELS[n - 1]);
			level.number = n;
			return level;
		} catch (Exception x) {
			loginfo("ERROR: Data for level %d are invalid!", n);
			loginfo("%s", LEVELS[n - 1]);
			loginfo(x.getMessage());
			System.exit(0);
			return null;
		}
	}

	private void createActors() {

		// create actor instances

		pacMan = new PacMan(this);
		blinky = new Ghost(this, "Blinky");
		inky = new Ghost(this, "Inky");
		pinky = new Ghost(this, "Pinky");
		clyde = new Ghost(this, "Clyde");
		bonus = new Bonus(this);

		// assign seats

		pacMan.seat = maze.pacManSeat;
		blinky.seat = maze.ghostSeats[0];
		inky.seat = maze.ghostSeats[1];
		pinky.seat = maze.ghostSeats[2];
		clyde.seat = maze.ghostSeats[3];

		// define behavior

		pacMan.behavior(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));

		// common ghost behavior

		ghosts().forEach(ghost -> {
			ghost.behavior(LOCKED, ghost::bouncingOnSeat);
			ghost.behavior(ENTERING_HOUSE, ghost.isTakingSeat());
			ghost.behavior(LEAVING_HOUSE, ghost::leavingGhostHouse);
			ghost.behavior(FRIGHTENED, ghost.isMovingRandomlyWithoutTurningBack());
			ghost.behavior(DEAD, ghost.isReturningToHouse());
		});

		// individual ghost behavior

		blinky.behavior(ENTERING_HOUSE, blinky.isTakingSeat(maze.ghostSeats[2]));

		// scattering

		blinky.behavior(SCATTERING, blinky.isHeadingFor(maze.horizonNE));
		inky.behavior(SCATTERING, inky.isHeadingFor(maze.horizonSE));
		pinky.behavior(SCATTERING, pinky.isHeadingFor(maze.horizonNW));
		clyde.behavior(SCATTERING, clyde.isHeadingFor(maze.horizonSW));

		// chasing

		blinky.behavior(CHASING, blinky.isHeadingFor(pacMan::tile));
		inky.behavior(CHASING, inky.isHeadingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		pinky.behavior(CHASING, pinky.isHeadingFor(() -> pacMan.tilesAhead(4)));
		clyde.behavior(CHASING, clyde.isHeadingFor(() -> clyde.distance(pacMan) > 8 ? pacMan.tile() : maze.horizonSW));
	}

	/**
	 * @return stream of all ghosts
	 */
	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	/**
	 * @return stream of ghosts currently on stage
	 */
	public Stream<Ghost> ghostsOnStage() {
		return ghosts().filter(stage::contains);
	}

	/**
	 * @return stream of all creatures (ghosts and Pac-Man)
	 */
	public Stream<Creature<?>> creatures() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	/**
	 * @return stream of creatures currently on stage (ghosts and Pac-Man)
	 */
	public Stream<Creature<?>> creaturesOnStage() {
		return creatures().filter(stage::contains);
	}

	/**
	 * @return number of remaining pellets and energizers
	 */
	public int remainingFoodCount() {
		return maze.totalFoodCount - level.eatenFoodCount;
	}

	/**
	 * Eats the pellet or eenrgizer on the given tile.
	 * 
	 * @param tile      tile containing food
	 * @param energizer tells if the pellet is an energizer
	 * @return points scored
	 */
	public int eatFood(Tile tile, boolean energizer) {
		if (!maze.containsFood(tile)) {
			loginfo("Tile %s does not contain food", tile);
			return 0;
		}
		maze.eatFood(tile);
		level.eatenFoodCount += 1;
		if (energizer) {
			level.ghostsKilledByEnergizer = 0;
			return POINTS_ENERGIZER;
		} else {
			return POINTS_PELLET;
		}
	}

	/**
	 * @return {@code true} if the number of eaten pellets causes the bonus to get active
	 */
	public boolean isBonusDue() {
		return level.eatenFoodCount == BONUS_ACTIVATION[0] || level.eatenFoodCount == BONUS_ACTIVATION[1];
	}

	/**
	 * Score the given number of points and handles high score, extra life etc.
	 * 
	 * @param points scored points
	 */
	public void score(int points) {
		int oldScore = score;
		score += points;
		gameScore.update(level, score);
		if (oldScore < POINTS_EXTRA_LIFE && POINTS_EXTRA_LIFE <= score) {
			lives += 1;
		}
	}

	/**
	 * Scores for killing a ghost. Value of a killed ghost doubles if killed in series using the same
	 * energizer.
	 * 
	 * @param ghost killed ghost
	 */
	public void scoreGhostKilled(Ghost ghost) {
		level.ghostsKilledByEnergizer += 1;
		level.ghostsKilled += 1;
		if (level.ghostsKilled == 16) {
			score(POINTS_KILLED_ALL_GHOSTS);
		}
		int points = killedGhostPoints();
		score(points);
		loginfo("Scored %d points for killing %s (%s ghost in sequence)", points, ghost.name,
				new String[] { "", "first", "2nd", "3rd", "4th" }[level.ghostsKilledByEnergizer]);
	}

	/**
	 * @return current value of a killed ghost. Value doubles for each ghost killed by the same
	 *         energizer.
	 */
	public int killedGhostPoints() {
		return POINTS_GHOST[level.ghostsKilledByEnergizer - 1];
	}

	/**
	 * @param actor a ghost or Pac-Man
	 * @return {@code true} if the actor is currently on stage
	 */
	public boolean onStage(Creature<?> actor) {
		return stage.contains(actor);
	}

	/**
	 * Puts the given actor on stage.
	 * 
	 * @param actor a ghost or Pac-Man
	 */
	public void putOnStage(Creature<?> actor) {
		stage.add(actor);
		actor.init();
		actor.visible = true;
		loginfo("%s entered the stage", actor.name);
	}

	/**
	 * Pulls the given actor from stage.
	 * 
	 * @param actor a ghost or Pac-Man
	 */
	public void pullFromStage(Creature<?> actor) {
		stage.remove(actor);
		actor.visible = false;
		loginfo("%s left the stage", actor.name);
	}
}