package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static java.lang.Math.PI;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Supplier;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.widgets.FramerateWidget;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.GhostHouse;
import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.BonusState;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.steering.PathProvidingSteering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.core.State;

/**
 * An extended play view that can visualize actor states, the ghost house pellet counters, ghost
 * routes, the grid background, ghost seats and the current framerate.
 * 
 * @author Armin Reichert
 */
public class PlayView extends SimplePlayView {

	private static final String INFTY = Character.toString('\u221E');

	private static Color alpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
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
			throw new IllegalArgumentException("Ghost name unknown: " + ghost.name);
		}
	}

	public Supplier<State<GhostState>> fnGhostCommandState = () -> null;
	public GhostHouse house; // (optional)

	public boolean showFrameRate = false;
	public boolean showGrid = false;
	public boolean showRoutes = false;
	public boolean showScores = true;
	public boolean showStates = false;

	private FramerateWidget frameRateDisplay;
	private final BufferedImage gridImage, inkyImage, clydeImage, pacManImage;
	private final Polygon arrowHead = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);
	private final Color[] gridPatternColor = { Color.BLACK, new Color(40, 40, 40) };

	public PlayView(Game game, Theme theme) {
		super(game, theme);
		frameRateDisplay = new FramerateWidget();
		frameRateDisplay.tf.setPosition(0, 18 * Tile.SIZE);
		frameRateDisplay.font = new Font(Font.MONOSPACED, Font.BOLD, 8);
		gridImage = createGridPatternImage(game.maze.numCols, game.maze.numRows);
		inkyImage = (BufferedImage) theme.spr_ghostColored(Theme.CYAN_GHOST, Direction.RIGHT).frame(0);
		clydeImage = (BufferedImage) theme.spr_ghostColored(Theme.ORANGE_GHOST, Direction.RIGHT).frame(0);
		pacManImage = (BufferedImage) theme.spr_pacManWalking(RIGHT).frame(0);
	}

	@Override
	public void draw(Graphics2D g) {
		if (showGrid) {
			g.drawImage(gridImage, 0, 0, null);
		}
		drawMaze(g);
		if (showFrameRate) {
			frameRateDisplay.draw(g);
		}
		drawPlayMode(g);
		drawMessage(g);
		if (showGrid) {
			drawUpwardsBlockedTileMarkers(g);
			drawGhostSeats(g);
		}
		if (showScores) {
			drawScores(g);
		}
		if (showRoutes) {
			drawGhostRoutes(g);
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
	protected Color tileColor(Tile tile) {
		return showGrid ? gridPatternColor[patternIndex(tile.col, tile.row)] : super.tileColor(tile);
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
				pen.color(Color.LIGHT_GRAY);
				pen.hcenter("Demo Mode", width, 15, Tile.SIZE);
			}
		}
	}

	private void drawActorStates(Graphics2D g) {
		game.ghostsOnStage().forEach(ghost -> drawGhostState(g, ghost));
		drawPacManState(g);
		drawBonusState(g, game.bonus);
	}

	private void drawPacManState(Graphics2D g) {
		PacMan pacMan = game.pacMan;
		if (pacMan.visible && pacMan.getState() != null) {
			String text = pacMan.getState().name();
			if (pacMan.power > 0) {
				text = String.format("POWER(%d)", pacMan.power);
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

	private void drawBonusState(Graphics2D g, Bonus bonus) {
		State<BonusState> state = bonus.state();
		String text = bonus.is(BonusState.INACTIVE) ? "Bonus inactive"
				: String.format("%s,%d|%d", bonus, state.getTicksRemaining(), state.getDuration());
		try (Pen pen = new Pen(g)) {
			pen.font(new Font("Arial Narrow", Font.PLAIN, 5));
			pen.color(Color.YELLOW);
			pen.hcenter(text, width, game.maze.bonusSeat.tile.row, Tile.SIZE);
		}
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
		game.creaturesOnStage().forEach(actor -> drawActorAlignment(actor, g));
	}

	private void drawActorAlignment(Creature<?> actor, Graphics2D g) {
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
		for (int row = 0; row < game.maze.numRows; ++row) {
			for (int col = 0; col < game.maze.numCols; ++col) {
				Tile tile = Tile.at(col, row);
				if (game.maze.isOneWayDown(tile)) {
					Tile above = game.maze.neighbor(tile, Direction.UP);
					drawDirectionIndicator(g, Color.WHITE, Direction.DOWN, above.centerX(), above.y() - 2);
				}
			}
		}
	}

	private void drawGhostSeats(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		game.ghosts().forEach(ghost -> {
			g.setColor(ghostColor(ghost));
			int x = ghost.seat.position.roundedX(), y = ghost.seat.position.roundedY();
			String text = String.valueOf(ghost.seat.number);
			g.drawRoundRect(x, y, Tile.SIZE, Tile.SIZE, 2, 2);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 6));
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D r = fm.getStringBounds(text, g);
			g.setColor(Color.WHITE);
			g.drawString(text, x + (Tile.SIZE - Math.round(r.getWidth())) / 2, y + Tile.SIZE - 2);
		});
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	private void drawDirectionIndicator(Graphics2D g, Color color, Direction dir, int x, int y) {
		g = (Graphics2D) g.create();
		g.translate(x, y);
		g.rotate((dir.ordinal() - 2) * (PI / 2));
		g.setColor(color);
		g.fillPolygon(arrowHead);
		g.dispose();
	}

	private void drawGhostRoutes(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//@formatter:off
		game.ghostsOnStage()
			.filter(ghost -> ghost.visible)
			.forEach(ghost -> drawGhostRoute(g, ghost));
		//@formatter:on
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void drawGhostRoute(Graphics2D g, Ghost ghost) {
		if (ghost.steering() instanceof PathProvidingSteering) {
			PathProvidingSteering steering = (PathProvidingSteering) ghost.steering();
			steering.setPathComputationEnabled(true);
			drawTargetPathAndRubberBand(g, ghost, steering);
		} else if (ghost.wishDir() != null) {
			Vector2f v = ghost.wishDir().vector();
			drawDirectionIndicator(g, ghostColor(ghost), ghost.wishDir(),
					ghost.tf.getCenter().roundedX() + v.roundedX() * Tile.SIZE,
					ghost.tf.getCenter().roundedY() + v.roundedY() * Tile.SIZE);
		}
		if (ghost.targetTile() == null) {
			return;
		}
		if (ghost == game.inky) {
			drawInkyChasing(g, game.inky);
		} else if (ghost == game.clyde) {
			drawClydeChasingArea(g, game.clyde);
		}
	}

	private void drawTargetPathAndRubberBand(Graphics2D g, Ghost ghost, PathProvidingSteering steering) {
		g = (Graphics2D) g.create();
		Tile targetTile = ghost.targetTile();
		if (targetTile == null) {
			return;
		}
		List<Tile> path = steering.pathToTarget();
		if (path.size() == 0 || targetTile == path.get(path.size() - 1)) {
			return;
		}
		Stroke solid = new BasicStroke(0.5f);
		Stroke dashed = new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
		Color ghostColor = ghostColor(ghost);
		// draw rubber band to target tile
		int x1 = ghost.tf.getCenter().roundedX(), y1 = ghost.tf.getCenter().roundedY();
		int x2 = targetTile.centerX(), y2 = targetTile.centerY();
		g.setStroke(dashed);
		g.setColor(alpha(ghostColor, 200));
		g.drawLine(x1, y1, x2, y2);
		g.translate(targetTile.x(), targetTile.y());
		g.setColor(ghostColor);
		g.setStroke(solid);
		g.fillRect(2, 2, 4, 4);
		g.translate(-targetTile.x(), -targetTile.y());
		if (path.size() > 1) {
			g.setColor(alpha(ghostColor, 200));
			for (int i = 0; i < path.size() - 1; ++i) {
				Tile from = path.get(i), to = path.get(i + 1);
				g.setColor(ghostColor);
				g.setStroke(solid);
				g.drawLine(from.centerX(), from.centerY(), to.centerX(), to.centerY());
				if (i + 1 == path.size() - 1) {
					drawDirectionIndicator(g, ghostColor, from.dirTo(to).get(), to.centerX(), to.centerY());
				}
			}
		}
		g.dispose();
	}

	private void drawInkyChasing(Graphics2D g, Ghost inky) {
		if (!inky.is(CHASING)) {
			return;
		}
		int x1, y1, x2, y2, x3, y3;
		x1 = game.blinky.tile().centerX();
		y1 = game.blinky.tile().centerY();
		x2 = inky.targetTile().centerX();
		y2 = inky.targetTile().centerY();
		g.setColor(Color.GRAY);
		g.drawLine(x1, y1, x2, y2);
		Tile pacManTile = game.pacMan.tile();
		Direction pacManDir = game.pacMan.moveDir();
		int s = Tile.SIZE / 2; // size of target square
		g.setColor(Color.GRAY);
		if (!settings.fixOverflowBug && pacManDir == Direction.UP) {
			Tile twoAhead = game.maze.tileToDir(pacManTile, pacManDir, 2);
			Tile twoLeft = game.maze.tileToDir(twoAhead, Direction.LEFT, 2);
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
			Tile twoTilesAhead = game.pacMan.tilesAhead(2);
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
		Color ghostColor = ghostColor(game.clyde);
		int cx = game.clyde.tile().centerX(), cy = game.clyde.tile().centerY();
		int r = 8 * Tile.SIZE;
		g.setColor(alpha(ghostColor, 100));
		g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
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

	private int patternIndex(int col, int row) {
		return (col + row) % gridPatternColor.length;
	}

	private BufferedImage createGridPatternImage(int cols, int rows) {
		int width = cols * Tile.SIZE, height = rows * Tile.SIZE + 1;
		BufferedImage img = Assets.createBufferedImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D g = img.createGraphics();
		g.setColor(gridPatternColor[0]);
		g.fillRect(0, 0, width, height);
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col) {
				int patternIndex = patternIndex(col, row);
				if (patternIndex != 0) {
					g.setColor(gridPatternColor[patternIndex]);
					g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
				}
			}
		}
		g.dispose();
		return img;
	}
}