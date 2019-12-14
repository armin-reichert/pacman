package de.amr.games.pacman.view.play;

import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManGameCast;
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
 * </ul>
 * 
 * @author Armin Reichert
 */
public class PlayView extends SimplePlayView {

	private static final String INFTY = Character.toString('\u221E');

	private static BufferedImage createGridImage(int numRows, int numCols) {
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage img = gc.createCompatibleImage(numCols * Tile.SIZE, numRows * Tile.SIZE + 1,
				Transparency.TRANSLUCENT);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(0, 60, 0));
		for (int row = 0; row <= numRows; ++row) {
			g.drawLine(0, row * Tile.SIZE, numCols * Tile.SIZE, row * Tile.SIZE);
		}
		for (int col = 1; col < numCols; ++col) {
			g.drawLine(col * Tile.SIZE, 0, col * Tile.SIZE, numRows * Tile.SIZE);
		}
		return img;
	}

	private boolean showRoutes = false;
	private boolean showGrid = false;
	private boolean showStates = false;
	private BufferedImage gridImage;

	public Supplier<State<GhostState, ?>> fnGhostMotionState = () -> null;

	public PlayView(PacManGameCast cast) {
		super(cast);
	}

	public void setShowRoutes(boolean showRoutes) {
		this.showRoutes = showRoutes;
		cast.pacMan.requireTargetPath = showRoutes;
		cast.ghosts().forEach(ghost -> ghost.requireTargetPath = showRoutes);
	}

	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
		if (showGrid && gridImage == null) {
			gridImage = createGridImage(cast.game.maze.numRows, cast.game.maze.numCols);
		}
	}

	public void setShowStates(boolean showStates) {
		this.showStates = showStates;
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_G)) {
			setShowGrid(!showGrid);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_S)) {
			setShowStates(!showStates);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			setShowRoutes(!showRoutes);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_B)) {
			toggleGhostActivationState(cast.blinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_P)) {
			toggleGhostActivationState(cast.pinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_I)) {
			toggleGhostActivationState(cast.inky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			toggleGhostActivationState(cast.clyde);
		}
		super.update();
	}

	private void toggleGhostActivationState(Ghost ghost) {
		if (cast.isActive(ghost)) {
			cast.deactivate(ghost);
		} else {
			cast.activate(ghost);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		drawMaze(g);
		drawScores(g);
		if (showRoutes) {
			cast.activeGhosts().filter(Ghost::visible).forEach(ghost -> drawRoute(g, ghost));
		}
		drawActors(g);
		if (showGrid) {
			g.drawImage(gridImage, 0, 0, null);
			if (cast.isActive(cast.pacMan)) {
				drawGridAlignment(cast.pacMan, g);
			}
			cast.activeGhosts().filter(Ghost::visible).forEach(ghost -> drawGridAlignment(ghost, g));
		}
		if (showStates) {
			drawActorStates(g);
		}
		drawInfoText(g);
	}

	private void drawActorStates(Graphics2D g) {
		if (cast.pacMan.getState() != null && cast.pacMan.visible()) {
			drawText(g, Color.YELLOW, cast.pacMan.tf.getX(), cast.pacMan.tf.getY(), pacManStateText(cast.pacMan));
		}
		cast.activeGhosts().filter(Ghost::visible).forEach(ghost -> {
			drawText(g, color(ghost), ghost.tf.getX(), ghost.tf.getY(), ghostStateText(ghost));
		});
		cast.bonus().ifPresent(bonus -> {
			drawText(g, Color.YELLOW, bonus.tf.getX(), bonus.tf.getY(), bonusStateText(bonus));
		});
	}

	private String bonusStateText(Bonus bonus) {
		return String.format("%s,%d|%d", bonus, bonus.state().getTicksRemaining(), bonus.state().getDuration());
	}

	private String pacManStateText(PacMan pacMan) {
		String text = pacMan.state().getDuration() != State.ENDLESS ? String.format("(%s,%d|%d)", pacMan.state().id(),
				pacMan.state().getTicksRemaining(), pacMan.state().getDuration())
				: String.format("(%s,%s)", pacMan.state().id(), INFTY);

		if (Application.app().settings.getAsBoolean("pacMan.immortable")) {
			text += "-immortable";
		}
		return text;
	}

	private String ghostStateText(Ghost ghost) {
		String displayName = ghost.getState() == GhostState.DEAD ? ghost.name() : "";
		String nextState = ghost.nextState != ghost.getState() ? String.format("[->%s]", ghost.nextState) : "";
		int duration = ghost.state().getDuration(), remaining = ghost.state().getTicksRemaining();

		if (ghost.getState() == GhostState.FRIGHTENED && cast.pacMan.hasPower()) {
			duration = cast.pacMan.state().getDuration();
			remaining = cast.pacMan.state().getTicksRemaining();
		} else if (ghost.getState() == GhostState.SCATTERING || ghost.getState() == GhostState.CHASING) {
			State<?, ?> attack = fnGhostMotionState.get();
			if (attack != null) {
				duration = attack.getDuration();
				remaining = attack.getTicksRemaining();
			}
		}

		return duration != State.ENDLESS
				? String.format("%s(%s,%d|%d)%s", displayName, ghost.getState(), remaining, duration, nextState)
				: String.format("%s(%s,%s)%s", displayName, ghost.getState(), INFTY, nextState);
	}

	private Color color(Ghost ghost) {
		return ghost == cast.blinky ? Color.RED
				: ghost == cast.pinky ? Color.PINK
						: ghost == cast.inky ? Color.CYAN : ghost == cast.clyde ? Color.ORANGE : Color.WHITE;
	}

	private void drawText(Graphics2D g, Color color, float x, float y, String text) {
		g.translate(x, y);
		g.setColor(color);
		g.setFont(new Font("Arial Narrow", Font.PLAIN, 5));
		int width = g.getFontMetrics().stringWidth(text);
		g.drawString(text, -width / 2, -Tile.SIZE / 2);
		g.translate(-x, -y);
	}

	private void drawGridAlignment(Entity actor, Graphics2D g) {
		g.setColor(Color.GREEN);
		g.translate(actor.tf.getX(), actor.tf.getY());
		int w = actor.tf.getWidth(), h = actor.tf.getHeight();
		if (round(actor.tf.getY()) % Tile.SIZE == 0) {
			g.drawLine(0, 0, w, 0);
			g.drawLine(0, h, w, h);
		}
		if (round(actor.tf.getX()) % Tile.SIZE == 0) {
			g.drawLine(0, 0, 0, h);
			g.drawLine(w, 0, w, h);
		}
		g.translate(-actor.tf.getX(), -actor.tf.getY());
	}

	private void drawRoute(Graphics2D g, Ghost ghost) {
		Color ghostColor = color(ghost);
		Stroke solid = g.getStroke();
		if (ghost.targetTile() != null) {
			// draw target tile indicator
			Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
			g.setStroke(dashed);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(ghostColor);
			g.drawLine((int) ghost.tf.getCenter().x, (int) ghost.tf.getCenter().y,
					ghost.targetTile().col * Tile.SIZE + Tile.SIZE / 2,
					ghost.targetTile().row * Tile.SIZE + Tile.SIZE / 2);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setStroke(solid);
			g.translate(ghost.targetTile().col * Tile.SIZE, ghost.targetTile().row * Tile.SIZE);
			g.setColor(ghostColor);
			g.fillRect(Tile.SIZE / 4, Tile.SIZE / 4, Tile.SIZE / 2, Tile.SIZE / 2);
			g.translate(-ghost.targetTile().col * Tile.SIZE, -ghost.targetTile().row * Tile.SIZE);
		}
		if (ghost.targetPath().size() > 1) {
			// draw path in ghost's color
			g.setColor(new Color(ghostColor.getRed(), ghostColor.getGreen(), ghostColor.getBlue(), 60));
			for (Tile tile : ghost.targetPath()) {
				g.fillRect(tile.col * Tile.SIZE, tile.row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
			}
		} else {
			// draw direction indicator
			Vector2f center = ghost.tf.getCenter();
			int dx = ghost.nextDir().dx, dy = ghost.nextDir().dy;
			int r = Tile.SIZE / 4;
			int lineLen = Tile.SIZE;
			int indX = (int) (center.x + dx * lineLen);
			int indY = (int) (center.y + dy * lineLen);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(ghostColor);
			g.fillOval(indX - r, indY - r, 2 * r, 2 * r);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		// draw Clyde's chasing zone
		if (ghost == cast.clyde && ghost.getState() == GhostState.CHASING && cast.clyde.targetTile() != null) {
			Vector2f center = cast.clyde.tf.getCenter();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(ghostColor.getRed(), ghostColor.getGreen(), ghostColor.getBlue(), 100));
			g.drawOval((int) center.x - 8 * Tile.SIZE, (int) center.y - 8 * Tile.SIZE, 16 * Tile.SIZE, 16 * Tile.SIZE);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}
}