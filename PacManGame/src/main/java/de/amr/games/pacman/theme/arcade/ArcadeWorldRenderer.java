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
package de.amr.games.pacman.theme.arcade;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.games.pacman.lib.Tile;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.arcade.ArcadeFood;
import de.amr.games.pacman.model.world.components.Door.DoorState;
import de.amr.games.pacman.theme.api.WorldRenderer;

class ArcadeWorldRenderer implements WorldRenderer {

	private Sprite spriteFlashingMaze;
	private final SpriteAnimation energizerAnimation;

	public ArcadeWorldRenderer() {
		energizerAnimation = new CyclicAnimation(2);
		energizerAnimation.setFrameDuration(150);
	}

	@Override
	public void render(Graphics2D g, TiledWorld world) {
		ArcadeSpritesheet spriteSheet = ArcadeTheme.THEME.asValue("sprites");
		// no anti-aliasing for maze image for better performance
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		if (world.isChanging()) {
			if (spriteFlashingMaze == null) {
				spriteFlashingMaze = spriteSheet.makeSpriteFlashingMaze(PacManGame.it().numFlashes);
			}
			spriteFlashingMaze.draw(g2, 0, 3 * Tile.TS);
		} else {
			spriteFlashingMaze = null;
			g.drawImage(spriteSheet.imageFullMaze(), 0, 3 * Tile.TS, null);
			drawContent(g, world);
			world.house(0).ifPresent(house -> {
				house.doors().filter(door -> door.state == DoorState.OPEN).forEach(door -> {
					g.setColor(Color.BLACK);
					door.tiles().forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.TS, Tile.TS));
				});
			});
		}
		g2.dispose();
	}

	private void drawContent(Graphics2D g, TiledWorld world) {
		// hide eaten food
		Color eatenFoodColor = Color.BLACK;
		world.tiles().filter(world::hasEatenFood).forEach(tile -> {
			g.setColor(eatenFoodColor);
			g.fillRect(tile.x(), tile.y(), Tile.TS, Tile.TS);
		});
		// simulate energizer blinking animation
		energizerAnimation.update();
		energizerAnimation.setEnabled(!world.isFrozen());
		if (energizerAnimation.isEnabled() && energizerAnimation.currentFrameIndex() == 1) {
			world.tiles().filter(tile -> world.hasFood(ArcadeFood.ENERGIZER, tile)).forEach(tile -> {
				g.setColor(eatenFoodColor);
				g.fillRect(tile.x(), tile.y(), Tile.TS, Tile.TS);
			});
		}
		// draw bonus as image when active or as number when consumed
		world.temporaryFood().ifPresent(bonus -> {
			if (bonus.isActive()) {
				if (bonus.isConsumed()) {
					Vector2f position = Vector2f.of(bonus.location().x(), bonus.location().y() - Tile.TS / 2);
					Image img = ArcadeTheme.THEME.asIimage("points-" + bonus.value());
					g.drawImage(img, position.roundedX(), position.roundedY(), null);
				} else {
					ArcadeBonus arcadeBonus = (ArcadeBonus) bonus;
					Vector2f position = Vector2f.of(bonus.location().x(), bonus.location().y() - Tile.TS / 2);
					Image img = ArcadeTheme.THEME.asIimage("symbol-" + arcadeBonus.symbol.name());
					g.drawImage(img, position.roundedX(), position.roundedY(), null);
				}
			}
		});
	}
}