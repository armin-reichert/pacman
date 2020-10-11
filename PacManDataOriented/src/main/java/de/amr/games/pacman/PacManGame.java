package de.amr.games.pacman;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.util.BitSet;
import java.util.List;

/**
 * My attempt at writing a minimal Pac-Man game with faithful behavior.
 * 
 * @author Armin Reichert
 *
 */
public class PacManGame {

	public static void main(String[] args) {
		EventQueue.invokeLater(new PacManGame()::start);
	}

	public static void log(String msg, Object... args) {
		System.err.println(String.format(msg, args));
	}

	public static V2 vec(float x, float y) {
		return new V2(x, y);
	}

	public static final int FPS = 60;
	public static final int TS = 8;
	public static final int HTS = TS / 2;
	public static final int WORLD_WIDTH_TILES = 28;
	public static final int WORLD_HEIGHT_TILES = 36;
	public static final int WORLD_WIDTH = WORLD_WIDTH_TILES * TS;
	public static final int WORLD_HEIGHT = WORLD_HEIGHT_TILES * TS;

	public static final String[] MAP = {
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

	public BitSet pressedKeys = new BitSet(256);
	public Creature[] ghosts = new Creature[4];
	public Creature pacMan, blinky, inky, pinky, clyde;
	public PacManGameUI ui;
	public long fps;
	public long framesTotal;

	public void start() {
		createEntities();
		initEntities();
		ui = new PacManGameUI(this);
		new Thread(this::gameLoop, "GameLoop").start();
	}

	public void gameLoop() {
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
			try {
				Thread.sleep(sleep / 1_000_000); // millis
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}

	public void createEntities() {
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

	public void initEntities() {
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
		}
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
		}
		pressedKeys.clear();
	}

	public void update() {
		readInput();
		updatePacMan();
		updateBlinky();
		updatePinky();
		updateInky();
		updateClyde();
		for (Creature ghost : ghosts) {
			if (ghost.stuck) {
				log("%s stuck", ghost);
			}
		}
	}

	private void updatePacMan() {
		pacMan.stuck = !move(pacMan);
	}

	private void updateBlinky() {
		blinky.targetTile = pacMan.tile;
		updateGhostDirection(blinky);
		updateGhostSpeed(blinky);
		blinky.stuck = !move(blinky);
	}

	private void updatePinky() {
		pinky.targetTile = pacMan.tile.sum(pacMan.dir.scaled(4));
		if (pacMan.dir.equals(V2.UP)) {
			pinky.targetTile.add(V2.LEFT.scaled(4));
		}
		updateGhostDirection(pinky);
		updateGhostSpeed(pinky);
		pinky.stuck = !move(pinky);
	}

	private void updateInky() {
		inky.targetTile = pacMan.tile.sum(pacMan.dir.scaled(2)).scaled(2).sum(blinky.tile.scaled(-1));
		updateGhostDirection(inky);
		updateGhostSpeed(inky);
		inky.stuck = !move(inky);
	}

	private void updateClyde() {
		float dx = clyde.tile.x - pacMan.tile.x;
		float dy = clyde.tile.y - pacMan.tile.y;
		if (dx * dx + dy * dy > 64) {
			clyde.targetTile = pacMan.tile;
		} else {
			clyde.targetTile = clyde.scatterTile;
		}
		updateGhostDirection(clyde);
		updateGhostSpeed(clyde);
		clyde.stuck = !move(clyde);
	}

	public void updateGhostSpeed(Creature ghost) {
		if (isInsideTunnel(ghost.tile)) {
			ghost.speed = 0.5f;
		} else {
			ghost.speed = 0.8f + (float) Math.random() * 0.3f;
		}
	}

	public void updateGhostDirection(Creature ghost) {
		if (!ghost.tileChanged) {
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

	public boolean move(Creature guy) {
		if (guy.speed == 0) {
			return false;
		}
		if (move(guy, guy.intendedDir)) {
			guy.dir = guy.intendedDir;
			return true;
		}
		return move(guy, guy.dir);
	}

	public boolean move(Creature guy, V2 dir) {
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
				guy.offset.y = 0;
			}
			if (dir.equals(V2.UP) || dir.equals(V2.DOWN)) {
				if (Math.abs(guy.offset.x) > 1f) {
					return false;
				}
				guy.offset.x = 0;
			}
		}

		V2 positionAfterMove = position(guy);
		positionAfterMove.add(dir.scaled(guy.speed));
		V2 tileAfterMove = tile(positionAfterMove);

		if (!canAccessTile(guy, tileAfterMove)) {
			return false;
		}

		V2 offsetAfterMove = offset(positionAfterMove, tileAfterMove);
		if (tileAfterMove.equals(guy.tile)) {
			V2 neighbor = guy.tile.sum(dir);
			if (!canAccessTile(guy, neighbor)) {
				if (dir.equals(V2.RIGHT) && offsetAfterMove.x > 0 || dir.equals(V2.LEFT) && offsetAfterMove.x < 0) {
					guy.offset.x = 0;
					return false;
				}
				if (dir.equals(V2.DOWN) && offsetAfterMove.y > 0 || dir.equals(V2.UP) && offsetAfterMove.y < 0) {
					guy.offset.y = 0;
					return false;
				}
			}
		}
		guy.tileChanged = !guy.tile.equals(tileAfterMove);
		guy.tile = tileAfterMove;
		guy.offset = offsetAfterMove;
		return true;
	}

	public void placeAtTile(Creature guy, V2 tile, V2 offset) {
		guy.tile = tile;
		guy.offset = offset;
	}

	public void placeAtHomeTile(Creature guy) {
		placeAtTile(guy, guy.homeTile, vec(HTS, 0));
	}

	public V2 tile(V2 position) {
		return vec((int) (position.x + HTS) / TS, (int) (position.y + HTS) / TS);
	}

	public V2 offset(V2 position, V2 tile) {
		return vec(position.x - tile.x * TS, position.y - tile.y * TS);
	}

	public V2 position(Creature guy) {
		return vec(guy.tile.x * TS + guy.offset.x, guy.tile.y * TS + guy.offset.y);
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
		return MAP[(int) tile.y].charAt((int) tile.x) != '1';
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

	public boolean isUpwardsBlocked(V2 tile) {
		//@formatter:off
		return tile.x == 12 && tile.y == 13
		  	|| tile.x == 15 && tile.y == 13
			  || tile.x == 12 && tile.y == 25
			  || tile.x == 15 && tile.y == 25;
		//@formatter:on
	}
}