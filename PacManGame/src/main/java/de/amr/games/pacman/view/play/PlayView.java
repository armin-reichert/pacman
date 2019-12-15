package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.app;
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
import de.amr.games.pacman.model.Direction;
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
	private boolean showFrameRate = false;
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

	public void setShowFrameRate(boolean showFrameRate) {
		this.showFrameRate = showFrameRate;
	}

	public void setShowStates(boolean showStates) {
		this.showStates = showStates;
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_T)) {
			setShowFrameRate(!showFrameRate);
		}
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
		if (cast.onStage(ghost)) {
			cast.removeFromStage(ghost);
		}
		else {
			cast.putOnStage(ghost);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		drawScores(g);
		drawMaze(g);
		if (showRoutes) {
			drawRoutes(g);
		}
		drawActors(g);
		if (showGrid) {
			drawGrid(g);
		}
		if (showStates) {
			drawActorStates(g);
		}
		drawInfoText(g);
		if (showFrameRate) {
			drawFPS(g);
		}
	}

	private void drawFPS(Graphics2D g) {
		Pen pen = new Pen(g);
		pen.color = new Color(240, 240, 240, 80);
		pen.font = new Font(Font.MONOSPACED, Font.BOLD, 12);
		pen.aaOn();
		pen.text(app().clock.getRenderRate() + "fps", 23, 21);
		pen.aaOff();
	}

	private void drawActorStates(Graphics2D g) {
		if (cast.pacMan.getState() != null && cast.pacMan.visible()) {
			drawText(g, Color.YELLOW, cast.pacMan.tf.getX(), cast.pacMan.tf.getY(), pacManStateText(cast.pacMan));
		}
		cast.ghostsOnStage().filter(Ghost::visible).forEach(ghost -> {
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
		String text = pacMan.state().getDuration() != State.ENDLESS ? String.format("(%s,%d|%d)",
				pacMan.state().id(), pacMan.state().getTicksRemaining(), pacMan.state().getDuration())
				: String.format("(%s,%s)", pacMan.state().id(), INFTY);

		if (Application.app().settings.getAsBoolean("pacMan.immortable")) {
			text += "-immortable";
		}
		return text;
	}

	private String ghostStateText(Ghost ghost) {
		StringBuilder text = new StringBuilder();
		// ghost name if dead
		text.append(ghost.getState() == GhostState.DEAD ? ghost.name() : "");
		// timer values
		int duration = ghost.state().getDuration();
		int remaining = ghost.state().getTicksRemaining();
		// Pac-Man power time
		if (ghost.getState() == GhostState.FRIGHTENED && cast.pacMan.hasPower()) {
			duration = cast.pacMan.state().getDuration();
			remaining = cast.pacMan.state().getTicksRemaining();
		}
		// chasing or scattering time
		else if (ghost.getState() == GhostState.SCATTERING || ghost.getState() == GhostState.CHASING) {
			State<GhostState, ?> attack = fnGhostMotionState.get();
			if (attack != null) {
				duration = attack.getDuration();
				remaining = attack.getTicksRemaining();
			}
		}
		if (duration == State.ENDLESS) {
			text.append(String.format("(%s,%s)", ghost.getState(), INFTY));
		}
		else {
			text.append(String.format("(%s,%d|%d)", ghost.getState(), remaining, duration));
		}
		// next state
		if (ghost.getState() == GhostState.LEAVING_HOUSE) {
			text.append(String.format("[->%s]", ghost.nextState));
		}
		return text.toString();
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

	private void drawGrid(Graphics2D g) {
		g.drawImage(gridImage, 0, 0, null);
		if (cast.onStage(cast.pacMan)) {
			drawGridAlignment(cast.pacMan, g);
		}
		cast.ghostsOnStage().filter(Ghost::visible).forEach(ghost -> drawGridAlignment(ghost, g));
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

	private void drawRoutes(Graphics2D g) {
		cast.ghostsOnStage().filter(Ghost::visible).forEach(ghost -> drawRoute(g, ghost));
	}

	private void drawRoute(Graphics2D g, Ghost ghost) {
		Color ghostColor = color(ghost);
		Stroke solid = g.getStroke();
		if (ghost.targetTile() != null) {
			// draw target tile indicator
			Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 },
					0);
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
		}
		else if (ghost.nextDir() != null) {
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
		// draw Inky's vector
		if (ghost == cast.inky && ghost.getState() == GhostState.CHASING && ghost.targetTile() != null) {
			{
				Vector2f bp = cast.blinky.tf.getCenter();
				int x1 = bp.roundedX();
				int y1 = bp.roundedY();
				int x2 = ghost.targetTile().col * Tile.SIZE + Tile.SIZE / 2;
				int y2 = ghost.targetTile().row * Tile.SIZE + Tile.SIZE / 2;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.GRAY);
				g.drawLine(x1, y1, x2, y2);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
			{
				// Note: we cannot use tilesAhead() because this simulates the overflow error
				Tile pacManTile = cast.pacMan.tile();
				Direction dir = cast.pacMan.moveDir();
				Tile twoTilesAheadPacMan = maze.tileAt(pacManTile.col + 2 * dir.dx, pacManTile.row + 2 * dir.dy);
				int x1 = pacManTile.col * Tile.SIZE + Tile.SIZE / 2;
				int y1 = pacManTile.row * Tile.SIZE + Tile.SIZE / 2;
				int x2 = twoTilesAheadPacMan.col * Tile.SIZE + Tile.SIZE / 2;
				int y2 = twoTilesAheadPacMan.row * Tile.SIZE + Tile.SIZE / 2;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.GRAY);
				g.drawLine(x1, y1, x2, y2);
				g.fillRect(x2 - Tile.SIZE / 4, y2 - Tile.SIZE / 4, Tile.SIZE / 2, Tile.SIZE / 2);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		}
		// draw Clyde's chasing zone
		if (ghost == cast.clyde && ghost.getState() == GhostState.CHASING && ghost.targetTile() != null) {
			Vector2f center = cast.clyde.tf.getCenter();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(ghostColor.getRed(), ghostColor.getGreen(), ghostColor.getBlue(), 100));
			g.drawOval((int) center.x - 8 * Tile.SIZE, (int) center.y - 8 * Tile.SIZE, 16 * Tile.SIZE,
					16 * Tile.SIZE);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}
}