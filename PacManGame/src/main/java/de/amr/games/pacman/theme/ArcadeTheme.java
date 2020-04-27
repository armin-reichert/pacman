package de.amr.games.pacman.theme;

import static de.amr.easy.game.Application.loginfo;
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
 * Theme based on original(?) sprites.
 * 
 * @author Armin Reichert
 */
public class ArcadeTheme implements Theme {

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

	public ArcadeTheme() {
		storeTrueTypeFont("font.joystix", "Joystix.ttf", Font.PLAIN, 12);

		int blue = -14605825; // debugger told me this
		mazeEmptyWhite = changeColor(mazeEmpty, blue, Color.WHITE.getRGB());

		// Symbols
		BufferedImage[] symbolImages = ht(8, 2, 3);
		for (Symbol symbol : Symbol.values()) {
			symbolMap.put(symbol, symbolImages[symbol.ordinal()]);
		}

		// Pac-Man
		pacManFull = t(2, 0);
		pacManDying = ht(12, 2, 0);
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
		ghostFrightened = ht(2, 8, 4);
		ghostFlashing = ht(4, 8, 4);
		ghostEyes = new BufferedImage[] { t(10, 5), t(8, 5), t(11, 5), t(9, 5) };

		// Green numbers (200, 400, 800, 1600)
		greenNumbers = ht(4, 0, 8);

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
		switch (number) {
		case 200:
			return Sprite.of(greenNumbers[0]);
		case 400:
			return Sprite.of(greenNumbers[1]);
		case 800:
			return Sprite.of(greenNumbers[2]);
		case 1600:
			return Sprite.of(greenNumbers[3]);
		case 100:
			return Sprite.of(pinkNumbers[0]);
		case 300:
			return Sprite.of(pinkNumbers[1]);
		case 500:
			return Sprite.of(pinkNumbers[2]);
		case 700:
			return Sprite.of(pinkNumbers[3]);
		case 1000:
			return Sprite.of(pinkNumbers[4]);
		case 2000:
			return Sprite.of(pinkNumbers[5]);
		case 3000:
			return Sprite.of(pinkNumbers[6]);
		case 5000:
			return Sprite.of(pinkNumbers[7]);
		default:
			throw new IllegalArgumentException("No sprite found for number" + number);
		}
	}

	@Override
	public BufferedImage spritesheet() {
		return sheet;
	}

	@Override
	public int cs() {
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
		return Sprite.of(mazeEmptyWhite, mazeEmpty).animate(CYCLIC, MAZE_FLASH_TIME_MILLIS / 2);
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
		return Sprite.of(ghostFlashing).animate(CYCLIC, 50);
	}

	@Override
	public Sprite spr_ghostEyes(int dir) {
		return Sprite.of(ghostEyes[dir]);
	}

	@Override
	public Font fnt_text() {
		return Assets.font("font.joystix");
	}

	@Override
	public Stream<Sound> snd_clips_all() {
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