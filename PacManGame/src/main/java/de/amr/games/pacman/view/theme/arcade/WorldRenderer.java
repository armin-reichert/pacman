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
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.Symbol;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Door.DoorState;
import de.amr.games.pacman.view.theme.IWorldRenderer;
import de.amr.games.pacman.model.world.core.Tile;

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
		for (Symbol symbol : Symbol.values()) {
			symbolImages.put(symbol.name(), ArcadeSprites.BUNDLE.spr_bonusSymbol(symbol.name()).frame(0));
		}
		for (int points : Game.POINTS_BONUS) {
			pointsImages.put(points, ArcadeSprites.BUNDLE.spr_number(points).frame(0));

		}
		mazeSprites = new SpriteMap();
		mazeSprites.set("maze-full", ArcadeSprites.BUNDLE.spr_fullMaze());
		mazeSprites.set("maze-flashing", ArcadeSprites.BUNDLE.spr_flashingMaze());
		energizerAnimation = new CyclicAnimation(2);
		energizerAnimation.setFrameDuration(150);
		energizerAnimation.setEnabled(false);
	}

	@Override
	public void setEatenFoodColor(Function<Tile, Color> fnEatenFoodColor) {
		this.fnEatenFoodColor = fnEatenFoodColor;
	}

	@Override
	public void draw(Graphics2D g) {
		if (world.isChangingLevel()) {
			mazeSprites.select("maze-flashing");
			mazeSprites.current().get().draw(g, 0, 3 * Tile.SIZE);
		} else {
			mazeSprites.select("maze-full");
			mazeSprites.current().get().draw(g, 0, 3 * Tile.SIZE);
			drawMazeContent(g);
			// draw doors depending on their state
			world.theHouse().doors().filter(door -> door.state == DoorState.OPEN).forEach(door -> {
				g.setColor(Color.BLACK);
				door.tiles.forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE));
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
		world.habitatTiles().filter(world::containsEatenFood).forEach(tile -> {
			g.setColor(fnEatenFoodColor.apply(tile));
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// simulate energizer blinking animation
		if (energizerAnimation.isEnabled() && energizerAnimation.currentFrameIndex() == 1) {
			world.habitatTiles().filter(world::containsEnergizer).forEach(tile -> {
				g.setColor(fnEatenFoodColor.apply(tile));
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		// draw bonus as image when active or as number when consumed
		world.getBonus().ifPresent(bonus -> {
			Vector2f position = Vector2f.of(world.bonusTile().x(), world.bonusTile().y() - Tile.SIZE / 2);
			if (bonus.state == BonusState.ACTIVE) {
				g.drawImage(symbolImages.get(bonus.symbol), position.roundedX(), position.roundedY(), null);
			} else if (bonus.state == BonusState.CONSUMED) {
				g.drawImage(pointsImages.get(bonus.value), position.roundedX(), position.roundedY(), null);
			}
		});
	}
}