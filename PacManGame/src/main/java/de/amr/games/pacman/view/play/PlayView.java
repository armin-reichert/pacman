package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Direction.RIGHT;
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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.BonusState;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.MovingActor;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.controller.GhostHouse;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.core.FPSDisplay;
import de.amr.statemachine.core.State;

/**
 * An extended play view.
 * 
 * @author Armin Reichert
 */
public class PlayView extends SimplePlayView {

	private static final String INFTY = Character.toString('\u221E');

	private static Color dimmed(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	public Supplier<State<GhostState>> fnGhostCommandState = () -> null;
	public GhostHouse house; // (optional)

	public boolean showFrameRate = false;
	public boolean showRoutes = false;
	public boolean showGrid = false;
	public boolean showScores = true;
	public boolean showStates = false;

	private FPSDisplay fps;
	private final BufferedImage gridImage, inkyImage, clydeImage, pacManImage;
	private final Polygon arrowHead;

	public PlayView(Game game, Theme theme) {
		super(game, theme);
		fps = new FPSDisplay();
		fps.tf.setPosition(0, 18 * Tile.SIZE);
		gridImage = createGridImage(game.maze);
		inkyImage = ghostImage(Theme.CYAN_GHOST);
		clydeImage = ghostImage(Theme.ORANGE_GHOST);
		pacManImage = (BufferedImage) theme.spr_pacManWalking(RIGHT).frame(0);
		arrowHead = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);
	}

	@Override
	public void draw(Graphics2D g) {
		drawBackground(g);
		drawMaze(g);
		if (showFrameRate) {
			fps.draw(g);
		}
		drawPlayMode(g);
		drawMessage(g);
		if (showGrid) {
			drawUpwardsBlockedTileMarkers(g);
			drawSeats(g);
		}
		if (showScores) {
			drawScores(g);
		}
		Arrays.asList(GhostState.values()).forEach(state -> {
			game.ghosts().forEach(ghost -> {
				ghost.steering(state).enableTargetPathComputation(showRoutes);
			});
		});
		if (showRoutes) {
			drawRoutes(g);
		}
		drawActors(g);
		if (showGrid) {
			drawActorAlignments(g);
		}
		if (showStates) {
			drawActorStates(g);
			drawGhostHouseState(g);
		}
	}

