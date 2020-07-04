package de.amr.games.pacman.view.render.block;

import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.function.Function;

import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Door.DoorState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class WorldRenderer implements IWorldRenderer {

	private final World world;

	public WorldRenderer(World world, Theme theme) {
		this.world = world;
	}

	@Override
	public void setEatenFoodColor(Function<Tile, Color> fnEatenFoodColor) {
	}

	@Override
	public void draw(Graphics2D g) {
		drawEmptyMaze(g);
		if (!world.isChangingLevel()) {
			drawMazeContent(g);
		}
		// draw doors depending on their state
		world.theHouse().doors().forEach(door -> {
			g.setColor(door.state == DoorState.CLOSED ? Color.PINK : Color.BLACK);
			door.tiles.forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE/2));
		});
	}

	private void drawMazeContent(Graphics2D g) {
		smoothOn(g);
		for (int row = 0; row < world.height(); ++row) {
			for (int col = 0; col < world.width(); ++col) {
				if (world.containsEnergizer(Tile.at(col, row))) {
					drawEnergizer(g, row, col);
				} else if (world.containsSimplePellet(Tile.at(col, row))) {
					drawSimplePellet(g, row, col);
				}
			}
		}
		smoothOff(g);
	}

	private void drawSimplePellet(Graphics2D g, int row, int col) {
		g.setColor(Color.PINK);
		g.fillOval(col * Tile.SIZE + 3, row * Tile.SIZE + 3, 2, 2);
	}

	private void drawEnergizer(Graphics2D g, int row, int col) {
		if (!world.isFrozen() && app().clock().getTotalTicks() % 30 < 15) {
			g.setColor(Color.BLACK);
			g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
		} else {
			g.setColor(Color.PINK);
			g.fillOval(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
		}
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