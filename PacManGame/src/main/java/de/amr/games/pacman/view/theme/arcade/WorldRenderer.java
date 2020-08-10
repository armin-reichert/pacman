package de.amr.games.pacman.view.theme.arcade;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.arcade.Pellet;
import de.amr.games.pacman.model.world.components.Door.DoorState;
import de.amr.games.pacman.view.api.IWorldRenderer;

class WorldRenderer implements IWorldRenderer {

	private WorldSpriteMap spriteMap;

	public WorldRenderer(WorldSpriteMap spriteMap) {
		this.spriteMap = spriteMap;
	}

	@Override
	public void render(Graphics2D g, World world) {
		if (world.isChanging()) {
			String selectedKey = spriteMap.selectedKey();
			if (!"maze-flashing".equals(selectedKey)) {
				spriteMap.select("maze-flashing");
				spriteMap.current().get().resetAnimation();
				spriteMap.current().get().enableAnimation(true);
			}
			spriteMap.current().get().draw(g, 0, 3 * Tile.SIZE);
		} else {
			spriteMap.select("maze-full");
			spriteMap.current().get().draw(g, 0, 3 * Tile.SIZE);
			drawContent(g, world);
			world.house(0).doors().filter(door -> door.state == DoorState.OPEN).forEach(door -> {
				g.setColor(Color.BLACK);
				door.tiles().forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE));
			});
		}
		spriteMap.getEnergizerAnimation().setEnabled(!world.isFrozen());
		spriteMap.getEnergizerAnimation().update();
	}

	private void drawContent(Graphics2D g, World world) {
		// hide eaten food
		Color eatenFoodColor = Color.BLACK;
		world.tiles().filter(world::hasEatenFood).forEach(tile -> {
			g.setColor(eatenFoodColor);
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// simulate energizer blinking animation
		if (spriteMap.getEnergizerAnimation().isEnabled() && spriteMap.getEnergizerAnimation().currentFrameIndex() == 1) {
			world.tiles().filter(tile -> world.hasFood(Pellet.ENERGIZER, tile)).forEach(tile -> {
				g.setColor(eatenFoodColor);
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		// draw bonus as image when active or as number when consumed
		world.bonusFood().ifPresent(bonus -> {
			if (bonus.isPresent()) {
				ArcadeBonus arcadeBonus = (ArcadeBonus) bonus;
				Vector2f position = Vector2f.of(bonus.location().x(), bonus.location().y() - Tile.SIZE / 2);
				Image img = ArcadeTheme.THEME.$image("symbol-" + arcadeBonus.symbol.name());
				g.drawImage(img, position.roundedX(), position.roundedY(), null);
			} else if (bonus.isConsumed()) {
				Vector2f position = Vector2f.of(bonus.location().x(), bonus.location().y() - Tile.SIZE / 2);
				Image img = ArcadeTheme.THEME.$image("points-" + bonus.value());
				g.drawImage(img, position.roundedX(), position.roundedY(), null);
			}
		});
	}
}