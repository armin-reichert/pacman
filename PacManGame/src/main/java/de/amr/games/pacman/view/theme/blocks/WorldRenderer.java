package de.amr.games.pacman.view.theme.blocks;

import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.arcade.ArcadeFood;
import de.amr.games.pacman.model.world.components.Door.DoorState;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.view.api.IWorldRenderer;
import de.amr.games.pacman.view.common.Rendering;

class WorldRenderer implements IWorldRenderer {

	@Override
	public void render(Graphics2D g, World world) {
		drawEmptyWorld(g, world);
		if (!world.isChanging()) {
			drawFood(g, world);
		}
		// draw doors depending on their state
		world.houses().flatMap(House::doors).forEach(door -> {
			g.setColor(door.state == DoorState.CLOSED ? Color.PINK : Color.BLACK);
			door.tiles().forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE / 4));
		});
	}

	private void drawFood(Graphics2D g, World world) {
		Rendering.smoothOn(g);
		world.tiles().forEach(location -> {
			if (world.hasFood(ArcadeFood.ENERGIZER, location)) {
				drawEnergizer(g, world, location);
			} else if (world.hasFood(ArcadeFood.PELLET, location)) {
				drawSimplePellet(g, location);
			}
		});
		world.bonusFood().ifPresent(bonus -> {
			Vector2f center = Vector2f.of(bonus.location().x() + Tile.SIZE, bonus.location().y() + Tile.SIZE / 2);
			if (bonus.isActive()) {
				if (bonus.isConsumed()) {
					drawConsumedBonus(g, center, bonus.value());
				} else {
					drawActiveBonus(g, center, ((ArcadeBonus) bonus));
				}
			}
		});
		Rendering.smoothOff(g);
	}

	private void drawActiveBonus(Graphics2D g, Vector2f center, ArcadeBonus symbol) {
		if (app().clock().getTotalTicks() % 60 < 30) {
			return; // blink effect
		}
		drawBonusSymbol(g, center, symbol);
		try (Pen pen = new Pen(g)) {
			pen.color(Color.GREEN);
			pen.font(BlocksTheme.THEME.$font("font"));
			String text = symbol.name().substring(0, 1) + symbol.name().substring(1).toLowerCase();
			pen.drawCentered(text, center.x, center.y + Tile.SIZE / 2);
		}
	}

	private void drawConsumedBonus(Graphics2D g, Vector2f center, int value) {
		try (Pen pen = new Pen(g)) {
			pen.color(Color.GREEN);
			pen.font(BlocksTheme.THEME.$font("font"));
			String text = String.valueOf(value);
			pen.drawCentered(text, center.x, center.y + 4);
		}
	}

	private void drawBonusSymbol(Graphics2D g, Vector2f center, ArcadeBonus symbol) {
		int radius = 4;
		g.setColor(BlocksTheme.THEME.symbolColor(symbol.name()));
		g.fillOval(center.roundedX() - radius, center.roundedY() - radius, 2 * radius, 2 * radius);
	}

	private void drawSimplePellet(Graphics2D g, Tile location) {
		g.setColor(Color.PINK);
		g.fillOval(location.x() + 3, location.y() + 3, 2, 2);
	}

	private void drawEnergizer(Graphics2D g, World world, Tile location) {
		int size = Tile.SIZE;
		int x = location.x() + (Tile.SIZE - size) / 2;
		int y = location.y() + (Tile.SIZE - size) / 2;
		g.translate(x, y);
		// create blink effect
		if (!world.isFrozen() && app().clock().getTotalTicks() % 60 < 30) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, size, size);
		} else {
			g.setColor(Color.PINK);
			g.fillOval(0, 0, size, size);
		}
		g.translate(-x, -y);
	}

	private void drawEmptyWorld(Graphics2D g, World world) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, world.width() * Tile.SIZE, world.height() * Tile.SIZE);
		for (int row = 0; row < world.height(); ++row) {
			for (int col = 0; col < world.width(); ++col) {
				if (!world.isAccessible(Tile.at(col, row))) {
					drawWall(g, world, row, col);
				}
			}
		}
	}

	private void drawWall(Graphics2D g, World world, int row, int col) {
		if (world.isChanging() && app().clock().getTotalTicks() % 30 < 15) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(BlocksTheme.THEME.$color("wall-color"));
		}
		g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
	}
}