package de.amr.games.pacman.theme;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.assets.Assets.readImage;
import static de.amr.easy.game.assets.Assets.storeTrueTypeFont;
import static de.amr.easy.game.ui.sprites.AnimationType.BACK_AND_FORTH;
import static de.amr.easy.game.ui.sprites.AnimationType.CYCLIC;
import static de.amr.easy.game.ui.sprites.AnimationType.LINEAR;
import static de.amr.graph.grid.impl.Grid4Topology.E;
import static de.amr.graph.grid.impl.Grid4Topology.N;
import static de.amr.graph.grid.impl.Grid4Topology.S;
import static de.amr.graph.grid.impl.Grid4Topology.W;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.Symbol;

/**
 * Theme based on MSX sprites.
 * 
 * @author Armin Reichert
 */
public class MSXTheme implements Theme {

	private final BufferedImage sheet;
	private final BufferedImage mazeEmpty;
	private final BufferedImage mazeFull;
	private final BufferedImage logo;

	private final BufferedImage mazeWhite;
	private final BufferedImage pacManFull;
	private final BufferedImage pacManWalking[][];
	private final BufferedImage pacManDying[];
	private final BufferedImage ghostColored[][];
	private final BufferedImage ghostFrightened[];
	private final BufferedImage ghostFlashing[];
	private final BufferedImage ghostEyes[];
	private final BufferedImage numbers[];
	private final Map<Symbol, BufferedImage> symbolMap = new EnumMap<>(Symbol.class);

	public MSXTheme() {
		storeTrueTypeFont("font.joystix", "Joystix.ttf", Font.PLAIN, 12);

		sheet = readImage("images/msx/sprites.png");
		mazeFull = readImage("images/msx/maze_full.png");
		mazeEmpty = readImage("images/msx/maze_empty.png");
		int blue = -14605825; // debugger told me this
		mazeWhite = changeColor(mazeEmpty, blue, Color.WHITE.getRGB());
		logo = readImage("images/msx/logo.png");

		// Symbols for bonuses
		Symbol[] symbols = Symbol.values();
		BufferedImage[] symbolImages = ht(8, 0, 4);
		for (int i = 0; i < 8; ++i) {
			symbolMap.put(symbols[i], symbolImages[i]);
		}

		// Pac-Man
		pacManFull = t(0, 1);
		pacManDying = ht(11, 0, 1);
		pacManWalking = new BufferedImage[][] {
			/*@formatter:off*/
			{ t(0, 1), t(2, 0), t(3, 0) }, 
			{ t(0, 1), t(0, 0), t(1, 0) },
			{ t(0, 1), t(6, 0), t(7, 0) }, 
			{ t(0, 1), t(4, 0), t(5, 0) }
			/*@formatter:on*/
		};

		// 0=RED, 1=PINK, 2=CYAN, 3=ORANGE
		ghostColored = new BufferedImage[4][8];
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghostColored[color][i] = t(i, 5 + color);
			}
		}
		ghostFrightened = ht(2, 0, 9);
		ghostFlashing = ht(2, 0, 9); // TODO

		ghostEyes = new BufferedImage[] { t(3, 9), t(2, 9), t(5, 9), t(4, 9) };

		// 200, 400, 800, 1600
		numbers = ht(4, 0, 10);

		// 100, 300, 500, 700, 1000, 2000, 3000, 5000
		// TODO no sprites for these!

		LOGGER.info(String.format("Theme '%s' created.", getClass().getSimpleName()));
	}

	@Override
	public BufferedImage spritesheet() {
		return sheet;
	}

	@Override
	public int raster() {
		return 16;
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
		return Sprite.of(mazeEmpty, mazeWhite).animate(CYCLIC, MAZE_FLASH_TIME_MILLIS / 2);
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
	public Sprite spr_pacManWalking(int dir) {
		return Sprite.of(pacManWalking[dir]).animate(BACK_AND_FORTH, 20);
	}

	@Override
	public Sprite spr_pacManDying() {
		return Sprite.of(pacManDying).animate(LINEAR, 100);
	}

	@Override
	public Sprite spr_ghostColored(GhostColor color, int direction) {
		BufferedImage[] frames;
		switch (direction) {
		case E:
			frames = Arrays.copyOfRange(ghostColored[color.ordinal()], 0, 2);
			break;
		case W:
			frames = Arrays.copyOfRange(ghostColored[color.ordinal()], 2, 4);
			break;
		case N:
			frames = Arrays.copyOfRange(ghostColored[color.ordinal()], 4, 6);
			break;
		case S:
			frames = Arrays.copyOfRange(ghostColored[color.ordinal()], 6, 8);
			break;
		default:
			throw new IllegalArgumentException("Illegal direction: " + direction);
		}
		return Sprite.of(frames).animate(BACK_AND_FORTH, 300);
	}

	@Override
	public Sprite spr_ghostFrightened() {
		return Sprite.of(ghostFrightened).animate(CYCLIC, 300);
	}

	@Override
	public Sprite spr_ghostFlashing() {
		return Sprite.of(ghostFlashing).animate(CYCLIC, 100);
	}

	@Override
	public Sprite spr_ghostEyes(int dir) {
		return Sprite.of(ghostEyes[dir]);
	}

	@Override
	public Sprite spr_number(int number) {
		switch (number) {
		case 200:
			return Sprite.of(numbers[0]);
		case 400:
			return Sprite.of(numbers[1]);
		case 800:
			return Sprite.of(numbers[2]);
		case 1600:
			return Sprite.of(numbers[3]);
		case 100:
			return null; // TODO
		case 300:
			return null; // TODO
		case 500:
			return null; // TODO
		case 700:
			return null; // TODO
		case 1000:
			return null; // TODO
		case 2000:
			return null; // TODO
		case 3000:
			return null; // TODO
		case 5000:
			return null; // TODO
		default:
			throw new IllegalArgumentException("No sprite found for number" + number);
		}
	}

	@Override
	public Font fnt_text() {
		return Assets.font("font.joystix");
	}

	@Override
	public Stream<Sound> snd_clips_all() {
		return Stream.of(snd_die(), snd_eatFruit(), snd_eatGhost(), snd_eatPill(), snd_extraLife(),
				snd_insertCoin(), snd_ready(), snd_ghost_chase(), snd_ghost_dead(), snd_waza());
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
		return wav("pacman_eat");
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
		return wav("ghost_dead");
	}

	@Override
	public Sound snd_ghost_chase() {
		return wav("ghost_chase");
	}

	@Override
	public Sound snd_waza() {
		return mp3("waza");
	}
}