package de.amr.games.pacman.theme;

import static de.amr.easy.game.Application.LOGGER;
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
public class SharpX68000Theme implements Theme {

	private final BufferedImage sheet = Assets.readImage("images/sharpx68000/sprites.png");
	private final BufferedImage mazeEmpty = Assets.readImage("images/sharpx68000/maze_empty.png");
	private final BufferedImage mazeEmptyWhite;
	private final BufferedImage mazeFull = Assets.readImage("images/sharpx68000/maze_full.png");
	private final BufferedImage logo = Assets.readImage("images/sharpx68000/logo.png");
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

	@Override
	public BufferedImage spritesheet() {
		return sheet;
	}

	@Override
	public int cs() {
		return 8;
	}

	public SharpX68000Theme() {

		final int up = 0, right = 1, down = 2, left = 3;
		final int big = 16; // 2x2 small cells
		BufferedImage area;

		storeTrueTypeFont("font.joystix", "Joystix.ttf", Font.PLAIN, 12);

		int blue = -14605825; // debugger told me this
		mazeEmptyWhite = changeColor(mazeEmpty, blue, Color.WHITE.getRGB());

		// Symbols
		area = crop(0, 4 * big, 8 * big, big);
		for (Symbol symbol : Symbol.values()) {
			int i = symbol.ordinal();
			symbolMap.put(symbol, area.getSubimage(i * big, 0, big, big));
		}

		// Pac-Man
		pacManFull = crop(0, 0, big, big);

		area = crop(0, 3 * big, 10 * big, big);
		pacManDying = new BufferedImage[10];
		for (int i = 0; i < 10; ++i) {
			pacManDying[i] = area.getSubimage(i * big, 0, big, big);
		}

		pacManWalking = new BufferedImage[4][3];
		/*@formatter:off*/
		pacManWalking[down] = new BufferedImage[] { 
				crop(0 * big, 0 * big, big, big),
				crop(1 * big, 0 * big, big, big),
				crop(2 * big, 0 * big, big, big)
		};
		pacManWalking[left] = new BufferedImage[] {
				crop(0 * big, 1 * big, big, big),
				crop(1 * big, 1 * big, big, big),
				crop(2 * big, 1 * big, big, big)
		};
		pacManWalking[right] = new BufferedImage[] { 
				crop(0 * big, 2 * big, big, big),
				crop(1 * big, 2 * big, big, big),
				crop(2 * big, 2 * big, big, big)
		};
		pacManWalking[up] = new BufferedImage[] { 
				crop(0 * big, 3 * big, big, big),
				crop(1 * big, 3 * big, big, big),
				crop(2 * big, 3 * big, big, big)
		};
		/*@formatter:on*/

		// Ghosts
		ghostColored = new BufferedImage[4][8];
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghostColored[color][i] = crop(i * big, (6 + color) * big, big, big);
			}
		}
		ghostFrightened = new BufferedImage[2];
		ghostFrightened[0] = crop(8 * big, 6 * big, big, big);
		ghostFrightened[1] = crop(9 * big, 6 * big, big, big);

		ghostFlashing = new BufferedImage[4];
		ghostFlashing[0] = ghostFrightened[0];
		ghostFlashing[1] = crop(10 * big, 6 * big, big, big);
		ghostFlashing[2] = ghostFrightened[1];
		ghostFlashing[0] = crop(11 * big, 6 * big, big, big);

		ghostEyes = new BufferedImage[4];
		ghostEyes[down] = crop(8 * big, 7 * big, big, big);
		ghostEyes[left] = crop(9 * big, 7 * big, big, big);
		ghostEyes[right] = crop(10 * big, 7 * big, big, big);
		ghostEyes[up] = crop(11 * big, 7 * big, big, big);

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

		LOGGER.info(String.format("Theme '%s' created.", getClass().getSimpleName()));
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
	public BufferedImage img_logo() {
		return logo;
	}

	@Override
	public Sprite spr_emptyMaze() {
		return Sprite.of(mazeEmpty).scale(228, 248);
	}

	@Override
	public Sprite spr_fullMaze() {
		return Sprite.of(mazeFull).scale(228, 248);
	}

	@Override
	public Sprite spr_flashingMaze() {
		return Sprite.of(mazeEmpty, mazeEmptyWhite).animate(CYCLIC, MAZE_FLASH_TIME_MILLIS / 2);
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