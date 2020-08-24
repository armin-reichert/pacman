package de.amr.games.pacman.view.theme.arcade;

import static de.amr.easy.game.ui.sprites.AnimationType.CYCLIC;
import static de.amr.easy.game.ui.sprites.AnimationType.FORWARD_BACKWARDS;
import static de.amr.easy.game.ui.sprites.AnimationType.LINEAR;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.Spritesheet;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;

/**
 * Arcade theme spritesheet.
 * 
 * @author Armin Reichert
 */
public class ArcadeSprites extends Spritesheet {

	BufferedImage empty_maze = Assets.readImage("themes/arcade/maze_empty.png");
	BufferedImage full_maze = Assets.readImage("themes/arcade/maze_full.png");
	
	BufferedImage empty_white_maze;
	BufferedImage pacMan_full;
	BufferedImage pacMan_walking[][];
	BufferedImage pacMan_dying[];
	BufferedImage ghost_colored[][];
	BufferedImage ghost_frightened[];
	BufferedImage ghost_flashing[];
	BufferedImage ghost_eyes[];
	BufferedImage green_numbers[];
	BufferedImage pink_numbers[];
	BufferedImage bonus_symbols[];

	public ArcadeSprites() {
		super("themes/arcade/sprites.png", 16);

		// Debugger told me RGB value of blue color in maze image
		empty_white_maze = exchangeColor(empty_maze, -14605825, Color.WHITE.getRGB());

		// Symbols for bonus food
		bonus_symbols = horizontalTiles(8, 2, 3);

		// Pac-Man
		pacMan_full = tile(2, 0);
		pacMan_dying = horizontalTiles(11, 3, 0);
		pacMan_walking = new BufferedImage[][] {
			/*@formatter:off*/
			{ tile(0, 0), tile(0, 0), tile(0, 0), tile(1, 0), tile(1, 0), tile(2, 0) }, // RIGHT
			{ tile(0, 1), tile(0, 1), tile(0, 1), tile(1, 1), tile(1, 1), tile(2, 0) }, // LEFT
			{ tile(0, 2), tile(0, 2), tile(0, 2), tile(1, 2), tile(1, 2), tile(2, 0) }, // UP
			{ tile(0, 3), tile(0, 3), tile(0, 3), tile(1, 3), tile(1, 3), tile(2, 0) }  // DOWN
			/*@formatter:on*/
		};

		// Ghosts
		ghost_colored = new BufferedImage[4][8];
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghost_colored[color][i] = tile(i, 4 + color);
			}
		}
		ghost_frightened = horizontalTiles(2, 8, 4);
		ghost_flashing = horizontalTiles(4, 8, 4);
		ghost_eyes = horizontalTiles(4, 8, 5);

		// Green numbers (200, 400, 800, 1600)
		green_numbers = horizontalTiles(4, 0, 8);

		// Pink numbers (100, 300, 500, 700, 1000, 2000, 3000, 5000)
		pink_numbers = new BufferedImage[] {
			/*@formatter:off*/
			tile(0,9), tile(1,9), tile(2,9), tile(3,9), 
			region(64, 144, 19, 16),
			region(56, 160, 32, 16),
			region(56, 176, 32, 16),
			region(56, 192, 32, 16)
			/*@formatter:on*/
		};
	}

	private int spriteSheetOrder(Direction dir) {
		if (dir == Direction.RIGHT) {
			return 0;
		}
		if (dir == Direction.LEFT) {
			return 1;
		}
		if (dir == Direction.UP) {
			return 2;
		}
		if (dir == Direction.DOWN) {
			return 3;
		}
		throw new IllegalArgumentException("Illegal direction: " + dir);
	}

	public Sprite makeSprite_number(int number) {
		int index = Arrays.asList(200, 400, 800, 1600, 100, 300, 500, 700, 1000, 2000, 3000, 5000).indexOf(number);
		if (index == -1) {
			throw new IllegalArgumentException("No sprite found for number" + number);
		}
		return Sprite.of(index < 4 ? green_numbers[index] : pink_numbers[index - 4]);
	}

	public Sprite makeSprite_emptyMaze() {
		return Sprite.of(empty_maze);
	}

	public Sprite makeSprite_fullMaze() {
		return Sprite.of(full_maze);
	}

	public Sprite makeSprite_flashingMaze() {
		return Sprite.of(empty_maze, empty_white_maze).animate(CYCLIC, 200);
	}

	public Sprite makeSprite_bonusSymbol(String symbol) {
		int index = ArcadeBonus.valueOf(symbol).ordinal();
		return Sprite.of(bonus_symbols[index]);
	}

	public Sprite makeSprite_pacManFull() {
		return Sprite.of(pacMan_full);
	}

	public Sprite makeSprite_pacManWalking(Direction dir) {
		return Sprite.of(pacMan_walking[spriteSheetOrder(dir)]).animate(FORWARD_BACKWARDS, 15);
	}

	public Sprite makeSprite_pacManCollapsing() {
		return Sprite.of(pacMan_dying).animate(LINEAR, 100);
	}

	public Sprite makeSprite_ghostColored(GhostPersonality personality, Direction dir) {
		BufferedImage[] frames = Arrays.copyOfRange(ghost_colored[personality.ordinal()], 2 * spriteSheetOrder(dir),
				2 * (spriteSheetOrder(dir) + 1));
		return Sprite.of(frames).animate(FORWARD_BACKWARDS, 300);
	}

	public Sprite makeSprite_ghostFrightened() {
		return Sprite.of(ghost_frightened).animate(CYCLIC, 300);
	}

	public Sprite makeSprite_ghostFlashing() {
		return Sprite.of(ghost_flashing).animate(CYCLIC, 125); // 4 frames take 0.5 sec
	}

	public Sprite makeSprite_ghostEyes(Direction dir) {
		return Sprite.of(ghost_eyes[spriteSheetOrder(dir)]);
	}
}