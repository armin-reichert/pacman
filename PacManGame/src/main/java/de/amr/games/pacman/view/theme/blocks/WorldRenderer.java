package de.amr.games.pacman.view.theme.blocks;

import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.function.Function;

import de.amr.easy.game.Application;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Bonus;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Door.DoorState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IWorldRenderer;

class WorldRenderer implements IWorldRenderer {

	static final Font font = new Font(Font.SANS_SERIF, Font.BOLD, 10);

	private final World world;

	public WorldRenderer(World world) {
		this.world = world;
	}

	@Override
	public void setEatenFoodColor(Function<Tile, Color> fnEatenFoodColor) {
	}

	@Override
	public void render(Graphics2D g) {
		drawEmptyMaze(g);
		if (!world.isChangingLevel()) {
			drawMazeContent(g);
		}
		// draw doors depending on their state
		world.theHouse().doors().forEach(door -> {
			g.setColor(door.state == DoorState.CLOSED ? Color.PINK : Color.BLACK);
			door.tiles.forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE / 4));
		});
	}

	private void drawMazeContent(Graphics2D g) {
		smoothDrawingOn(g);
		for (int row = 0; row < world.height(); ++row) {
			for (int col = 0; col < world.width(); ++col) {
				if (world.containsEnergizer(Tile.at(col, row))) {
					drawEnergizer(g, row, col);
				} else if (world.containsSimplePellet(Tile.at(col, row))) {
					drawSimplePellet(g, row, col);
				}
			}
		}
		// draw bonus as image when active or as number when consumed
		world.getBonus().ifPresent(bonus -> {
			Vector2f center = Vector2f.of(world.bonusTile().x() + Tile.SIZE, world.bonusTile().y() + Tile.SIZE / 2);
			if (bonus.state == BonusState.ACTIVE) {
				drawActiveBonus(g, center, bonus);
			} else if (bonus.state == BonusState.CONSUMED) {
				drawConsumedBonus(g, center, bonus);
			}
		});
		smoothDrawingOff(g);
	}

	private void drawActiveBonus(Graphics2D g, Vector2f center, Bonus bonus) {
		if (Application.app().clock().getTotalTicks() % 60 < 30) {
			return; // blink effect
		}
		drawBonusShape(g, center, bonus);
		try (Pen pen = new Pen(g)) {
			pen.color(Color.GREEN);
			pen.font(font);
			String text = bonus.symbol.substring(0, 1) + bonus.symbol.substring(1).toLowerCase();
			pen.drawCentered(text, center.x, center.y + Tile.SIZE / 2);
		}
	}

	private void drawBonusShape(Graphics2D g, Vector2f center, Bonus bonus) {
		int radius = 4;
		g.setColor(BlocksTheme.symbolColor(bonus.symbol));
		g.fillOval(center.roundedX() - radius, center.roundedY() - radius, 2 * radius, 2 * radius);
	}

	private void drawConsumedBonus(Graphics2D g, Vector2f center, Bonus bonus) {
		try (Pen pen = new Pen(g)) {
			pen.color(Color.GREEN);
			pen.font(font);
			String text = String.valueOf(bonus.value);
			pen.drawCentered(text, center.x, center.y + 4);
		}
	}

	private void drawSimplePellet(Graphics2D g, int row, int col) {
		g.setColor(Color.PINK);
		g.fillOval(col * Tile.SIZE + 3, row * Tile.SIZE + 3, 2, 2);
	}

	private void drawEnergizer(Graphics2D g, int row, int col) {
		int size = Tile.SIZE;
		int x = col * Tile.SIZE + (Tile.SIZE - size) / 2;
		int y = row * Tile.SIZE + (Tile.SIZE - size) / 2;
		g.translate(x, y);
		if (!world.isFrozen() && app().clock().getTotalTicks() % 60 < 30) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, size, size);
		} else {
			g.setColor(Color.PINK);
			g.fillOval(0, 0, size, size);
		}
		g.translate(-x, -y);
	}

	private void drawEmptyMaze(Graphics2D g) {
		for (int row = 0; row < world.height(); ++row) {
			for (int col = 0; col < world.width(); ++col) {
				if (!world.isAccessible(Tile.at(col, row))) {
					drawWall(g, row, col);
				} else {
					drawPassage(g, row, col);
				}
			}
		}
	}

	private void drawPassage(Graphics2D g, int row, int col) {
		g.setColor(Color.BLACK);
		g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
	}

	private void drawWall(Graphics2D g, int row, int col) {
		g.setColor(new Color(139, 69, 19));
		g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
	}
}