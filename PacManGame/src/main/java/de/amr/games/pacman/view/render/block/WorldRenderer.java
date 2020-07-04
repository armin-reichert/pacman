package de.amr.games.pacman.view.render.block;

import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.function.Function;

import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class WorldRenderer implements IWorldRenderer {

	enum Mode {
		EMPTY, EMPTY_FLASHING, FULL
	}

	private final World world;
	private Mode mode;
	private boolean energizersBlink;

	public WorldRenderer(World world, Theme theme) {
		this.world = world;
		mode = Mode.FULL;
	}

	@Override
	public void setEatenFoodColor(Function<Tile, Color> fnEatenFoodColor) {
	}

	@Override
	public void draw(Graphics2D g) {
		drawEmptyMaze(g);
		if (mode == Mode.FULL) {
			drawMazeContent(g);
		}
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
		if (energizersBlink && app().clock().getTotalTicks() % 20 < 10) {
			g.setColor(Color.BLACK);
			g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
		} else {
			g.setColor(Color.PINK);
			g.fillOval(col * Tile.SIZE, row * Tile.SIZE, 8, 8);
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

	@Override
	public void letEnergizersBlink(boolean blink) {
		energizersBlink = blink;
	}

	@Override
	public void turnMazeFlashingOn() {
		mode = Mode.EMPTY_FLASHING;
	}

	@Override
	public void turnMazeFlashingOff() {
		mode = Mode.EMPTY;
	}

	@Override
	public void turnFullMazeOn() {
		mode = Mode.FULL;
	}
}