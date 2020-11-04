package de.amr.games.pacman;

import static de.amr.games.pacman.Direction.DOWN;
import static de.amr.games.pacman.Direction.LEFT;
import static de.amr.games.pacman.Direction.RIGHT;
import static de.amr.games.pacman.Direction.UP;
import static de.amr.games.pacman.GhostCharacter.KIMAGURE;
import static de.amr.games.pacman.GhostCharacter.MACHIBUSE;
import static de.amr.games.pacman.GhostCharacter.OIKAKE;
import static de.amr.games.pacman.GhostCharacter.OTOBOKE;
import static de.amr.games.pacman.World.BLINKY_CORNER;
import static de.amr.games.pacman.World.CLYDE_CORNER;
import static de.amr.games.pacman.World.HOUSE_CENTER;
import static de.amr.games.pacman.World.HOUSE_ENTRY;
import static de.amr.games.pacman.World.HOUSE_LEFT;
import static de.amr.games.pacman.World.HOUSE_RIGHT;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.INKY_CORNER;
import static de.amr.games.pacman.World.PACMAN_HOME;
import static de.amr.games.pacman.World.PINKY_CORNER;
import static de.amr.games.pacman.World.PORTAL_LEFT_ENTRY;
import static de.amr.games.pacman.World.PORTAL_RIGHT_ENTRY;
import static de.amr.games.pacman.World.TOTAL_FOOD_COUNT;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * My attempt at writing a minimal Pac-Man game with faithful behavior.
 * 
 * @author Armin Reichert
 */
public class PacManGame implements Runnable {

	public static void main(String[] args) {
		PacManGame game = new PacManGame();
		EventQueue.invokeLater(() -> {
			game.ui = new PacManGameUI(game, 2);
			game.initGame();
			new Thread(game, "GameLoop").start();
		});
	}

	private static final int BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

	public static final int FPS = 60;

	public static int sec(float seconds) {
		return (int) (seconds * FPS);
	}

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	public static void log(String msg, Object... args) {
		String timestamp = TIME_FORMAT.format(LocalTime.now());
		System.err.println(String.format("[%s] %s", timestamp, String.format(msg, args)));
	}

