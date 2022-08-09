/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.theme.arcade;

import static de.amr.easy.game.ui.sprites.AnimationType.CYCLIC;
import static de.amr.easy.game.ui.sprites.AnimationType.FORWARD_BACKWARDS;
import static de.amr.easy.game.ui.sprites.AnimationType.LINEAR;
import static de.amr.games.pacmanfsm.lib.Direction.DOWN;
import static de.amr.games.pacmanfsm.lib.Direction.LEFT;
import static de.amr.games.pacmanfsm.lib.Direction.RIGHT;
import static de.amr.games.pacmanfsm.lib.Direction.UP;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.Spritesheet;
import de.amr.games.pacmanfsm.lib.Direction;
import de.amr.games.pacmanfsm.model.world.arcade.ArcadeBonus;

/**
 * Arcade theme spritesheet.
 * 
 * @author Armin Reichert
 */
public class ArcadeSpritesheet extends Spritesheet {

	public enum GhostColor {
		RED, PINK, CYAN, ORANGE
	}

	static final List<Direction> DIRECTIONS = List.of(RIGHT, LEFT, UP, DOWN);
	static final List<Integer> NUMBERS = List.of(200, 400, 800, 1600, 100, 300, 500, 700, 1000, 2000, 3000, 5000);

	private static int dirIndex(Direction dir) {
		return DIRECTIONS.indexOf(dir);
	}

	BufferedImage emptyMazeImage = Assets.readImage("themes/arcade/maze_empty.png");
	BufferedImage fullMazeImage = Assets.readImage("themes/arcade/maze_full.png");

	BufferedImage emptyWhiteMazeImage;
	BufferedImage pacManFullImage;
	BufferedImage[] pacManBlockedImages;
	BufferedImage[][] pacManWalkingImages;
	BufferedImage[] pacManDyingImages;
	BufferedImage livesCounterImage;
	BufferedImage[][] ghostColoredImagesById;
	BufferedImage[] ghostFrightenedImages;
	BufferedImage[] ghostFlashingImages;
	BufferedImage[] ghostEyesImages;
	BufferedImage[] greenNumberImages;
	BufferedImage[] pinkNumberImages;
	BufferedImage[] bonusSymbolImages;

	public ArcadeSpritesheet() {
		super("themes/arcade/sprites.png", 16);

		// Debugger told me RGB value of blue color in maze image
		emptyWhiteMazeImage = exchangeColor(emptyMazeImage, -14605825, Color.WHITE.getRGB());

		// Symbols for bonus food
		bonusSymbolImages = horizontalTiles(8, 2, 3);

		// Pac-Man
		pacManFullImage = tile(2, 0);
		pacManDyingImages = horizontalTiles(11, 3, 0);
		pacManWalkingImages = new BufferedImage[][] {
			/*@formatter:off*/
			{ tile(0, 0), tile(0, 0), tile(0, 0), tile(0, 0), tile(1, 0), tile(1, 0), tile(2, 0) }, // RIGHT
			{ tile(0, 1), tile(0, 1), tile(0, 1), tile(0, 1), tile(1, 1), tile(1, 1), tile(2, 0) }, // LEFT
			{ tile(0, 2), tile(0, 2), tile(0, 2), tile(0, 2), tile(1, 2), tile(1, 2), tile(2, 0) }, // UP
			{ tile(0, 3), tile(0, 3), tile(0, 3), tile(0, 3), tile(1, 3), tile(1, 3), tile(2, 0) }  // DOWN
			/*@formatter:on*/
		};
		pacManBlockedImages = new BufferedImage[] { tile(0, 0), tile(0, 1), tile(0, 2), tile(0, 3) };
		livesCounterImage = tile(8, 1);

		// Ghosts
		ghostColoredImagesById = new BufferedImage[4][8];
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghostColoredImagesById[color][i] = tile(i, 4 + color);
			}
		}
		ghostFrightenedImages = horizontalTiles(2, 8, 4);
		ghostFlashingImages = horizontalTiles(4, 8, 4);
		ghostEyesImages = horizontalTiles(4, 8, 5);

		// Green numbers (200, 400, 800, 1600)
		greenNumberImages = horizontalTiles(4, 0, 8);

		// Pink numbers (100, 300, 500, 700, 1000, 2000, 3000, 5000)
		pinkNumberImages = new BufferedImage[] {
			/*@formatter:off*/
			tile(0,9), tile(1,9), tile(2,9), tile(3,9), 
			region(64, 144, 19, 16),
			region(56, 160, 32, 16),
			region(56, 176, 32, 16),
			region(56, 192, 32, 16)
			/*@formatter:on*/
		};
	}

	public BufferedImage imageFullMaze() {
		return fullMazeImage;
	}

	public Sprite makeSpriteFlashingMaze(int flashes) {
		if (flashes == 0) {
			return Sprite.of(emptyMazeImage);
		}
		BufferedImage[] frames = new BufferedImage[2 * flashes];
		for (int i = 0; i < flashes; ++i) {
			frames[2 * i] = emptyMazeImage;
			frames[2 * i + 1] = emptyWhiteMazeImage;
		}
		return Sprite.of(frames).animate(LINEAR, 200);
	}

	public Sprite makeSpriteBonusSymbol(String symbolName) {
		int index = ArcadeBonus.Symbol.valueOf(symbolName).ordinal();
		return Sprite.of(bonusSymbolImages[index]);
	}

	public Sprite makeSpritePacManFull() {
		return Sprite.of(pacManFullImage);
	}

	public Sprite makeSpritePacManBlocked(Direction dir) {
		return Sprite.of(pacManBlockedImages[dirIndex(dir)]);
	}

	public Sprite makeSpritePacManWalking(Direction dir) {
		return Sprite.of(pacManWalkingImages[dirIndex(dir)]).animate(FORWARD_BACKWARDS, 5);
	}

	public Sprite makeSpritePacManCollapsing() {
		return Sprite.of(pacManDyingImages).animate(LINEAR, 100);
	}

	public BufferedImage imageLivesCounter() {
		return livesCounterImage;
	}

	public BufferedImage imageBonusSymbol(int bonus) {
		return bonusSymbolImages[bonus];
	}

	public BufferedImage imageNumber(int number) {
		int index = NUMBERS.indexOf(number);
		if (index == -1) {
			throw new IllegalArgumentException("No sprite found for number" + number);
		}
		return index < 4 ? greenNumberImages[index] : pinkNumberImages[index - 4];
	}

	public Sprite makeSpritGhostColored(GhostColor color, Direction dir) {
		BufferedImage[] frames = Arrays.copyOfRange(ghostColoredImagesById[color.ordinal()], 2 * dirIndex(dir),
				2 * (dirIndex(dir) + 1));
		return Sprite.of(frames).animate(FORWARD_BACKWARDS, 300);
	}

	public Sprite makeSpriteGhostFrightened() {
		return Sprite.of(ghostFrightenedImages).animate(CYCLIC, 300);
	}

	public Sprite makeSpriteGhostFlashing() {
		return Sprite.of(ghostFlashingImages).animate(CYCLIC, 125); // 4 frames take 0.5 sec
	}

	public Sprite makeSpriteGhostEyes(Direction dir) {
		return Sprite.of(ghostEyesImages[dirIndex(dir)]);
	}
}