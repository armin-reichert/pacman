package de.amr.games.pacman.view.theme.arcade;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.easy.game.assets.Assets.storeTrueTypeFont;
import static de.amr.easy.game.ui.sprites.AnimationType.BACK_AND_FORTH;
import static de.amr.easy.game.ui.sprites.AnimationType.CYCLIC;
import static de.amr.easy.game.ui.sprites.AnimationType.LINEAR;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.arcade.Symbol;

/**
 * Resources for Arcade theme.
 * 
 * @author Armin Reichert
 */
public class ArcadeThemeAssets {

	public final Font messageFont;

	private static final int TILE_SIZE = 16;

	private final BufferedImage spriteSheet = Assets.readImage("images/arcade/sprites.png");
	private final BufferedImage imageMazeEmpty = Assets.readImage("images/arcade/maze_empty.png");
	private final BufferedImage imageMazeEmptyWhite;
	private final BufferedImage imageMazeFull = Assets.readImage("images/arcade/maze_full.png");
	private final BufferedImage imageLogo = Assets.readImage("images/arcade/logo.png");
	private final BufferedImage imagePacManFull;
	private final BufferedImage imagePacManWalking[][];
	private final BufferedImage imagePacManDying[];
	private final BufferedImage imageGhostColored[][];
	private final BufferedImage imageGhostFrightened[];
	private final BufferedImage imageGhostFlashing[];
	private final BufferedImage imageGhostEyes[];
	private final BufferedImage imageGreenNumbers[];
	private final BufferedImage imagePinkNumbers[];
	private final Map<String, BufferedImage> imageMapSymbols = new HashMap<>();

	// in the spritesheet, the order of directions is: RIGHT, LEFT, UP, DOWN
	private int sheetOrder(Direction dir) {
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
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
	}

	private BufferedImage section(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x, y, w, h);
	}

	private BufferedImage tile(int col, int row) {
		return section(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
	}

	private BufferedImage[] tilesHorizontally(int n, int col, int row) {
		return IntStream.range(0, n).mapToObj(i -> tile(col + i, row)).toArray(BufferedImage[]::new);
	}

	private static BufferedImage exchangeColor(BufferedImage img, int oldColorRGB, int newColorRGB) {
		BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g = copy.createGraphics();
		g.drawImage(img, 0, 0, null);
		for (int x = 0; x < copy.getWidth(); ++x) {
			for (int y = 0; y < copy.getHeight(); ++y) {
				if (copy.getRGB(x, y) == oldColorRGB) {
					copy.setRGB(x, y, newColorRGB);
				}
			}
		}
		g.dispose();
		return copy;
	}

	ArcadeThemeAssets() {

		messageFont = storeTrueTypeFont("font.hud", "PressStart2P-Regular.ttf", Font.PLAIN, 8);

		// debugger told me RGB value of blue color in maze image
		imageMazeEmptyWhite = exchangeColor(imageMazeEmpty, -14605825, Color.WHITE.getRGB());

		// Symbols
		BufferedImage[] symbolImages = tilesHorizontally(8, 2, 3);
		for (Symbol symbol : Symbol.values()) {
			imageMapSymbols.put(symbol.name(), symbolImages[symbol.ordinal()]);
		}

		// Pac-Man
		imagePacManFull = tile(2, 0);
		imagePacManDying = tilesHorizontally(11, 3, 0);
		imagePacManWalking = new BufferedImage[][] {
			/*@formatter:off*/
			{ tile(0, 0), tile(1, 0), imagePacManFull }, // RIGHT
			{ tile(0, 1), tile(1, 1), imagePacManFull }, // LEFT
			{ tile(0, 2), tile(1, 2), imagePacManFull }, // UP
			{ tile(0, 3), tile(1, 3), imagePacManFull }  // DOWN
			/*@formatter:on*/
		};

		// Ghosts
		imageGhostColored = new BufferedImage[4][8];
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				imageGhostColored[color][i] = tile(i, 4 + color);
			}
		}
		imageGhostFrightened = tilesHorizontally(2, 8, 4);
		imageGhostFlashing = tilesHorizontally(4, 8, 4);
		imageGhostEyes = tilesHorizontally(4, 8, 5);

		// Green numbers (200, 400, 800, 1600)
		imageGreenNumbers = tilesHorizontally(4, 0, 8);

		// Pink numbers (100, 300, 500, 700, 1000, 2000, 3000, 5000)
		imagePinkNumbers = new BufferedImage[] {
			/*@formatter:off*/
			tile(0,9), tile(1,9), tile(2,9), tile(3,9), 
			section(64, 144, 19, 16),
			section(56, 160, 32, 16),
			section(56, 176, 32, 16),
			section(56, 192, 32, 16)
			/*@formatter:on*/
		};

		loginfo("Theme '%s' created.", getClass().getName());
	}

	public Sprite makeSprite_number(int number) {
		int index = Arrays.asList(200, 400, 800, 1600, 100, 300, 500, 700, 1000, 2000, 3000, 5000).indexOf(number);
		if (index == -1) {
			throw new IllegalArgumentException("No sprite found for number" + number);
		}
		return Sprite.of(index < 4 ? imageGreenNumbers[index] : imagePinkNumbers[index - 4]);
	}

	public BufferedImage image_logo() {
		return imageLogo;
	}

	public Sprite makeSprite_emptyMaze() {
		return Sprite.of(imageMazeEmpty);
	}

	public Sprite makeSprite_fullMaze() {
		return Sprite.of(imageMazeFull);
	}

	public Sprite makeSprite_flashingMaze() {
		return Sprite.of(imageMazeEmptyWhite, imageMazeEmpty).animate(BACK_AND_FORTH, 200);
	}

	public Sprite makeSprite_bonusSymbol(String symbol) {
		return Sprite.of(imageMapSymbols.get(symbol));
	}

	public Sprite makeSprite_pacManFull() {
		return Sprite.of(imagePacManFull);
	}

	public Sprite makeSprite_pacManWalking(Direction dir) {
		return Sprite.of(imagePacManWalking[sheetOrder(dir)]).animate(BACK_AND_FORTH, 20);
	}

	public Sprite makeSprite_pacManDying() {
		return Sprite.of(imagePacManDying).animate(LINEAR, 100);
	}

	public Sprite makeSprite_ghostColored(int color, Direction dir) {
		BufferedImage[] frames = Arrays.copyOfRange(imageGhostColored[color], 2 * sheetOrder(dir),
				2 * (sheetOrder(dir) + 1));
		return Sprite.of(frames).animate(BACK_AND_FORTH, 300);
	}

	public Sprite makeSprite_ghostFrightened() {
		return Sprite.of(imageGhostFrightened).animate(CYCLIC, 300);
	}

	public Sprite makeSprite_ghostFlashing() {
		return Sprite.of(imageGhostFlashing).animate(CYCLIC, 125); // 4 frames take 0.5 sec
	}

	public Sprite makeSprite_ghostEyes(Direction dir) {
		return Sprite.of(imageGhostEyes[sheetOrder(dir)]);
	}
}