	@Override
	protected void drawBackground(Graphics2D g) {
		if (showGrid) {
			g.drawImage(gridImage, 0, 0, null);
		} else {
			super.drawBackground(g);
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

	private BufferedImage ghostImage(int color) {
		return (BufferedImage) theme.spr_ghostColored(color, Direction.RIGHT).frame(0);
	}

	private Color patternColor(int col, int row) {
		return (row + col) % 2 == 0 ? Color.BLACK : new Color(40, 40, 40);
	}

	@Override
	protected Color tileColor(Tile tile) {
		return showGrid ? patternColor(tile.col, tile.row) : super.tileColor(tile);
	}

	private Color ghostColor(Ghost ghost) {
		if (ghost == game.blinky)
			return Color.RED;
		if (ghost == game.pinky)
			return Color.PINK;
		if (ghost == game.inky)
			return Color.CYAN;
		if (ghost == game.clyde)
			return Color.ORANGE;
		throw new IllegalArgumentException("Unknown ghost: " + ghost);
	}

	private void drawSmallText(Graphics2D g, Color color, float x, float y, String text) {
		g.setColor(color);
		g.setFont(new Font("Arial Narrow", Font.PLAIN, 5));
		int sw = g.getFontMetrics().stringWidth(text);
		g.drawString(text, x - sw / 2, y - Tile.SIZE / 2);
	}

	private void drawPlayMode(Graphics2D g) {
		if (settings.demoMode) {
			try (Pen pen = new Pen(g)) {
				pen.font(theme.fnt_text());
				pen.color(Color.DARK_GRAY);
				pen.hcenter("Demo Mode", width(), 21, Tile.SIZE);
			}
		}
	}

	private void drawActorStates(Graphics2D g) {
		game.ghostsOnStage().forEach(ghost -> drawGhostState(g, ghost));
		drawPacManState(g);
		drawBonusState(g);
	}

	private void drawPacManState(Graphics2D g) {
		PacMan pacMan = game.pacMan;
		if (pacMan.visible && pacMan.getState() != null) {
			String text = pacMan.getState().name();
			if (pacMan.powerTicks > 0) {
				text = String.format("POWER(%d)", pacMan.powerTicks);
			}
			if (settings.pacManImmortable) {
				text += ",lives " + INFTY;
			}
			drawSmallText(g, Color.YELLOW, pacMan.tf.x, pacMan.tf.y, text);
		}
	}

	private void drawGhostState(Graphics2D g, Ghost ghost) {
		if (!ghost.visible) {
			return;
		}
		StringBuilder text = new StringBuilder();
		// show ghost name if not obvious
		text.append(ghost.is(DEAD, FRIGHTENED, ENTERING_HOUSE) ? ghost.name : "");
		// timer values
		int duration = ghost.state().getDuration();
		int remaining = ghost.state().getTicksRemaining();
		// chasing or scattering time
		if (ghost.is(SCATTERING, CHASING)) {
			State<GhostState> attack = fnGhostCommandState.get();
			if (attack != null) {
				duration = attack.getDuration();
				remaining = attack.getTicksRemaining();
			}
		}
		if (duration != Integer.MAX_VALUE) {
			text.append(String.format("(%s,%d|%d)", ghost.getState(), remaining, duration));
		} else {
			text.append(String.format("(%s,%s)", ghost.getState(), INFTY));
		}
		if (ghost.is(LEAVING_HOUSE)) {
			text.append(String.format("[->%s]", ghost.followState));
		}
		drawSmallText(g, ghostColor(ghost), ghost.tf.x, ghost.tf.y, text.toString());
	}

	private void drawBonusState(Graphics2D g) {
		Bonus bonus = game.bonus;
		String text = "";
		if (bonus.getState() == BonusState.INACTIVE) {
			text = "Bonus inactive";
		} else {
			text = String.format("%s,%d|%d", bonus, bonus.state().getTicksRemaining(), bonus.state().getDuration());
		}
		drawSmallText(g, Color.YELLOW, bonus.tf.x, bonus.tf.y, text);
	}

	private void drawPacManStarvingTime(Graphics2D g) {
		int col = 1, row = 14;
		int time = house.pacManStarvingTicks();
		g.drawImage(pacManImage, col * Tile.SIZE, row * Tile.SIZE, 10, 10, null);
		try (Pen pen = new Pen(g)) {
			pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
			pen.color(Color.WHITE);
			pen.smooth(() -> pen.drawAtGridPosition(time == -1 ? INFTY : String.format("%d", time), col + 2, row, Tile.SIZE));
		}
	}

	private void drawActorAlignments(Graphics2D g) {
		game.movingActorsOnStage().forEach(actor -> drawActorAlignment(actor, g));
	}

	private void drawActorAlignment(MovingActor<?> actor, Graphics2D g) {
		if (!actor.visible) {
			return;
		}
		Stroke normal = g.getStroke();
		Stroke fine = new BasicStroke(0.2f);
		g.setStroke(fine);
		g.setColor(Color.GREEN);
		g.translate(actor.tf.x, actor.tf.y);
		int w = actor.tf.width, h = actor.tf.height;
		if (round(actor.tf.y) % Tile.SIZE == 0) {
			g.drawLine(0, 0, w, 0);
			g.drawLine(0, h, w, h);
		}
		if (round(actor.tf.x) % Tile.SIZE == 0) {
			g.drawLine(0, 0, 0, h);
			g.drawLine(w, 0, w, h);
		}
		g.translate(-actor.tf.x, -actor.tf.y);
		g.setStroke(normal);
	}

	private void drawUpwardsBlockedTileMarkers(Graphics2D g) {
		g.setColor(Color.WHITE);
		for (int row = 0; row < game.maze.numRows; ++row) {
			for (int col = 0; col < game.maze.numCols; ++col) {
				Tile tile = new Tile(col, row);
				if (game.maze.isUpwardsBlocked(tile)) {
					Tile above = game.maze.neighbor(tile, Direction.UP);
					drawArrowHead(g, Direction.DOWN, above.centerX(), above.y() - 2);
				}
			}
		}
	}

	private void drawSeats(Graphics2D g) {
		Ghost[] ghostsBySeat = { game.blinky, game.inky, game.pinky, game.clyde };
		IntStream.rangeClosed(0, 3).forEach(seat -> {
			Tile seatTile = game.maze.ghostHome[seat];
			g.setColor(ghostColor(ghostsBySeat[seat]));
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

	private void drawRoutes(Graphics2D g2) {
		Graphics2D g = (Graphics2D) g2.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		game.ghostsOnStage().filter(ghost -> ghost.visible).forEach(ghost -> drawRoute(g, ghost));
		g.dispose();
	}

	private void drawRoute(Graphics2D g, Ghost ghost) {
		Tile target = ghost.targetTile();
		List<Tile> targetPath = ghost.steering().targetPath();
		int pathLen = targetPath.size();
		Color ghostColor = ghostColor(ghost);
		Stroke solid = new BasicStroke(0.5f);
		Stroke dashed = new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
		boolean drawRubberBand = target != null && pathLen > 0 && target != targetPath.get(pathLen - 1);
		if (drawRubberBand) {
			// draw rubber band to target tile
			int x1 = ghost.tf.getCenter().roundedX(), y1 = ghost.tf.getCenter().roundedY();
			int x2 = target.centerX(), y2 = target.centerY();
			g.setStroke(dashed);
			g.setColor(dimmed(ghostColor, 200));
			g.drawLine(x1, y1, x2, y2);
			g.translate(target.x(), target.y());
			g.setColor(ghostColor);
			g.setStroke(solid);
			g.fillRect(2, 2, 4, 4);
			g.translate(-target.x(), -target.y());
		}
		if (pathLen > 1) {
			// draw path
			g.setColor(dimmed(ghostColor, 200));
			for (int i = 0; i < targetPath.size() - 1; ++i) {
				Tile from = targetPath.get(i), to = targetPath.get(i + 1);
				g.setColor(ghostColor);
				g.setStroke(solid);
				g.drawLine(from.centerX(), from.centerY(), to.centerX(), to.centerY());
				if (i + 1 == targetPath.size() - 1) {
					drawArrowHead(g, game.maze.direction(from, to).get(), to.centerX(), to.centerY());
				}
			}
		} else if (ghost.wishDir() != null) {
			// draw direction indicator
			Direction nextDir = ghost.wishDir();
			int x = ghost.tf.getCenter().roundedX(), y = ghost.tf.getCenter().roundedY();
			g.setColor(ghostColor);
			Vector2f dirVector = nextDir.vector();
			drawArrowHead(g, nextDir, x + dirVector.roundedX() * Tile.SIZE, y + dirVector.roundedY() * Tile.SIZE);
		}
		// visualize Inky's chasing (target tile may be null if Blinky is not on stage!)
		if (ghost == game.inky && ghost.is(CHASING) && ghost.targetTile() != null) {
			{
				int x1 = game.blinky.tile().centerX(), y1 = game.blinky.tile().centerY();
				int x2 = ghost.targetTile().centerX(), y2 = ghost.targetTile().centerY();
				g.setColor(Color.GRAY);
				g.drawLine(x1, y1, x2, y2);
			}
			{
				Tile pacManTile = game.pacMan.tile();
				Direction pacManDir = game.pacMan.moveDir();
				int s = Tile.SIZE / 2; // size of target square
				g.setColor(Color.GRAY);
				if (settings.overflowBug && pacManDir == Direction.UP) {
					Tile twoAhead = game.maze.tileToDir(pacManTile, pacManDir, 2);
					Tile twoLeft = game.maze.tileToDir(twoAhead, Direction.LEFT, 2);
					int x1 = pacManTile.centerX(), y1 = pacManTile.centerY();
					int x2 = twoAhead.centerX(), y2 = twoAhead.centerY();
					int x3 = twoLeft.centerX(), y3 = twoLeft.centerY();
					g.drawLine(x1, y1, x2, y2);
					g.drawLine(x2, y2, x3, y3);
					g.fillRect(x3 - s / 2, y3 - s / 2, s, s);
				} else {
					Tile twoTilesAhead = game.pacMan.tilesAhead(2);
					int x1 = pacManTile.centerX(), y1 = pacManTile.centerY();
					int x2 = twoTilesAhead.centerX(), y2 = twoTilesAhead.centerY();
					g.drawLine(x1, y1, x2, y2);
					g.fillRect(x2 - s / 2, y2 - s / 2, s, s);
				}
			}
		}
		// draw Clyde's chasing zone
		if (ghost == game.clyde && ghost.is(CHASING)) {
			int cx = game.clyde.tile().centerX(), cy = game.clyde.tile().centerY(), r = 8 * Tile.SIZE;
			g.setColor(new Color(ghostColor.getRed(), ghostColor.getGreen(), ghostColor.getBlue(), 100));
			g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
		}
	}

	private void drawGhostHouseState(Graphics2D g) {
		if (house == null) {
			return; // test scenes can have no ghost house
		}
		drawPacManStarvingTime(g);
		drawDotCounter(g, clydeImage, house.ghostDotCount(game.clyde), 1, 20,
				!house.isGlobalDotCounterEnabled() && house.isPreferredGhost(game.clyde));
		drawDotCounter(g, inkyImage, house.ghostDotCount(game.inky), 24, 20,
				!house.isGlobalDotCounterEnabled() && house.isPreferredGhost(game.inky));
		drawDotCounter(g, null, house.globalDotCount(), 24, 14, house.isGlobalDotCounterEnabled());
	}

	private void drawDotCounter(Graphics2D g, BufferedImage image, int value, int col, int row, boolean emphasized) {
		try (Pen pen = new Pen(g)) {
			if (image != null) {
				g.drawImage(image, col * Tile.SIZE, row * Tile.SIZE, 10, 10, null);
			}
			pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
			pen.color(emphasized ? Color.GREEN : Color.WHITE);
			pen.smooth(() -> pen.drawAtGridPosition(String.format("%d", value), col + 2, row, Tile.SIZE));
		}
	}
}