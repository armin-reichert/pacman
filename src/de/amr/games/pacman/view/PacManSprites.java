package de.amr.games.pacman.view;

import static de.amr.easy.game.sprite.AnimationType.BACK_AND_FORTH;
import static de.amr.easy.game.sprite.AnimationType.CYCLIC;
import static de.amr.easy.game.sprite.AnimationType.LINEAR;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.BonusSymbol;

public class PacManSprites {

	public static final int RED_GHOST = 0;
	public static final int PINK_GHOST = 1;
	public static final int TURQUOISE_GHOST = 2;
	public static final int ORANGE_GHOST = 3;

	private final BufferedImage sheet;
	private final BufferedImage mazeEmpty;
	private final BufferedImage mazeFull;
	private final BufferedImage mazeWhite;
	private final BufferedImage pacManFull;
	private final BufferedImage pacManWalking[][];
	private final BufferedImage pacManDying[];
	private final BufferedImage ghostNormal[][];
	private final BufferedImage ghostAwed[];
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

	public PacManSprites() {
		sheet = Assets.readImage("sprites.png");

		// Mazes
		mazeFull = $(0, 0, 224, 248);
		mazeEmpty = $(228, 0, 224, 248);
		mazeWhite = Assets.image("maze_white.png");

		// Symbols for bonuses
		int offset = 0;
		for (BonusSymbol symbol : BonusSymbol.values()) {
			symbolMap.put(symbol, $(488 + offset, 48));
			offset += 16;
		}

		// Pac-Man
		pacManFull = $(488, 0);

		int[] dirs = { Top4.E, Top4.W, Top4.N, Top4.S };
		pacManWalking = new BufferedImage[4][];
		for (int dir = 0; dir < 4; ++dir) {
			pacManWalking[dirs[dir]] = new BufferedImage[] { $(456, dir * 16), $(472, dir * 16), pacManFull };
		}

		pacManDying = new BufferedImage[12];
		for (int i = 0; i < 12; ++i) {
			pacManDying[i] = $(488 + i * 16, 0);
		}

		// Ghosts
		ghostNormal = new BufferedImage[4][8];
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghostNormal[color][i] = $(456 + i * 16, 64 + color * 16);
			}
		}

		ghostAwed = new BufferedImage[2];
		for (int i = 0; i < 2; ++i) {
			ghostAwed[i] = $(584 + i * 16, 64);
		}

		ghostFlashing = new BufferedImage[4];
		for (int i = 0; i < 4; ++i) {
			ghostFlashing[i] = $(584 + i * 16, 64);
		}

		ghostEyes = new BufferedImage[4];
		for (int i = 0; i < 4; ++i) {
			ghostEyes[i] = $(584 + i * 16, 80);
		}

		// Green numbers (200, 400, 800, 1600)
		greenNumbers = new BufferedImage[4];
		for (int i = 0; i < 4; ++i) {
			greenNumbers[i] = $(456 + i * 16, 128);
		}

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

		Application.LOGGER.info("Pac-Man sprite images extracted");
	}

	public Sprite mazeEmpty() {
		return new Sprite(mazeEmpty);
	}

	public Sprite mazeFull() {
		return new Sprite(mazeFull);
	}

	public Sprite mazeFlashing() {
		return new Sprite(mazeEmpty, mazeWhite).animate(CYCLIC, 100);
	}

	public Sprite symbol(BonusSymbol symbol) {
		return new Sprite(symbolMap.get(symbol));
	}

	public BufferedImage symbolImage(BonusSymbol symbol) {
		return symbolMap.get(symbol);
	}

	public Sprite pacManFull() {
		return new Sprite(pacManFull);
	}

	public Sprite pacManWalking(int dir) {
		return new Sprite(pacManWalking[dir]).animate(BACK_AND_FORTH, 100);
	}

	public Sprite pacManDying() {
		return new Sprite(pacManDying).animate(LINEAR, 100);
	}

	public Sprite ghostColored(int color, int direction) {
		BufferedImage[] frames;
		switch (direction) {
		case Top4.E:
			frames = Arrays.copyOfRange(ghostNormal[color], 0, 2);
			break;
		case Top4.W:
			frames = Arrays.copyOfRange(ghostNormal[color], 2, 4);
			break;
		case Top4.N:
			frames = Arrays.copyOfRange(ghostNormal[color], 4, 6);
			break;
		case Top4.S:
			frames = Arrays.copyOfRange(ghostNormal[color], 6, 8);
			break;
		default:
			throw new IllegalArgumentException("Illegal direction: " + direction);
		}
		return new Sprite(frames).animate(BACK_AND_FORTH, 300);
	}

	public Sprite ghostAwed() {
		return new Sprite(ghostAwed).animate(CYCLIC, 200);
	}

	public Sprite ghostFlashing() {
		return new Sprite(ghostFlashing).animate(CYCLIC, 250);
	}

	public Sprite ghostEyes(int dir) {
		int[] dirs = { Top4.E, Top4.W, Top4.N, Top4.S };
		return new Sprite(ghostEyes[dirs[dir]]);
	}

	public Sprite greenNumber(int i) {
		return new Sprite(greenNumbers[i]);
	}

	public Sprite pinkNumber(int i) {
		return new Sprite(pinkNumbers[i]);
	}
}