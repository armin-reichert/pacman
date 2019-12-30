package de.amr.games.pacman.theme;

import static de.amr.easy.game.Application.LOGGER;
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
import java.util.HashMap;
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
	private final BufferedImage mazeFull = Assets.readImage("images/arcade/maze_full.png");
	private final BufferedImage logo = Assets.readImage("images/arcade/logo.png");

	private final BufferedImage mazeWhite;
	private final BufferedImage pacManFull;
	private final BufferedImage pacManWalking[][];
	private final BufferedImage pacManDying[];
	private final BufferedImage ghostColored[][];
	private final BufferedImage ghostFrightened[];
	private final BufferedImage ghostFlashing[];
	private final BufferedImage ghostEyes[];
	private final BufferedImage greenNumbers[];
	private final BufferedImage pinkNumbers[];
	private final Map<Symbol, BufferedImage> symbolMap = new HashMap<>();

	public ArcadeTheme() {
		Assets.storeTrueTypeFont("font.joystix", "Joystix.ttf", Font.PLAIN, 12);

		int blue = -14605825; // debugger told me this
		mazeWhite = changeColor(mazeEmpty, blue, Color.WHITE.getRGB());

		// Symbols for bonuses
		Symbol[] symbols = Symbol.values();
		BufferedImage[] symbolImages = hstrip(8, 2, 3);
		for (int i = 0; i < 8; ++i) {
			symbolMap.put(symbols[i], symbolImages[i]);
		}

		// Pac-Man
		pacManFull = t(2, 0);

		// E, W, N, S -> 0(N), 1(E), 2(S), 3(W)
		int reorder[] = { 1, 3, 0, 2 };
		pacManWalking = new BufferedImage[4][];
		for (int dir = 0; dir < 4; ++dir) {
			BufferedImage mouthOpen = t(0, dir), mouthHalfOpen = t(1, dir);
			pacManWalking[reorder[dir]] = new BufferedImage[] { mouthOpen, mouthHalfOpen, pacManFull };
		}

		pacManDying = hstrip(12, 2, 0);

		// Ghosts
		ghostColored = new BufferedImage[4][8];
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghostColored[color][i] = t(i, 4 + color);
			}
		}

		ghostFrightened = hstrip(2, 8, 4);
		ghostFlashing = hstrip(4, 8, 4);

		ghostEyes = new BufferedImage[4];
		for (int dir = 0; dir < 4; ++dir) {
			ghostEyes[reorder[dir]] = t(8 + dir, 5);
		}

		// Green numbers (200, 400, 800, 1600)
		greenNumbers = hstrip(4, 0, 8);

		// Pink numbers
		pinkNumbers = new BufferedImage[8];
		// horizontal: 100, 300, 500, 700
		for (int i = 0; i < 4; ++i) {
			pinkNumbers[i] = t(i, 9);
		}
		// 1000
		pinkNumbers[4] = $(64, 144, 19, 16);
		// vertical: 2000, 3000, 5000)
		for (int j = 0; j < 3; ++j) {
			pinkNumbers[5 + j] = $(56, 160 + j * 16, 2 * 16, 16);
		}
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
	public Sprite spr_greenNumber(int i) {
		return Sprite.of(greenNumbers[i]);
	}

	@Override
	public Sprite spr_pinkNumber(int i) {
		return Sprite.of(pinkNumbers[i]);
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