	private static final List<LevelData> LEVEL_DATA = List.of(
	/*@formatter:off*/
	LevelData.of("Cherries",   100,  80,  71,  75, 40,  20,  80, 10,  85,  90, 79, 50, 6, 5),
	LevelData.of("Strawberry", 300,  90,  79,  85, 45,  30,  90, 15,  95,  95, 83, 55, 5, 5),
	LevelData.of("Peach",      500,  90,  79,  85, 45,  40,  90, 20,  95,  95, 83, 55, 4, 5),
	LevelData.of("Peach",      500,  90,  79,  85, 50,  40, 100, 20,  95,  95, 83, 55, 3, 5),
	LevelData.of("Apple",      700, 100,  87,  95, 50,  40, 100, 20, 105, 100, 87, 60, 2, 5),
	LevelData.of("Apple",      700, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 5, 5),
	LevelData.of("Grapes",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5),
	LevelData.of("Grapes",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5),
	LevelData.of("Galaxian",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 1, 3),
	LevelData.of("Galaxian",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 5, 5),
	LevelData.of("Bell",      3000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 2, 5),
	LevelData.of("Bell",      3000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3),
	LevelData.of("Key",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3),
	LevelData.of("Key",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 3, 5),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 1, 3),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,  0,  0, 1, 3),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 0, 0),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,   0, 0, 1, 0),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0),
	LevelData.of("Key",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0),
	LevelData.of("Key",       5000,  90,  79,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0)
	//@formatter:on
	);

	public static LevelData levelData(int level) {
		return level <= 21 ? LEVEL_DATA.get(level - 1) : LEVEL_DATA.get(20);
	}

	public LevelData levelData() {
		return levelData(level);
	}

	private static final long[][] SCATTERING_TIMES = {
		//@formatter:off
		{ sec(7), sec(7), sec(5), sec(5) },
		{ sec(7), sec(7), sec(5), 1      },
		{ sec(5), sec(5), sec(5), 1      },
		//@formatter:on
	};

	private static final long[][] CHASING_TIMES = {
		//@formatter:off
		{ sec(20), sec(20), sec(20),   Long.MAX_VALUE },
		{ sec(20), sec(20), sec(1033), Long.MAX_VALUE },
		{ sec(5),  sec(5),  sec(1037), Long.MAX_VALUE },
		//@formatter:on
	};

	private static int waveTimes(int level) {
		return level == 1 ? 0 : level <= 4 ? 1 : 2;
	}

	public final World world = new World();
	public final Creature pacMan;
	public final Ghost[] ghosts = new Ghost[4];

	public GameState state;
	public PacManGameUI ui;
	public long fps;
	public long framesTotal;
	public int level;
	public int attackWave;
	public int foodRemaining;
	public int lives;
	public int points;
	public int ghostsKilledUsingEnergizer;
	public int mazeFlashes;
	public long pacManPowerTimer;
	public long readyStateTimer;
	public long scatteringStateTimer;
	public long chasingStateTimer;
	public long changingLevelStateTimer;
	public long pacManDyingStateTimer;
	public long bonusAvailableTimer;
	public long bonusConsumedTimer;

	public PacManGame() {
		pacMan = new Creature("Pac-Man", PACMAN_HOME);
		ghosts[BLINKY] = new Ghost("Blinky", OIKAKE, HOUSE_ENTRY, BLINKY_CORNER);
		ghosts[PINKY] = new Ghost("Pinky", MACHIBUSE, HOUSE_CENTER, PINKY_CORNER);
		ghosts[INKY] = new Ghost("Inky", KIMAGURE, HOUSE_LEFT, INKY_CORNER);
		ghosts[CLYDE] = new Ghost("Clyde", OTOBOKE, HOUSE_RIGHT, CLYDE_CORNER);
	}

	private void initGame() {
		points = 0;
		lives = 3;
		initLevel(1);
		enterReadyState();
	}

	private void initLevel(int n) {
		level = n;
		world.restoreFood();
		foodRemaining = TOTAL_FOOD_COUNT;
		attackWave = 0;
		mazeFlashes = 0;
		ghostsKilledUsingEnergizer = 0;
		pacManPowerTimer = 0;
		readyStateTimer = 0;
		scatteringStateTimer = 0;
		chasingStateTimer = 0;
		changingLevelStateTimer = 0;
		pacManDyingStateTimer = 0;
		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
	}

	private void initEntities() {
		pacMan.speed = 0;
		pacMan.dir = pacMan.wishDir = RIGHT;
		pacMan.tile = pacMan.homeTile;
		pacMan.offset = new V2f(HTS, 0);
		pacMan.stuck = false;
		pacMan.dead = false;
		pacMan.visible = true;
		pacMan.forcedOnTrack = true;

		for (Ghost ghost : ghosts) {
			ghost.speed = 0;
			ghost.tile = ghost.homeTile;
			ghost.offset = new V2f(HTS, 0);
			ghost.targetTile = null;
			ghost.changedTile = true;
			ghost.stuck = false;
			ghost.forcedTurningBack = false;
			ghost.forcedOnTrack = false;
			ghost.dead = false;
			ghost.frightened = false;
			ghost.visible = true;
			ghost.enteringHouse = false;
			ghost.leavingHouse = false;
			ghost.bounty = 0;
			ghost.bountyTimer = 0;
		}
		ghosts[BLINKY].dir = ghosts[BLINKY].wishDir = LEFT;
		ghosts[PINKY].dir = ghosts[PINKY].wishDir = DOWN;
		ghosts[INKY].dir = ghosts[INKY].wishDir = UP;
		ghosts[CLYDE].dir = ghosts[CLYDE].wishDir = UP;

		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
	}

	@Override
	public void run() {
		final long intendedFrameDuration = 1_000_000_000 / FPS;
		long fpsCountStart = 0;
		long frames = 0;
		while (true) {
			long frameStartTime = System.nanoTime();
			update();
			ui.render();
			long frameEndTime = System.nanoTime();
			long frameDuration = frameEndTime - frameStartTime;
			long sleep = Math.max(intendedFrameDuration - frameDuration, 0);
			if (sleep > 0) {
				try {
					Thread.sleep(sleep / 1_000_000); // milliseconds
				} catch (InterruptedException x) {
					x.printStackTrace();
				}
			}

			++frames;
			++framesTotal;
			if (frameEndTime - fpsCountStart >= 1_000_000_000) {
				fps = frames;
				frames = 0;
				fpsCountStart = System.nanoTime();
			}
		}
	}

	private void readInput() {
		if (ui.keyPressed(KeyEvent.VK_LEFT)) {
			pacMan.wishDir = LEFT;
		} else if (ui.keyPressed(KeyEvent.VK_RIGHT)) {
			pacMan.wishDir = RIGHT;
		} else if (ui.keyPressed(KeyEvent.VK_UP)) {
			pacMan.wishDir = UP;
		} else if (ui.keyPressed(KeyEvent.VK_DOWN)) {
			pacMan.wishDir = DOWN;
		} else if (ui.keyPressed(KeyEvent.VK_D)) {
			ui.debugDraw = !ui.debugDraw;
		} else if (ui.keyPressed(KeyEvent.VK_E)) {
			eatAllFood();
		} else if (ui.keyPressed(KeyEvent.VK_X)) {
			ghostsKilledUsingEnergizer = 0;
			for (Ghost ghost : ghosts) {
				killGhost(ghost);
			}
		}
	}

	private void update() {
		readInput();
		if (state == GameState.READY) {
			runReadyState();
		} else if (state == GameState.CHASING) {
			runChasingState();
		} else if (state == GameState.SCATTERING) {
			runScatteringState();
		} else if (state == GameState.CHANGING_LEVEL) {
			runChangingLevelState();
		} else if (state == GameState.PACMAN_DYING) {
			runPacManDyingState();
		} else if (state == GameState.GAME_OVER) {
			runGameOverState();
		}
	}

	private void runReadyState() {
		if (readyStateTimer == 0) {
			exitReadyState();
			enterScatteringState();
			return;
		}
		for (int i = 1; i <= 3; ++i) {
			letGhostBounce(ghosts[i]);
		}
		--readyStateTimer;
	}

	private void enterReadyState() {
		state = GameState.READY;
		readyStateTimer = sec(3);
		ui.setMessage("Ready!", false);
		initEntities();
	}

	private void exitReadyState() {
		ui.setMessage(null, false);
		for (Ghost ghost : ghosts) {
			ghost.leavingHouse = true;
		}
		ghosts[BLINKY].leavingHouse = false;
		ghosts[BLINKY].forcedOnTrack = true;
	}

	private void runScatteringState() {
		if (pacMan.dead) {
			enterPacManDyingState();
			return;
		}
		if (foodRemaining == 0) {
			enterChangingLevelState();
			return;
		}
		if (scatteringStateTimer == 0) {
			enterChasingState();
			return;
		}
		if (pacManPowerTimer == 0) {
			--scatteringStateTimer;
		}
		updatePacMan();
		updateGhosts();
		updateBonus();
	}

	private void enterScatteringState() {
		state = GameState.SCATTERING;
		scatteringStateTimer = SCATTERING_TIMES[waveTimes(level)][attackWave];
		forceLivingGhostsTurningBack();
	}

	private void runChasingState() {
		if (pacMan.dead) {
			enterPacManDyingState();
			return;
		}
		if (foodRemaining == 0) {
			enterChangingLevelState();
			return;
		}
		if (chasingStateTimer == 0) {
			++attackWave;
			enterScatteringState();
			return;
		}
		if (pacManPowerTimer == 0) {
			--chasingStateTimer;
		}
		updatePacMan();
		updateGhosts();
		updateBonus();
	}

	private void enterChasingState() {
		state = GameState.CHASING;
		chasingStateTimer = CHASING_TIMES[waveTimes(level)][attackWave];
		forceLivingGhostsTurningBack();
	}

	private void runPacManDyingState() {
		if (pacManDyingStateTimer == 0) {
			exitPacManDyingState();
			if (lives > 0) {
				enterReadyState();
				return;
			} else {
				enterGameOverState();
				return;
			}
		}
		if (pacManDyingStateTimer == sec(2.5f) + 88) {
			for (Creature ghost : ghosts) {
				ghost.visible = false;
			}
		}
		pacManDyingStateTimer--;
	}

	private void enterPacManDyingState() {
		state = GameState.PACMAN_DYING;
		// 11 animation frames, 8 ticks each, 2 seconds before animation, 2 seconds after
		pacManDyingStateTimer = sec(2) + 88 + sec(2);
	}

	private void exitPacManDyingState() {
		for (Creature ghost : ghosts) {
			ghost.visible = true;
		}
	}

	private void runChangingLevelState() {
		if (changingLevelStateTimer == 0) {
			log("Level %d complete, entering level %d", level, level + 1);
			initLevel(++level);
			enterReadyState();
			return;
		}
		--changingLevelStateTimer;
	}

	private void enterChangingLevelState() {
		state = GameState.CHANGING_LEVEL;
		changingLevelStateTimer = sec(7);
		mazeFlashes = levelData().numFlashes();
		log("Maze flashes: %d", mazeFlashes);
		for (Creature ghost : ghosts) {
			ghost.visible = false;
		}
	}

	private void runGameOverState() {
		if (ui.keyPressed(KeyEvent.VK_SPACE)) {
			exitGameOverState();
			initGame();
		}
	}

	private void enterGameOverState() {
		state = GameState.GAME_OVER;
		ui.setMessage("Game Over!", true);
		log("Entered game over state");
	}

	private void exitGameOverState() {
		ui.setMessage(null, false);
		log("Left game over state");
	}

	private void updatePacMan() {
		pacMan.speed = levelData().pacManSpeed();
		move(pacMan);

		// Pac-man power expiring?
		if (pacManPowerTimer > 0) {
			pacManPowerTimer--;
			if (pacManPowerTimer == 0) {
				for (Ghost ghost : ghosts) {
					ghost.frightened = false;
				}
			}
		}

		// food found?
		int x = pacMan.tile.x, y = pacMan.tile.y;
		if (world.isFoodTile(x, y) && !world.hasEatenFood(x, y)) {
			world.eatFood(x, y);
			foodRemaining--;
			points += 10;
			// energizer found?
			if (world.isEnergizerTile(x, y)) {
				points += 40;
				pacManPowerTimer = sec(levelData().ghostFrightenedSeconds());
				log("Pac-Man got power for %d seconds", levelData().ghostFrightenedSeconds());
				for (Ghost ghost : ghosts) {
					ghost.frightened = !ghost.dead;
				}
				ghostsKilledUsingEnergizer = 0;
				forceLivingGhostsTurningBack();
			}
			// bonus reached?
			if (bonusAvailableTimer == 0 && (foodRemaining == 70 || foodRemaining == 170)) {
				bonusAvailableTimer = sec(9) + new Random().nextInt(FPS);
			}
		}
		// bonus found?
		if (bonusAvailableTimer > 0 && world.isBonusTile(x, y)) {
			bonusAvailableTimer = 0;
			bonusConsumedTimer = sec(3);
			points += levelData().bonusPoints();
			log("Pac-Man found bonus %s of value %d", levelData().bonusSymbol(), levelData().bonusPoints());
		}
		// meeting ghost?
		for (Ghost ghost : ghosts) {
			if (!pacMan.tile.equals(ghost.tile)) {
				continue;
			}
			// killing ghost?
			if (ghost.frightened) {
				killGhost(ghost);
			}
			// getting killed by ghost?
			if (pacManPowerTimer == 0 && !ghost.dead) {
				log("Pac-Man killed by %s at tile (%d,%d)", ghost.name, ghost.tile.x, ghost.tile.y);
				pacMan.dead = true;
				--lives;
				break;
			}
		}
	}

	private void updateBonus() {
		if (bonusAvailableTimer > 0) {
			--bonusAvailableTimer;
		}
		if (bonusConsumedTimer > 0) {
			--bonusConsumedTimer;
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.dead = true;
		ghost.frightened = false;
		ghost.targetTile = HOUSE_ENTRY;
		ghostsKilledUsingEnergizer++;
		ghost.bounty = (int) Math.pow(2, ghostsKilledUsingEnergizer) * 100;
		ghost.bountyTimer = sec(0.5f);
		log("Ghost %s killed at tile (%d,%d), Pac-Man wins %d points", ghost.name, ghost.tile.x, ghost.tile.y,
				ghost.bounty);
	}

	private void updateGhosts() {
		for (int ghostIndex = 0; ghostIndex < 4; ++ghostIndex) {
			Ghost ghost = ghosts[ghostIndex];
//			log("%s", ghost);
			if (ghost.bountyTimer > 0) {
				--ghost.bountyTimer;
			} else if (ghost.enteringHouse) {
				letGhostEnterHouse(ghost);
			} else if (ghost.leavingHouse) {
				letGhostLeaveHouse(ghost);
			} else if (ghost.dead) {
				letGhostReturnHome(ghost);
			} else if (state == GameState.SCATTERING) {
				ghost.targetTile = ghost.scatterTile;
				letGhostHeadForTargetTile(ghost);
			} else if (state == GameState.CHASING) {
				ghost.targetTile = computeChasingTarget(ghostIndex);
				letGhostHeadForTargetTile(ghost);
			}
		}
	}

	private V2i computeChasingTarget(int ghostIndex) {
		switch (ghostIndex) {
		case BLINKY: {
			return pacMan.tile;
		}
		case PINKY: {
			V2i p = pacMan.tile.sum(pacMan.dir.vec.scaled(4));
			// simulate offset bug when Pac-Man is looking UP
			return pacMan.dir.equals(UP) ? p.sum(LEFT.vec.scaled(4)) : p;
		}
		case INKY: {
			V2i b = ghosts[BLINKY].tile;
			V2i p = pacMan.tile.sum(pacMan.dir.vec.scaled(2));
			return p.scaled(2).sum(b.scaled(-1));
		}
		case CLYDE: {
			return ghosts[CLYDE].tile.distance(pacMan.tile) < 8 ? ghosts[CLYDE].scatterTile : pacMan.tile;
		}
		default:
			throw new IllegalArgumentException("Unknown ghost index: " + ghostIndex);
		}
	}

	private void letGhostHeadForTargetTile(Ghost ghost) {
		newWishDir(ghost).ifPresent(dir -> ghost.wishDir = dir);
		updateGhostSpeed(ghost);
		move(ghost);
	}

	private void letGhostReturnHome(Ghost ghost) {
		if (atGhostHouseDoor(ghost)) {
			ghost.targetTile = ghost == ghosts[BLINKY] ? HOUSE_CENTER : ghost.homeTile;
			ghost.offset = new V2f(HTS, 0);
			ghost.dir = ghost.wishDir = DOWN;
			ghost.forcedOnTrack = false;
			ghost.enteringHouse = true;
			log("%s starts entering house", ghost);
			return;
		}
		letGhostHeadForTargetTile(ghost);
	}

	private boolean atGhostHouseDoor(Creature guy) {
		return guy.at(HOUSE_ENTRY) && differsAtMost(guy.offset.x, HTS, 2);
	}

	private boolean differsAtMost(float value, float target, float deviation) {
		return Math.abs(value - target) <= deviation;
	}

	private void letGhostEnterHouse(Ghost ghost) {
		// reached target inside house?
		if (ghost.at(ghost.targetTile) && ghost.offset.y >= 0 && differsAtMost(ghost.offset.x, HTS, 2)) {
			ghost.dead = false;
			ghost.dir = ghost.wishDir = ghost.wishDir.inverse();
			ghost.enteringHouse = false;
			ghost.leavingHouse = true;
			log("%s starts leaving house", ghost);
			return;
		}
		// have Inky or Clyde reached the house position where they start moving sidewards?
		if (ghost.at(HOUSE_CENTER) && ghost.offset.y >= 0) {
			ghost.dir = ghost.wishDir = ghost.homeTile.x < HOUSE_CENTER.x ? LEFT : RIGHT;
		}
		updateGhostSpeed(ghost);
		move(ghost, ghost.wishDir);
		log("%s is entering house", ghost);
	}

	private void letGhostLeaveHouse(Ghost ghost) {
		// has left house?
		if (ghost.at(HOUSE_ENTRY) && differsAtMost(ghost.offset.y, 0, 1)) {
			ghost.leavingHouse = false;
			ghost.wishDir = LEFT;
			ghost.forcedOnTrack = true;
			ghost.offset = new V2f(HTS, 0);
			log("%s has left house", ghost);
			return;
		}
		// has reached middle of house?
		if (ghost.at(HOUSE_CENTER) && differsAtMost(ghost.offset.x, 3, 1)) {
			ghost.wishDir = UP;
			ghost.offset = new V2f(HTS, 0);
			updateGhostSpeed(ghost);
			move(ghost);
			return;
		}
		// keep bouncing until ghost can move towards middle of house
		if (ghost.wishDir.equals(UP) || ghost.wishDir.equals(DOWN)) {
			if (ghost.at(ghost.homeTile)) {
				ghost.offset = new V2f(HTS, 0);
				ghost.wishDir = ghost.homeTile.x < HOUSE_CENTER.x ? RIGHT : LEFT;
				return;
			}
			letGhostBounce(ghost);
			return;
		}
		updateGhostSpeed(ghost);
		move(ghost);
		log("%s is leaving house", ghost);
	}

	private void updateGhostSpeed(Ghost ghost) {
		if (ghost.bountyTimer > 0) {
			ghost.speed = 0;
		} else if (ghost.enteringHouse) {
			ghost.speed = levelData().ghostSpeed();
		} else if (ghost.leavingHouse) {
			ghost.speed = 0.5f * levelData().ghostSpeed();
		} else if (ghost.dead) {
			ghost.speed = 1f * levelData().ghostSpeed();
		} else if (world.isInsideTunnel(ghost.tile.x, ghost.tile.y)) {
			ghost.speed = levelData().ghostTunnelSpeed();
		} else if (ghost.frightened) {
			ghost.speed = levelData().frightenedGhostSpeed();
		} else {
			ghost.speed = levelData().ghostSpeed();
			if (ghost == ghosts[BLINKY]) {
				updateElroySpeed(ghost);
			}
		}
	}

	private void updateElroySpeed(Ghost blinky) {
		if (foodRemaining <= levelData().elroy2DotsLeft()) {
			blinky.speed = levelData().elroy2Speed();
		} else if (foodRemaining <= levelData().elroy1DotsLeft()) {
			blinky.speed = levelData().elroy1Speed();
		}
	}

	private Optional<Direction> newWishDir(Ghost ghost) {
		int x = ghost.tile.x, y = ghost.tile.y;
		if (!ghost.changedTile) {
			return Optional.empty();
		}
		if (world.isPortalTile(x, y)) {
			return Optional.empty();
		}
		if (ghost.forcedTurningBack) {
			ghost.forcedTurningBack = false;
			return Optional.of(ghost.wishDir.inverse());
		}
		if (ghost.frightened && world.isIntersectionTile(x, y)) {
			return Optional.of(randomMoveDir(ghost));
		}

		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction dir : List.of(RIGHT, DOWN, LEFT, UP) /* order matters! */) {
			if (dir.equals(ghost.dir.inverse())) {
				continue;
			}
			V2i neighbor = ghost.tile.sum(dir.vec);
			if (!canAccessTile(ghost, neighbor.x, neighbor.y)) {
				continue;
			}
			if (dir.equals(UP) && world.isUpwardsBlocked(neighbor.x, neighbor.y) && !ghost.dead) {
				continue;
			}
			double dist = neighbor.distance(ghost.targetTile);
			// if more than neighbor tile has minimum target distance, most-right in list above wins!
			if (dist <= minDist) {
				minDistDir = dir;
				minDist = dist;
			}
		}
		return Optional.ofNullable(minDistDir);
	}

	private void forceLivingGhostsTurningBack() {
		for (Ghost ghost : ghosts) {
			if (!ghost.dead) {
				ghost.forcedTurningBack = true;
			}
		}
	}

	private void letGhostBounce(Ghost ghost) {
		if (ghost.stuck) {
			ghost.dir = ghost.wishDir = ghost.wishDir.inverse();
		}
		ghost.speed = levelData().ghostSpeed();
		move(ghost, ghost.wishDir);
	}

	private void move(Creature guy) {
		if (guy.speed == 0) {
			return;
		}

		// portal
		if (guy.at(PORTAL_RIGHT_ENTRY) && guy.dir.equals(RIGHT)) {
			guy.tile = PORTAL_LEFT_ENTRY;
			guy.offset = V2f.NULL;
			return;
		}
		if (guy.at(PORTAL_LEFT_ENTRY) && guy.dir.equals(LEFT)) {
			guy.tile = PORTAL_RIGHT_ENTRY;
			guy.offset = V2f.NULL;
			return;
		}

		move(guy, guy.wishDir);
		if (!guy.stuck) {
			guy.dir = guy.wishDir;
		} else {
			move(guy, guy.dir);
		}
	}

	private void move(Creature guy, Direction dir) {
		// turns
		if (guy.forcedOnTrack && canAccessTile(guy, guy.tile.x + dir.vec.x, guy.tile.y + dir.vec.y)) {
			if (dir.equals(LEFT) || dir.equals(RIGHT)) {
				if (Math.abs(guy.offset.y) > 1) {
					guy.stuck = true;
					return;
				}
				guy.offset = new V2f(guy.offset.x, 0);
			} else if (dir.equals(UP) || dir.equals(DOWN)) {
				if (Math.abs(guy.offset.x) > 1) {
					guy.stuck = true;
					return;
				}
				guy.offset = new V2f(0, guy.offset.y);
			}
		}

		// 100% speed corresponds to 1.25 pixels/tick
		V2f velocity = new V2f(dir.vec).scaled(1.25f * guy.speed);
		V2f newPosition = world.position(guy).sum(velocity);
		V2i newTile = world.tile(newPosition);
		V2f newOffset = world.offset(newPosition, newTile);

		if (!canAccessTile(guy, newTile.x, newTile.y)) {
			guy.stuck = true;
			return;
		}

		// avoid moving (partially) into inaccessible tile
		if (guy.at(newTile)) {
			if (!canAccessTile(guy, guy.tile.x + dir.vec.x, guy.tile.y + dir.vec.y)) {
				if (dir.equals(RIGHT) && newOffset.x > 0 || dir.equals(LEFT) && newOffset.x < 0) {
					guy.offset = new V2f(0, guy.offset.y);
					guy.stuck = true;
					return;
				}
				if (dir.equals(DOWN) && newOffset.y > 0 || dir.equals(UP) && newOffset.y < 0) {
					guy.offset = new V2f(guy.offset.x, 0);
					guy.stuck = true;
					return;
				}
			}
		}
		guy.changedTile = !guy.at(newTile);
		guy.tile = newTile;
		guy.offset = newOffset;
		guy.stuck = false;
	}

	private void eatAllFood() {
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (world.isFoodTile(x, y) && !world.hasEatenFood(x, y)) {
					world.eatFood(x, y);
					foodRemaining = 0;
				}
			}
		}
	}

	private boolean canAccessTile(Creature guy, int x, int y) {
		if (x < 0 || x >= WORLD_WIDTH_TILES) {
			return y == PORTAL_LEFT_ENTRY.y;
		}
		if (y < 0 || y >= WORLD_HEIGHT_TILES) {
			return false;
		}
		if (guy instanceof Ghost && world.isGhostHouseDoor(x, y)) {
			Ghost ghost = (Ghost) guy;
			return ghost.enteringHouse || ghost.leavingHouse;
		}
		return world.map(x, y) != '1';
	}

	private Direction randomMoveDir(Creature guy) {
		List<Direction> dirs = new ArrayList<>(3);
		for (Direction dir : Direction.values()) {
			if (dir.equals(guy.dir.inverse())) {
				continue;
			}
			V2i neighbor = guy.tile.sum(dir.vec);
			if (world.isAccessibleTile(neighbor.x, neighbor.y)) {
				dirs.add(dir);
			}
		}
		return dirs.get(new Random().nextInt(dirs.size()));
	}
}