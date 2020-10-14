package de.amr.games.pacman;

import static de.amr.games.pacman.V2.distance;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
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
			new Thread(game::gameLoop, "GameLoop").start();
		});
	}

	public enum GameState {

		READY, SCATTERING, CHASING, CHANGING_LEVEL;
	}

	public static final int FPS = 60;

	public static void log(String msg, Object... args) {
		System.err.println(String.format(msg, args));
	}

	public static int sec(float seconds) {
		return (int) (seconds * FPS);
	}

	/**
	 * Returns the level-specific data.
	 * 
	 * <img src="http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">
	 * 
	 * @param level level number (1..)
	 * @return data for level with given number
	 */
	public static List<?> levelData(int level) {
		if (level < 1) {
			throw new IllegalArgumentException("Illegal game level number: " + level);
		}
		switch (level) {
		/*@formatter:off*/
		case  1: return List.of("CHERRIES",   100,  80,  71,  75, 40,  20,  80, 10,  85,  90, 79, 50, 6, 5);
		case  2: return List.of("STRAWBERRY", 300,  90,  79,  85, 45,  30,  90, 15,  95,  95, 83, 55, 5, 5);
		case  3: return List.of("PEACH",      500,  90,  79,  85, 45,  40,  90, 20,  95,  95, 83, 55, 4, 5);
		case  4: return List.of("PEACH",      500,  90,  79,  85, 50,  40, 100, 20,  95,  95, 83, 55, 3, 5);
		case  5: return List.of("APPLE",      700, 100,  87,  95, 50,  40, 100, 20, 105, 100, 87, 60, 2, 5);
		case  6: return List.of("APPLE",      700, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 5, 5);
		case  7: return List.of("GRAPES",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5);
		case  8: return List.of("GRAPES",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5);
		case  9: return List.of("GALAXIAN",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 1, 3);
		case 10: return List.of("GALAXIAN",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 5, 5);
		case 11: return List.of("BELL",      3000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 2, 5);
		case 12: return List.of("BELL",      3000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3);
		case 13: return List.of("KEY",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3);
		case 14: return List.of("KEY",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 3, 5);
		case 15: return List.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 1, 3);
		case 16: return List.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,  0,  0, 1, 3);
		case 17: return List.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 0, 0);
		case 18: return List.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,   0, 0, 1, 0);
		case 19: return List.of("KEY",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0);
		case 20: return List.of("KEY",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0);
		default: return List.of("KEY",       5000,  90,  79,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0);
		//@formatter:on
		}
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
	public List<?> levelData;
	public int attackWave;
	public int foodRemaining;
	public int points;
	public long pacManPowerTimer;
	public long readyStateTimer;
	public long scatteringStateTimer;
	public long chasingStateTimer;
	public long levelChangeStateTimer;
	public long bonusTimer;
	public long bonusValueTimer;

	public PacManGame() {
		world = new World();
		eatenFood = new BitSet();
		createEntities();
		initGame();
	}

	private void initGame() {
		points = 0;
		initLevel(1);
		enterReadyState();
	}

	private void initLevel(int n) {
		level = n;
		levelData = levelData(level);
		eatenFood.clear();
		foodRemaining = 244;
		pacManPowerTimer = 0;
		chasingStateTimer = 0;
		levelChangeStateTimer = 0;
		attackWave = 0;
		bonusTimer = 0;
		bonusValueTimer = 0;
	}

	private void createEntities() {
		pacMan = new Creature("Pac-Man", Color.YELLOW);
		pacMan.homeTile = new V2(13, 26);

		ghosts = new Creature[4];

		ghosts[0] = new Creature("Blinky", Color.RED);
		ghosts[0].homeTile = new V2(13, 14);
		ghosts[0].scatterTile = new V2(WORLD_WIDTH_TILES - 3, 0);

		ghosts[1] = new Creature("Pinky", Color.PINK);
		ghosts[1].homeTile = new V2(13, 17);
		ghosts[1].scatterTile = new V2(2, 0);

		ghosts[2] = new Creature("Inky", Color.CYAN);
		ghosts[2].homeTile = new V2(11, 17);
		ghosts[2].scatterTile = new V2(WORLD_WIDTH_TILES - 1, WORLD_HEIGHT_TILES - 1);

		ghosts[3] = new Creature("Clyde", Color.ORANGE);
		ghosts[3].homeTile = new V2(15, 17);
		ghosts[3].scatterTile = new V2(0, WORLD_HEIGHT_TILES - 1);
	}

	private void initEntities() {
		pacMan.tile = pacMan.homeTile;
		pacMan.offset = new V2(HTS, 0);
		pacMan.dir = pacMan.intendedDir = V2.RIGHT;
		pacMan.speed = 0;
		pacMan.stuck = false;
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
		}
		ghosts[0].dir = ghosts[0].intendedDir = V2.LEFT;
		ghosts[1].dir = ghosts[1].intendedDir = V2.DOWN;
		ghosts[2].dir = ghosts[2].intendedDir = V2.UP;
		ghosts[3].dir = ghosts[3].intendedDir = V2.UP;
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
				log("Time: %-18s %3d frames/sec", LocalTime.now(), fps);
				fps = frames;
				frames = 0;
				start = System.nanoTime();
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
			pacMan.intendedDir = V2.LEFT;
		} else if (ui.pressedKeys.get(KeyEvent.VK_RIGHT)) {
			pacMan.intendedDir = V2.RIGHT;
		} else if (ui.pressedKeys.get(KeyEvent.VK_UP)) {
			pacMan.intendedDir = V2.UP;
		} else if (ui.pressedKeys.get(KeyEvent.VK_DOWN)) {
			pacMan.intendedDir = V2.DOWN;
		} else if (ui.pressedKeys.get(KeyEvent.VK_D)) {
			ui.debugDraw = !ui.debugDraw;
		} else if (ui.pressedKeys.get(KeyEvent.VK_E)) {
			eatAllFood();
		}
	}

	private void update() {
		readInput();
		if (state == GameState.READY) {
			handleReadyState();
		} else if (state == GameState.CHASING) {
			handleChasingState();
		} else if (state == GameState.SCATTERING) {
			handleScatteringState();
		} else if (state == GameState.CHANGING_LEVEL) {
			handleChangingLevelState();
		}
	}

	private void handleReadyState() {
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
		readyStateTimer = sec(5);
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
	}

	private void handleScatteringState() {
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

	private void handleChasingState() {
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

	private void handleChangingLevelState() {
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

	private void updateGuys() {
		updatePacMan();
		updateBlinky();
		updatePinky();
		updateInky();
		updateClyde();
	}

	private void updatePacMan() {
		pacMan.speed = (int) levelData.get(2) / 100f;
		pacMan.stuck = !move(pacMan);
		int x = (int) pacMan.tile.x, y = (int) pacMan.tile.y;
		if (world.isFoodTile(x, y) && !hasEatenFood(x, y)) {
			eatenFood.set(world.index(x, y));
			foodRemaining--;
			points += 10;
			if (world.isEnergizerTile(pacMan.tile)) {
				points += 40;
				int powerSeconds = (int) levelData.get(13);
				pacManPowerTimer = sec(powerSeconds);
				log("Pac-Man got power for %d seconds", powerSeconds);
				for (Creature ghost : ghosts) {
					ghost.vulnerable = !ghost.dead;
				}
				forceGhostsTurnBack();
			}
		}
		if (bonusTimer > 0) {
			if (pacMan.tile.x == 13 && pacMan.tile.y == 20) {
				bonusTimer = 0;
				bonusValueTimer = sec(3);
				String bonusName = (String) levelData.get(0);
				int bonusValue = (int) levelData.get(1);
				points += bonusValue;
				log("Pac-Man found bonus %s of value %d", bonusName, bonusValue);
			}
		}
		if (pacManPowerTimer > 0) {
			for (Creature ghost : ghosts) {
				if (pacMan.tile.equals(ghost.tile) && ghost.vulnerable) {
					ghost.dead = true;
					log("Pac-Man killed %s at location %s", ghost.name, ghost.tile);
				}
			}
		}
		// TODO handle Pac-Man death, reset timers when that happens
		if (pacManPowerTimer == 1) {
			for (Creature ghost : ghosts) {
				ghost.vulnerable = false;
			}
		}
		pacManPowerTimer = Math.max(0, pacManPowerTimer - 1);
	}

	private void updateBonus() {
		if (bonusTimer > 0) {
			--bonusTimer;
		}
		if (bonusValueTimer > 0) {
			--bonusValueTimer;
		}
		if (foodRemaining == 70 || foodRemaining == 170) {
			bonusTimer = sec(9) + new Random().nextInt(FPS);
		}
	}

	private void updateDeadGhost(Creature ghost) {
		if (ghost.tile.equals(ghosts[0].homeTile) && ghost.offset.x - HTS < 2) {
			ghost.dead = false;
			ghost.vulnerable = false;
		} else {
			ghost.targetTile = ghosts[0].homeTile;
		}
	}

	private void updateBlinky() {
		Creature blinky = ghosts[0];
		if (blinky.dead) {
			updateDeadGhost(blinky);
		} else if (state == GameState.SCATTERING) {
			blinky.targetTile = blinky.scatterTile;
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
			pinky.targetTile = pinky.scatterTile;
		} else if (state == GameState.CHASING) {
			pinky.targetTile = pacMan.tile.sum(pacMan.dir.scaled(4));
			if (pacMan.dir.equals(V2.UP)) {
				// simulate offset bug
				pinky.targetTile = pinky.targetTile.sum(V2.LEFT.scaled(4));
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
			inky.targetTile = inky.scatterTile;
		} else if (state == GameState.CHASING) {
			inky.targetTile = pacMan.tile.sum(pacMan.dir.scaled(2)).scaled(2).sum(blinky.tile.inverse());
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
			clyde.targetTile = clyde.scatterTile;
		} else if (state == GameState.CHASING) {
			clyde.targetTile = distance(clyde.tile, pacMan.tile) > 8 ? pacMan.tile : clyde.scatterTile;
		}
		updateGhostDirection(clyde);
		updateGhostSpeed(clyde);
		clyde.stuck = !move(clyde);
	}

	private void updateGhostSpeed(Creature ghost) {
		if (ghost.dead) {
			ghost.speed = 2 * (int) levelData.get(4) / 100f;
		} else if (world.isInsideTunnel(ghost.tile)) {
			ghost.speed = (int) levelData.get(5) / 100f;
		} else if (pacManPowerTimer > 0) {
			ghost.speed = (int) levelData.get(12) / 100f;
		} else {
			ghost.speed = (int) levelData.get(4) / 100f;
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
			ghost.intendedDir = ghost.intendedDir.inverse();
			ghost.forceTurnBack = false;
			return;
		}
		if (pacManPowerTimer > 0 && world.isIntersectionTile(ghost.tile)) {
			ghost.intendedDir = randomAccessibleDir(ghost);
			return;
		}
		V2 newDir = null;
		double min = Double.MAX_VALUE;
		for (V2 dir : List.of(V2.RIGHT, V2.DOWN, V2.LEFT, V2.UP)) {
			if (dir.equals(ghost.dir.inverse())) {
				continue;
			}
			if (dir.equals(V2.UP) && world.isUpwardsBlocked(ghost.tile.sum(V2.UP))) {
				continue;
			}
			V2 neighbor = ghost.tile.sum(dir);
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
			ghost.intendedDir = newDir;
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
			guy.intendedDir = guy.intendedDir.inverse();
		}
	}

	private boolean move(Creature guy) {
		if (guy.speed == 0) {
			return false;
		}
		if (move(guy, guy.intendedDir)) {
			guy.dir = guy.intendedDir;
			return true;
		}
		return move(guy, guy.dir);
	}

	private boolean move(Creature guy, V2 dir) {

		// portal
		if (guy.tile.equals(new V2(28, 17)) && dir.equals(V2.RIGHT)) {
			guy.tile = new V2(-1, 17);
			guy.offset = V2.NULL;
			return true;
		}
		if (guy.tile.equals(new V2(-1, 17)) && dir.equals(V2.LEFT)) {
			guy.tile = new V2(28, 17);
			guy.offset = V2.NULL;
			return true;
		}

		// turns
		if (!world.isInsideGhostHouse(guy.tile) && canAccessTile(guy, guy.tile.sum(dir))) {
			if (dir.equals(V2.LEFT) || dir.equals(V2.RIGHT)) {
				if (Math.abs(guy.offset.y) > 1) {
					return false;
				}
				guy.offset = new V2(guy.offset.x, 0);
			}
			if (dir.equals(V2.UP) || dir.equals(V2.DOWN)) {
				if (Math.abs(guy.offset.x) > 1) {
					return false;
				}
				guy.offset = new V2(0, guy.offset.y);
			}
		}

		V2 velocity = dir.scaled(1.25f * guy.speed); // 100% speed corresponds to 1.25 pixels/tick
		V2 positionAfterMove = world.position(guy).sum(velocity);
		V2 tileAfterMove = world.tile(positionAfterMove);

		if (!canAccessTile(guy, tileAfterMove)) {
			return false;
		}

		V2 offsetAfterMove = world.offset(positionAfterMove, tileAfterMove);

		// avoid moving partially into inaccessible tile
		if (tileAfterMove.equals(guy.tile)) {
			if (!canAccessTile(guy, guy.tile.sum(dir))) {
				if (dir.equals(V2.RIGHT) && offsetAfterMove.x > 0 || dir.equals(V2.LEFT) && offsetAfterMove.x < 0) {
					guy.offset = new V2(0, guy.offset.y);
					return false;
				}
				if (dir.equals(V2.DOWN) && offsetAfterMove.y > 0 || dir.equals(V2.UP) && offsetAfterMove.y < 0) {
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

	public boolean canAccessTile(Creature guy, V2 tile) {
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

	public V2 randomAccessibleDir(Creature guy) {
		List<V2> dirs = new ArrayList<>(3);
		for (V2 dir : List.of(V2.DOWN, V2.LEFT, V2.RIGHT, V2.UP)) {
			if (dir.equals(guy.dir.inverse())) {
				continue;
			}
			if (world.isAccessibleTile(guy.tile.sum(dir))) {
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