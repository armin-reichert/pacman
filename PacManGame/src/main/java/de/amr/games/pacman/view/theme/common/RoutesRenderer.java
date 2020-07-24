package de.amr.games.pacman.view.theme.common;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.view.theme.common.Rendering.alpha;
import static de.amr.games.pacman.view.theme.common.Rendering.drawDirectionIndicator;
import static de.amr.games.pacman.view.theme.common.Rendering.ghostColor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;
import java.util.Optional;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.api.IRenderer;

/**
 * Renderes the routes of the creatures towards their current target tiles.
 * 
 * @author Armin Reichert
 */
public class RoutesRenderer implements IRenderer {

	private final World world;
	private final Folks folks;

	public RoutesRenderer(World world, Folks folks) {
		this.world = world;
		this.folks = folks;
	}

	@Override
	public void render(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (folks.pacMan.isVisible()) {
			drawPacManRoute(g, folks.pacMan);
		}
		folks.ghosts().filter(world::contains).filter(Ghost::isVisible).forEach(ghost -> drawGhostRoute(g, ghost));
		if (folks.inky.isInsideWorld() && folks.inky.isVisible()) {
			drawInkyChasing(g, folks.inky);
		}
		if (folks.clyde.isInsideWorld() && folks.clyde.isVisible()) {
			drawClydeChasingArea(g, folks.clyde);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public void drawPacManRoute(Graphics2D g, PacMan pacMan) {
		if (pacMan.steering() instanceof PathProvidingSteering) {
			PathProvidingSteering steering = (PathProvidingSteering) pacMan.steering();
			drawTargetTilePath(g, steering.pathToTarget(), Color.YELLOW);
		}
	}

	public void drawGhostRoute(Graphics2D g, Ghost ghost) {
		if (ghost.steering() instanceof PathProvidingSteering && ghost.targetTile() != null) {
			drawTargetTileRubberband(g, ghost, ghost.targetTile());
			PathProvidingSteering steering = (PathProvidingSteering) ghost.steering();
			drawTargetTilePath(g, steering.pathToTarget(), Rendering.ghostColor(ghost));
		} else if (ghost.wishDir() != null) {
			Vector2f v = ghost.wishDir().vector();
			Rendering.drawDirectionIndicator(g, Rendering.ghostColor(ghost), true, ghost.wishDir(),
					ghost.entity.tf.getCenter().roundedX() + v.roundedX() * Tile.SIZE,
					ghost.entity.tf.getCenter().roundedY() + v.roundedY() * Tile.SIZE);
		}
	}

	private void drawTargetTileRubberband(Graphics2D g, Ghost ghost, Optional<Tile> targetTile) {
		if (targetTile.isEmpty()) {
			return;
		}
		g = (Graphics2D) g.create();
		smoothDrawingOn(g);

		// draw dashed line from ghost position to target tile
		Stroke dashed = new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
		int x1 = ghost.entity.tf.getCenter().roundedX(), y1 = ghost.entity.tf.getCenter().roundedY();
		int x2 = targetTile.get().centerX(), y2 = targetTile.get().centerY();
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
		smoothDrawingOn(g);
		g.setStroke(new BasicStroke(0.5f));
		g.setColor(alpha(ghostColor, 200));
		Tile[] tiles = path.toArray(Tile[]::new);
		int from = 0, to = 1;
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

	private void drawInkyChasing(Graphics2D g, Ghost inky) {
		PacMan pacMan = folks.pacMan;
		Ghost blinky = folks.blinky;
		if (!inky.is(CHASING) || inky.targetTile().isEmpty() || !world.contains(blinky)) {
			return;
		}
		int x1, y1, x2, y2, x3, y3;
		x1 = blinky.tileLocation().centerX();
		y1 = blinky.tileLocation().centerY();
		x2 = inky.targetTile().get().centerX();
		y2 = inky.targetTile().get().centerY();
		g.setColor(Color.GRAY);
		g.drawLine(x1, y1, x2, y2);
		Tile pacManTile = pacMan.tileLocation();
		Direction pacManDir = pacMan.moveDir();
		int s = Tile.SIZE / 2; // size of target square
		g.setColor(Color.GRAY);
		if (!settings.fixOverflowBug && pacManDir == Direction.UP) {
			Tile twoAhead = world.tileToDir(pacManTile, pacManDir, 2);
			Tile twoLeft = world.tileToDir(twoAhead, Direction.LEFT, 2);
			x1 = pacManTile.centerX();
			y1 = pacManTile.centerY();
			x2 = twoAhead.centerX();
			y2 = twoAhead.centerY();
			x3 = twoLeft.centerX();
			y3 = twoLeft.centerY();
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

	private void drawClydeChasingArea(Graphics2D g, Ghost clyde) {
		if (!clyde.is(CHASING)) {
			return;
		}
		Color ghostColor = ghostColor(clyde);
		int cx = clyde.tileLocation().centerX(), cy = clyde.tileLocation().centerY();
		int r = 8 * Tile.SIZE;
		g.setColor(alpha(ghostColor, 200));
		g.setStroke(new BasicStroke(0.2f));
		g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
	}
}