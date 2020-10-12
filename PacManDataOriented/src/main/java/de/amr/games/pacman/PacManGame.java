package de.amr.games.pacman;

import static de.amr.games.pacman.V2.distance;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/**
 * My attempt at writing a minimal Pac-Man game with faithful behavior.
 * 
 * @author Armin Reichert
 */
public class PacManGame {

	public static void main(String[] args) {
		EventQueue.invokeLater(new PacManGame()::start);
	}

	public enum GameState {
		SCATTERING, CHASING, CHANGING_LEVEL;
	}

	public static void log(String msg, Object... args) {
		System.err.println(String.format(msg, args));
	}

	public static V2 vec(float x, float y) {
		return new V2(x, y);
	}

	public static V2 tile(V2 position) {
		return vec((int) (position.x + HTS) / TS, (int) (position.y + HTS) / TS);
	}

	public static V2 offset(V2 position, V2 tile) {
		return vec(position.x - tile.x * TS, position.y - tile.y * TS);
	}

	public static V2 position(Creature guy) {
		return vec(guy.tile.x * TS + guy.offset.x, guy.tile.y * TS + guy.offset.y);
	}

	public static int sec(float seconds) {
		return (int) (seconds * FPS);
	}

	private static int index(int x, int y) {
		return y * WORLD_WIDTH_TILES + x;
	}

