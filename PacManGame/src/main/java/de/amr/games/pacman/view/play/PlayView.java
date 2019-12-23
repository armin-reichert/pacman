package de.amr.games.pacman.view.play;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static java.lang.Math.PI;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
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
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.controller.GhostHouse;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.view.core.Pen;
import de.amr.statemachine.core.State;

/**
 * An extended play view.
 * 
 * <p>
 * Commands:
 * <ul>
 * <li>switch ghosts on/off (keys 'b', 'p', 'i', 'c')
 * <li>display grid, ghosthouse seats and alignment of actors (key 'g')
 * <li>display actor states and dot counters (key 's')
 * <li>display actor routes (key 'r')
 * </ul>
 * 
 * @author Armin Reichert
 */
public class PlayView extends SimplePlayView {

	private static final String INFTY = Character.toString('\u221E');

	private static Color dimmed(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	public Supplier<State<GhostState, ?>> fnGhostMotionState = () -> null;
	public GhostHouse ghostHouse;

	private boolean showRoutes = false;
	private boolean showGrid = false;
	private boolean showStates = false;
	private final BufferedImage gridImage, pinkyImage, inkyImage, clydeImage;
	private final Polygon arrowHead;

	public PlayView(PacManGameCast cast, int width, int height) {
		super(cast, width, height);
		gridImage = createGridImage(cast.game.maze);
		pinkyImage = ghostImage(GhostColor.PINK);
		inkyImage = ghostImage(GhostColor.CYAN);
		clydeImage = ghostImage(GhostColor.ORANGE);
		arrowHead = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);
	}

	public PlayView(PacManGameCast cast) {
		this(cast, app().settings.width, app().settings.height);
	}

	public void showRoutes(boolean showRoutes) {
		this.showRoutes = showRoutes;
		cast().pacMan.requireTargetPath = showRoutes;
		cast().ghosts().forEach(ghost -> ghost.requireTargetPath = showRoutes);
	}

	public void showGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	public void showStates(boolean showStates) {
		this.showStates = showStates;
	}

