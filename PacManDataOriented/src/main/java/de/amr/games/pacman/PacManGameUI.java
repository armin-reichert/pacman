package de.amr.games.pacman;

import static de.amr.games.pacman.PacManGame.TILE_SIZE;
import static de.amr.games.pacman.PacManGame.WORLD_HEIGHT;
import static de.amr.games.pacman.PacManGame.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.PacManGame.WORLD_WIDTH;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class PacManGameUI {

	public boolean debugDraw;
	public float scaling = 2;
	public Canvas canvas;
	public BufferedImage imageMaze;
	public BufferedImage spriteSheet;

	public PacManGameUI(PacManGame game) {

		loadResources();

		JFrame window = new JFrame("PacMan");
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				game.pressedKeys.set(e.getKeyCode());
			}
		});

		canvas = new Canvas();
		canvas.setSize((int) (PacManGame.WORLD_WIDTH * scaling), (int) (PacManGame.WORLD_HEIGHT * scaling));
		canvas.setFocusable(false);

		window.add(canvas);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();

		canvas.createBufferStrategy(2);
	}

	public void loadResources() {
		try {
			spriteSheet = ImageIO.read(getClass().getResourceAsStream("/sprites.png"));
			imageMaze = ImageIO.read(getClass().getResourceAsStream("/maze_full.png"));
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	public void render(PacManGame game) {
		BufferStrategy strategy = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				draw(game, g);
				g.dispose();
			} while (strategy.contentsRestored());
			strategy.show();
		} while (strategy.contentsLost());
	}

	private void draw(PacManGame game, Graphics2D g) {
		drawMaze(g, game);
		drawPacMan(g, game, game.pacMan);
		for (int i = 0; i < game.ghosts.length; ++i) {
			drawGhost(g, game, i);
		}
	}

	private void drawMaze(Graphics2D g, PacManGame game) {
		g = (Graphics2D) g.create();
		g.drawImage(imageMaze, 0, 3 * TILE_SIZE, null);
		if (debugDraw) {
			g.setColor(new Color(200, 200, 200, 100));
			g.setStroke(new BasicStroke(0.1f));
			for (int row = 1; row < WORLD_HEIGHT_TILES; ++row) {
				g.drawLine(0, row * TILE_SIZE, WORLD_WIDTH, row * TILE_SIZE);
			}
			for (int col = 1; col < PacManGame.WORLD_WIDTH_TILES; ++col) {
				g.drawLine(col * TILE_SIZE, 0, col * TILE_SIZE, WORLD_HEIGHT);
			}
			g.setColor(Color.WHITE);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(String.format("%d frames/sec", game.fps), 20, 20);
		}
		g.dispose();
	}

	private void drawPacMan(Graphics2D g, PacManGame game, Creature pacMan) {
		int dirIndex = dirIndex(pacMan.dir);
		long interval = game.framesTotal % 30;
		int frame = (int) interval / 10;
		BufferedImage sprite;
		if (frame < 2) {
			sprite = spriteSheet.getSubimage(frame * 16, dirIndex * 16, 16, 16);
		} else {
			sprite = spriteSheet.getSubimage(2 * 16, 0, 16, 16);
		}
		V2 position = game.position(pacMan);
		g.drawImage(sprite, (int) position.x - 4, (int) position.y - 4, null);
//	g.fillRect((int) position.x, (int) position.y, (int) pacMan.size.x, (int) pacMan.size.y);
	}

	private void drawGhost(Graphics2D g, PacManGame game, int ghostIndex) {
		Creature ghost = game.ghosts[ghostIndex];
		int dirIndex = dirIndex(ghost.dir);
		int frame = game.framesTotal % 60 < 30 ? 0 : 1;
		BufferedImage sprite = spriteSheet.getSubimage((2 * dirIndex + frame) * 16, (4 + ghostIndex) * 16, 16, 16);
		V2 position = game.position(ghost);
		g.setColor(ghost.color);
		g.drawImage(sprite, (int) position.x - 4, (int) position.y - 4, null);
		if (debugDraw) {
//		g.fillRect((int) position.x, (int) position.y, (int) ghost.size.x, (int) ghost.size.y);
			g.drawRect((int) ghost.scatterTile.x * TILE_SIZE, (int) ghost.scatterTile.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
			g.fillRect((int) ghost.targetTile.x * TILE_SIZE + TILE_SIZE / 4,
					(int) ghost.targetTile.y * TILE_SIZE + TILE_SIZE / 4, TILE_SIZE / 2, TILE_SIZE / 2);
		}
	}

	private int dirIndex(V2 dir) {
		if (V2.RIGHT.equals(dir)) {
			return 0;
		}
		if (V2.LEFT.equals(dir)) {
			return 1;
		}
		if (V2.UP.equals(dir)) {
			return 2;
		}
		if (V2.DOWN.equals(dir)) {
			return 3;
		}
		return 0;
	}
}