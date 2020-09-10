package de.amr.games.pacman.theme.arcade;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.arcade.ArcadeFood;
import de.amr.games.pacman.model.world.components.Door.DoorState;
import de.amr.games.pacman.theme.api.WorldRenderer;
import de.amr.games.pacman.model.world.components.Tile;

class ArcadeWorldRenderer implements WorldRenderer {

	private SpriteMap spriteMap;
	private SpriteAnimation energizerAnimation;

	public ArcadeWorldRenderer(ArcadeSpritesheet sprites) {
		spriteMap = new SpriteMap();
		spriteMap.set("maze-full", sprites.makeSprite_fullMaze());
		spriteMap.set("maze-flashing", sprites.makeSprite_flashingMaze());
		energizerAnimation = new CyclicAnimation(2);
		energizerAnimation.setFrameDuration(150);
	}

	@Override
	public void render(Graphics2D g, World world) {
		// no anti-aliasing for maze image for better performance
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		if (world.isChanging()) {
			String selectedKey = spriteMap.selectedKey();
			if (!"maze-flashing".equals(selectedKey)) {
				spriteMap.select("maze-flashing");
				spriteMap.current().get().resetAnimation();
				spriteMap.current().get().enableAnimation(true);
			}
			spriteMap.current().get().draw(g2, 0, 3 * Tile.SIZE);
		} else {
			spriteMap.select("maze-full");
			spriteMap.current().get().draw(g2, 0, 3 * Tile.SIZE);
			drawContent(g, world);
			world.house(0).doors().filter(door -> door.state == DoorState.OPEN).forEach(door -> {
				g.setColor(Color.BLACK);
				door.tiles().forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE));
			});
		}
		g2.dispose();
	}

	private void drawContent(Graphics2D g, World world) {
		// hide eaten food
		Color eatenFoodColor = Color.BLACK;
		world.tiles().filter(world::hasEatenFood).forEach(tile -> {
			g.setColor(eatenFoodColor);
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// simulate energizer blinking animation
		energizerAnimation.update();
		energizerAnimation.setEnabled(!world.isFrozen());
		if (energizerAnimation.isEnabled() && energizerAnimation.currentFrameIndex() == 1) {
			world.tiles().filter(tile -> world.hasFood(ArcadeFood.ENERGIZER, tile)).forEach(tile -> {
				g.setColor(eatenFoodColor);
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		// draw bonus as image when active or as number when consumed
		world.temporaryFood().ifPresent(bonus -> {
			if (bonus.isActive()) {
				if (bonus.isConsumed()) {
					Vector2f position = Vector2f.of(bonus.location().x(), bonus.location().y() - Tile.SIZE / 2);
					Image img = ArcadeTheme.THEME.$image("points-" + bonus.value());
					g.drawImage(img, position.roundedX(), position.roundedY(), null);
				} else {
					ArcadeBonus arcadeBonus = (ArcadeBonus) bonus;
					Vector2f position = Vector2f.of(bonus.location().x(), bonus.location().y() - Tile.SIZE / 2);
					Image img = ArcadeTheme.THEME.$image("symbol-" + arcadeBonus.name());
					g.drawImage(img, position.roundedX(), position.roundedY(), null);
				}
			}
		});
	}
}