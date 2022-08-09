/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.theme.blocks;

import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.math.V2f.v;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.math.V2f;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.api.TiledWorld;
import de.amr.games.pacmanfsm.model.world.arcade.ArcadeBonus;
import de.amr.games.pacmanfsm.model.world.arcade.ArcadeFood;
import de.amr.games.pacmanfsm.model.world.components.Door.DoorState;
import de.amr.games.pacmanfsm.model.world.components.House;
import de.amr.games.pacmanfsm.theme.api.WorldRenderer;
import de.amr.games.pacmanfsm.view.common.Rendering;

/**
 * @author Armin Reichert
 */
class BlocksWorldRenderer implements WorldRenderer {

	@Override
	public void render(Graphics2D g, TiledWorld world) {
		drawEmptyWorld(g, world);
		if (!world.isChanging()) {
			drawFood(g, world);
		}
		// draw doors depending on their state
		world.houses().flatMap(House::doors).forEach(door -> {
			g.setColor(door.state == DoorState.CLOSED ? Color.PINK : Color.BLACK);
			door.tiles().forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.TS, Tile.TS / 4));
		});
	}

	private void drawFood(Graphics2D g, TiledWorld world) {
		Rendering.smoothOn(g);
		world.tiles().forEach(location -> {
			if (world.hasFood(ArcadeFood.ENERGIZER, location)) {
				drawEnergizer(g, world, location);
			} else if (world.hasFood(ArcadeFood.PELLET, location)) {
				drawSimplePellet(g, location);
			}
		});
		world.temporaryFood().ifPresent(bonus -> {
			V2f center = v(bonus.location().x() + Tile.TS, bonus.location().y() + Tile.TS / 2);
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

	private void drawActiveBonus(Graphics2D g, V2f center, ArcadeBonus bonus) {
		if (app().clock().getTotalTicks() % 60 < 30) {
			return; // blink effect
		}
		drawBonusSymbol(g, center, bonus);
		try (Pen pen = new Pen(g)) {
			pen.color(Color.GREEN);
			pen.font(BlocksTheme.THEME.asFont("font"));
			String text = bonus.symbol.name().substring(0, 1) + bonus.symbol.name().substring(1).toLowerCase();
			pen.drawCentered(text, center.x(), center.y() + Tile.TS / 2);
		}
	}

	private void drawConsumedBonus(Graphics2D g, V2f center, int value) {
		try (Pen pen = new Pen(g)) {
			pen.color(Color.GREEN);
			pen.font(BlocksTheme.THEME.asFont("font"));
			String text = String.valueOf(value);
			pen.drawCentered(text, center.x(), center.y() + 4);
		}
	}

	private void drawBonusSymbol(Graphics2D g, V2f center, ArcadeBonus bonus) {
		int radius = 4;
		g.setColor(BlocksTheme.THEME.symbolColor(bonus.symbol.name()));
		g.fillOval(center.roundedX() - radius, center.roundedY() - radius, 2 * radius, 2 * radius);
	}

	private void drawSimplePellet(Graphics2D g, Tile location) {
		g.setColor(Color.PINK);
		g.fillOval(location.x() + 3, location.y() + 3, 2, 2);
	}

	private void drawEnergizer(Graphics2D g, TiledWorld world, Tile location) {
		int size = Tile.TS;
		int x = location.x() + (Tile.TS - size) / 2;
		int y = location.y() + (Tile.TS - size) / 2;
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

	private void drawEmptyWorld(Graphics2D g, TiledWorld world) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, world.width() * Tile.TS, world.height() * Tile.TS);
		for (int row = 0; row < world.height(); ++row) {
			for (int col = 0; col < world.width(); ++col) {
				if (!world.isAccessible(Tile.at(col, row))) {
					drawWall(g, world, row, col);
				}
			}
		}
	}

	private void drawWall(Graphics2D g, TiledWorld world, int row, int col) {
		if (world.isChanging() && app().clock().getTotalTicks() % 30 < 15) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(BlocksTheme.THEME.asColor("wall-color"));
		}
		g.fillRect(col * Tile.TS, row * Tile.TS, Tile.TS, Tile.TS);
	}
}