	@Override
	public void update() {
		super.update();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_G)) {
			showGrid(!showGrid);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_S)) {
			showStates(!showStates);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			showRoutes(!showRoutes);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_B)) {
			toggleGhost(cast().blinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_P)) {
			toggleGhost(cast().pinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_I)) {
			toggleGhost(cast().inky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			toggleGhost(cast().clyde);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		if (showGrid) {
			g.drawImage(gridImage, 0, 0, null);
		}
		else {
			drawMazeBackground(g);
		}
		drawMaze(g);
		drawMessage(g);
		if (showGrid) {
			drawUpwardsBlockedTileMarkers(g);
			drawSeats(g);
		}
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
		if (showFrameRate) {
			drawFPS(g);
		}
	}

	private BufferedImage createGridImage(Maze maze) {
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage img = gc.createCompatibleImage(maze.numCols * Tile.SIZE, maze.numRows * Tile.SIZE + 1,
				Transparency.TRANSLUCENT);
		Graphics2D g = img.createGraphics();
		for (int row = 0; row < maze.numRows; ++row) {
			for (int col = 0; col < maze.numCols; ++col) {
				g.setColor(patternColor(col, row));
				g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
			}
		}
		return img;
	}

	private BufferedImage ghostImage(GhostColor color) {
		return (BufferedImage) theme().spr_ghostColored(color, Direction.RIGHT.ordinal()).frame(0);
	}

	private Color patternColor(int col, int row) {
		return (row + col) % 2 == 0 ? Color.BLACK : new Color(30, 30, 30);
	}

	@Override
	protected Color cellBackground(int col, int row) {
		return showGrid ? patternColor(col, row) : super.cellBackground(col, row);
	}

	private void toggleGhost(Ghost ghost) {
		if (cast().onStage(ghost)) {
			cast().removeFromStage(ghost);
		}
		else {
			cast().putOnStage(ghost);
		}
	}

	private String pacManStateText(PacMan pacMan) {
		int duration = pacMan.state().getDuration();
		String text = pacMan.getState().name();
		if (pacMan.hasPower()) {
			text += "+POWER";
		}
		if (duration != State.ENDLESS && duration > 0) {
			text += String.format("(%d|%d)", pacMan.state().getTicksRemaining(), duration);
		}
		if (Application.app().settings.getAsBoolean("PacMan.immortable")) {
			text += "-immortable";
		}
		return text;
	}

	private String ghostStateText(Ghost ghost) {
		StringBuilder text = new StringBuilder();
		// show ghost name if not obvious
		text.append(ghost.is(DEAD, FRIGHTENED, ENTERING_HOUSE) ? ghost.name() : "");
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
		text.append(duration == State.ENDLESS ? String.format("(%s,%s)", ghost.getState(), INFTY)
				: String.format("(%s,%d|%d)", ghost.getState(), remaining, duration));
		if (ghost.is(LEAVING_HOUSE)) {
			text.append(String.format("[->%s]", ghost.nextState));
		}
		return text.toString();
	}

	private Color color(Ghost ghost) {
		if (ghost == cast().blinky)
			return Color.RED;
		if (ghost == cast().pinky)
			return Color.PINK;
		if (ghost == cast().inky)
			return Color.CYAN;
		if (ghost == cast().clyde)
			return Color.ORANGE;
		throw new IllegalArgumentException("Unknown ghost: " + ghost);
	}

	private void drawSmallText(Graphics2D g, Color color, float x, float y, String text) {
		g.setColor(color);
		g.setFont(new Font("Arial Narrow", Font.PLAIN, 5));
		int sw = g.getFontMetrics().stringWidth(text);
		g.drawString(text, x - sw / 2, y - Tile.SIZE / 2);
	}

	private void drawActorStates(Graphics2D g) {
		if (cast().pacMan.getState() != null && cast().pacMan.visible()) {
			drawSmallText(g, Color.YELLOW, cast().pacMan.tf.getX(), cast().pacMan.tf.getY(),
					pacManStateText(cast().pacMan));
		}
		cast().ghostsOnStage().filter(Ghost::visible).forEach(ghost -> {
			drawSmallText(g, color(ghost), ghost.tf.getX(), ghost.tf.getY(), ghostStateText(ghost));
		});
		cast().bonus().ifPresent(bonus -> {
			String text = String.format("%s,%d|%d", bonus, bonus.state().getTicksRemaining(),
					bonus.state().getDuration());
			drawSmallText(g, Color.YELLOW, bonus.tf.getX(), bonus.tf.getY(), text);
		});
	}

	private void drawActorAlignments(Graphics2D g) {
		if (cast().onStage(cast().pacMan)) {
			drawActorAlignment(cast().pacMan, g);
		}
		cast().ghostsOnStage().filter(Ghost::visible).forEach(ghost -> drawActorAlignment(ghost, g));
	}

	private void drawActorAlignment(Entity actor, Graphics2D g) {
		if (!actor.visible()) {
			return;
		}
		Stroke normal = g.getStroke();
		Stroke fine = new BasicStroke(0.2f);
		g.setStroke(fine);
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
		g.setStroke(normal);
	}

	private void drawUpwardsBlockedTileMarkers(Graphics2D g) {
		g.setColor(dimmed(Color.LIGHT_GRAY, 80));
		for (int row = 0; row < maze().numRows; ++row) {
			for (int col = 0; col < maze().numCols; ++col) {
				Tile tile = maze().tileAt(col, row);
				if (maze().isNoUpIntersection(tile)) {
					Tile above = maze().tileToDir(tile, Direction.UP);
					drawArrowHead(g, Direction.DOWN, above.centerX(), above.y() - 2);
				}
			}
		}
	}

	private void drawSeats(Graphics2D g) {
		Ghost[] ghostsBySeat = { cast.blinky, cast.inky, cast.pinky, cast.clyde };
		IntStream.rangeClosed(0, 3).forEach(seat -> {
			Tile seatTile = maze().ghostHouseSeats[seat];
			g.setColor(color(ghostsBySeat[seat]));
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
		double[] angleForDir = { PI, -PI / 2, 0, PI / 2 };
		double angle = angleForDir[dir.ordinal()];
		g.translate(x, y);
		g.rotate(angle);
		g.fillPolygon(arrowHead);
		g.rotate(-angle);
		g.translate(-x, -y);
	}

	private void drawRoutes(Graphics2D g) {
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
			Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 },
					0);
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
		}
		else if (ghost.nextDir() != null) {
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
				if (app().settings.getAsBoolean("PacMan.overflowBug") && pacManDir == Direction.UP) {
					Tile twoAhead = maze().tileToDir(pacManTile, pacManDir, 2);
					Tile twoLeft = maze().tileToDir(twoAhead, Direction.LEFT, 2);
					int x1 = pacManTile.centerX(), y1 = pacManTile.centerY();
					int x2 = twoAhead.centerX(), y2 = twoAhead.centerY();
					int x3 = twoLeft.centerX(), y3 = twoLeft.centerY();
					g.drawLine(x1, y1, x2, y2);
					g.drawLine(x2, y2, x3, y3);
					g.fillRect(x3 - s / 2, y3 - s / 2, s, s);
				}
				else {
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
		Ghost preferredGhost = ghostHouse.preferredLockedGhost().orElse(null);
		drawDotCounter(g, pinkyImage, cast.pinky.dotCounter, 1, 14,
				!ghostHouse.isGlobalDotCounterEnabled() && preferredGhost == cast.pinky);
		drawDotCounter(g, clydeImage, cast.clyde.dotCounter, 1, 20,
				!ghostHouse.isGlobalDotCounterEnabled() && preferredGhost == cast.clyde);
		drawDotCounter(g, inkyImage, cast.inky.dotCounter, 24, 20,
				!ghostHouse.isGlobalDotCounterEnabled() && preferredGhost == cast.inky);
		drawDotCounter(g, null, ghostHouse.globalDotCounter(), 24, 14, ghostHouse.isGlobalDotCounterEnabled());
	}

	private void drawDotCounter(Graphics2D g, BufferedImage image, int value, int col, int row,
			boolean emphasized) {
		try (Pen pen = new Pen(g)) {
			if (image != null) {
				g.drawImage(image, col * Tile.SIZE, row * Tile.SIZE, 10, 10, null);
			}
			pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
			pen.color(emphasized ? Color.GREEN : Color.WHITE);
			pen.smooth(() -> pen.draw(String.format("%d", value), col + 2, row));
		}
	}
}