	public static final int FPS = 60;
	public static final int TS = 8;
	public static final int HTS = TS / 2;
	public static final int WORLD_WIDTH_TILES = 28;
	public static final int WORLD_HEIGHT_TILES = 36;
	public static final int WORLD_WIDTH = WORLD_WIDTH_TILES * TS;
	public static final int WORLD_HEIGHT = WORLD_HEIGHT_TILES * TS;

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
		if (level == 1) {
			return 0;
		}
		if (level < 5) {
			return 1;
		}
		return 2;
	}

	static final String[] MAP = {
		//@formatter:off
		"1111111111111111111111111111",
		"1111111111111111111111111111",
		"1111111111111111111111111111",
		"1111111111111111111111111111",
		"1222222222222112222222222221",
		"1211112111112112111112111121",
		"1211112111112112111112111121",
		"1211112111112112111112111121",
		"1222222222222222222222222221",
		"1211112112111111112112111121",
		"1211112112111111112112111121",
		"1222222112222112222112222221",
		"1111112111110110111112111111",
		"1111112111110110111112111111",
		"1111112110000000000112111111",
		"1111112110111001110112111111",
		"1111112110100000010112111111",
		"0000002000100000010002000000",
		"1111112110100000010112111111",
		"1111112110111111110112111111",
		"1111112110000000000112111111",
		"1111112110111111110112111111",
		"1111112110111111110112111111",
		"1222222222222112222222222221",
		"1211112111112112111112111121",
		"1211112111112112111112111121",
		"1222112222222002222222112221",
		"1112112112111111112112112111",
		"1112112112111111112112112111",
		"1222222112222112222112222221",
		"1211111111112112111111111121",
		"1211111111112112111111111121",
		"1222222222222222222222222221",
		"1111111111111111111111111111",
		"1111111111111111111111111111",
		"1111111111111111111111111111",
		//@formatter:on
	};

	public GameState state;
	public BitSet pressedKeys = new BitSet(256);
	public Creature[] ghosts = new Creature[4];
	public Creature pacMan, blinky, inky, pinky, clyde;
	public PacManGameUI ui;
	public long fps;
	public long framesTotal;
	public BitSet food = new BitSet(244);
	public BitSet eaten = new BitSet(244);
	public int level;
	public int attackWave;
	public int foodRemaining;
	public int points;
	public long pacManPowerTimer;
	public long scatteringTimer;
	public long chasingTimer;
	public long levelChangeTimer;

	private void gameLoop() {
		long start = 0;
		long frames = 0;
		while (true) {
			long time = System.nanoTime();
			update();
			ui.render(this);
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

	private void start() {
		createEntities();
		initEntities();
		ui = new PacManGameUI(this, 2);
		initGame();
		new Thread(this::gameLoop, "GameLoop").start();
	}

	private void createEntities() {
		pacMan = new Creature("Pac-Man", Color.YELLOW);
		pacMan.homeTile = vec(13, 26);

		ghosts[0] = blinky = new Creature("Blinky", Color.RED);
		blinky.homeTile = vec(13, 14);
		blinky.scatterTile = vec(WORLD_WIDTH_TILES - 3, 0);

		ghosts[1] = pinky = new Creature("Pinky", Color.PINK);
//		pinky.homeTile = vec(13, 17);
		pinky.homeTile = vec(13, 14);
		pinky.scatterTile = vec(2, 0);

		ghosts[2] = inky = new Creature("Inky", Color.CYAN);
//		inky.homeTile = vec(11, 17);
		inky.homeTile = vec(13, 14);
		inky.scatterTile = vec(WORLD_WIDTH_TILES - 1, WORLD_HEIGHT_TILES - 1);

		ghosts[3] = clyde = new Creature("Clyde", Color.ORANGE);
//		clyde.homeTile = vec(15, 17);
		clyde.homeTile = vec(13, 14);
		clyde.scatterTile = vec(0, WORLD_HEIGHT_TILES - 1);
	}

	private void initEntities() {
		placeAtHomeTile(pacMan);
		pacMan.dir = pacMan.intendedDir = V2.RIGHT;
		pacMan.speed = 1.25f;
		pacMan.stuck = false;

		blinky.dir = blinky.intendedDir = V2.LEFT;
		inky.dir = inky.intendedDir = V2.UP;
		pinky.dir = pinky.intendedDir = V2.DOWN;
		clyde.dir = clyde.intendedDir = V2.UP;
		for (int i = 0; i < ghosts.length; ++i) {
			Creature ghost = ghosts[i];
			placeAtHomeTile(ghost);
			ghost.speed = 0;
			ghost.tileChanged = true;
			ghost.stuck = false;
			ghost.forceTurnBack = false;
		}
	}

	private void initGame() {
		points = 0;
		initLevel(1);
	}

	private void initLevel(int n) {
		level = n;
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				char c = map(x, y);
				if (c == '2') {
					food.set(index(x, y));
				}
			}
		}
		eaten.clear();
		foodRemaining = 244;
		pacManPowerTimer = 0;
		chasingTimer = 0;
		levelChangeTimer = 0;
		attackWave = 0;
		initEntities();
		enterScatteringState();
	}

	private void readInput() {
		if (pressedKeys.get(KeyEvent.VK_LEFT)) {
			pacMan.intendedDir = V2.LEFT;
		} else if (pressedKeys.get(KeyEvent.VK_RIGHT)) {
			pacMan.intendedDir = V2.RIGHT;
		} else if (pressedKeys.get(KeyEvent.VK_UP)) {
			pacMan.intendedDir = V2.UP;
		} else if (pressedKeys.get(KeyEvent.VK_DOWN)) {
			pacMan.intendedDir = V2.DOWN;
		} else if (pressedKeys.get(KeyEvent.VK_D)) {
			ui.debugDraw = !ui.debugDraw;
		} else if (pressedKeys.get(KeyEvent.VK_C)) {
			state = GameState.CHASING;
			forceGhostsTurnBack();
		} else if (pressedKeys.get(KeyEvent.VK_S)) {
			state = GameState.SCATTERING;
			forceGhostsTurnBack();
		}
	}

	private void update() {
		readInput();
		updateGame();
	}

	private void updateGame() {
		if (state == GameState.CHASING) {
			updateGuys();
			if (foodRemaining == 0) {
				enterChangingLevelState();
			}
			if (chasingTimer == 0) {
				++attackWave;
				enterScatteringState();
			} else {
				if (pacManPowerTimer == 0) {
					--chasingTimer;
				}
			}
		} else if (state == GameState.SCATTERING) {
			updateGuys();
			if (foodRemaining == 0) {
				enterChangingLevelState();
			}
			if (scatteringTimer == 0) {
				enterChasingState();
			} else {
				if (pacManPowerTimer == 0) {
					--scatteringTimer;
				}
			}
		} else if (state == GameState.CHANGING_LEVEL) {
			if (levelChangeTimer == 0) {
				exitChangingLevelState();
				enterScatteringState();
			} else {
				--levelChangeTimer;
			}
		}
	}

	private void enterScatteringState() {
		state = GameState.SCATTERING;
		scatteringTimer = SCATTERING_TIMES[attackWaveIndex(level)][attackWave];
		forceGhostsTurnBack();
	}

	private void enterChasingState() {
		state = GameState.CHASING;
		chasingTimer = CHASING_TIMES[attackWaveIndex(level)][attackWave];
		forceGhostsTurnBack();
	}

	private void enterChangingLevelState() {
		state = GameState.CHANGING_LEVEL;
		levelChangeTimer = sec(3);
	}

	private void exitChangingLevelState() {
		initLevel(++level);
	}

	private void updateGuys() {
		updatePacMan();
		updateBlinky();
		updatePinky();
		updateInky();
		updateClyde();
	}

	private void updatePacMan() {
		pacMan.stuck = !move(pacMan);
		int x = (int) pacMan.tile.x, y = (int) pacMan.tile.y;
		if (hasUneatenFood(x, y)) {
			eaten.set(index(x, y));
			foodRemaining--;
			points += 10;
			if (isEnergizerTile(pacMan.tile)) {
				points += 40;
				pacManPowerTimer = sec(5);
				forceGhostsTurnBack();
			}
		}
		pacManPowerTimer = Math.max(0, pacManPowerTimer - 1);
	}

	private void updateBlinky() {
		if (state == GameState.SCATTERING) {
			blinky.targetTile = blinky.scatterTile;
		} else if (state == GameState.CHASING) {
			blinky.targetTile = pacMan.tile;
		}
		updateGhostDirection(blinky);
		updateGhostSpeed(blinky);
		blinky.stuck = !move(blinky);
	}

	private void updatePinky() {
		if (state == GameState.SCATTERING) {
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
		if (state == GameState.SCATTERING) {
			inky.targetTile = inky.scatterTile;
		} else if (state == GameState.CHASING) {
			inky.targetTile = pacMan.tile.sum(pacMan.dir.scaled(2)).scaled(2).sum(blinky.tile.inverse());
		}
		updateGhostDirection(inky);
		updateGhostSpeed(inky);
		inky.stuck = !move(inky);
	}

	private void updateClyde() {
		if (state == GameState.SCATTERING) {
			clyde.targetTile = clyde.scatterTile;
		} else if (state == GameState.CHASING) {
			clyde.targetTile = distance(clyde.tile, pacMan.tile) > 8 ? pacMan.tile : clyde.scatterTile;
		}
		updateGhostDirection(clyde);
		updateGhostSpeed(clyde);
		clyde.stuck = !move(clyde);
	}

	private void updateGhostSpeed(Creature ghost) {
		if (isInsideTunnel(ghost.tile)) {
			ghost.speed = 0.5f;
		} else if (pacManPowerTimer > 0) {
			ghost.speed = 0.6f;
		} else {
			ghost.speed = 0.8f;
		}
	}

	private void updateGhostDirection(Creature ghost) {
		if (!ghost.tileChanged) {
			return;
		}
		if (isPortalTile(ghost.tile)) {
			return;
		}
		if (ghost.forceTurnBack) {
			ghost.intendedDir = ghost.intendedDir.inverse();
			ghost.forceTurnBack = false;
			return;
		}
		if (pacManPowerTimer > 0 && isIntersectionTile(ghost.tile)) {
			ghost.intendedDir = randomAccessibleDir(ghost);
			return;
		}
		V2 newDir = null;
		double min = Double.MAX_VALUE;
		for (V2 dir : List.of(V2.RIGHT, V2.DOWN, V2.LEFT, V2.UP)) {
			if (dir.equals(ghost.dir.inverse())) {
				continue;
			}
			if (dir.equals(V2.UP) && isUpwardsBlocked(ghost.tile.sum(V2.UP))) {
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
		if (guy.tile.equals(vec(28, 17)) && dir.equals(V2.RIGHT)) {
			placeAtTile(guy, vec(-1, 17), V2.NULL);
			return true;
		}
		if (guy.tile.equals(vec(-1, 17)) && dir.equals(V2.LEFT)) {
			placeAtTile(guy, vec(28, 17), V2.NULL);
			return true;
		}
		if (!isInsideGhostHouse(guy.tile)) {
			if (dir.equals(V2.LEFT) || dir.equals(V2.RIGHT)) {
				if (Math.abs(guy.offset.y) > 1) {
					return false;
				}
				guy.offset = vec(guy.offset.x, 0);
			}
			if (dir.equals(V2.UP) || dir.equals(V2.DOWN)) {
				if (Math.abs(guy.offset.x) > 1f) {
					return false;
				}
				guy.offset = vec(0, guy.offset.y);
			}
		}

		V2 positionAfterMove = position(guy).sum(dir.scaled(guy.speed));
		V2 tileAfterMove = tile(positionAfterMove);

		if (!canAccessTile(guy, tileAfterMove)) {
			return false;
		}

		V2 offsetAfterMove = offset(positionAfterMove, tileAfterMove);
		if (tileAfterMove.equals(guy.tile)) {
			V2 neighbor = guy.tile.sum(dir);
			if (!canAccessTile(guy, neighbor)) {
				if (dir.equals(V2.RIGHT) && offsetAfterMove.x > 0 || dir.equals(V2.LEFT) && offsetAfterMove.x < 0) {
					guy.offset = vec(0, guy.offset.y);
					return false;
				}
				if (dir.equals(V2.DOWN) && offsetAfterMove.y > 0 || dir.equals(V2.UP) && offsetAfterMove.y < 0) {
					guy.offset = vec(guy.offset.x, 0);
					return false;
				}
			}
		}
		guy.tileChanged = !guy.tile.equals(tileAfterMove);
		guy.tile = tileAfterMove;
		guy.offset = offsetAfterMove;
		return true;
	}

	private void placeAtTile(Creature guy, V2 tile, V2 offset) {
		guy.tile = tile;
		guy.offset = offset;
	}

	private void placeAtHomeTile(Creature guy) {
		placeAtTile(guy, guy.homeTile, vec(HTS, 0));
	}

	private char map(int x, int y) {
		return MAP[y].charAt(x);
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
		if (isGhostHouseDoor(tile)) {
			return false; // TODO ghost can access door when leaving or entering ghosthouse
		}
		return map((int) tile.x, (int) tile.y) != '1';
	}

	public boolean isGhostHouseDoor(V2 tile) {
		return tile.y == 15 && (tile.x == 13 || tile.x == 14);
	}

	public boolean isInsideGhostHouse(V2 tile) {
		return tile.x >= 10 && tile.x <= 17 && tile.y >= 15 && tile.y <= 22;
	}

	public boolean isInsideTunnel(V2 tile) {
		return tile.y == 17 && (tile.x <= 5 || tile.x >= 21);
	}

	private boolean isUpwardsBlocked(V2 tile) {
		//@formatter:off
		return tile.x == 12 && tile.y == 13
		  	|| tile.x == 15 && tile.y == 13
			  || tile.x == 12 && tile.y == 25
			  || tile.x == 15 && tile.y == 25;
		//@formatter:on
	}

	public boolean isFoodTile(int x, int y) {
		return food.get(index(x, y));
	}

	public boolean hasEatenFood(int x, int y) {
		return isFoodTile(x, y) && eaten.get(index(x, y));
	}

	public boolean hasUneatenFood(int x, int y) {
		return isFoodTile(x, y) && !hasEatenFood(x, y);
	}

	public boolean isEnergizerTile(V2 tile) {
		//@formatter:off
		return tile.x == 1  && tile.y == 6
		  	|| tile.x == 26 && tile.y == 6
			  || tile.x == 1  && tile.y == 26
			  || tile.x == 26 && tile.y == 26;
		//@formatter:on
	}

	public boolean isIntersectionTile(V2 tile) {
		int accessibleNeighbors = 0;
		for (V2 dir : List.of(V2.DOWN, V2.LEFT, V2.RIGHT, V2.UP)) {
			V2 neighbor = tile.sum(dir);
			if (isAccessibleTile(neighbor)) {
				++accessibleNeighbors;
			}
		}
		return accessibleNeighbors >= 3;
	}

	public V2 randomAccessibleDir(Creature guy) {
		List<V2> dirs = new ArrayList<>(3);
		for (V2 dir : List.of(V2.DOWN, V2.LEFT, V2.RIGHT, V2.UP)) {
			if (dir.equals(guy.dir.inverse())) {
				continue;
			}
			if (isAccessibleTile(guy.tile.sum(dir))) {
				dirs.add(dir);
			}
		}
		Collections.shuffle(dirs);
		return dirs.get(0);
	}

	public boolean isAccessibleTile(V2 tile) {
		if (isPortalTile(tile)) {
			return true;
		}
		int x = (int) tile.x, y = (int) tile.y;
		if (x >= 0 && x < WORLD_WIDTH_TILES && y > 0 && y < WORLD_HEIGHT_TILES) {
			return false;
		}
		return map(x, y) != '1';
	}

	public boolean isPortalTile(V2 tile) {
		return tile.equals(vec(28, 17)) || tile.equals(vec(-1, 17));
	}
}