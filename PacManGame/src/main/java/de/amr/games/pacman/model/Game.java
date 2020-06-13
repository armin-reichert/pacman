package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.actor.PacManState.EATING;
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
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.Creature;
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
	public Hiscore hiscore;
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
		hiscore = new Hiscore(new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml"));
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
		maze.restoreFood();
		hiscore.save();
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

		// define actor behavior

		pacMan.behavior(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));

		blinky.seat = 0;
		blinky.insane = true;
		blinky.behavior(LOCKED, blinky.isHeadingFor(blinky::tile));
		blinky.behavior(ENTERING_HOUSE, blinky.isTakingSeat(maze.ghostSeats[2].position));
		blinky.behavior(LEAVING_HOUSE, blinky.isLeavingGhostHouse());
		blinky.behavior(FRIGHTENED, blinky.isMovingRandomlyWithoutTurningBack());
		blinky.behavior(SCATTERING, blinky.isHeadingFor(maze.horizonNE));
		blinky.behavior(CHASING, blinky.isHeadingFor(pacMan::tile));
		blinky.behavior(DEAD, blinky.isHeadingFor(() -> maze.ghostHouseEntry));

		inky.seat = 1;
		inky.behavior(LOCKED, inky.isJumpingUpAndDown(maze.ghostSeats[1].position));
		inky.behavior(ENTERING_HOUSE, inky.isTakingSeat(maze.ghostSeats[1].position));
		inky.behavior(LEAVING_HOUSE, inky.isLeavingGhostHouse());
		inky.behavior(FRIGHTENED, inky.isMovingRandomlyWithoutTurningBack());
		inky.behavior(SCATTERING, inky.isHeadingFor(maze.horizonSE));
		inky.behavior(CHASING, inky.isHeadingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return new Tile(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		inky.behavior(DEAD, inky.isHeadingFor(() -> maze.ghostHouseEntry));

		pinky.seat = 2;
		pinky.behavior(LOCKED, pinky.isJumpingUpAndDown(maze.ghostSeats[2].position));
		pinky.behavior(ENTERING_HOUSE, pinky.isTakingSeat(maze.ghostSeats[2].position));
		pinky.behavior(LEAVING_HOUSE, pinky.isLeavingGhostHouse());
		pinky.behavior(FRIGHTENED, pinky.isMovingRandomlyWithoutTurningBack());
		pinky.behavior(SCATTERING, pinky.isHeadingFor(maze.horizonNW));
		pinky.behavior(CHASING, pinky.isHeadingFor(() -> pacMan.tilesAhead(4)));
		pinky.behavior(DEAD, pinky.isHeadingFor(() -> maze.ghostHouseEntry));

		clyde.seat = 3;
		clyde.behavior(LOCKED, clyde.isJumpingUpAndDown(maze.ghostSeats[3].position));
		clyde.behavior(ENTERING_HOUSE, clyde.isTakingSeat(maze.ghostSeats[3].position));
		clyde.behavior(LEAVING_HOUSE, clyde.isLeavingGhostHouse());
		clyde.behavior(FRIGHTENED, clyde.isMovingRandomlyWithoutTurningBack());
		clyde.behavior(SCATTERING, clyde.isHeadingFor(maze.horizonSW));
		clyde.behavior(CHASING,
				clyde.isHeadingFor(() -> clyde.tile().distance(pacMan.tile()) > 8 ? pacMan.tile() : maze.horizonSW));
		clyde.behavior(DEAD, clyde.isHeadingFor(() -> maze.ghostHouseEntry));

		// define actor speed

		pacMan.fnSpeed = this::pacManSpeed;
		ghosts().forEach(ghost -> ghost.fnSpeed = this::ghostSpeed);
	}

	private float pacManSpeed(PacMan pacMan, GameLevel level) {
		return pacMan.is(EATING) ? speed(pacMan.power > 0 ? level.pacManPowerSpeed : level.pacManSpeed) : 0;
	}

	private float ghostSpeed(Ghost ghost, GameLevel level) {
		switch (ghost.getState()) {
		case LOCKED:
			return speed(maze.insideGhostHouse(ghost.tile()) ? level.ghostSpeed / 2 : 0);
		case LEAVING_HOUSE:
			return speed(level.ghostSpeed / 2);
		case ENTERING_HOUSE:
			return speed(level.ghostSpeed);
		case CHASING:
		case SCATTERING:
			if (maze.isTunnel(ghost.tile())) {
				return speed(level.ghostTunnelSpeed);
			} else if (ghost.insanityLevel == 2) {
				return speed(level.elroy2Speed);
			} else if (ghost.insanityLevel == 1) {
				return speed(level.elroy1Speed);
			} else {
				return speed(level.ghostSpeed);
			}
		case FRIGHTENED:
			return speed(maze.isTunnel(ghost.tile()) ? level.ghostTunnelSpeed : level.ghostFrightenedSpeed);
		case DEAD:
			return speed(2 * level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s", ghost.getState()));
		}
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
	 * @return stream of all moving actors (ghosts and Pac-Man)
	 */
	public Stream<Creature<?>> movingActors() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	/**
	 * @return stream of moving actors currently on stage (ghosts and Pac-Man)
	 */
	public Stream<Creature<?>> movingActorsOnStage() {
		return movingActors().filter(stage::contains);
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
		if (!maze.containsSimplePellet(tile) && !maze.containsEnergizer(tile)) {
			loginfo("Tile %s does not contain food");
			return 0;
		}
		maze.removeFood(tile);
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
		if (score > hiscore.points) {
			hiscore.points = score;
			hiscore.levelNumber = level.number;
		}
		if (oldScore < POINTS_EXTRA_LIFE && POINTS_EXTRA_LIFE <= score) {
			lives += 1;
		}
	}

	/**
	 * Scores for killing a ghost. Value of a killed ghost doubles if killed in series using the same
	 * energizer.
	 * 
	 * @param ghostName name of killed ghost, just for logging
	 */
	public void scoreKilledGhost(String ghostName) {
		int points = POINTS_GHOST[level.ghostsKilledByEnergizer];
		level.ghostsKilledByEnergizer += 1;
		level.ghostsKilled += 1;
		score(points);
		if (level.ghostsKilled == 16) {
			score(POINTS_KILLED_ALL_GHOSTS);
		}
		loginfo("Scored %d points for killing %s (%s ghost in sequence)", points, ghostName,
				new String[] { "", "first", "2nd", "3rd", "4th" }[level.ghostsKilledByEnergizer]);
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