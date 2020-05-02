package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Symbol.APPLE;
import static de.amr.games.pacman.model.Symbol.BELL;
import static de.amr.games.pacman.model.Symbol.CHERRIES;
import static de.amr.games.pacman.model.Symbol.GALAXIAN;
import static de.amr.games.pacman.model.Symbol.GRAPES;
import static de.amr.games.pacman.model.Symbol.KEY;
import static de.amr.games.pacman.model.Symbol.PEACH;
import static de.amr.games.pacman.model.Symbol.STRAWBERRY;
import static de.amr.games.pacman.model.Timing.sec;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MovingActor;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.tiles.Tile;

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

	public static final int POINTS_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;
	public static final int POINTS_BONUS[] = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
	public static final int POINTS_GHOST[] = { 200, 400, 800, 1600 };

	public static final int DIGEST_PELLET_TICKS = 1;
	public static final int DIGEST_ENERGIZER_TICKS = 3;

	public static final int SPEED_1_FPS = 60;
	public static final int SPEED_2_FPS = 70;
	public static final int SPEED_3_FPS = 80;

	static final GameLevel[] LEVELS = {
		/*@formatter:off*/
		null, // level numbering starts at 1
		new GameLevel(CHERRIES,   100,  .80f, .71f, .75f, .40f,  20, .8f,  10,  .85f, .90f, .79f, .50f,   6, 5 ),
		new GameLevel(STRAWBERRY, 300,  .90f, .79f, .85f, .45f,  30, .8f,  15,  .95f, .95f, .83f, .55f,   5, 5 ),
		new GameLevel(PEACH,      500,  .90f, .79f, .85f, .45f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   4, 5 ),
		new GameLevel(PEACH,      500,  .90f, .79f, .85f, .50f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   3, 5 ),
		new GameLevel(APPLE,      700,    1f, .87f, .95f, .50f,  40, .8f,  20, .105f,   1f, .87f, .60f,   2, 5 ),
		new GameLevel(APPLE,      700,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   5, 5 ),
		new GameLevel(GRAPES,    1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 ),
		new GameLevel(GRAPES,    1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 ),
		new GameLevel(GALAXIAN,  2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(GALAXIAN,  2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   5, 5 ),
		new GameLevel(BELL,      3000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   2, 5 ),
		new GameLevel(BELL,      3000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   3, 5 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 ),
		new GameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 ),
		new GameLevel(KEY,       5000,  .90f, .79f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 ),
		/*@formatter:on*/
	};

	public PacMan pacMan;
	public Ghost blinky, pinky, inky, clyde;
	public Bonus bonus;
	public Maze maze;
	public Stage stage;
	public Deque<Symbol> levelCounter;
	public Hiscore hiscore;
	public GameLevel level;
	public int lives;
	public int score;

	public Game() {
		lives = 3;
		score = 0;
		levelCounter = new ArrayDeque<>(7);
		hiscore = new Hiscore(new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml"));
		maze = new Maze();
		stage = new Stage();
		createActors();
		enterLevel(1);
	}

	private void createActors() {
		pacMan = new PacMan(this);
		blinky = new Ghost(this, "Blinky");
		inky = new Ghost(this, "Inky");
		pinky = new Ghost(this, "Pinky");
		clyde = new Ghost(this, "Clyde");
		bonus = new Bonus(this);

		pacMan.behavior(pacMan.isFollowingKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		pacMan.setTeleportingDuration(sec(0.5f));

		blinky.seatNumber = 0;
		blinky.seatEyesDir = LEFT;
		blinky.setTeleportingDuration(sec(0.5f));
		blinky.behavior(LOCKED, blinky.isHeadingFor(blinky::tile));
		blinky.behavior(ENTERING_HOUSE, blinky.isTakingSeat(maze.seatPosition(2)));
		blinky.behavior(LEAVING_HOUSE, blinky.isLeavingGhostHouse());
		blinky.behavior(FRIGHTENED, blinky.isMovingRandomlyWithoutTurningBack());
		blinky.behavior(SCATTERING, blinky.isHeadingFor(maze.horizonNE));
		blinky.behavior(CHASING, blinky.isHeadingFor(pacMan::tile));
		blinky.behavior(DEAD, blinky.isHeadingFor(() -> maze.ghostHouseSeats[0]));

		inky.seatNumber = 1;
		inky.seatEyesDir = UP;
		inky.setTeleportingDuration(sec(0.5f));
		inky.behavior(LOCKED, inky.isJumpingUpAndDown(maze.seatPosition(1)));
		inky.behavior(ENTERING_HOUSE, inky.isTakingSeat(maze.seatPosition(1)));
		inky.behavior(LEAVING_HOUSE, inky.isLeavingGhostHouse());
		inky.behavior(FRIGHTENED, inky.isMovingRandomlyWithoutTurningBack());
		inky.behavior(SCATTERING, inky.isHeadingFor(maze.horizonSE));
		inky.behavior(CHASING, inky.isHeadingFor(() -> {
			Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
			return maze.tileAt(2 * p.col - b.col, 2 * p.row - b.row);
		}));
		inky.behavior(DEAD, inky.isHeadingFor(() -> maze.ghostHouseSeats[0]));

		pinky.seatNumber = 2;
		pinky.seatEyesDir = DOWN;
		pinky.setTeleportingDuration(sec(0.5f));
		pinky.behavior(LOCKED, pinky.isJumpingUpAndDown(maze.seatPosition(2)));
		pinky.behavior(ENTERING_HOUSE, pinky.isTakingSeat(maze.seatPosition(2)));
		pinky.behavior(LEAVING_HOUSE, pinky.isLeavingGhostHouse());
		pinky.behavior(FRIGHTENED, pinky.isMovingRandomlyWithoutTurningBack());
		pinky.behavior(SCATTERING, pinky.isHeadingFor(maze.horizonNW));
		pinky.behavior(CHASING, pinky.isHeadingFor(() -> pacMan.tilesAhead(4)));
		pinky.behavior(DEAD, pinky.isHeadingFor(() -> maze.ghostHouseSeats[0]));

		clyde.seatNumber = 3;
		clyde.seatEyesDir = UP;
		clyde.setTeleportingDuration(sec(0.5f));
		clyde.behavior(LOCKED, clyde.isJumpingUpAndDown(maze.seatPosition(3)));
		clyde.behavior(ENTERING_HOUSE, clyde.isTakingSeat(maze.seatPosition(3)));
		clyde.behavior(LEAVING_HOUSE, clyde.isLeavingGhostHouse());
		clyde.behavior(FRIGHTENED, clyde.isMovingRandomlyWithoutTurningBack());
		clyde.behavior(SCATTERING, clyde.isHeadingFor(maze.horizonSW));
		clyde.behavior(CHASING, clyde
				.isHeadingFor(() -> clyde.tile().distSq(pacMan.tile()) > 8 * 8 ? pacMan.tile() : maze.horizonSW));
		clyde.behavior(DEAD, clyde.isHeadingFor(() -> maze.ghostHouseSeats[0]));
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

	public void enterLevel(int n) {
		loginfo("Enter level %d", n);
		level = LEVELS[Math.min(n, LEVELS.length - 1)];
		level.number = n;
		level.eatenFoodCount = 0;
		level.ghostsKilledByEnergizer = 0;
		level.ghostsKilledInLevel = 0;
		if (levelCounter.size() == 7) {
			levelCounter.removeLast();
		}
		levelCounter.addFirst(level.bonusSymbol);
		maze.restoreFood();
		hiscore.save();
	}

	public int remainingFoodCount() {
		return maze.totalFoodCount - level.eatenFoodCount;
	}

	/**
	 * @param tile
	 *               tile containing food
	 * @return points scored
	 */
	public int eatFoodAt(Tile tile) {
		level.eatenFoodCount += 1;
		if (maze.isEnergizer(tile)) {
			level.ghostsKilledByEnergizer = 0;
			maze.removeFood(tile);
			return POINTS_ENERGIZER;
		}
		else if (maze.isPellet(tile)) {
			maze.removeFood(tile);
			return POINTS_PELLET;
		}
		throw new IllegalArgumentException("No food tile");
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
		if (oldScore < 10_000 && 10_000 <= score) {
			lives += 1;
		}
	}

	public void scoreKilledGhost(String ghostName) {
		int points = POINTS_GHOST[level.ghostsKilledByEnergizer];
		level.ghostsKilledByEnergizer += 1;
		level.ghostsKilledInLevel += 1;
		score(points);
		if (level.ghostsKilledInLevel == 16) {
			score(12000);
		}
		loginfo("Scored %d points for killing %s (%s ghost in sequence)", points, ghostName,
				new String[] { "", "first", "2nd", "3rd", "4th" }[level.ghostsKilledByEnergizer]);
	}
}