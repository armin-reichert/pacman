package de.amr.games.pacman;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class PacManGame {

	public static void main(String[] args) {
		EventQueue.invokeLater(new PacManGame()::start);
	}

	static V2 vec(float x, float y) {
		return new V2(x, y);
	}

	static final int FPS = 60;
	static final int TILE_SIZE = 8;
	static final int WORLD_WIDTH_TILES = 28;
	static final int WORLD_HEIGHT_TILES = 36;
	static final int WORLD_WIDTH = WORLD_WIDTH_TILES * TILE_SIZE;
	static final int WORLD_HEIGHT = WORLD_HEIGHT_TILES * TILE_SIZE;

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

	private Creature[] creatures = new Creature[5];
	private Creature pacMan, blinky, inky, pinky, clyde;
	private Canvas canvas;
	private BufferedImage imageMaze;
	private float scaling;

	public PacManGame() {
		scaling = 2;
	}

	private void createUI() {
		JFrame window = new JFrame("PacMan");
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new Canvas();
		canvas.setSize((int) (WORLD_WIDTH * scaling), (int) (WORLD_HEIGHT * scaling));

		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (KeyEvent.VK_LEFT == key) {
					pacMan.intendedDir = V2.LEFT;
				}
				if (KeyEvent.VK_RIGHT == key) {
					pacMan.intendedDir = V2.RIGHT;
				}
				if (KeyEvent.VK_UP == key) {
					pacMan.intendedDir = V2.UP;
				}
				if (KeyEvent.VK_DOWN == key) {
					pacMan.intendedDir = V2.DOWN;
				}
			}
		});

		window.add(canvas);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();

		canvas.createBufferStrategy(2);
	}

	private void loadResources() {
		try {
			imageMaze = ImageIO.read(getClass().getResourceAsStream("/maze_full.png"));
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	private void start() {
		loadResources();
		createEntities();
		initEntities();
		createUI();
		new Thread(this::gameLoop, "GameLoop").start();
	}

	private void gameLoop() {
		long start = 0;
		long frames = 0;
		while (true) {
			long time = System.nanoTime();
			update();
			render();
			time = System.nanoTime() - time;
			++frames;
			if (System.nanoTime() - start >= 1_000_000_000) {
				log("Time: %-18s %3d frames/sec", LocalTime.now(), frames);
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

	private void render() {
		BufferStrategy strategy = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				drawMaze(g);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				drawPacMan(g);
				drawGhosts(g);
				g.dispose();
			} while (strategy.contentsRestored());
			strategy.show();
		} while (strategy.contentsLost());
	}

	private void log(String msg, Object... args) {
		System.err.println(String.format(msg, args));
	}

	private void createEntities() {
		pacMan = new Creature("Pac-Man");
		pacMan.color = Color.YELLOW;
		pacMan.homeTile = vec(13, 26);

		blinky = new Creature("Blinky");
		blinky.color = Color.RED;
		blinky.homeTile = vec(13, 14);
		blinky.scatterTile = vec(WORLD_WIDTH_TILES - 3, 0);

		inky = new Creature("Inky");
		inky.color = Color.CYAN;
		inky.homeTile = vec(11, 17);
		inky.scatterTile = vec(WORLD_WIDTH_TILES - 1, WORLD_HEIGHT_TILES - 1);

		pinky = new Creature("Pinky");
		pinky.color = Color.PINK;
		pinky.homeTile = vec(13, 17);
		pinky.scatterTile = vec(2, 0);

		clyde = new Creature("Clyde");
		clyde.color = Color.ORANGE;
		clyde.homeTile = vec(15, 17);
		clyde.scatterTile = vec(0, WORLD_HEIGHT_TILES - 1);

		creatures[0] = pacMan;
		creatures[1] = blinky;
		creatures[2] = inky;
		creatures[3] = pinky;
		creatures[4] = clyde;
	}

	private void initEntities() {
		placeAtHomeTile(pacMan);
		pacMan.dir = pacMan.intendedDir = V2.RIGHT;
		pacMan.speed = 1.25f;

		for (Creature ghost : List.of(blinky, inky, pinky, clyde)) {
			placeAtHomeTile(ghost);
			ghost.dir = ghost.intendedDir = V2.RIGHT;
			ghost.speed = 0;
		}
	}

	private void update() {
		blinky.targetTile = pacMan.tile;
		pinky.targetTile = pacMan.tile.sum(pacMan.dir.scaled(4));
		if (pacMan.dir.equals(V2.UP)) {
			pinky.targetTile.add(V2.LEFT.scaled(4));
		}
		inky.targetTile = pacMan.tile.sum(pacMan.dir.scaled(2)).scaled(2).sum(blinky.tile.scaled(-1));
		float dx = clyde.tile.x - pacMan.tile.x;
		float dy = clyde.tile.y - pacMan.tile.y;
		if (dx * dx + dy * dy > 64) {
			clyde.targetTile = pacMan.tile;
		} else {
			clyde.targetTile = clyde.scatterTile;
		}
		for (Creature guy : creatures) {
			moveCreature(guy);
		}
	}

	private void moveCreature(Creature guy) {
		if (guy.speed == 0) {
			return;
		}
		if (moveCreature(guy, guy.intendedDir)) {
			guy.dir = guy.intendedDir;
		} else {
			moveCreature(guy, guy.dir);
		}
	}

	private boolean moveCreature(Creature guy, V2 direction) {

		if (direction.equals(V2.LEFT) || direction.equals(V2.RIGHT)) {
			if (Math.abs(guy.offset.y) > 1) {
				return false;
			}
			guy.offset.y = 0;
		}
		if (direction.equals(V2.UP) || direction.equals(V2.DOWN)) {
			if (Math.abs(guy.offset.x) > 1f) {
				return false;
			}
			guy.offset.x = 0;
		}

		V2 positionAfterMove = position(guy);
		positionAfterMove.add(direction.scaled(guy.speed));
		V2 tileAfterMove = tile(positionAfterMove);

		if (!isAccessibleTile(tileAfterMove)) {
			return false;
		}

		V2 offsetAfterMove = offset(positionAfterMove, tileAfterMove);
		if (tileAfterMove.equals(guy.tile)) {
			V2 neighbor = guy.tile.sum(direction);
			if (!isAccessibleTile(neighbor)) {
				if (direction.equals(V2.RIGHT) && offsetAfterMove.x > 0 || direction.equals(V2.LEFT) && offsetAfterMove.x < 0) {
					guy.offset.x = 0;
					return false;
				}
				if (direction.equals(V2.DOWN) && offsetAfterMove.y > 0 || direction.equals(V2.UP) && offsetAfterMove.y < 0) {
					guy.offset.y = 0;
					return false;
				}
			}
		}
		guy.tile = tileAfterMove;
		guy.offset = offsetAfterMove;
		return true;
	}

	private void placeAtTile(Creature guy, V2 tile, V2 offset) {
		guy.tile = tile;
		guy.offset = offset;
	}

	private void placeAtHomeTile(Creature guy) {
		placeAtTile(guy, guy.homeTile, vec(TILE_SIZE / 2, 0));
	}

	private V2 tile(V2 position) {
		return vec((int) (position.x + TILE_SIZE / 2) / TILE_SIZE, (int) (position.y + TILE_SIZE / 2) / TILE_SIZE);
	}

	private V2 offset(V2 position, V2 tile) {
		return vec(position.x - tile.x * TILE_SIZE, position.y - tile.y * TILE_SIZE);
	}

	private V2 position(Creature guy) {
		return vec(guy.tile.x * TILE_SIZE + guy.offset.x, guy.tile.y * TILE_SIZE + guy.offset.y);
	}

	private boolean isAccessibleTile(V2 tile) {
		if (tile.x < 0 || tile.x >= WORLD_WIDTH_TILES) {
			return false;
		}
		if (tile.y < 0 || tile.y >= WORLD_HEIGHT_TILES) {
			return false;
		}
		return MAP[(int) tile.y].charAt((int) tile.x) != '1';
	}

	private void drawPacMan(Graphics2D g) {
		V2 position = position(pacMan);
		g.setColor(pacMan.color);
		g.fillRect((int) position.x, (int) position.y, (int) pacMan.size.x, (int) pacMan.size.y);
	}

	private void drawGhost(Graphics2D g, Creature ghost) {
		V2 position = position(ghost);
		g.setColor(ghost.color);
		g.fillRect((int) position.x, (int) position.y, (int) ghost.size.x, (int) ghost.size.y);
		g.fillRect((int) ghost.scatterTile.x * TILE_SIZE, (int) ghost.scatterTile.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		g.fillRect((int) ghost.targetTile.x * TILE_SIZE + TILE_SIZE / 4,
				(int) ghost.targetTile.y * TILE_SIZE + TILE_SIZE / 4, TILE_SIZE / 2, TILE_SIZE / 2);
	}

	private void drawGhosts(Graphics2D g) {
		for (Creature ghost : List.of(blinky, inky, pinky, clyde)) {
			drawGhost(g, ghost);
		}
	}

	private void drawMaze(Graphics2D g) {
		g.drawImage(imageMaze, 0, 3 * TILE_SIZE, null);
		g.setColor(new Color(200, 200, 200, 100));
		g.setStroke(new BasicStroke(0.1f));
		for (int row = 1; row < WORLD_HEIGHT_TILES; ++row) {
			g.drawLine(0, row * TILE_SIZE, WORLD_WIDTH, row * TILE_SIZE);
		}
		for (int col = 1; col < WORLD_WIDTH_TILES; ++col) {
			g.drawLine(col * TILE_SIZE, 0, col * TILE_SIZE, WORLD_HEIGHT);
		}
	}
}