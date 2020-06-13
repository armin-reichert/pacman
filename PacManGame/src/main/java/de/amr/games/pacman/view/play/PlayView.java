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
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

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
import java.util.Objects;
import java.util.function.Supplier;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.widgets.FramerateWidget;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.GhostHouse;
import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.BonusState;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.MovingActor;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.core.State;

/**
 * An extended play view.
 * 
 * @author Armin Reichert
 */
public class PlayView extends SimplePlayView {

	private static final String INFTY = Character.toString('\u221E');

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
			drawSeats(g);
		}
		if (showScores) {
			drawScores(g);
		}
		game.ghosts().map(Ghost::steering).filter(Objects::nonNull)
				.forEach(steering -> steering.enableTargetPathComputation(showRoutes));
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
	protected Color tileColor(Tile tile) {
		return showGrid ? gridPatternColor[patternIndex(tile.col, tile.row)] : super.tileColor(tile);
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
				pen.color(Color.LIGHT_GRAY);
				pen.hcenter("Demo Mode", width, 15, Tile.SIZE);
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
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		List<Ghost> ghostsBySeat = game.ghosts().sorted(comparingInt(ghost -> ghost.seat)).collect(toList());
		for (int seat = 0; seat < 4; ++seat) {
			Vector2f seatPosition = game.maze.ghostSeats[seat].position;
			Ghost ghostAtSeat = ghostsBySeat.get(seat);
			g.setColor(ghostColor(ghostAtSeat));
			int x = seatPosition.roundedX(), y = seatPosition.roundedY();
			String text = String.valueOf(seat);
			g.drawRoundRect(x, y, Tile.SIZE, Tile.SIZE, 2, 2);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 6));
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D r = fm.getStringBounds(text, g);
			g.setColor(Color.WHITE);
			g.drawString(text, x + (Tile.SIZE - Math.round(r.getWidth())) / 2, y + Tile.SIZE - 2);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
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
		Stroke solid = new BasicStroke(0.5f);
		Stroke dashed = new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
		Tile targetTile = ghost.targetTile();
		List<Tile> path = ghost.steering().targetPath();
		int len = path.size();
		Color ghostColor = ghostColor(ghost);
		if (targetTile != null && len > 0 && targetTile != path.get(len - 1)) {
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
		}
		if (len > 1) {
			// draw path
			g.setColor(alpha(ghostColor, 200));
			for (int i = 0; i < path.size() - 1; ++i) {
				Tile from = path.get(i), to = path.get(i + 1);
				g.setColor(ghostColor);
				g.setStroke(solid);
				g.drawLine(from.centerX(), from.centerY(), to.centerX(), to.centerY());
				if (i + 1 == len - 1) {
					drawArrowHead(g, game.maze.direction(from, to).get(), to.centerX(), to.centerY());
				}
			}
		} else if (ghost.wishDir() != null) {
			// draw direction indicator
			int x = ghost.tf.getCenter().roundedX(), y = ghost.tf.getCenter().roundedY();
			g.setColor(ghostColor);
			Vector2f dirVector = ghost.wishDir().vector();
			drawArrowHead(g, ghost.wishDir(), x + dirVector.roundedX() * Tile.SIZE, y + dirVector.roundedY() * Tile.SIZE);
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
				if (!settings.fixOverflowBug && pacManDir == Direction.UP) {
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

	private Color alpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}