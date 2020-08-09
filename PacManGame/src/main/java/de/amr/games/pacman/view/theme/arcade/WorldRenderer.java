package de.amr.games.pacman.view.theme.arcade;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Symbol;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.arcade.Pellet;
import de.amr.games.pacman.model.world.components.Door.DoorState;
import de.amr.games.pacman.view.api.IWorldRenderer;

public class WorldRenderer implements IWorldRenderer {

	private final Map<String, Image> symbolImages = new HashMap<>();
	private final Map<Integer, Image> pointsImages = new HashMap<>();
	private final SpriteMap mazeSprites;
	private final SpriteAnimation energizerAnimation;

	public WorldRenderer() {
		ArcadeThemeSprites arcadeSprites = ArcadeTheme.THEME.$value("sprites");
		for (Symbol symbol : Symbol.values()) {
			symbolImages.put(symbol.name(), arcadeSprites.makeSprite_bonusSymbol(symbol.name()).frame(0));
		}
		for (int points : Game.POINTS_BONUS) {
			pointsImages.put(points, arcadeSprites.makeSprite_number(points).frame(0));

		}
		mazeSprites = new SpriteMap();
		mazeSprites.set("maze-full", arcadeSprites.makeSprite_fullMaze());
		mazeSprites.set("maze-flashing", arcadeSprites.makeSprite_flashingMaze());
		energizerAnimation = new CyclicAnimation(2);
		energizerAnimation.setFrameDuration(150);
		energizerAnimation.setEnabled(false);
	}

	@Override
	public void render(Graphics2D g, World world) {
		if (world.isChanging()) {
			if (!mazeSprites.selectedKey().equals("maze-flashing")) {
				mazeSprites.select("maze-flashing");
				mazeSprites.current().get().resetAnimation();
			}
		} else {
			mazeSprites.select("maze-full");
			drawContent(g, world);
			world.house(0).doors().filter(door -> door.state == DoorState.OPEN).forEach(door -> {
				g.setColor(Color.BLACK);
				door.tiles().forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE));
			});
		}
		energizerAnimation.setEnabled(!world.isFrozen());
		energizerAnimation.update();
		mazeSprites.current().get().draw(g, 0, 3 * Tile.SIZE);
	}

	private void drawContent(Graphics2D g, World world) {
		// hide eaten food
		Color eatenFoodColor = Color.BLACK; // TODO handle grid pattern
		world.tiles().filter(world::hasEatenFood).forEach(tile -> {
			g.setColor(eatenFoodColor);
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// simulate energizer blinking animation
		if (energizerAnimation.isEnabled() && energizerAnimation.currentFrameIndex() == 1) {
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
				g.drawImage(symbolImages.get(arcadeBonus.symbol.name()), position.roundedX(), position.roundedY(), null);
			} else if (bonus.isConsumed()) {
				Vector2f position = Vector2f.of(bonus.location().x(), bonus.location().y() - Tile.SIZE / 2);
				g.drawImage(pointsImages.get(bonus.value()), position.roundedX(), position.roundedY(), null);
			}
		});
	}
}