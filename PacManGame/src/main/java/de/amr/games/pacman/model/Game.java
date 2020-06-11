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
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.MovingActor;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.PacManState;

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

	/**
	 * In Shaun William's <a href="https://github.com/masonicGIT/pacman">Pac-Man remake</a> there is a
	 * speed table giving the number of steps (=pixels?) which Pac-Man is moving in 16 frames. In level
	 * 5, he uses 4 * 2 + 12 = 20 steps in 16 frames, which is 1.25 pixels/frame. The table from
	 * Gamasutra ({@link Game#LEVELS}) states that this corresponds to 100% base speed for Pac-Man at
	 * level 5. Therefore I use 1.25 pixel/frame for 100% speed.
	 */
	static final float BASE_SPEED = 1.25f;

	/**
	 * Returns the number of ticks corresponding to the given time (in seconds) for a framerate of 60
	 * ticks/sec. This is useful to be able to speed up the actors by increasing the framerate of the
	 * game but keep all timer values as they are at 60 ticks/sec.
	 * 
	 * @param seconds seconds
	 * @return ticks corresponding to given number of seconds
	 */
	public static int sec(float seconds) {
		return Math.round(60 * seconds);
	}

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	public static final int POINTS_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;
	public static final int POINTS_EXTRA_LIFE = 10_000;
	public static final int POINTS_KILLED_ALL_GHOSTS = 12_000;
	public static final int POINTS_BONUS[] = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
	public static final int POINTS_GHOST[] = { 200, 400, 800, 1600 };
	public static final int DIGEST_PELLET_TICKS = 1;
	public static final int DIGEST_ENERGIZER_TICKS = 3;

	public PacMan pacMan;
	public Ghost blinky, pinky, inky, clyde;
	public Bonus bonus;
	public Maze maze;
	public Stage stage;
	public List<Symbol> levelCounter;
	public Hiscore hiscore;
	public GameLevel level;
	public int lives;
	public int score;

	public Game(int startLevel) {
		lives = 3;
		score = 0;
		levelCounter = new ArrayList<>();
		hiscore = new Hiscore(new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml"));
		maze = new Maze();
		stage = new Stage();
		createActors();
		enterLevel(startLevel);
	}

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
		level.number = n;
		level.eatenFoodCount = 0;
		level.ghostsKilledByEnergizer = 0;
		level.ghostsKilled = 0;
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
		return new GameLevel(LEVELS[n - 1]);
	}

	private void createActors() {
		pacMan = new PacMan(this);
		blinky = new Ghost(this, "Blinky");
		inky = new Ghost(this, "Inky");
		pinky = new Ghost(this, "Pinky");
		clyde = new Ghost(this, "Clyde");
		bonus = new Bonus(this);

		pacMan.fnSpeed = this::pacManSpeed;
		ghosts().forEach(ghost -> ghost.fnSpeed = this::ghostSpeed);

		pacMan.behavior(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));

		blinky.seat = 0;
		blinky.insane = true;
		blinky.behavior(LOCKED, blinky.isHeadingFor(blinky::tile));
		blinky.behavior(ENTERING_HOUSE, blinky.isTakingSeat(maze.seatPosition(2)));
		blinky.behavior(LEAVING_HOUSE, blinky.isLeavingGhostHouse());
		blinky.behavior(FRIGHTENED, blinky.isMovingRandomlyWithoutTurningBack());
		blinky.behavior(SCATTERING, blinky.isHeadingFor(maze.horizonNE));
		blinky.behavior(CHASING, blinky.isHeadingFor(pacMan::tile));
		blinky.behavior(DEAD, blinky.isHeadingFor(() -> maze.ghostHouseEntry));

		inky.seat = 1;
		inky.behavior(LOCKED, inky.isJumpingUpAndDown(maze.seatPosition(1)));
		inky.behavior(ENTERING_HOUSE, inky.isTakingSeat(maze.seatPosition(1)));
		inky.behavior(LEAVING_HOUSE, inky.isLeavingGhostHouse());
		inky.behavior(FRIGHTENED, inky.isMovingRandomlyWithoutTurningBack());
		inky.behavior(SCATTERING, inky.isHeadingFor(maze.horizonSE));
		inky.behavior(CHASING, inky.isHeadingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return new Tile(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		inky.behavior(DEAD, inky.isHeadingFor(() -> maze.ghostHouseEntry));

		pinky.seat = 2;
		pinky.behavior(LOCKED, pinky.isJumpingUpAndDown(maze.seatPosition(2)));
		pinky.behavior(ENTERING_HOUSE, pinky.isTakingSeat(maze.seatPosition(2)));
		pinky.behavior(LEAVING_HOUSE, pinky.isLeavingGhostHouse());
		pinky.behavior(FRIGHTENED, pinky.isMovingRandomlyWithoutTurningBack());
		pinky.behavior(SCATTERING, pinky.isHeadingFor(maze.horizonNW));
		pinky.behavior(CHASING, pinky.isHeadingFor(() -> pacMan.tilesAhead(4)));
		pinky.behavior(DEAD, pinky.isHeadingFor(() -> maze.ghostHouseEntry));

		clyde.seat = 3;
		clyde.behavior(LOCKED, clyde.isJumpingUpAndDown(maze.seatPosition(3)));
		clyde.behavior(ENTERING_HOUSE, clyde.isTakingSeat(maze.seatPosition(3)));
		clyde.behavior(LEAVING_HOUSE, clyde.isLeavingGhostHouse());
		clyde.behavior(FRIGHTENED, clyde.isMovingRandomlyWithoutTurningBack());
		clyde.behavior(SCATTERING, clyde.isHeadingFor(maze.horizonSW));
		clyde.behavior(CHASING,
				clyde.isHeadingFor(() -> clyde.tile().distSq(pacMan.tile()) > 8 * 8 ? pacMan.tile() : maze.horizonSW));
		clyde.behavior(DEAD, clyde.isHeadingFor(() -> maze.ghostHouseEntry));
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> ghostsOnStage() {
		return ghosts().filter(stage::contains);
	}

	public Stream<MovingActor<?>> movingActors() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<MovingActor<?>> movingActorsOnStage() {
		return movingActors().filter(stage::contains);
	}

	public int remainingFoodCount() {
		return maze.totalFoodCount - level.eatenFoodCount;
	}

	/**
	 * @param tile      tile containing food
	 * @param energizer tells if the pellet is an energizer
	 * @return points scored
	 */
	public int eatFood(Tile tile, boolean energizer) {
		if (!maze.isSimplePellet(tile) && !maze.isEnergizer(tile)) {
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

	public boolean isBonusScoreReached() {
		return level.eatenFoodCount == 70 || level.eatenFoodCount == 170;
	}

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

	float pacManSpeed(MovingActor<PacManState> actor) {
		PacMan pacMan = (PacMan) actor;
		float fraction = 0;
		switch (pacMan.getState()) {
		case SLEEPING:
		case DEAD:
			break;
		case EATING:
			fraction = pacMan.powerTicks > 0 ? level.pacManPowerSpeed : level.pacManSpeed;
			break;
		default:
			throw new IllegalStateException("Illegal Pac-Man state: " + pacMan.getState());
		}
		return speed(fraction);
	}

	float ghostSpeed(MovingActor<GhostState> actor) {
		Ghost ghost = (Ghost) actor;
		float fraction = 0;
		switch (ghost.getState()) {
		case LOCKED:
			fraction = maze.insideGhostHouse(ghost.tile()) ? level.ghostSpeed / 2 : 0;
			break;
		case LEAVING_HOUSE:
			fraction = level.ghostSpeed / 2;
			break;
		case ENTERING_HOUSE:
			fraction = level.ghostSpeed;
			break;
		case CHASING:
		case SCATTERING:
			if (maze.isTunnel(ghost.tile())) {
				fraction = level.ghostTunnelSpeed;
			} else if (ghost.cruiseElroyState == 2) {
				fraction = level.elroy2Speed;
			} else if (ghost.cruiseElroyState == 1) {
				fraction = level.elroy1Speed;
			} else {
				fraction = level.ghostSpeed;
			}
			break;
		case FRIGHTENED:
			fraction = maze.isTunnel(ghost.tile()) ? level.ghostTunnelSpeed : level.ghostFrightenedSpeed;
			break;
		case DEAD:
			fraction = 2 * level.ghostSpeed;
			break;
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s", ghost.getState()));
		}
		return speed(fraction);
	}
}