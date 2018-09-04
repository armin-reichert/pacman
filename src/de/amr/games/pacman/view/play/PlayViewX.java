package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.model.Game.TS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Level;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.TilePlacement;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.statemachine.State;

/**
 * An extended play view.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Key 'l' toggles logging on/off
 * <li>Can display grid and alignment of actors (key 'g')
 * <li>Can display actor state (key 's')
 * <li>Can display actor routes (key 'r')
 * <li>Can switch ghosts on and off (keys 'b', 'p', 'i', 'c')
 * <li>Cheat key 'k' kills all active ghosts
 * <li>Cheat key 'e' eats all normal pellets
 * </ul>
 * 
 * @author Armin Reichert
 */
public class PlayViewX extends PlayView {

	private static final String INFTY = Character.toString('\u221E');

	private final BufferedImage gridImage;

	public boolean showGrid = false;
	public boolean showRoutes = false;
	public boolean showStates = false;

	private static BufferedImage createGridImage(int numRows, int numCols) {
		GraphicsConfiguration conf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage image = conf.createCompatibleImage(numCols * TS, numRows * TS + 1,
				Transparency.TRANSLUCENT);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.DARK_GRAY);
		for (int row = 0; row <= numRows; ++row) {
			g.drawLine(0, row * TS, numCols * TS, row * TS);
		}
		for (int col = 1; col < numCols; ++col) {
			g.drawLine(col * TS, 0, col * TS, numRows * TS);
		}
		return image;
	}

	public PlayViewX(Game game) {
		super(game);
		gridImage = createGridImage(game.getMaze().numRows(), game.getMaze().numCols());
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			LOGGER.setLevel(LOGGER.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_G)) {
			showGrid = !showGrid;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_S)) {
			showStates = !showStates;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			showRoutes = !showRoutes;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_K)) {
			killActiveGhosts();
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_E)) {
			eatAllPellets();
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_B)) {
			toggleGhost(actors.blinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_P)) {
			toggleGhost(actors.pinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_I)) {
			toggleGhost(actors.inky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			toggleGhost(actors.clyde);
		}
		super.update();
	}

	private void killActiveGhosts() {
		actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(new GhostKilledEvent(ghost)));
	}

	public void eatAllPellets() {
		game.getMaze().tiles().filter(game.getMaze()::isPellet).forEach(game::eatFoodAtTile);
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		if (showGrid) {
			g.drawImage(gridImage, 0, 0, null);
			drawActorAlignment(actors.pacMan, g);
			actors.getActiveGhosts().filter(Ghost::isVisible).forEach(ghost -> drawActorAlignment(ghost, g));
		}
		if (showRoutes) {
			actors.getActiveGhosts().filter(Ghost::isVisible).forEach(ghost -> drawRoute(g, ghost));
		}
		if (showStates) {
			drawEntityStates(g);
		}
	}

	private void drawEntityStates(Graphics2D g) {
		if (actors.pacMan.getState() != null) {
			drawText(g, Color.YELLOW, actors.pacMan.tf().getX(), actors.pacMan.tf().getY(),
					pacManState(actors.pacMan));
		}
		actors.getActiveGhosts().filter(Ghost::isVisible).forEach(ghost -> {
			if (ghost.getState() != null) {
				drawText(g, ghostColor(ghost), ghost.tf().getX() - TS, ghost.tf().getY(), ghostState(ghost));
			}
		});
	}

	private String pacManState(PacMan pacMan) {
		State<?, ?> state = pacMan.getStateObject();
		return state.getDuration() != State.ENDLESS
				? String.format("(%s,%d|%d)", state.id(), state.getRemaining(), state.getDuration())
				: String.format("(%s,%s)", state.id(), INFTY);
	}

	private String ghostState(Ghost ghost) {
		State<?, ?> state = ghost.getStateObject();
		return state.getDuration() != State.ENDLESS
				? String.format("%s(%s,%d|%d)[%s]", ghost.getName(), state.id(), state.getRemaining(),
						state.getDuration(), Top4.name(ghost.getCurrentDir()))
				: String.format("%s(%s,%s)[%s]", ghost.getName(), state.id(), INFTY,
						Top4.name(ghost.getCurrentDir()));
	}

	private void toggleGhost(Ghost ghost) {
		actors.setActive(ghost, !actors.isActive(ghost));
	}

	private static Color ghostColor(Ghost ghost) {
		switch (ghost.getName()) {
		case "Blinky":
			return Color.RED;
		case "Pinky":
			return Color.PINK;
		case "Inky":
			return new Color(64, 224, 208);
		case "Clyde":
			return Color.ORANGE;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void drawText(Graphics2D g, Color color, float x, float y, String text) {
		g.translate(x, y);
		g.setColor(color);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TS / 2));
		g.drawString(text, 0, -TS / 2);
		g.translate(-x, -y);
	}

	private void drawActorAlignment(TilePlacement actor, Graphics2D g) {
		g.setColor(Color.GREEN);
		g.translate(actor.tf().getX(), actor.tf().getY());
		int w = actor.tf().getWidth(), h = actor.tf().getHeight();
		if (actor.getTileAlignmentY() == 0) {
			g.drawLine(0, 0, w, 0);
			g.drawLine(0, h, w, h);
		}
		if (actor.getTileAlignmentX() == 0) {
			g.drawLine(0, 0, 0, h);
			g.drawLine(w, 0, w, h);
		}
		g.translate(-actor.tf().getX(), -actor.tf().getY());
	}

	private void drawRoute(Graphics2D g, Ghost ghost) {
		g.setColor(ghostColor(ghost));
		MazeRoute route = ghost.getMoveBehavior().computeRoute(ghost);
		List<Tile> path = route.getPath();

		if (path.size() > 1) {
			for (int i = 0; i < path.size() - 1; ++i) {
				Tile u = path.get(i), v = path.get(i + 1);
				int u1 = u.col * TS + TS / 2;
				int u2 = u.row * TS + TS / 2;
				int v1 = v.col * TS + TS / 2;
				int v2 = v.row * TS + TS / 2;
				g.drawLine(u1, u2, v1, v2);
			}
			Tile targetTile = path.get(path.size() - 1);
			g.translate(targetTile.col * TS, targetTile.row * TS);
			g.fillRect(TS / 4, TS / 4, TS / 2, TS / 2);
			g.translate(-targetTile.col * TS, -targetTile.row * TS);
		} else if (route.getTargetTile() != null) {
			g.drawLine((int) ghost.tf().getCenter().x, (int) ghost.tf().getCenter().y,
					route.getTargetTile().col * TS + TS / 2, route.getTargetTile().row * TS + TS / 2);
			g.translate(route.getTargetTile().col * TS, route.getTargetTile().row * TS);
			g.fillRect(TS / 4, TS / 4, TS / 2, TS / 2);
			g.translate(-route.getTargetTile().col * TS, -route.getTargetTile().row * TS);
		}

		if (ghost == actors.clyde && ghost.getState() == GhostState.CHASING) {
			Vector2f center = ghost.tf().getCenter();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawOval((int) center.x - 8 * TS, (int) center.y - 8 * TS, 16 * TS, 16 * TS);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}
}