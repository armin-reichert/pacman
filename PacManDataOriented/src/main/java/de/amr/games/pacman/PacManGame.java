package de.amr.games.pacman;

import static de.amr.games.pacman.Direction.DOWN;
import static de.amr.games.pacman.Direction.LEFT;
import static de.amr.games.pacman.Direction.RIGHT;
import static de.amr.games.pacman.Direction.UP;
import static de.amr.games.pacman.V2.distance;
import static de.amr.games.pacman.World.BLINKY_CORNER;
import static de.amr.games.pacman.World.CLYDE_CORNER;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.INKY_CORNER;
import static de.amr.games.pacman.World.PINKY_CORNER;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * My attempt at writing a minimal Pac-Man game with faithful behavior.
 * 
 * @author Armin Reichert
 */
public class PacManGame {

	public static void main(String[] args) {
		PacManGame game = new PacManGame();
		EventQueue.invokeLater(() -> {
			game.ui = new PacManGameUI(game, 2);
			game.initGame();
			new Thread(game::gameLoop, "GameLoop").start();
		});
	}

	public static final int FPS = 60;

	public static void log(String msg, Object... args) {
		System.err.println(String.format("%-20s: %s", LocalTime.now(), String.format(msg, args)));
	}

	public static int sec(float seconds) {
		return (int) (seconds * FPS);
	}

