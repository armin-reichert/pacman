package de.amr.games.pacman.theme;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.easy.game.assets.Assets.storeTrueTypeFont;
import static de.amr.easy.game.ui.sprites.AnimationType.BACK_AND_FORTH;
import static de.amr.easy.game.ui.sprites.AnimationType.CYCLIC;
import static de.amr.easy.game.ui.sprites.AnimationType.LINEAR;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Symbol;

/**
 * Theme based on original(?) sprites.
 * 
 * @author Armin Reichert
 */
public class ArcadeTheme implements Theme {

	private final int tileSize = 16;

	private final BufferedImage sheet = Assets.readImage("images/arcade/sprites.png");
	private final BufferedImage mazeEmpty = Assets.readImage("images/arcade/maze_empty.png");
	private final BufferedImage mazeEmptyWhite;
	private final BufferedImage mazeFull = Assets.readImage("images/arcade/maze_full.png");
	private final BufferedImage logo = Assets.readImage("images/arcade/logo.png");
	private final BufferedImage pacManFull;
	private final BufferedImage pacManWalking[][];
	private final BufferedImage pacManDying[];
	private final BufferedImage ghostColored[][];
	private final BufferedImage ghostFrightened[];
	private final BufferedImage ghostFlashing[];
	private final BufferedImage ghostEyes[];
	private final BufferedImage greenNumbers[];
	private final BufferedImage pinkNumbers[];
	private final Map<Symbol, BufferedImage> symbolMap = new EnumMap<>(Symbol.class);

	BufferedImage crop(int x, int y, int w, int h) {
		return sheet.getSubimage(x, y, w, h);
	}

	BufferedImage t(int col, int row) {
		return crop(col * tileSize, row * tileSize, tileSize, tileSize);
	}

	BufferedImage[] nHorTiles(int n, int col, int row) {
		return IntStream.range(0, n).mapToObj(i -> t(col + i, row)).toArray(BufferedImage[]::new);
	}

	Sound mp3(String name) {
		return Assets.sound("sfx/" + name + ".mp3");
	}

	BufferedImage exchangeColor(BufferedImage img, int oldColorRGB, int newColorRGB) {
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

	public ArcadeTheme() {
		storeTrueTypeFont("font.hud", "PressStart2P-Regular.ttf", Font.PLAIN, 8);

		// debugger told me RGB value of blue color in maze image
		mazeEmptyWhite = exchangeColor(mazeEmpty, -14605825, Color.WHITE.getRGB());

		// Symbols
		BufferedImage[] symbolImages = nHorTiles(8, 2, 3);
		for (Symbol symbol : Symbol.values()) {
			symbolMap.put(symbol, symbolImages[symbol.ordinal()]);
		}

		// Pac-Man
		pacManFull = t(2, 0);
		pacManDying = nHorTiles(12, 2, 0);
		pacManWalking = new BufferedImage[][] {
			/*@formatter:off*/
			{ t(0, 2), t(1, 2), pacManFull }, 
			{ t(0, 0), t(1, 0), pacManFull },
			{ t(0, 3), t(1, 3), pacManFull }, 
			{ t(0, 1), t(1, 1), pacManFull }
			/*@formatter:on*/
		};

		// Ghosts
		ghostColored = new BufferedImage[4][8];
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghostColored[color][i] = t(i, 4 + color);
			}
		}
		ghostFrightened = nHorTiles(2, 8, 4);
		ghostFlashing = nHorTiles(4, 8, 4);
		ghostEyes = new BufferedImage[] { t(10, 5), t(8, 5), t(11, 5), t(9, 5) };

		// Green numbers (200, 400, 800, 1600)
		greenNumbers = nHorTiles(4, 0, 8);

		// Pink numbers (100, 300, 500, 700, 1000, 2000, 3000, 5000)
		pinkNumbers = new BufferedImage[] {
			/*@formatter:off*/
			t(0,9), t(1,9), t(2,9), t(3,9), 
			crop(64, 144, 19, 16),
			crop(56, 160, 32, 16),crop(56, 176, 32, 16),crop(56, 192, 32, 16)
			/*@formatter:on*/
		};

