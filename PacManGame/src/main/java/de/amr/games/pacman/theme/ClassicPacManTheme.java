package de.amr.games.pacman.theme;

import static de.amr.easy.game.Application.LOGGER;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.graph.grid.impl.Top4;

public class ClassicPacManTheme implements PacManTheme {

	private final BufferedImage sheet;
	private final BufferedImage mazeEmpty;
	private final BufferedImage mazeFull;
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
	private final Map<BonusSymbol, BufferedImage> symbolMap = new HashMap<>();

	private BufferedImage $(int x, int y, int w, int h) {
		return sheet.getSubimage(x, y, w, h);
	}

	private BufferedImage $(int x, int y) {
		return $(x, y, 16, 16);
	}

	private BufferedImage[] hstrip(int n, int x, int y) {
		return IntStream.range(0, n).mapToObj(i -> $(x + i * 16, y)).toArray(BufferedImage[]::new);
	}

	public ClassicPacManTheme() {
		sheet = Assets.readImage("arcade_pacman_sprites.png");

		// Mazes
		mazeFull = $(0, 0, 224, 248);
		mazeEmpty = $(228, 0, 224, 248);
		int blue = -14605825; // debugger told me this
		mazeWhite = changeColor(mazeEmpty, blue, Color.WHITE.getRGB());

		// Symbols for bonuses
		BonusSymbol[] symbols = BonusSymbol.values();
		BufferedImage[] symbolImages = hstrip(8, 488, 48);
		for (int i = 0; i < 8; ++i) {
			symbolMap.put(symbols[i], symbolImages[i]);
		}

		// Pac-Man
		pacManFull = $(488, 0);

		// E, W, N, S -> 0(N), 1(E), 2(S), 3(W)
		int permuted[] = { 1, 3, 0, 2 };
		pacManWalking = new BufferedImage[4][];
		for (int d = 0; d < 4; ++d) {
			BufferedImage mouthOpen = $(456, d * 16), mouthHalfOpen = $(472, d * 16);
			pacManWalking[permuted[d]] = new BufferedImage[] { mouthOpen, mouthHalfOpen, pacManFull };
		}

		pacManDying = hstrip(12, 488, 0);

		// Ghosts
		ghostColored = new BufferedImage[4][8];
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghostColored[color][i] = $(456 + i * 16, 64 + color * 16);
			}
		}

		ghostFrightened = hstrip(2, 584, 64);
		ghostFlashing = hstrip(4, 584, 64);

		ghostEyes = new BufferedImage[4];
		for (int d = 0; d < 4; ++d) {
			ghostEyes[permuted[d]] = $(584 + d * 16, 80);
		}

		// Green numbers (200, 400, 800, 1600)
		greenNumbers = hstrip(4, 456, 128);

		// Pink numbers
		pinkNumbers = new BufferedImage[8];
		// horizontal: 100, 300, 500, 700
		for (int i = 0; i < 4; ++i) {
			pinkNumbers[i] = $(456 + i * 16, 144);
		}
		// 1000
		pinkNumbers[4] = $(520, 144, 19, 16);
		// vertical: 2000, 3000, 5000)
		for (int j = 0; j < 3; ++j) {
			pinkNumbers[5 + j] = $(512, 160 + j * 16, 2 * 16, 16);
		}
		Application.LOGGER.info("Pac-Man sprites extracted.");

		// Text font
		Assets.storeTrueTypeFont("font.arcadeclassic", "arcadeclassic.ttf", Font.PLAIN, 12);

		LOGGER.info(String.format("Theme '%s' created.", getClass().getSimpleName()));
	}

	private BufferedImage changeColor(BufferedImage src, int from, int to) {
		BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		Graphics2D g = copy.createGraphics();
		g.drawImage(src, 0, 0, null);
		for (int x = 0; x < copy.getWidth(); ++x) {
			for (int y = 0; y < copy.getHeight(); ++y) {
				if (copy.getRGB(x, y) == from) {
					copy.setRGB(x, y, to);
				}
			}
		}
		g.dispose();
		return copy;
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
		return Sprite.of(mazeEmpty, mazeWhite).animate(CYCLIC, 200);
	}

	@Override
	public Sprite spr_bonusSymbol(BonusSymbol symbol) {
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
		case Top4.E:
			frames = Arrays.copyOfRange(ghostColored[color.ordinal()], 0, 2);
			break;
		case Top4.W:
			frames = Arrays.copyOfRange(ghostColored[color.ordinal()], 2, 4);
			break;
		case Top4.N:
			frames = Arrays.copyOfRange(ghostColored[color.ordinal()], 4, 6);
			break;
		case Top4.S:
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
		return Assets.font("font.arcadeclassic");
	}

	private Sound sound(String name) {
		return sound(name, "mp3");
	}

	private Sound sound(String name, String type) {
		return Assets.sound("sfx/" + name + "." + type);
	}

	@Override
	public Stream<Sound> snd_clips_all() {
		return Stream.of(snd_die(), snd_eatFruit(), snd_eatGhost(), snd_eatPill(), snd_extraLife(),
				snd_insertCoin(), snd_ready(), snd_ghost_chase(), snd_ghost_dead(), snd_waza());
	}

	@Override
	public Sound music_playing() {
		return sound("bgmusic");
	}

	@Override
	public Sound music_gameover() {
		return sound("ending");
	}

	@Override
	public void loadMusic() {
		CompletableFuture.runAsync(() -> {
			LOGGER.info("Loading music...");
			music_playing();
		}).thenAccept(result -> LOGGER.info("Music loaded."));
	}

	@Override
	public Sound snd_die() {
		return sound("die");
	}

	@Override
	public Sound snd_eatFruit() {
		return sound("eat-fruit");
	}

	@Override
	public Sound snd_eatGhost() {
		return sound("eat-ghost");
	}

	@Override
	public Sound snd_eatPill() {
		return sound("pacman_eat", "wav");
	}

	@Override
	public Sound snd_extraLife() {
		return sound("extra-life");
	}

	@Override
	public Sound snd_insertCoin() {
		return sound("insert-coin");
	}

	@Override
	public Sound snd_ready() {
		return sound("ready");
	}

	@Override
	public Sound snd_ghost_dead() {
		return sound("ghost_dead", "wav");
	}

	@Override
	public Sound snd_ghost_chase() {
		return sound("ghost_chase", "wav");
	}

	@Override
	public Sound snd_waza() {
		return sound("waza");
	}
}