	/**
	 * The level-specific data.
	 * 
	 * <img src="../../../../../resources/levels.png">
	 */
	private static final List<LevelData> LEVEL_DATA = List.of(
	/*@formatter:off*/
		LevelData.of("CHERRIES",   100,  80,  71,  75, 40,  20,  80, 10,  85,  90, 79, 50, 6, 5),
		LevelData.of("STRAWBERRY", 300,  90,  79,  85, 45,  30,  90, 15,  95,  95, 83, 55, 5, 5),
		LevelData.of("PEACH",      500,  90,  79,  85, 45,  40,  90, 20,  95,  95, 83, 55, 4, 5),
		LevelData.of("PEACH",      500,  90,  79,  85, 50,  40, 100, 20,  95,  95, 83, 55, 3, 5),
		LevelData.of("APPLE",      700, 100,  87,  95, 50,  40, 100, 20, 105, 100, 87, 60, 2, 5),
		LevelData.of("APPLE",      700, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 5, 5),
		LevelData.of("GRAPES",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5),
		LevelData.of("GRAPES",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5),
		LevelData.of("GALAXIAN",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 1, 3),
		LevelData.of("GALAXIAN",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 5, 5),
		LevelData.of("BELL",      3000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 2, 5),
		LevelData.of("BELL",      3000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3),
		LevelData.of("KEY",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3),
		LevelData.of("KEY",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 3, 5),
		LevelData.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 1, 3),
		LevelData.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,  0,  0, 1, 3),
		LevelData.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 0, 0),
		LevelData.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,   0, 0, 1, 0),
		LevelData.of("KEY",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0),
		LevelData.of("KEY",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0),
		LevelData.of("KEY",       5000,  90,  79,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0)
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

	private static int attackingTimesRow(int level) {
		return level == 1 ? 0 : level <= 4 ? 1 : 2;
	}

	public final World world;
	public final BitSet eatenFood;
	public final Creature pacMan;
	public final Creature[] ghosts;
	public GameState state;
	public PacManGameUI ui;
	public String messageText;
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
	public long levelChangeStateTimer;
	public long pacManDyingStateTimer;
	public long bonusAvailableTimer;
	public long bonusConsumedTimer;

	public PacManGame() {
		world = new World();
		eatenFood = new BitSet();
		pacMan = new Creature("Pac-Man", Color.YELLOW, new V2(13, 26));
		ghosts = new Creature[] {
			//@formatter:off
			new Creature("Blinky", Color.RED,    new V2(13, 14)),
			new Creature("Pinky",  Color.PINK,   new V2(13, 17)), 
			new Creature("Inky",   Color.CYAN,   new V2(11, 17)),
			new Creature("Clyde",  Color.ORANGE, new V2(15, 17))
			//@formatter:on
		};
	}

	private void initGame() {
		points = 0;
		lives = 3;
		initLevel(1);
		enterReadyState();
	}

	private void initLevel(int n) {
		level = n;
		eatenFood.clear();
		foodRemaining = 244;
		attackWave = 0;
		mazeFlashes = 0;
		ghostsKilledUsingEnergizer = 0;
		pacManPowerTimer = 0;
		readyStateTimer = 0;
		scatteringStateTimer = 0;
		chasingStateTimer = 0;
		levelChangeStateTimer = 0;
		pacManDyingStateTimer = 0;
		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
	}

	private void initEntities() {
		pacMan.speed = 0;
		pacMan.dir = pacMan.wishDir = RIGHT;
		pacMan.tile = pacMan.homeTile;
		pacMan.offset = new V2(HTS, 0);
		pacMan.stuck = false;
		pacMan.dead = false;
		pacMan.visible = true;
		pacMan.forceOnTrack = true;

		for (Creature ghost : ghosts) {
			ghost.speed = 0;
			ghost.tile = ghost.homeTile;
			ghost.offset = new V2(HTS, 0);
			ghost.targetTile = null;
			ghost.tileChanged = true;
			ghost.stuck = false;
			ghost.forceTurnBack = false;
			ghost.forceOnTrack = false;
			ghost.dead = false;
			ghost.vulnerable = false;
			ghost.visible = true;
			ghost.enteringHouse = false;
			ghost.leavingHouse = false;
			ghost.bounty = 0;
			ghost.bountyTimer = 0;
		}
		ghosts[0].dir = ghosts[0].wishDir = LEFT;
		ghosts[1].dir = ghosts[1].wishDir = DOWN;
		ghosts[2].dir = ghosts[2].wishDir = UP;
		ghosts[3].dir = ghosts[3].wishDir = UP;

		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
	}

	private void gameLoop() {
		long start = 0;
		long frames = 0;
		while (true) {
			long time = System.nanoTime();
			update();
			ui.render();
			time = System.nanoTime() - time;
			++frames;
			++framesTotal;
			if (System.nanoTime() - start >= 1_000_000_000) {
				fps = frames;
				frames = 0;
				start = System.nanoTime();
//				log("Time: %-18s %3d frames/sec", LocalTime.now(), fps);
			}
			long sleep = Math.max(1_000_000_000 / FPS - time, 0);
			if (sleep > 0) {
				try {
					Thread.sleep(sleep / 1_000_000); // millis
				} catch (InterruptedException x) {
					x.printStackTrace();
				}
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
			bounce(ghosts[i]);
		}
		--readyStateTimer;
	}

	private void enterReadyState() {
		state = GameState.READY;
		readyStateTimer = sec(3);
		ui.yellowText();
		messageText = "Ready!";
		initEntities();
	}

	private void exitReadyState() {
		messageText = null;
		// TODO move ghosts out of house
		for (int i = 1; i < ghosts.length; ++i) {
			ghosts[i].tile = ghosts[0].homeTile;
			ghosts[i].offset = new V2(HTS, 0);
			ghosts[i].tileChanged = true;
		}
		for (Creature ghost : ghosts) {
			ghost.forceOnTrack = true;
		}
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
		scatteringStateTimer = SCATTERING_TIMES[attackingTimesRow(level)][attackWave];
		forceGhostsTurnBack();
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
		chasingStateTimer = CHASING_TIMES[attackingTimesRow(level)][attackWave];
		forceGhostsTurnBack();
	}

	private void runPacManDyingState() {
		if (pacManDyingStateTimer == 0) {
			exitPacManDyingState();
			if (lives > 0) {
				enterReadyState();
			} else {
				enterGameOverState();
			}
			return;
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
		if (levelChangeStateTimer == 0) {
			log("Level %d complete, entering level %d", level, level + 1);
			initLevel(++level);
			enterReadyState();
			return;
		}
		--levelChangeStateTimer;
	}

	private void enterChangingLevelState() {
		state = GameState.CHANGING_LEVEL;
		levelChangeStateTimer = sec(7);
		mazeFlashes = levelData().intValue(14);
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
		ui.redText();
		messageText = "Game Over!";
	}

	private void exitGameOverState() {
		messageText = null;
	}

	private void updatePacMan() {
		pacMan.speed = levelData().percentValue(2);
		updatePosition(pacMan);

		// Pac-man power expiring?
		if (pacManPowerTimer > 0) {
			pacManPowerTimer--;
			if (pacManPowerTimer == 0) {
				for (Creature ghost : ghosts) {
					ghost.vulnerable = false;
				}
			}
		}

		// food found?
		int x = pacMan.tile.x_int(), y = pacMan.tile.y_int();
		if (world.isFoodTile(x, y) && !hasEatenFood(x, y)) {
			eatenFood.set(world.index(x, y));
			foodRemaining--;
			points += 10;
			// energizer found?
			if (world.isEnergizerTile(pacMan.tile)) {
				points += 40;
				int powerSeconds = levelData().intValue(13);
				pacManPowerTimer = sec(powerSeconds);
				log("Pac-Man got power for %d seconds", powerSeconds);
				for (Creature ghost : ghosts) {
					ghost.vulnerable = !ghost.dead;
				}
				ghostsKilledUsingEnergizer = 0;
				forceGhostsTurnBack();
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
			String bonusName = levelData().stringValue(0);
			int bonusValue = levelData().intValue(1);
			points += bonusValue;
			log("Pac-Man found bonus %s of value %d", bonusName, bonusValue);
		}
		// meeting ghost?
		for (Creature ghost : ghosts) {
			if (!pacMan.tile.equals(ghost.tile)) {
				continue;
			}
			// killing ghost?
			if (ghost.vulnerable) {
				killGhost(ghost);
			}
			// getting killed by ghost?
			if (pacManPowerTimer == 0 && !ghost.dead) {
				log("Pac-Man killed by %s at location %s", ghost.name, ghost.tile);
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

	private void killGhost(Creature ghost) {
		ghost.dead = true;
		ghost.vulnerable = false;
		ghost.targetTile = ghosts[0].homeTile;
		ghostsKilledUsingEnergizer++;
		ghost.bounty = (int) Math.pow(2, ghostsKilledUsingEnergizer) * 100;
		ghost.bountyTimer = sec(0.5f);
		log("Ghost %s killed at location %s, Pac-Man wins %d points", ghost.name, ghost.tile, ghost.bounty);
	}

	private void updateGhosts() {
		for (Creature ghost : ghosts) {
			if (ghost.bountyTimer > 0) {
				--ghost.bountyTimer;
			} else if (ghost.enteringHouse) {
				letGhostEnterHouse(ghost);
			} else if (ghost.leavingHouse) {
				letGhostLeaveHouse(ghost);
			} else if (ghost.dead) {
				letGhostReturnHome(ghost);
			} else if (ghost == ghosts[0]) {
				computeShadowGhostTarget(ghost);
				letGhostHeadForTargetTile(ghost);
			} else if (ghost == ghosts[1]) {
				computeSpeedyGhostTarget(ghost);
				letGhostHeadForTargetTile(ghost);
			} else if (ghost == ghosts[2]) {
				computeBashfulGhostTarget(ghost);
				letGhostHeadForTargetTile(ghost);
			} else if (ghost == ghosts[3]) {
				computePokeyGhostTarget(ghost);
				letGhostHeadForTargetTile(ghost);
			}
		}
	}

	private void letGhostHeadForTargetTile(Creature ghost) {
		updateGhostDir(ghost);
		updateGhostSpeed(ghost);
		updatePosition(ghost);
	}

	private void letGhostReturnHome(Creature ghost) {
		// house entry reached?
		if (ghost.tile.equals(ghosts[0].homeTile) && Math.abs(ghost.offset.x - HTS) <= 2) {
			ghost.offset = new V2(HTS - 1, 0);
			ghost.targetTile = new V2(13, 17);
			ghost.wishDir = DOWN;
			ghost.forceOnTrack = false;
			ghost.enteringHouse = true;
			log("%s entering house", ghost);
			return;
		}
		letGhostHeadForTargetTile(ghost);
	}

	private void letGhostEnterHouse(Creature ghost) {
		// reached target in house?
		if (ghost.tile.equals(ghost.targetTile) && ghost.offset.y > 0) {
			ghost.dead = false;
			ghost.wishDir = UP;
			ghost.enteringHouse = false;
			ghost.leavingHouse = true;
			log("%s leaving house", ghost);
			return;
		}
		updateGhostSpeed(ghost);
		updatePosition(ghost);
		log("%s entering house", ghost);
	}

	private void letGhostLeaveHouse(Creature ghost) {
		// has left house?
		if (ghost.stuck) {
			ghost.leavingHouse = false;
			ghost.wishDir = LEFT;
			ghost.forceOnTrack = true;
			ghost.offset = new V2(HTS, 0);
			return;
		}
		updateGhostSpeed(ghost);
		updatePosition(ghost);
	}

	private void computeShadowGhostTarget(Creature blinky) {
		if (state == GameState.SCATTERING) {
			blinky.targetTile = BLINKY_CORNER;
		} else if (state == GameState.CHASING) {
			blinky.targetTile = pacMan.tile;
		}
	}

	private void computeSpeedyGhostTarget(Creature pinky) {
		if (state == GameState.SCATTERING) {
			pinky.targetTile = PINKY_CORNER;
		} else if (state == GameState.CHASING) {
			if (pacMan.dir.equals(UP)) {
				// simulate offset bug
				pinky.targetTile = pacMan.tile.sum(pacMan.dir.vector.scaled(4)).sum(LEFT.vector.scaled(4));
			} else {
				pinky.targetTile = pacMan.tile.sum(pacMan.dir.vector.scaled(4));
			}
		}
	}

	private void computeBashfulGhostTarget(Creature inky) {
		if (state == GameState.SCATTERING) {
			inky.targetTile = INKY_CORNER;
		} else if (state == GameState.CHASING) {
			Creature blinky = ghosts[0];
			inky.targetTile = pacMan.tile.sum(pacMan.dir.vector.scaled(2)).scaled(2).sum(blinky.tile.scaled(-1));
		}
	}

	private void computePokeyGhostTarget(Creature clyde) {
		if (state == GameState.SCATTERING) {
			clyde.targetTile = CLYDE_CORNER;
		} else if (state == GameState.CHASING) {
			clyde.targetTile = distance(clyde.tile, pacMan.tile) > 8 ? pacMan.tile : CLYDE_CORNER;
		}
	}

	private void updateGhostSpeed(Creature ghost) {
		if (ghost.bountyTimer > 0) {
			ghost.speed = 0;
		} else if (ghost.enteringHouse || ghost.leavingHouse) {
			ghost.speed = levelData().percentValue(4);
		} else if (ghost.dead) {
			ghost.speed = 2 * levelData().percentValue(4);
		} else if (world.isInsideTunnel(ghost.tile)) {
			ghost.speed = levelData().percentValue(5);
		} else if (ghost.vulnerable) {
			ghost.speed = levelData().percentValue(12);
		} else {
			ghost.speed = levelData().percentValue(4);
			if (ghost == ghosts[0]) {
				checkElroySpeed(ghost);
			}
		}
	}

	private void checkElroySpeed(Creature blinky) {
		if (foodRemaining <= levelData().intValue(8)) {
			blinky.speed = levelData().percentValue(9);
		} else if (foodRemaining <= levelData().intValue(6)) {
			blinky.speed = levelData().percentValue(7);
		}
	}

	private void updateGhostDir(Creature ghost) {
		if (ghost.targetTile == null) {
			return;
		}
		if (!ghost.tileChanged) {
			return;
		}
		if (world.isPortalTile(ghost.tile)) {
			return;
		}
		if (ghost.forceTurnBack) {
			ghost.wishDir = ghost.wishDir.inverse();
			ghost.forceTurnBack = false;
			return;
		}
		if (pacManPowerTimer > 0 && world.isIntersectionTile(ghost.tile)) {
			ghost.wishDir = randomMoveDir(ghost);
			return;
		}
		Direction newDir = null;
		double min = Double.MAX_VALUE;
		for (Direction dir : List.of(RIGHT, DOWN, LEFT, UP) /* order matters! */) {
			if (dir.equals(ghost.dir.inverse())) {
				continue;
			}
			V2 neighbor = ghost.tile.sum(dir.vector);
			if (dir.equals(UP) && world.isUpwardsBlocked(neighbor)) {
				continue;
			}
			if (!canAccessTile(ghost, neighbor)) {
				continue;
			}
			double d = distance(neighbor, ghost.targetTile);
			if (d <= min) {
				newDir = dir;
				min = d;
			}
		}
		if (newDir != null) {
			ghost.wishDir = newDir;
//			log("%s's intended direction is %s", ghost.name, ghost.intendedDir);
		}
	}

	private void forceGhostsTurnBack() {
		for (Creature ghost : ghosts) {
			ghost.forceTurnBack = true;
		}
	}

	private void bounce(Creature ghost) {
		if (ghost.stuck) {
			ghost.wishDir = ghost.wishDir.inverse();
		}
		ghost.speed = levelData().percentValue(4);
		updatePosition(ghost);
	}

	private void updatePosition(Creature guy) {
		if (guy.speed == 0) {
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

		// portal
		if (guy.tile.equals(World.PORTAL_RIGHT_ENTRY) && dir.equals(RIGHT)) {
			guy.tile = World.PORTAL_LEFT_ENTRY;
			guy.offset = V2.NULL;
			guy.stuck = false;
			return;
		}
		if (guy.tile.equals(World.PORTAL_LEFT_ENTRY) && dir.equals(LEFT)) {
			guy.tile = World.PORTAL_RIGHT_ENTRY;
			guy.offset = V2.NULL;
			guy.stuck = false;
			return;
		}

		// turns
		if (guy.forceOnTrack && canAccessTile(guy, guy.tile.sum(dir.vector))) {
			if (dir.equals(LEFT) || dir.equals(RIGHT)) {
				if (Math.abs(guy.offset.y) > 1) {
					guy.stuck = true;
					return;
				}
				guy.offset = new V2(guy.offset.x, 0);
			} else if (dir.equals(UP) || dir.equals(DOWN)) {
				if (Math.abs(guy.offset.x) > 1) {
					guy.stuck = true;
					return;
				}
				guy.offset = new V2(0, guy.offset.y);
			}
		}

		V2 velocity = dir.vector.scaled(1.25f * guy.speed); // 100% speed corresponds to 1.25 pixels/tick
		V2 positionAfterMove = world.position(guy).sum(velocity);
		V2 tileAfterMove = world.tile(positionAfterMove);
		V2 offsetAfterMove = world.offset(positionAfterMove, tileAfterMove);

		if (!canAccessTile(guy, tileAfterMove)) {
			guy.stuck = true;
			return;
		}

		// avoid moving partially into inaccessible tile
		if (tileAfterMove.equals(guy.tile)) {
			if (!canAccessTile(guy, guy.tile.sum(dir.vector))) {
				if (dir.equals(RIGHT) && offsetAfterMove.x > 0 || dir.equals(LEFT) && offsetAfterMove.x < 0) {
					guy.offset = new V2(0, guy.offset.y);
					guy.stuck = true;
					return;
				}
				if (dir.equals(DOWN) && offsetAfterMove.y > 0 || dir.equals(UP) && offsetAfterMove.y < 0) {
					guy.offset = new V2(guy.offset.x, 0);
					guy.stuck = true;
					return;
				}
			}
		}
		guy.tileChanged = !guy.tile.equals(tileAfterMove);
		guy.tile = tileAfterMove;
		guy.offset = offsetAfterMove;
		guy.stuck = false;
	}

	private void eatAllFood() {
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				int index = world.index(x, y);
				if (world.isFoodTile(x, y) && !eatenFood.get(index)) {
					eatenFood.set(index);
					foodRemaining = 0;
				}
			}
		}
	}

	private boolean canAccessTile(Creature guy, V2 tile) {
		int x = tile.x_int(), y = tile.y_int();
		if (x < 0 || x >= WORLD_WIDTH_TILES) {
			return y == 17; // can leave world through horizontal tunnel
		}
		if (y < 0 || y >= WORLD_HEIGHT_TILES) {
			return false;
		}
		if (world.isGhostHouseDoor(tile)) {
			return guy.enteringHouse || guy.leavingHouse;
		}
		return world.map(x, y) != '1';
	}

	private Direction randomMoveDir(Creature guy) {
		List<Direction> dirs = new ArrayList<>(3);
		for (Direction dir : Direction.values()) {
			if (dir.equals(guy.dir.inverse())) {
				continue;
			}
			V2 neighbor = guy.tile.sum(dir.vector);
			if (world.isAccessibleTile(neighbor)) {
				dirs.add(dir);
			}
		}
		return dirs.get(new Random().nextInt(dirs.size()));
	}

	public boolean hasEatenFood(int x, int y) {
		return eatenFood.get(world.index(x, y));
	}
}