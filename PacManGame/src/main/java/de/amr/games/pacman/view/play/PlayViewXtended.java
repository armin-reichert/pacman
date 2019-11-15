package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static java.lang.Math.round;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.State;

/**
 * An extended play view.
 * 
 * <p>
 * Features:
 * <ul>
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
public class PlayViewXtended extends PlayView {

	private static final String INFTY = Character.toString('\u221E');

	private final BufferedImage gridImage;
	private boolean showGrid = false;
	private boolean showRoutes = false;
	private boolean showStates = false;

	private static BufferedImage createGridImage(int numRows, int numCols) {
		GraphicsConfiguration conf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage image = conf.createCompatibleImage(numCols * TS, numRows * TS + 1,
				Transparency.TRANSLUCENT);
		Graphics2D g = image.createGraphics();
		g.setColor(new Color(0, 60, 0));
		for (int row = 0; row <= numRows; ++row) {
			g.drawLine(0, row * TS, numCols * TS, row * TS);
		}
		for (int col = 1; col < numCols; ++col) {
			g.drawLine(col * TS, 0, col * TS, numRows * TS);
		}
		return image;
	}

	public PlayViewXtended(PacManGame game) {
		super(game);
		gridImage = createGridImage(Maze.ROWS, Maze.COLS);
	}

	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	public void setShowRoutes(boolean showRoutes) {
		this.showRoutes = showRoutes;
	}

	public void setShowStates(boolean showStates) {
		this.showStates = showStates;
	}

	public boolean isShowGrid() {
		return showGrid;
	}

	public boolean isShowRoutes() {
		return showRoutes;
	}

	public boolean isShowStates() {
		return showStates;
	}

	@Override
	public void update() {
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
			toggleGhost(game.blinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_P)) {
			toggleGhost(game.pinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_I)) {
			toggleGhost(game.inky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			toggleGhost(game.clyde);
		}
		super.update();
	}

	private void killActiveGhosts() {
		game.activeGhosts().forEach(ghost -> ghost.processEvent(new GhostKilledEvent(ghost)));
	}

	public void eatAllPellets() {
		game.maze.tiles().filter(game.maze::containsPellet).forEach(game::eatFoodAtTile);
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		if (showGrid) {
			g.drawImage(gridImage, 0, 0, null);
			if (game.pacMan.visible()) {
				drawActorAlignment(game.pacMan, g);
			}
			game.activeGhosts().filter(Ghost::visible).forEach(ghost -> drawActorAlignment(ghost, g));
		}
		if (showRoutes) {
			game.activeGhosts().filter(Ghost::visible).forEach(ghost -> drawRoute(g, ghost));
		}
		if (showStates) {
			drawEntityStates(g);
		}
	}

	private void drawEntityStates(Graphics2D g) {
		if (game.pacMan.getState() != null && game.pacMan.visible()) {
			drawText(g, Color.YELLOW, game.pacMan.tf.getX(), game.pacMan.tf.getY(), pacManStateText(game.pacMan));
		}
		game.activeGhosts().filter(Ghost::visible).forEach(ghost -> {
			if (ghost.getState() != null) {
				drawText(g, ghostColor(ghost), ghost.tf.getX(), ghost.tf.getY(), ghostStateText(ghost));
			}
		});
	}

	private String pacManStateText(PacMan pacMan) {
		return pacMan.state().getDuration() != State.ENDLESS ? String.format("(%s,%d|%d)", pacMan.state().id(),
				pacMan.state().getTicksRemaining(), pacMan.state().getDuration())
				: String.format("(%s,%s)", pacMan.state().id(), INFTY);
	}

	private String ghostStateText(Ghost ghost) {
		String name = ghost.getState() == GhostState.DEAD ? ghost.name : "";
		if (ghost.getState() == GhostState.FRIGHTENED) {
			GhostState nextState = ghost.getNextState();
			return ghost.state().getDuration() != State.ENDLESS
					? String.format("%s(%s,%d|%d)[->%s]", name, ghost.state().id(), ghost.state().getTicksRemaining(),
							ghost.state().getDuration(), nextState)
					: String.format("%s(%s,%s)[->%s]", name, ghost.state().id(), INFTY, nextState);
		}
		else {
			return ghost.state().getDuration() != State.ENDLESS ? String.format("%s(%s,%d|%d)", name,
					ghost.state().id(), ghost.state().getTicksRemaining(), ghost.state().getDuration())
					: String.format("%s(%s,%s)", name, ghost.state().id(), INFTY);
		}
	}

	private void toggleGhost(Ghost ghost) {
		game.setActive(ghost, !game.isActive(ghost));
	}

	private static Color ghostColor(Ghost ghost) {
		switch (ghost.name) {
		case "Blinky":
			return Color.RED;
		case "Pinky":
			return Color.PINK;
		case "Inky":
			return Color.CYAN;
		case "Clyde":
			return Color.ORANGE;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void drawText(Graphics2D g, Color color, float x, float y, String text) {
		g.translate(x, y);
		g.setColor(color);
		g.setFont(new Font("Arial Narrow", Font.PLAIN, 6));
		int width = g.getFontMetrics().stringWidth(text);
		g.drawString(text, -width / 2, -TS / 2);
		g.translate(-x, -y);
	}

	private void drawActorAlignment(MazeMover actor, Graphics2D g) {
		g.setColor(Color.GREEN);
		g.translate(actor.tf.getX(), actor.tf.getY());
		int w = actor.tf.getWidth(), h = actor.tf.getHeight();
		if (round(actor.tf.getY()) % TS == 0) {
			g.drawLine(0, 0, w, 0);
			g.drawLine(0, h, w, h);
		}
		if (round(actor.tf.getX()) % TS == 0) {
			g.drawLine(0, 0, 0, h);
			g.drawLine(w, 0, w, h);
		}
		g.translate(-actor.tf.getX(), -actor.tf.getY());
	}

	private void drawRoute(Graphics2D g, Ghost ghost) {
		g.setColor(ghostColor(ghost));
		if (ghost.targetPath.size() > 1) {
			for (int i = 0; i < ghost.targetPath.size() - 1; ++i) {
				Tile u = ghost.targetPath.get(i), v = ghost.targetPath.get(i + 1);
				int u1 = u.col * TS + TS / 2;
				int u2 = u.row * TS + TS / 2;
				int v1 = v.col * TS + TS / 2;
				int v2 = v.row * TS + TS / 2;
				g.drawLine(u1, u2, v1, v2);
			}
			Tile targetTile = ghost.targetPath.get(ghost.targetPath.size() - 1);
			g.translate(targetTile.col * TS, targetTile.row * TS);
			g.fillRect(TS / 4, TS / 4, TS / 2, TS / 2);
			g.translate(-targetTile.col * TS, -targetTile.row * TS);
		}
		else if (ghost.targetTile != null) {
			// draw target tile indicator
			g.drawLine((int) ghost.tf.getCenter().x, (int) ghost.tf.getCenter().y,
					ghost.targetTile.col * TS + TS / 2, ghost.targetTile.row * TS + TS / 2);
			g.translate(ghost.targetTile.col * TS, ghost.targetTile.row * TS);
			g.fillRect(TS / 4, TS / 4, TS / 2, TS / 2);
			g.translate(-ghost.targetTile.col * TS, -ghost.targetTile.row * TS);
		}
		else {
			// draw direction indicator
			if (ghost.nextDir != -1) {
				Vector2f center = ghost.tf.getCenter();
				int dx = NESW.dx(ghost.nextDir), dy = NESW.dy(ghost.nextDir);
				int r = TS / 4;
				int lineLen = TS;
				int indX = (int) (center.x + dx * lineLen);
				int indY = (int) (center.y + dy * lineLen);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.drawLine((int) center.x, (int) center.y, indX, indY);
				g.fillOval(indX - r, indY - r, 2 * r, 2 * r);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		}

		if (ghost == game.clyde && ghost.getState() == GhostState.CHASING) {
			Vector2f center = ghost.tf.getCenter();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawOval((int) center.x - 8 * TS, (int) center.y - 8 * TS, 16 * TS, 16 * TS);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}
}