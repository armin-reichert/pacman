package de.amr.games.pacman;

import static de.amr.games.pacman.PacManGame.levelData;
import static de.amr.games.pacman.PacManGame.log;
import static de.amr.games.pacman.PacManGame.sec;
import static de.amr.games.pacman.World.HTS;
import static de.amr.games.pacman.World.TS;
import static de.amr.games.pacman.World.WORLD_HEIGHT;
import static de.amr.games.pacman.World.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.World.WORLD_WIDTH;
import static de.amr.games.pacman.World.WORLD_WIDTH_TILES;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class PacManGameUI {

	public boolean debugDraw = true;

	private final PacManGame game;
	private final float scaling;
	private final Canvas canvas;
	private final BitSet pressedKeys = new BitSet(256);

	private BufferedImage imageMazeFull;
	private BufferedImage imageMazeEmpty;
	private BufferedImage imageMazeEmptyWhite;
	private BufferedImage spriteSheet;
	private Map<String, BufferedImage> symbols;
	private Map<Integer, BufferedImage> numbers;
	private Map<Integer, BufferedImage> bountyNumbers;
	private Font scoreFont;
	private String messageText;
	private Color messageColor;

	public PacManGameUI(PacManGame game, float scaling) {
		this.game = game;
		this.scaling = scaling;

		messageText = null;
		messageColor = Color.YELLOW;

		try {
			loadResources();
		} catch (IOException | FontFormatException x) {
			x.printStackTrace();
		}

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

	public void render() {
		BufferStrategy strategy = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				drawGame(g);
				g.dispose();
			} while (strategy.contentsRestored());
			strategy.show();
		} while (strategy.contentsLost());
	}

	public void setMessage(String text, boolean red) {
		messageColor = red ? Color.RED : Color.YELLOW;
		messageText = text;
	}

	public boolean keyPressed(int keyCode) {
		return pressedKeys.get(keyCode);
	}

	private void loadResources() throws IOException, FontFormatException {
		spriteSheet = image("/sprites.png");
		imageMazeFull = image("/maze_full.png");
		imageMazeEmpty = image("/maze_empty.png");
		imageMazeEmptyWhite = image("/maze_empty_white.png");
		try (InputStream fontData = getClass().getResourceAsStream("/PressStart2P-Regular.ttf")) {
			scoreFont = Font.createFont(Font.TRUETYPE_FONT, fontData).deriveFont((float) TS);
		}
		//@formatter:off
		symbols = Map.of(
			"Cherries",   sheet(2, 3),
			"Strawberry", sheet(3, 3),
			"Peach",      sheet(4, 3),
			"Apple",      sheet(5, 3),
			"Grapes",     sheet(6, 3),
			"Galaxian",   sheet(7, 3),
			"Bell",       sheet(8, 3),
			"Key",        sheet(9, 3)
		);
		numbers = Map.of(
			100,  sheet(0, 9),
			300,  sheet(1, 9),
			500,  sheet(2, 9),
			700,  sheet(3, 9),
			1000, sheet(4, 9, 2, 1),
			2000, sheet(4, 10, 2, 1),
			3000, sheet(4, 11, 2, 1),
			5000, sheet(4, 12, 2, 1)
		);
		bountyNumbers = Map.of(
			200,  sheet(0, 8),
			400,  sheet(1, 8),
			800,  sheet(2, 8),
			1600, sheet(3, 8)
		);
		//@formatter:on
	}

	private BufferedImage sheet(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	private BufferedImage sheet(int x, int y) {
		return sheet(x, y, 1, 1);
	}

	private BufferedImage image(String path) throws IOException {
		return ImageIO.read(getClass().getResourceAsStream(path));
	}

	private void drawGame(Graphics2D g) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawPacMan(g);
		for (int i = 0; i < 4; ++i) {
			drawGhost(g, i);
		}
		drawDebugInfo(g);
	}

	private void drawDebugInfo(Graphics2D g) {
		if (debugDraw) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			g.drawString(String.format("%d frames/sec", game.fps), 1 * TS, 3 * TS);

			String text = "";
			if (game.state == GameState.READY) {
				text = String.format("%s %d ticks remaining", game.state, game.readyStateTimer);
			} else if (game.state == GameState.CHANGING_LEVEL) {
				text = String.format("%s %d ticks remaining", game.state, game.changingLevelStateTimer);
			} else if (game.state == GameState.SCATTERING) {
				text = String.format("%d. %s %d ticks remaining", game.attackWave + 1, game.state, game.scatteringStateTimer);
			} else if (game.state == GameState.CHASING) {
				text = String.format("%d. %s %d ticks remaining", game.attackWave + 1, game.state, game.chasingStateTimer);
			} else if (game.state == GameState.PACMAN_DYING) {
				text = String.format("%s %d ticks remaining", game.state, game.pacManDyingStateTimer);
			} else if (game.state == GameState.GAME_OVER) {
				text = String.format("%s", game.state);
			}
			g.drawString(text, 8 * TS, 3 * TS);
			for (Ghost ghost : game.ghosts) {
				if (ghost.targetTile != null) {
					g.setColor(color(ghost.character));
					g.fillRect(ghost.targetTile.x * TS + HTS / 2, ghost.targetTile.y * TS + HTS / 2, HTS, HTS);
				}
			}
		}
	}

	private Color color(GhostCharacter character) {
		switch (character) {
		case KIMAGURE:
			return Color.CYAN;
		case OTOBOKE:
			return Color.ORANGE;
		case OIKAKE:
			return Color.RED;
		case MACHIBUSE:
			return Color.PINK;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void drawScore(Graphics2D g) {
		g.setFont(scoreFont);
		g.setColor(Color.WHITE);
		g.drawString(String.format("SCORE %d", game.points), 16, 16);
	}

	private void drawLivesCounter(Graphics2D g) {
		BufferedImage sprite = sheet(8, 1);
		for (int i = 0; i < game.lives; ++i) {
			g.drawImage(sprite, 2 * (i + 1) * TS, WORLD_HEIGHT - 2 * TS, null);
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = WORLD_WIDTH - 4 * TS;
		int first = Math.max(1, game.level - 6);
		for (int level = first; level <= game.level; ++level) {
			BufferedImage symbol = symbols.get(levelData(level).bonusSymbol());
			g.drawImage(symbol, x, WORLD_HEIGHT - 2 * TS, null);
			x -= 2 * TS;
		}
	}

	private void hideTile(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(x * TS, y * TS, TS, TS);
	}

	private void drawMazeFlashing(Graphics2D g) {
		if (game.mazeFlashes > 0 && game.framesTotal % 30 < 15) {
			g.drawImage(imageMazeEmptyWhite, 0, 3 * TS, null);
			if (game.framesTotal % 30 == 14) {
				--game.mazeFlashes;
				log("Maze flashes: %d", game.mazeFlashes);
			}
		} else {
			g.drawImage(imageMazeEmpty, 0, 3 * TS, null);
		}
	}

	private void drawMaze(Graphics2D g) {
		if (game.changingLevelStateTimer > 0 && game.changingLevelStateTimer <= sec(4f)) {
			drawMazeFlashing(g);
			return;
		}
		g.drawImage(imageMazeFull, 0, 3 * TS, null);
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (game.world.hasEatenFood(x, y)) {
					hideTile(g, x, y);
					continue;
				}
				// energizer blinking
				if (game.world.isEnergizerTile(x, y) && game.framesTotal % 20 < 10
						&& (game.state == GameState.CHASING || game.state == GameState.SCATTERING)) {
					hideTile(g, x, y);
				}
			}
		}
		if (game.bonusAvailableTimer > 0) {
			String symbolName = game.levelData().bonusSymbol();
			g.drawImage(symbols.get(symbolName), 13 * TS, 20 * TS - HTS, null);
		}
		if (game.bonusConsumedTimer > 0) {
			int bonusPoints = game.levelData().bonusPoints();
			g.drawImage(numbers.get(bonusPoints), 13 * TS, 20 * TS - HTS, null);
		}
		if (messageText != null) {
			g.setFont(scoreFont);
			g.setColor(messageColor);
			int textLength = g.getFontMetrics().stringWidth(messageText);
			g.drawString(messageText, WORLD_WIDTH / 2 - textLength / 2, 21 * TS);
		}
	}

	private void drawPacMan(Graphics2D g) {
		Creature pacMan = game.pacMan;
		BufferedImage sprite;
		int mouthFrame = (int) game.framesTotal % 15 / 5;
		if (pacMan.dead) {
			// 2 seconds full sprite before collapsing animation starts
			if (game.pacManDyingStateTimer >= sec(2) + 11 * 8) {
				sprite = sheet(2, 0);
			} else if (game.pacManDyingStateTimer >= sec(2)) {
				// collapsing animation
				int frame = (int) (game.pacManDyingStateTimer - sec(2)) / 8;
				sprite = sheet(13 - frame, 0);
			} else {
				// collapsed sprite after collapsing
				sprite = sheet(13, 0);
			}
		} else if (game.state == GameState.READY || game.state == GameState.CHANGING_LEVEL) {
			// full sprite
			sprite = sheet(2, 0);
		} else if (pacMan.stuck) {
			// wide open mouth
			sprite = sheet(0, directionFrame(pacMan.dir));
		} else {
			// closed mouth or open mouth pointing to move direction
			sprite = mouthFrame == 2 ? sheet(mouthFrame, 0) : sheet(mouthFrame, directionFrame(pacMan.dir));
		}
		V2f position = game.world.position(pacMan);
		g.drawImage(sprite, (int) position.x - HTS, (int) position.y - HTS, null);
	}

	private void drawGhost(Graphics2D g, int ghostIndex) {
		BufferedImage sprite;
		Ghost ghost = game.ghosts[ghostIndex];
		if (!ghost.visible) {
			return;
		}
		if (ghost.dead) {
			// number (bounty) or eyes looking into move direction
			sprite = ghost.bountyTimer > 0 ? bountyNumbers.get(ghost.bounty) : sheet(8 + directionFrame(ghost.dir), 5);
		} else if (ghost.frightened) {
			int walkingFrame = game.framesTotal % 60 < 30 ? 0 : 1;
			if (game.pacManPowerTimer < sec(2)) {
				// flashing blue/white, walking
				int flashingFrame = game.framesTotal % 20 < 10 ? 8 : 10;
				sprite = sheet(flashingFrame + walkingFrame, 4);
			} else {
				// blue, walking
				sprite = sheet(8 + walkingFrame, 4);
			}
		} else {
			int walkingFrame = game.framesTotal % 60 < 30 ? 0 : 1;
			sprite = sheet(2 * directionFrame(ghost.dir) + walkingFrame, 4 + ghostIndex);
		}
		V2f position = game.world.position(ghost);
		g.drawImage(sprite, (int) position.x - HTS, (int) position.y - HTS, null);
	}

	private int directionFrame(Direction dir) {
		switch (dir) {
		case RIGHT:
			return 0;
		case LEFT:
			return 1;
		case UP:
			return 2;
		case DOWN:
			return 3;
		default:
			throw new IllegalStateException();
		}
	}
}