package de.amr.games.pacman;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
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

	private static final int FPS = 60;
	private static final int TILE_SIZE = 8;
	private static final int WORLD_WIDTH_TILES = 28;
	private static final int WORLD_HEIGHT_TILES = 36;
	private static final int WORLD_WIDTH = WORLD_WIDTH_TILES * TILE_SIZE;
	private static final int WORLD_HEIGHT = WORLD_HEIGHT_TILES * TILE_SIZE;

	private static final String[] MAP = {
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

	private Creature pacMan, blinky, pinky, inky, clyde;
	private Canvas canvas;
	private BufferedImage imageMaze;
	private float scaling = 2;

	public PacManGame() {
		pacMan = new Creature("Pac-Man");
		blinky = new Creature("Blinky");
		pinky = new Creature("Pinky");
		inky = new Creature("Inky");
		clyde = new Creature("Clyde");
	}

	private void render() {
		BufferStrategy strategy = canvas.getBufferStrategy();
		if (strategy == null) {
			canvas.createBufferStrategy(2);
			return;
		}
		do {
			do {
				Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				drawMaze(g);
				drawPacMan(g);
				drawGhosts(g);
				g.dispose();
			} while (strategy.contentsRestored());
			strategy.show();
		} while (strategy.contentsLost());
	}

	private void createUI() {
		try {
			imageMaze = ImageIO.read(getClass().getResourceAsStream("/maze_full.png"));
		} catch (IOException x) {
			x.printStackTrace();
		}
		JFrame window = new JFrame("PacMan");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (KeyEvent.VK_LEFT == key) {
					pacMan.intendedDirection = V2.LEFT;
				}
				if (KeyEvent.VK_RIGHT == key) {
					pacMan.intendedDirection = V2.RIGHT;
				}
				if (KeyEvent.VK_UP == key) {
					pacMan.intendedDirection = V2.UP;
				}
				if (KeyEvent.VK_DOWN == key) {
					pacMan.intendedDirection = V2.DOWN;
				}
			}
		});
		canvas = new Canvas();
		canvas.setSize((int) (WORLD_WIDTH * scaling), (int) (WORLD_HEIGHT * scaling));
		window.add(canvas);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	private void start() {
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
				log("Time: %s FPS: %d", LocalTime.now(), frames);
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

	private void log(String msg, Object... args) {
		System.err.println(String.format(msg, args));
	}

	private void initEntities() {
		pacMan.size = new V2(TILE_SIZE, TILE_SIZE);
		pacMan.direction = V2.RIGHT;
		pacMan.intendedDirection = V2.RIGHT;
		pacMan.speed = 1.25f;
		placeAtTile(pacMan, 13, 26);

		for (Creature ghost : List.of(blinky, inky, pinky, clyde)) {
			ghost.size = new V2(TILE_SIZE, TILE_SIZE);
			ghost.direction = V2.RIGHT;
			ghost.speed = 0;
			placeAtTile(ghost, 13, 15);
		}
	}

	private void update() {
		moveCreature(pacMan);
//		Stream.of(blinky, pinky, inky, clyde).forEach(ghost -> moveCreature(ghost));
	}

	private void moveCreature(Creature guy) {
		if (guy.speed == 0) {
			return;
		}
		if (tryMovingCreature(guy, guy.intendedDirection)) {
			guy.direction = guy.intendedDirection;
		} else {
			tryMovingCreature(guy, guy.direction);
		}
	}

	private boolean tryMovingCreature(Creature guy, V2 direction) {

		if ((direction.equals(V2.LEFT) || direction.equals(V2.RIGHT))) {
			if (Math.abs(guy.offset.y) > 1) {
				return false;
			}
			guy.offset.y = 0;
		}
		if ((direction.equals(V2.UP) || direction.equals(V2.DOWN))) {
			if (Math.abs(guy.offset.x) > 1f) {
				return false;
			}
			guy.offset.x = 0;
		}

		V2 velocity = direction.scaled(guy.speed);
		V2 positionAfterMove = position(guy).sum(velocity);
		V2 tileAfterMove = tile(positionAfterMove);
		V2 offsetAfterMove = offset(positionAfterMove, tileAfterMove);

		if (!isAccessibleTile(tileAfterMove)) {
			return false;
		}

//		log("Attempting to move %s to position %s tile %s offset %s", guy, positionAfterMove, tileAfterMove,
//				offsetAfterMove);

		if (tileAfterMove.equals(guy.tile)) {
			if (direction.equals(V2.RIGHT)) {
				V2 tile_right = new V2(guy.tile.x + 1, guy.tile.y);
				if (offsetAfterMove.x > 0 && !isAccessibleTile(tile_right)) {
					guy.offset.x = 0;
					return false;
				}
			}
			if (direction.equals(V2.LEFT)) {
				V2 tile_left = new V2(guy.tile.x - 1, guy.tile.y);
				if (offsetAfterMove.x < 0 && !isAccessibleTile(tile_left)) {
					guy.offset.x = 0;
					return false;
				}
			}
			if (direction.equals(V2.DOWN)) {
				V2 tile_down = new V2(guy.tile.x, guy.tile.y + 1);
				if (offsetAfterMove.y > 0 && !isAccessibleTile(tile_down)) {
					guy.offset.y = 0;
					return false;
				}
			}
			if (direction.equals(V2.UP)) {
				V2 tile_up = new V2(guy.tile.x, guy.tile.y - 1);
				if (offsetAfterMove.y < 0 && !isAccessibleTile(tile_up)) {
					guy.offset.y = 0;
					return false;
				}
			}
		}
		guy.tile = tileAfterMove;
		guy.offset = offsetAfterMove;
		return true;
	}

	private void placeAtTile(Creature guy, float tile_x, float tile_y) {
		guy.tile = new V2(tile_x, tile_y);
		guy.offset = new V2(0, 0);
	}

	private V2 tile(V2 position) {
		return new V2((int) (position.x + TILE_SIZE / 2) / TILE_SIZE, (int) (position.y + TILE_SIZE / 2) / TILE_SIZE);
	}

	private V2 offset(V2 position, V2 tile) {
		return new V2(position.x - tile.x * TILE_SIZE, position.y - tile.y * TILE_SIZE);
	}

	private V2 position(Creature guy) {
		return new V2(guy.tile.x * TILE_SIZE + guy.offset.x, guy.tile.y * TILE_SIZE + guy.offset.y);
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
		g.setColor(Color.YELLOW);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillRect((int) position.x, (int) position.y, (int) pacMan.size.x, (int) pacMan.size.y);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void drawGhosts(Graphics g) {

	}

	private void drawMaze(Graphics2D g) {
		g.drawImage(imageMaze, 0, 24, null);
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