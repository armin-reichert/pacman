package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.view.Pen;
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

	private BufferedImage createGridPattern(int numRows, int numCols) {
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage img = gc.createCompatibleImage(numCols * Tile.SIZE, numRows * Tile.SIZE + 1,
				Transparency.TRANSLUCENT);
		Graphics2D g = img.createGraphics();
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				g.setColor(patternColor(col, row));
				g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
			}
		}
		return img;
	}

	private static Color dimmed(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private boolean showRoutes = false;
	private boolean showGrid = false;
	private boolean showStates = false;
	private boolean showFrameRate = false;
	private BufferedImage gridImage;
	private Sprite pinkySprite, inkySprite, clydeSprite;

	public Supplier<State<GhostState, ?>> fnGhostMotionState = () -> null;

	public PlayView(PacManGameCast cast) {
		super(cast);
		pinkySprite = theme().spr_ghostColored(GhostColor.PINK, Direction.RIGHT.ordinal());
		inkySprite = theme().spr_ghostColored(GhostColor.CYAN, Direction.RIGHT.ordinal());
		clydeSprite = theme().spr_ghostColored(GhostColor.ORANGE, Direction.RIGHT.ordinal());
	}

	private Color patternColor(int col, int row) {
		return (row + col) % 2 == 0 ? Color.BLACK : new Color(30, 30, 30);
	}

	@Override
	protected Color cellBackground(int col, int row) {
		return showGrid ? patternColor(col, row) : super.cellBackground(col, row);
	}

	public void setShowRoutes(boolean showRoutes) {
		this.showRoutes = showRoutes;
		cast().pacMan.requireTargetPath = showRoutes;
		cast().ghosts().forEach(ghost -> ghost.requireTargetPath = showRoutes);
	}

	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
		if (showGrid && gridImage == null) {
			gridImage = createGridPattern(cast().game.maze.numRows, cast().game.maze.numCols);
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
			toggleGhostActivationState(cast().blinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_P)) {
			toggleGhostActivationState(cast().pinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_I)) {
			toggleGhostActivationState(cast().inky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			toggleGhostActivationState(cast().clyde);
		}
		super.update();
	}

	private void toggleGhostActivationState(Ghost ghost) {
		if (cast().onStage(ghost)) {
			cast().removeFromStage(ghost);
		} else {
			cast().putOnStage(ghost);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		if (showGrid) {
			g.drawImage(gridImage, 0, 0, null);
		} else {
			drawMazeBackground(g);
		}
		drawMaze(g);
		drawScores(g);
		if (showRoutes) {
			drawRoutes(g);
		}
		drawActors(g);
		if (showGrid) {
			drawActorAlignments(g);
		}
		if (showStates) {
			drawActorStates(g);
			drawGhostDotCounters(g);
		}
		drawInfoText(g);
		if (showFrameRate) {
			drawFPS(g);
		}
	}

	private void drawFPS(Graphics2D g) {
		Pen pen = new Pen(g);
		pen.color(new Color(200, 200, 200));
		pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
		pen.smooth(() -> {
			pen.draw(String.format("%d|%dfps", app().clock.getRenderRate(), app().clock.getFrequency()), 0, 17);
		});
	}

	private void drawActorStates(Graphics2D g) {
		if (cast().pacMan.getState() != null && cast().pacMan.visible()) {
			drawText(g, Color.YELLOW, cast().pacMan.tf.getX(), cast().pacMan.tf.getY(), pacManStateText(cast().pacMan));
		}
		cast().ghostsOnStage().filter(Ghost::visible).forEach(ghost -> {
			drawText(g, color(ghost), ghost.tf.getX(), ghost.tf.getY(), ghostStateText(ghost));
		});
		cast().bonus().ifPresent(bonus -> {
			drawText(g, Color.YELLOW, bonus.tf.getX(), bonus.tf.getY(), bonusStateText(bonus));
		});
	}

	private String bonusStateText(Bonus bonus) {
		return String.format("%s,%d|%d", bonus, bonus.state().getTicksRemaining(), bonus.state().getDuration());
	}

	private String pacManStateText(PacMan pacMan) {
		String text = pacMan.state().getDuration() != State.ENDLESS
				? String.format("(%s,%d|%d)", pacMan.state().id(), pacMan.state().getTicksRemaining(),
						pacMan.state().getDuration())
				: String.format("(%s,%s)", pacMan.state().id(), INFTY);

		if (Application.app().settings.getAsBoolean("pacMan.immortable")) {
			text += "-immortable";
		}
		return text;
	}

	private String ghostStateText(Ghost ghost) {
		StringBuilder text = new StringBuilder();
		// ghost name if dead
		text.append(ghost.is(DEAD) ? ghost.name() : "");
		// timer values
		int duration = ghost.state().getDuration();
		int remaining = ghost.state().getTicksRemaining();
		// Pac-Man power time
		if (ghost.is(FRIGHTENED) && cast().pacMan.hasPower()) {
			duration = cast().pacMan.state().getDuration();
			remaining = cast().pacMan.state().getTicksRemaining();
		}
		// chasing or scattering time
		else if (ghost.is(SCATTERING) || ghost.is(CHASING)) {
			State<GhostState, ?> attack = fnGhostMotionState.get();
			if (attack != null) {
				duration = attack.getDuration();
				remaining = attack.getTicksRemaining();
			}
		}
		if (duration == State.ENDLESS) {
			text.append(String.format("(%s,%s)", ghost.getState(), INFTY));
		} else {
			text.append(String.format("(%s,%d|%d)", ghost.getState(), remaining, duration));
		}
		// next state
		if (ghost.is(LEAVING_HOUSE)) {
			text.append(String.format("[->%s]", ghost.nextState));
		}
		return text.toString();
	}

	private Color color(Ghost ghost) {
		return ghost == cast().blinky ? Color.RED
				: ghost == cast().pinky ? Color.PINK
						: ghost == cast().inky ? Color.CYAN : ghost == cast().clyde ? Color.ORANGE : Color.WHITE;
	}

	private void drawText(Graphics2D g, Color color, float x, float y, String text) {
		g.translate(x, y);
		g.setColor(color);
		g.setFont(new Font("Arial Narrow", Font.PLAIN, 5));
		int width = g.getFontMetrics().stringWidth(text);
		g.drawString(text, -width / 2, -Tile.SIZE / 2);
		g.translate(-x, -y);
	}

	private void drawActorAlignments(Graphics2D g) {
		if (cast().onStage(cast().pacMan)) {
			drawActorAlignment(cast().pacMan, g);
		}
		cast().ghostsOnStage().filter(Ghost::visible).forEach(ghost -> drawActorAlignment(ghost, g));
	}

	private void drawActorAlignment(Entity actor, Graphics2D g) {
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

	private void drawSeats(Graphics2D g) {
		IntStream.rangeClosed(0, 3).forEach(seat -> {
			Tile seatTile = maze().ghostHouseSeats[seat];
			g.setColor(Color.BLUE);
			int x = seatTile.centerX(), y = seatTile.y();
			String text = String.valueOf(seat);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawRoundRect(x, y, Tile.SIZE, Tile.SIZE, 2, 2);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 6));
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D r = fm.getStringBounds(text, g);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setColor(Color.WHITE);
			g.drawString(text, x + (Tile.SIZE - Math.round(r.getWidth())) / 2, y + Tile.SIZE - 2);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		});
	}

	private void drawArrowHead(Graphics2D g, Direction dir, int x, int y) {
		double angle = 0;
		switch (dir) {
		case DOWN:
			angle = 0;
			break;
		case LEFT:
			angle = Math.PI / 2;
			break;
		case RIGHT:
			angle = -Math.PI / 2;
			break;
		case UP:
			angle = Math.PI;
			break;
		default:
			break;
		}
		g.translate(x, y);
		g.rotate(angle);
		g.fillPolygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);
		g.rotate(-angle);
		g.translate(-x, -y);
	}

	private void drawRoutes(Graphics2D g) {
		drawSeats(g);
		cast().ghostsOnStage().filter(Ghost::visible).forEach(ghost -> drawRoute(g, ghost));
	}

	private void drawRoute(Graphics2D g, Ghost ghost) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Tile target = ghost.targetTile();
		Color ghostColor = color(ghost);
		Stroke solid = g.getStroke();
		boolean drawTargetTileArrow = target != null && ghost.targetPath().size() > 0
				&& target != ghost.targetPath().get(ghost.targetPath().size() - 1);
		if (drawTargetTileArrow) {
			Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
			g.setStroke(dashed);
			g.setColor(dimmed(ghostColor, 200));
			int x1 = ghost.centerX(), y1 = ghost.centerY();
			int x2 = target.centerX(), y2 = target.centerY();
			g.drawLine(x1, y1, x2, y2);
			g.setStroke(solid);
			g.translate(target.x(), target.y());
			g.setColor(ghostColor);
			g.fillRect(Tile.SIZE / 4, Tile.SIZE / 4, Tile.SIZE / 2, Tile.SIZE / 2);
			g.translate(-target.x(), -target.y());
		}
		if (ghost.targetPath().size() > 1) {
			g.setColor(dimmed(ghostColor, 200));
			for (int i = 0; i < ghost.targetPath().size() - 1; ++i) {
				Tile from = ghost.targetPath().get(i), to = ghost.targetPath().get(i + 1);
				g.drawLine(from.centerX(), from.centerY(), to.centerX(), to.centerY());
				if (i + 1 == ghost.targetPath().size() - 1) {
					drawArrowHead(g, maze().directionBetween(from, to).get(), to.centerX(), to.centerY());
				}
			}
		} else if (ghost.nextDir() != null) {
			// draw direction indicator
			Direction dir = ghost.nextDir();
			int x = ghost.centerX(), y = ghost.centerY();
			g.setColor(ghostColor);
			drawArrowHead(g, dir, x + dir.dx * Tile.SIZE, y + dir.dy * Tile.SIZE);
		}
		// visualize Inky's chasing (target tile may be null if Blinky is not on stage!)
		if (ghost == cast().inky && ghost.is(CHASING) && ghost.targetTile() != null) {
			{
				int x1 = cast().blinky.tile().centerX(), y1 = cast().blinky.tile().centerY();
				int x2 = ghost.targetTile().centerX(), y2 = ghost.targetTile().centerY();
				g.setColor(Color.GRAY);
				g.drawLine(x1, y1, x2, y2);
			}
			{
				Tile pacManTile = cast().pacMan.tile();
				Direction pacManDir = cast().pacMan.moveDir();
				int s = Tile.SIZE / 2; // size of target square
				g.setColor(Color.GRAY);
				if (app().settings.getAsBoolean("overflowBug") && pacManDir == Direction.UP) {
					Tile twoAhead = maze().tileToDir(pacManTile, pacManDir, 2);
					Tile twoLeft = maze().tileToDir(twoAhead, Direction.LEFT, 2);
					int x1 = pacManTile.centerX(), y1 = pacManTile.centerY();
					int x2 = twoAhead.centerX(), y2 = twoAhead.centerY();
					int x3 = twoLeft.centerX(), y3 = twoLeft.centerY();
					g.drawLine(x1, y1, x2, y2);
					g.drawLine(x2, y2, x3, y3);
					g.fillRect(x3 - s / 2, y3 - s / 2, s, s);
				} else {
					Tile twoTilesAhead = cast().pacMan.tilesAhead(2);
					int x1 = pacManTile.centerX(), y1 = pacManTile.centerY();
					int x2 = twoTilesAhead.centerX(), y2 = twoTilesAhead.centerY();
					g.drawLine(x1, y1, x2, y2);
					g.fillRect(x2 - s / 2, y2 - s / 2, s, s);
				}
			}
		}
		// draw Clyde's chasing zone
		if (ghost == cast().clyde && ghost.is(CHASING)) {
			int cx = cast().clyde.tile().centerX(), cy = cast().clyde.tile().centerY(), r = 8 * Tile.SIZE;
			g.setColor(new Color(ghostColor.getRed(), ghostColor.getGreen(), ghostColor.getBlue(), 100));
			g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void drawGhostDotCounters(Graphics2D g) {
		drawDotCounter(g, pinkySprite, cast.pinky.dotCounter, 1, 14);
		drawDotCounter(g, null, game().globalDotCounter, 24, 14);
		drawDotCounter(g, clydeSprite, cast.clyde.dotCounter, 1, 20);
		drawDotCounter(g, inkySprite, cast.inky.dotCounter, 24, 20);
	}

	private void drawDotCounter(Graphics2D g, Sprite sprite, int value, int col, int row) {
		Pen pen = new Pen(g);
		if (sprite != null) {
			g.drawImage(sprite.frame(0), col * Tile.SIZE, row * Tile.SIZE, null);
		}
		pen.color(Color.WHITE);
		pen.fontSize(8);
		pen.draw(String.format("%d", value), col + 2, row);
	}

}