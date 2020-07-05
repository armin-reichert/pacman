package de.amr.games.pacman.view.render.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.function.Function;

import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Door.DoorState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.api.IWorldRenderer;

public class WorldRenderer implements IWorldRenderer {

	private final World world;
	private final ArcadeSprites theme;
	private final SpriteMap mazeSprites;
	private final SpriteAnimation energizerAnimation;
	private Function<Tile, Color> fnEatenFoodColor;

	public WorldRenderer(World world, ArcadeSprites theme) {
		this.world = world;
		this.theme = theme;
		fnEatenFoodColor = tile -> Color.BLACK;
		mazeSprites = new SpriteMap();
		mazeSprites.set("maze-full", theme.spr_fullMaze());
		mazeSprites.set("maze-flashing", theme.spr_flashingMaze());
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
		// draw bonus when active or consumed
		world.getBonus().filter(bonus -> bonus.state != BonusState.INACTIVE).ifPresent(bonus -> {
			Sprite sprite = bonus.state == BonusState.CONSUMED ? theme.spr_number(bonus.value)
					: theme.spr_bonusSymbol(bonus.symbol);
			g.drawImage(sprite.frame(0), world.bonusTile().x(), world.bonusTile().y() - Tile.SIZE / 2, null);
		});
	}
}