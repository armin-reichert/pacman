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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
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

	public enum GameState {

		READY, SCATTERING, CHASING, CHANGING_LEVEL, PACMAN_DYING, GAME_OVER;
	}

	public static final int FPS = 60;

	public static void log(String msg, Object... args) {
		System.err.println(String.format(msg, args));
	}

	public static int sec(float seconds) {
		return (int) (seconds * FPS);
	}

	public static class LevelData {

		private List<?> values;

		public static LevelData of(Object... values) {
			LevelData data = new LevelData();
			data.values = List.of(values);
			return data;
		}

		public float percentValue(int index) {
			return intValue(index) / 100f;
		}

		public int intValue(int index) {
			return (int) values.get(index);
		}

		public String stringValue(int index) {
			return (String) values.get(index);
		}
	}

	/**
	 * The level-specific data.
	 * 
	 * <img src="http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">
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
		return level < 22 ? LEVEL_DATA.get(level - 1) : LEVEL_DATA.get(20);
	}

	static final long[][] SCATTERING_TIMES = {
		//@formatter:off
		{ sec(7), sec(7), sec(5), sec(5) },
		{ sec(7), sec(7), sec(5), 1      },
		{ sec(5), sec(5), sec(5), 1      },
		//@formatter:on
	};

	static final long[][] CHASING_TIMES = {
		//@formatter:off
		{ sec(20), sec(20), sec(20),   Long.MAX_VALUE },
		{ sec(20), sec(20), sec(1033), Long.MAX_VALUE },
		{ sec(5),  sec(5),  sec(1037), Long.MAX_VALUE },
		//@formatter:on
	};

	private static int attackWaveIndex(int level) {
		return level == 1 ? 0 : level <= 4 ? 1 : 2;
	}

	public World world;
	public BitSet eatenFood;
	public GameState state;
	public Creature pacMan;
	public Creature[] ghosts;
	public PacManGameUI ui;
	public String messageText;
	public long fps;
	public long framesTotal;
	public int level;
	public int attackWave;
	public int foodRemaining;
	public int lives;
	public int points;
	public int ghostsKilledByEnergizer;
	public long pacManPowerTimer;
	public long readyStateTimer;
	public long scatteringStateTimer;
	public long chasingStateTimer;
	public long levelChangeStateTimer;
	public long bonusAvailableTimer;
	public long bonusConsumedTimer;
	public long pacManDyingStateTimer;

	public PacManGame() {
		world = new World();
		eatenFood = new BitSet();
		createEntities();
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
		ghostsKilledByEnergizer = 0;
		pacManPowerTimer = 0;
		chasingStateTimer = 0;
		levelChangeStateTimer = 0;
		attackWave = 0;
		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
	}

	private void createEntities() {
		pacMan = new Creature("Pac-Man", Color.YELLOW);
		pacMan.homeTile = new V2(13, 26);

		ghosts = new Creature[4];

		ghosts[0] = new Creature("Blinky", Color.RED);
		ghosts[0].homeTile = new V2(13, 14);

		ghosts[1] = new Creature("Pinky", Color.PINK);
		ghosts[1].homeTile = new V2(13, 17);

		ghosts[2] = new Creature("Inky", Color.CYAN);
		ghosts[2].homeTile = new V2(11, 17);

		ghosts[3] = new Creature("Clyde", Color.ORANGE);
		ghosts[3].homeTile = new V2(15, 17);
	}

	private void initEntities() {
		pacMan.tile = pacMan.homeTile;
		pacMan.offset = new V2(HTS, 0);
		pacMan.dir = pacMan.wishDir = RIGHT;
		pacMan.speed = 0;
		pacMan.stuck = false;
		pacMan.dead = false;
		pacMan.visible = true;
		for (int i = 0; i < ghosts.length; ++i) {
			Creature ghost = ghosts[i];
			ghost.tile = ghost.homeTile;
			ghost.targetTile = null;
			ghost.offset = new V2(HTS, 0);
			ghost.speed = 0;
			ghost.tileChanged = true;
			ghost.stuck = false;
			ghost.forceTurnBack = false;
			ghost.dead = false;
			ghost.vulnerable = false;
			ghost.bounty = 0;
			ghost.visible = true;
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
		if (ui.pressedKeys.get(KeyEvent.VK_LEFT)) {
			pacMan.wishDir = LEFT;
		} else if (ui.pressedKeys.get(KeyEvent.VK_RIGHT)) {
			pacMan.wishDir = RIGHT;
		} else if (ui.pressedKeys.get(KeyEvent.VK_UP)) {
			pacMan.wishDir = UP;
		} else if (ui.pressedKeys.get(KeyEvent.VK_DOWN)) {
			pacMan.wishDir = DOWN;
		} else if (ui.pressedKeys.get(KeyEvent.VK_D)) {
			ui.debugDraw = !ui.debugDraw;
		} else if (ui.pressedKeys.get(KeyEvent.VK_E)) {
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
		updateInky();
		updatePinky();
		updateClyde();
		--readyStateTimer;
	}

	private void enterReadyState() {
		state = GameState.READY;
		readyStateTimer = sec(3);
		messageText = "Ready!";
		ui.messageColor = Color.YELLOW;
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
		updateGuys();
		updateBonus();
	}

	private void enterScatteringState() {
		state = GameState.SCATTERING;
		scatteringStateTimer = SCATTERING_TIMES[attackWaveIndex(level)][attackWave];
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
		updateGuys();
		updateBonus();
	}

	private void enterChasingState() {
		state = GameState.CHASING;
		chasingStateTimer = CHASING_TIMES[attackWaveIndex(level)][attackWave];
		forceGhostsTurnBack();
	}

	private void enterPacManDyingState() {
		state = GameState.PACMAN_DYING;
		// 11 animation frames, 8 ticks each, 2 seconds before animation, 2 seconds after
		pacManDyingStateTimer = sec(2) + 88 + sec(2);
	}

	private void runPacManDyingState() {
		if (pacManDyingStateTimer == 0) {
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
		levelChangeStateTimer = sec(3);
	}

	private void runGameOverState() {
		if (ui.pressedKeys.get(KeyEvent.VK_SPACE)) {
			exitGameOverState();
			initGame();
		}
	}

	private void enterGameOverState() {
		state = GameState.GAME_OVER;
		messageText = "Game Over!";
		ui.messageColor = Color.RED;
	}

	private void exitGameOverState() {
		messageText = null;
	}

	private void updateGuys() {
		updatePacMan();
		updateBlinky();
		updatePinky();
		updateInky();
		updateClyde();
	}

	private void updatePacMan() {

		pacMan.speed = levelData(level).percentValue(2);
		pacMan.stuck = !move(pacMan);

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
		int x = (int) pacMan.tile.x, y = (int) pacMan.tile.y;
		if (world.isFoodTile(x, y) && !hasEatenFood(x, y)) {
			eatenFood.set(world.index(x, y));
			foodRemaining--;
			points += 10;
			// energizer found?
			if (world.isEnergizerTile(pacMan.tile)) {
				points += 40;
				int powerSeconds = levelData(level).intValue(13);
				pacManPowerTimer = sec(powerSeconds);
				log("Pac-Man got power for %d seconds", powerSeconds);
				for (Creature ghost : ghosts) {
					ghost.vulnerable = !ghost.dead;
				}
				ghostsKilledByEnergizer = 0;
				forceGhostsTurnBack();
			}
			// bonus reached?
			if (bonusAvailableTimer == 0 && (foodRemaining == 70 || foodRemaining == 170)) {
				bonusAvailableTimer = sec(9) + new Random().nextInt(FPS);
			}
		}
		// bonus found?
		if (bonusAvailableTimer > 0 && x == 13 && y == 20) {
			bonusAvailableTimer = 0;
			bonusConsumedTimer = sec(3);
			String bonusName = levelData(level).stringValue(0);
			int bonusValue = levelData(level).intValue(1);
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
				ghost.dead = true;
				ghost.vulnerable = false;
				ghost.targetTile = ghosts[0].homeTile;
				ghostsKilledByEnergizer++;
				ghost.bounty = (int) Math.pow(2, ghostsKilledByEnergizer) * 100;
				ghost.bountyTimer = sec(0.5f);
				log("Ghost %s killed at location %s, Pac-Man wins %d points", ghost.name, ghost.tile, ghost.bounty);
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

	private void updateDeadGhost(Creature ghost) {
		if (ghost.tile.equals(ghosts[0].homeTile) && ghost.offset.x - HTS < 2) {
			ghost.dead = false;
		} else {
			if (ghost.bountyTimer > 0) {
				--ghost.bountyTimer;
			}
		}
	}

	private void updateBlinky() {
		Creature blinky = ghosts[0];
		if (blinky.dead) {
			updateDeadGhost(blinky);
		} else if (state == GameState.SCATTERING) {
			blinky.targetTile = BLINKY_CORNER;
		} else if (state == GameState.CHASING) {
			blinky.targetTile = pacMan.tile;
		}
		updateGhostDirection(blinky);
		updateGhostSpeed(blinky);
		blinky.stuck = !move(blinky);
	}

	private void updatePinky() {
		Creature pinky = ghosts[1];
		if (pinky.dead) {
			updateDeadGhost(pinky);
		} else if (state == GameState.READY) {
			bounce(pinky);
		} else if (state == GameState.SCATTERING) {
			pinky.targetTile = PINKY_CORNER;
		} else if (state == GameState.CHASING) {
			pinky.targetTile = pacMan.tile.sum(pacMan.dir.vector.scaled(4));
			if (pacMan.dir.equals(UP)) {
				// simulate offset bug
				pinky.targetTile = pinky.targetTile.sum(LEFT.vector.scaled(4));
			}
		}
		updateGhostDirection(pinky);
		updateGhostSpeed(pinky);
		pinky.stuck = !move(pinky);
	}

	private void updateInky() {
		Creature inky = ghosts[2];
		Creature blinky = ghosts[0];
		if (inky.dead) {
			updateDeadGhost(inky);
		} else if (state == GameState.READY) {
			bounce(inky);
		} else if (state == GameState.SCATTERING) {
			inky.targetTile = INKY_CORNER;
		} else if (state == GameState.CHASING) {
			inky.targetTile = pacMan.tile.sum(pacMan.dir.vector.scaled(2)).scaled(2).sum(blinky.tile.scaled(-1));
		}
		updateGhostDirection(inky);
		updateGhostSpeed(inky);
		inky.stuck = !move(inky);
	}

	private void updateClyde() {
		Creature clyde = ghosts[3];
		if (clyde.dead) {
			updateDeadGhost(clyde);
		} else if (state == GameState.READY) {
			bounce(clyde);
		} else if (state == GameState.SCATTERING) {
			clyde.targetTile = CLYDE_CORNER;
		} else if (state == GameState.CHASING) {
			clyde.targetTile = distance(clyde.tile, pacMan.tile) > 8 ? pacMan.tile : CLYDE_CORNER;
		}
		updateGhostDirection(clyde);
		updateGhostSpeed(clyde);
		clyde.stuck = !move(clyde);
	}

	private void updateGhostSpeed(Creature ghost) {
		if (ghost.bountyTimer > 0) {
			ghost.speed = 0;
		} else if (ghost.dead) {
			ghost.speed = 2 * levelData(level).percentValue(4);
		} else if (world.isInsideTunnel(ghost.tile)) {
			ghost.speed = levelData(level).percentValue(5);
		} else if (pacManPowerTimer > 0) {
			ghost.speed = levelData(level).percentValue(12);
		} else {
			ghost.speed = levelData(level).percentValue(4);
		}
	}

	private void updateGhostDirection(Creature ghost) {
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
		for (Direction dir : List.of(RIGHT, DOWN, LEFT, UP)) {
			if (dir.equals(ghost.dir.inverse())) {
				continue;
			}
			if (dir.equals(UP) && world.isUpwardsBlocked(ghost.tile.sum(UP.vector))) {
				continue;
			}
			V2 neighbor = ghost.tile.sum(dir.vector);
			if (!canAccessTile(ghost, neighbor)) {
				continue;
			}
			double d = V2.distance(neighbor, ghost.targetTile);
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

	private void bounce(Creature guy) {
		if (guy.stuck) {
			guy.wishDir = guy.wishDir.inverse();
		}
	}

	private boolean move(Creature guy) {
		if (guy.speed == 0) {
			return false;
		}
		if (move(guy, guy.wishDir)) {
			guy.dir = guy.wishDir;
			return true;
		}
		return move(guy, guy.dir);
	}

	private boolean move(Creature guy, Direction dir) {

		// portal
		if (guy.tile.equals(World.PORTAL_RIGHT_ENTRY) && dir.equals(RIGHT)) {
			guy.tile = World.PORTAL_LEFT_ENTRY;
			guy.offset = V2.NULL;
			return true;
		}
		if (guy.tile.equals(World.PORTAL_LEFT_ENTRY) && dir.equals(LEFT)) {
			guy.tile = World.PORTAL_RIGHT_ENTRY;
			guy.offset = V2.NULL;
			return true;
		}

		// turns
		if (!world.isInsideGhostHouse(guy.tile) && canAccessTile(guy, guy.tile.sum(dir.vector))) {
			if (dir.equals(LEFT) || dir.equals(RIGHT)) {
				if (Math.abs(guy.offset.y) > 1) {
					return false;
				}
				guy.offset = new V2(guy.offset.x, 0);
			}
			if (dir.equals(UP) || dir.equals(DOWN)) {
				if (Math.abs(guy.offset.x) > 1) {
					return false;
				}
				guy.offset = new V2(0, guy.offset.y);
			}
		}

		V2 velocity = dir.vector.scaled(1.25f * guy.speed); // 100% speed corresponds to 1.25 pixels/tick
		V2 positionAfterMove = world.position(guy).sum(velocity);
		V2 tileAfterMove = world.tile(positionAfterMove);

		if (!canAccessTile(guy, tileAfterMove)) {
			return false;
		}

		V2 offsetAfterMove = world.offset(positionAfterMove, tileAfterMove);

		// avoid moving partially into inaccessible tile
		if (tileAfterMove.equals(guy.tile)) {
			if (!canAccessTile(guy, guy.tile.sum(dir.vector))) {
				if (dir.equals(RIGHT) && offsetAfterMove.x > 0 || dir.equals(LEFT) && offsetAfterMove.x < 0) {
					guy.offset = new V2(0, guy.offset.y);
					return false;
				}
				if (dir.equals(DOWN) && offsetAfterMove.y > 0 || dir.equals(UP) && offsetAfterMove.y < 0) {
					guy.offset = new V2(guy.offset.x, 0);
					return false;
				}
			}
		}
		guy.tileChanged = !guy.tile.equals(tileAfterMove);
		guy.tile = tileAfterMove;
		guy.offset = offsetAfterMove;
		return true;
	}

	private void eatAllFood() {
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (world.isFoodTile(x, y) && !eatenFood.get(world.index(x, y))) {
					eatenFood.set(world.index(x, y));
					foodRemaining = 0;
				}
			}
		}
	}

	private boolean canAccessTile(Creature guy, V2 tile) {
		if (tile.y == 17 && (tile.x < 0 || tile.x >= WORLD_WIDTH_TILES)) {
			return true;
		}
		if (tile.x < 0 || tile.x >= WORLD_WIDTH_TILES) {
			return false;
		}
		if (tile.y < 0 || tile.y >= WORLD_HEIGHT_TILES) {
			return false;
		}
		if (world.isGhostHouseDoor(tile)) {
			return false; // TODO ghost can access door when leaving or entering ghosthouse
		}
		return world.content((int) tile.x, (int) tile.y) != '1';
	}

	private Direction randomMoveDir(Creature guy) {
		List<Direction> dirs = new ArrayList<>(3);
		for (Direction dir : Direction.values()) {
			if (!dir.equals(guy.dir.inverse()) && world.isAccessibleTile(guy.tile.sum(dir.vector))) {
				dirs.add(dir);
			}
		}
		Collections.shuffle(dirs);
		return dirs.get(0);
	}

	public boolean hasEatenFood(int x, int y) {
		return world.isFoodTile(x, y) && eatenFood.get(world.index(x, y));
	}
}