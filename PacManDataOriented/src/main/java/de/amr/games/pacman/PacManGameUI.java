package de.amr.games.pacman;

import static de.amr.games.pacman.PacManGame.levelData;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.TS;
import static de.amr.games.pacman.World.WORLD_HEIGHT;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import de.amr.games.pacman.PacManGame.GameState;

public class PacManGameUI {

	public boolean debugDraw;
	public BitSet pressedKeys = new BitSet(256);

	private final float scaling;
	private final Canvas canvas;

	private BufferedImage imageMaze;
	private BufferedImage spriteSheet;
	private Map<String, BufferedImage> levelSymbols;
	private Font scoreFont;

	public PacManGameUI(PacManGame game, float scaling) {
		this.scaling = scaling;
		loadResources();

		JFrame window = new JFrame("Pac-Man");
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				pressedKeys.set(e.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent e) {
				pressedKeys.clear(e.getKeyCode());
			}
		});

		canvas = new Canvas();
		canvas.setSize((int) (WORLD_WIDTH * scaling), (int) (WORLD_HEIGHT * scaling));
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
			spriteSheet = image("/sprites.png");
			imageMaze = image("/maze_full.png");
			levelSymbols = new HashMap<>();
			levelSymbols.put("CHERRIES", sheet(2, 3));
			levelSymbols.put("STRAWBERRY", sheet(3, 3));
			levelSymbols.put("PEACH", sheet(4, 3));
			levelSymbols.put("APPLE", sheet(5, 3));
			levelSymbols.put("GRAPES", sheet(6, 3));
			levelSymbols.put("GALAXIAN", sheet(7, 3));
			levelSymbols.put("BELL", sheet(8, 3));
			levelSymbols.put("KEY", sheet(9, 3));
			scoreFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/PressStart2P-Regular.ttf"))
					.deriveFont((float) TS);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	private BufferedImage sheet(int x, int y) {
		return spriteSheet.getSubimage(x * 16, y * 16, 16, 16);
	}

	private BufferedImage image(String path) throws IOException {
		return ImageIO.read(getClass().getResourceAsStream(path));
	}

	public void render(PacManGame game) {
		BufferStrategy strategy = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				draw(g, game);
				g.dispose();
			} while (strategy.contentsRestored());
			strategy.show();
		} while (strategy.contentsLost());
	}

	private void draw(Graphics2D g, PacManGame game) {
		drawScore(g, game);
		drawMaze(g, game);
		drawPacMan(g, game);
		for (int i = 0; i < game.ghosts.length; ++i) {
			drawGhost(g, game, i);
		}
		drawLevelCounter(g, game);
	}

	private void drawScore(Graphics2D g, PacManGame game) {
		g.setFont(scoreFont);
		g.setColor(Color.WHITE);
		g.drawString(String.format("SCORE %d", game.points), 16, 16);
	}

	private void drawLevelCounter(Graphics2D g, PacManGame game) {
		int x = WORLD_WIDTH - 4 * TS;
		int y = WORLD_HEIGHT - 2 * TS;
		for (int i = 1; i <= game.level; ++i) {
			BufferedImage symbol = levelSymbols.get(levelData(i).get(0));
			g.drawImage(symbol, x, y, null);
			x -= 2 * TS;
		}
	}

	private void drawTileHidden(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(x * TS, y * TS, TS, TS);
	}

	private void drawMaze(Graphics2D g, PacManGame game) {
		g = (Graphics2D) g.create();
		g.drawImage(imageMaze, 0, 3 * TS, null);
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				V2 tile = new V2(x, y);
				// hide eaten food
				if (game.hasEatenFood(x, y)) {
					drawTileHidden(g, x, y);
					continue;
				}
				// uneaten energizer blinking
				if (game.world.isEnergizerTile(tile) && game.framesTotal % 20 < 10
						&& (game.state == GameState.CHASING || game.state == GameState.SCATTERING)) {
					drawTileHidden(g, x, y);
				}
			}
		}
		if (game.messageText != null) {
			g.setFont(scoreFont);
			g.setColor(Color.YELLOW);
			int textLength = g.getFontMetrics().stringWidth(game.messageText);
			g.drawString(game.messageText, WORLD_WIDTH / 2 - textLength / 2, 21 * TS);
		}
		if (debugDraw) {

//			g.setColor(new Color(200, 200, 200, 100));
//			g.setStroke(new BasicStroke(0.1f));
//			for (int row = 1; row < WORLD_HEIGHT_TILES; ++row) {
//				g.drawLine(0, row * TS, WORLD_WIDTH, row * TS);
//			}
//			for (int col = 1; col < WORLD_WIDTH_TILES; ++col) {
//				g.drawLine(col * TS, 0, col * TS, WORLD_HEIGHT);
//			}

			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			g.drawString(String.format("%d frames/sec", game.fps), 2 * TS, 3 * TS);

			long timer = 0;
			if (game.state == GameState.READY) {
				timer = game.readyStateTimer;
			} else if (game.state == GameState.CHANGING_LEVEL) {
				timer = game.levelChangeStateTimer;
			} else if (game.state == GameState.SCATTERING) {
				timer = game.scatteringStateTimer;
			} else if (game.state == GameState.CHASING) {
				timer = game.chasingStateTimer;
			}
			g.drawString(String.format("%s %d ticks remaining", game.state, timer), 12 * TS, 3 * TS);
		}
		g.dispose();
	}

	private void drawPacMan(Graphics2D g, PacManGame game) {
		Creature pacMan = game.pacMan;
		BufferedImage sprite;
		long interval = game.framesTotal % 15;
		int frame = (int) interval / 5;
		if (game.state == GameState.READY || game.state == GameState.CHANGING_LEVEL) {
			sprite = spriteSheet.getSubimage(2 * 16, 0, 16, 16);
		} else if (pacMan.stuck) {
			sprite = spriteSheet.getSubimage(0, dirIndex(pacMan.dir) * 16, 16, 16);
		} else if (frame == 2) {
			sprite = spriteSheet.getSubimage(2 * 16, 0, 16, 16);
		} else {
			sprite = spriteSheet.getSubimage(frame * 16, dirIndex(pacMan.dir) * 16, 16, 16);
		}
		V2 position = game.world.position(pacMan);
		g.drawImage(sprite, (int) position.x - 4, (int) position.y - 4, null);
//	g.fillRect((int) position.x, (int) position.y, (int) pacMan.size.x, (int) pacMan.size.y);
	}

	private void drawGhost(Graphics2D g, PacManGame game, int ghostIndex) {
		if (game.state == GameState.CHANGING_LEVEL) {
			return;
		}
		Creature ghost = game.ghosts[ghostIndex];
		int dirIndex = dirIndex(ghost.dir);
		int frame = game.framesTotal % 60 < 30 ? 0 : 1;
		BufferedImage sprite;
		if (game.pacManPowerTimer > 0) {
			sprite = spriteSheet.getSubimage((8 + frame) * 16, 4 * 16, 16, 16);
		} else {
			sprite = spriteSheet.getSubimage((2 * dirIndex + frame) * 16, (4 + ghostIndex) * 16, 16, 16);
		}
		V2 position = game.world.position(ghost);
		g.setColor(ghost.color);
		g.drawImage(sprite, (int) position.x - 4, (int) position.y - 4, null);
		if (debugDraw) {
//		g.fillRect((int) position.x, (int) position.y, (int) ghost.size.x, (int) ghost.size.y);
			g.drawRect((int) ghost.scatterTile.x * TS, (int) ghost.scatterTile.y * TS, TS, TS);
			if (ghost.targetTile != null) {
				g.fillRect((int) ghost.targetTile.x * TS + TS / 4, (int) ghost.targetTile.y * TS + TS / 4, HTS, HTS);
			}
		}
	}

	private int dirIndex(V2 dir) {
		if (V2.RIGHT.equals(dir)) {
			return 0;
		} else if (V2.LEFT.equals(dir)) {
			return 1;
		} else if (V2.UP.equals(dir)) {
			return 2;
		} else if (V2.DOWN.equals(dir)) {
			return 3;
		} else {
			return 0; // TODO
		}
	}
}