		loginfo("Theme '%s' created.", getClass().getName());
	}

	@Override
	public Sprite spr_number(int number) {
		int index = Arrays.asList(200, 400, 800, 1600, 100, 300, 500, 700, 1000, 2000, 3000, 5000).indexOf(number);
		if (index == -1) {
			throw new IllegalArgumentException("No sprite found for number" + number);
		}
		return Sprite.of(index < 4 ? greenNumbers[index] : pinkNumbers[index - 4]);
	}

	@Override
	public Color color_mazeBackground() {
		return Color.BLACK;
	}

	@Override
	public BufferedImage img_logo() {
		return logo;
	}

	@Override
	public Sprite spr_emptyMaze() {
		return Sprite.of(mazeEmpty);
	}

	@Override
	public Sprite spr_fullMaze() {
		return Sprite.of(mazeFull);
	}

	@Override
	public Sprite spr_flashingMaze() {
		return Sprite.of(mazeEmptyWhite, mazeEmpty).animate(CYCLIC, 200);
	}

	@Override
	public Sprite spr_bonusSymbol(Symbol symbol) {
		return Sprite.of(symbolMap.get(symbol));
	}

	@Override
	public Sprite spr_pacManFull() {
		return Sprite.of(pacManFull);
	}

	@Override
	public Sprite spr_pacManWalking(Direction dir) {
		return Sprite.of(pacManWalking[dir.ordinal()]).animate(BACK_AND_FORTH, 20);
	}

	@Override
	public Sprite spr_pacManDying() {
		return Sprite.of(pacManDying).animate(LINEAR, 100);
	}

	@Override
	public Sprite spr_ghostColored(int color, Direction dir) {
		int i = Stream.of(RIGHT, LEFT, UP, DOWN).filter(d -> d == dir).map(Direction::ordinal).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Illegal direction: " + dir));
		BufferedImage[] frames = Arrays.copyOfRange(ghostColored[color], 2 * i, 2 * (i + 1));
		return Sprite.of(frames).animate(BACK_AND_FORTH, 300);
	}

	@Override
	public Sprite spr_ghostFrightened() {
		return Sprite.of(ghostFrightened).animate(CYCLIC, 300);
	}

	@Override
	public Sprite spr_ghostFlashing() {
		return Sprite.of(ghostFlashing).animate(CYCLIC, 50);
	}

	@Override
	public Sprite spr_ghostEyes(Direction dir) {
		return Sprite.of(ghostEyes[dir.ordinal()]);
	}

	@Override
	public Font fnt_text() {
		return Assets.font("font.hud");
	}

	@Override
	public Stream<Sound> clips_all() {
		return Stream.of(snd_die(), snd_eatFruit(), snd_eatGhost(), snd_eatPill(), snd_extraLife(), snd_insertCoin(),
				snd_ready(), snd_ghost_chase(), snd_ghost_dead(), snd_waza());
	}

	@Override
	public Sound music_playing() {
		return mp3("bgmusic");
	}

	@Override
	public Sound music_gameover() {
		return mp3("ending");
	}

	@Override
	public Sound snd_die() {
		return mp3("die");
	}

	@Override
	public Sound snd_eatFruit() {
		return mp3("eat-fruit");
	}

	@Override
	public Sound snd_eatGhost() {
		return mp3("eat-ghost");
	}

	@Override
	public Sound snd_eatPill() {
		return mp3("eating");
	}

	@Override
	public Sound snd_extraLife() {
		return mp3("extra-life");
	}

	@Override
	public Sound snd_insertCoin() {
		return mp3("insert-coin");
	}

	@Override
	public Sound snd_ready() {
		return mp3("ready");
	}

	@Override
	public Sound snd_ghost_dead() {
		return mp3("ghost_dead");
	}

	@Override
	public Sound snd_ghost_chase() {
		return mp3("ghost_chase");
	}

	@Override
	public Sound snd_waza() {
		return mp3("waza");
	}
}