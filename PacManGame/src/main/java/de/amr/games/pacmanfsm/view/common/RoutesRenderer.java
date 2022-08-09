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
package de.amr.games.pacmanfsm.view.common;

import static de.amr.games.pacmanfsm.PacManApp.appSettings;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacmanfsm.view.common.Rendering.alpha;
import static de.amr.games.pacmanfsm.view.common.Rendering.drawDirectionIndicator;
import static de.amr.games.pacmanfsm.view.common.Rendering.ghostColor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;
import java.util.Optional;

import de.amr.easy.game.math.V2f;
import de.amr.games.pacmanfsm.controller.creatures.Folks;
import de.amr.games.pacmanfsm.controller.creatures.ghost.Ghost;
import de.amr.games.pacmanfsm.controller.creatures.pacman.PacMan;
import de.amr.games.pacmanfsm.controller.steering.api.Steering;
import de.amr.games.pacmanfsm.lib.Direction;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.api.TiledWorld;

/**
 * Renderes the routes of the creatures towards their current target tiles.
 * 
 * @author Armin Reichert
 */
public class RoutesRenderer {

	public void renderRoutes(Graphics2D g, Folks folks) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (folks.pacMan.visible) {
			drawPacManRoute(g, folks.pacMan);
		}
		folks.ghostsInWorld().filter(ghost -> ghost.visible).forEach(ghost -> drawGhostRoute(g, ghost));
		if (folks.inky.visible) {
			drawInkyChasing(g, folks);
		}
		if (folks.clyde.visible) {
			drawClydeChasingArea(g, folks);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public void drawPacManRoute(Graphics2D g, PacMan pacMan) {
		drawTargetTilePath(g, pacMan.getSteering().pathToTarget(), Color.YELLOW);
	}

	public void drawGhostRoute(Graphics2D g, Ghost ghost) {
		Steering steering = ghost.getSteering();
		if (steering.targetTile().isPresent()) {
			drawTargetTileRubberband(g, ghost, steering.targetTile());
			drawTargetTilePath(g, steering.pathToTarget(), ghostColor(ghost));
			return;
		}
		if (ghost.wishDir != null) {
			V2f center = ghost.tf.getCenter();
			V2f dirVector = ghost.wishDir.vector();
			drawDirectionIndicator(g, ghostColor(ghost), true, ghost.wishDir, (int) (center.x() + dirVector.x() * Tile.TS),
					(int) (center.y() + dirVector.y() * Tile.TS));
		}
	}

	private void drawTargetTileRubberband(Graphics2D g, Ghost ghost, Optional<Tile> targetTile) {
		if (targetTile.isEmpty()) {
			return;
		}
		g = (Graphics2D) g.create();
		Rendering.smoothOn(g);

		// draw dashed line from ghost position to target tile
		Stroke dashed = new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
		int x1 = ghost.tf.getCenter().roundedX();
		int y1 = ghost.tf.getCenter().roundedY();
		int x2 = targetTile.get().centerX();
		int y2 = targetTile.get().centerY();
		g.setStroke(dashed);
		g.setColor(alpha(ghostColor(ghost), 200));
		g.drawLine(x1, y1, x2, y2);

		// draw solid rectangle indicating target tile
		g.translate(targetTile.get().x(), targetTile.get().y());
		g.setColor(ghostColor(ghost));
		g.setStroke(new BasicStroke(0.5f));
		g.fillRect(2, 2, 4, 4);
		g.dispose();
	}

	private void drawTargetTilePath(Graphics2D g, List<Tile> path, Color ghostColor) {
		if (path == null || path.size() <= 1) {
			return;
		}
		g = (Graphics2D) g.create();
		Rendering.smoothOn(g);
		g.setStroke(new BasicStroke(0.5f));
		g.setColor(alpha(ghostColor, 200));
		Tile[] tiles = path.toArray(Tile[]::new);
		int from = 0;
		int to = 1;
		while (to < tiles.length) {
			g.drawLine(tiles[from].centerX(), tiles[from].centerY(), tiles[to].centerX(), tiles[to].centerY());
			if (to == tiles.length - 1) {
				Optional<Direction> optDir = tiles[from].dirTo(tiles[to]);
				if (optDir.isPresent()) {
					drawDirectionIndicator(g, ghostColor, true, optDir.get(), tiles[to].centerX(), tiles[to].centerY());
				}
			}
			++from;
			++to;
		}
		g.dispose();
	}

	private void drawInkyChasing(Graphics2D g, Folks folks) {
		PacMan pacMan = folks.pacMan;
		TiledWorld world = pacMan.world;
		Ghost inky = folks.inky;
		Ghost blinky = folks.blinky;
		var inkyTargetTile = inky.getSteering().targetTile();
		if (inkyTargetTile.isEmpty()) {
			return;
		}
		if (!inky.ai.is(CHASING) || !world.contains(blinky)) {
			return;
		}
		int x1 = blinky.tile().centerX();
		int y1 = blinky.tile().centerY();
		int x2 = inkyTargetTile.get().centerX();
		int y2 = inkyTargetTile.get().centerY();
		g.setColor(Color.GRAY);
		g.drawLine(x1, y1, x2, y2);
		Tile pacManTile = pacMan.tile();
		Direction pacManDir = pacMan.moveDir;
		int s = Tile.TS / 2; // size of target square
		g.setColor(Color.GRAY);
		if (!appSettings.fixOverflowBug && pacManDir == Direction.UP) {
			Tile twoAhead = world.tileToDir(pacManTile, pacManDir, 2);
			Tile twoLeft = world.tileToDir(twoAhead, Direction.LEFT, 2);
			x1 = pacManTile.centerX();
			y1 = pacManTile.centerY();
			x2 = twoAhead.centerX();
			y2 = twoAhead.centerY();
			int x3 = twoLeft.centerX();
			int y3 = twoLeft.centerY();
			g.drawLine(x1, y1, x2, y2);
			g.drawLine(x2, y2, x3, y3);
			g.fillRect(x3 - s / 2, y3 - s / 2, s, s);
		} else {
			Tile twoTilesAhead = pacMan.tilesAhead(2);
			x1 = pacManTile.centerX();
			y1 = pacManTile.centerY();
			x2 = twoTilesAhead.centerX();
			y2 = twoTilesAhead.centerY();
			g.drawLine(x1, y1, x2, y2);
			g.fillRect(x2 - s / 2, y2 - s / 2, s, s);
		}
	}

	private void drawClydeChasingArea(Graphics2D g, Folks folks) {
		Ghost clyde = folks.clyde;
		if (!clyde.ai.is(CHASING)) {
			return;
		}
		Color ghostColor = ghostColor(clyde);
		int cx = clyde.tile().centerX();
		int cy = clyde.tile().centerY();
		int r = 8 * Tile.TS;
		g.setColor(alpha(ghostColor, 200));
		g.setStroke(new BasicStroke(0.2f));
		g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
	}
}