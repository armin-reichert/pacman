package de.amr.games.pacman.view.theme.arcade;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.world.arcade.Symbol;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.BonusState;
import de.amr.games.pacman.model.world.api.Door.DoorState;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.api.IWorldRenderer;

public class WorldRenderer implements IWorldRenderer {

	private final World world;
	private final Map<String, Image> symbolImages = new HashMap<>();
	private final Map<Integer, Image> pointsImages = new HashMap<>();
	private final SpriteMap mazeSprites;
	private final SpriteAnimation energizerAnimation;
	private Function<Tile, Color> fnEatenFoodColor;

	public WorldRenderer(World world) {
		this.world = world;
		fnEatenFoodColor = tile -> Color.BLACK;
		ArcadeThemeSprites arcadeSprites = ArcadeTheme.IT.$value("sprites");
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
	public void setEatenFoodColor(Function<Tile, Color> fnEatenFoodColor) {
		this.fnEatenFoodColor = fnEatenFoodColor;
	}

	@Override
	public void render(Graphics2D g) {
		if (world.isChanging()) {
			mazeSprites.select("maze-flashing");
			mazeSprites.current().get().draw(g, 0, 3 * Tile.SIZE);
		} else {
			mazeSprites.select("maze-full");
			mazeSprites.current().get().draw(g, 0, 3 * Tile.SIZE);
			drawMazeContent(g);
			// draw doors depending on their state
			world.theHouse().doors().filter(door -> door.state == DoorState.OPEN).forEach(door -> {
				g.setColor(Color.BLACK);
				door.tiles().forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE));
			});
			// draw portals
			world.portals().forEach(portal -> {
				g.setColor(Color.BLACK);
				g.fillRect(portal.left.x(), portal.left.y(), Tile.SIZE, Tile.SIZE);
				g.fillRect(portal.right.x(), portal.right.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		energizerAnimation.setEnabled(!world.isFrozen());
		energizerAnimation.update();
	}

	@Override
	public void enableAnimation(boolean enabled) {
		mazeSprites.current().ifPresent(sprite -> {
			sprite.enableAnimation(enabled);
		});
	}

	private void drawMazeContent(Graphics2D g) {
		// hide eaten food
		world.habitat().filter(world::didContainFood).forEach(tile -> {
			g.setColor(fnEatenFoodColor.apply(tile));
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// simulate energizer blinking animation
		if (energizerAnimation.isEnabled() && energizerAnimation.currentFrameIndex() == 1) {
			world.habitat().filter(world::containsEnergizer).forEach(tile -> {
				g.setColor(fnEatenFoodColor.apply(tile));
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		// draw bonus as image when active or as number when consumed
		world.getBonus().ifPresent(bonus -> {
			Vector2f position = Vector2f.of(bonus.location.x(), bonus.location.y() - Tile.SIZE / 2);
			if (bonus.state == BonusState.ACTIVE) {
				g.drawImage(symbolImages.get(bonus.symbol), position.roundedX(), position.roundedY(), null);
			} else if (bonus.state == BonusState.CONSUMED) {
				g.drawImage(pointsImages.get(bonus.value), position.roundedX(), position.roundedY(), null);
			}
		});
	}
}