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

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.widgets.FramerateWidget;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.GhostHouseAccess;
import de.amr.games.pacman.controller.actor.Bonus;
import de.amr.games.pacman.controller.actor.BonusState;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.steering.PathProvidingSteering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.api.Fsm;
import de.amr.statemachine.core.State;

/**
 * An extended play view that can visualize actor states, the ghost house pellet counters, ghost
 * routes, the grid background, ghost seats and the current framerate.
 * 
 * @author Armin Reichert
 */
public class PlayView extends SimplePlayView {

	private static final String INFTY = Character.toString('\u221E');
	private static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);
	private static final Color[] GRID_PATTERN = { Color.BLACK, new Color(40, 40, 40) };
	private static final Font SMALL_FONT = new Font("Arial Narrow", Font.PLAIN, 6);

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

	private static int patternIndex(int col, int row) {
		return (col + row) % GRID_PATTERN.length;
	}

	private static BufferedImage createGridPatternImage(int cols, int rows) {
		int width = cols * Tile.SIZE, height = rows * Tile.SIZE + 1;
		BufferedImage img = Assets.createBufferedImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D g = img.createGraphics();
		g.setColor(GRID_PATTERN[0]);
		g.fillRect(0, 0, width, height);
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col) {
				int i = patternIndex(col, row);
				if (i != 0) {
					g.setColor(GRID_PATTERN[i]);
					g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
				}
			}
		}
		g.dispose();
		return img;
	}

	public boolean showFrameRate = false;
	public boolean showGrid = false;
	public boolean showRoutes = false;
	public boolean showScores = true;
	public boolean showStates = false;

	public Fsm<GhostState, ?> ghostCommand; // (optional)
	public GhostHouseAccess house; // (optional)

	private FramerateWidget frameRateDisplay;
	private final BufferedImage gridImage, inkyImage, clydeImage, pacManImage;

	public PlayView(PacManWorld world, Game game, Theme theme) {
		super(world, game, theme);
		frameRateDisplay = new FramerateWidget();
		frameRateDisplay.tf.setPosition(0, 18 * Tile.SIZE);
		frameRateDisplay.font = new Font(Font.MONOSPACED, Font.BOLD, 8);
		gridImage = createGridPatternImage(world.width(), world.height());
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
			drawOneWayTiles(g);
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
			drawActorOffTrack(g);
		}
		if (showStates) {
			drawActorStates(g);
			drawGhostHouseState(g);
		}
	}

	@Override
	protected Color tileColor(Tile tile) {
		return showGrid ? GRID_PATTERN[patternIndex(tile.col, tile.row)] : super.tileColor(tile);
	}

	private void drawEntityState(Graphics2D g, Entity entity, String text, Color color) {
		try (Pen pen = new Pen(g)) {
			pen.color(color);
			pen.font(SMALL_FONT);
			pen.drawCentered(text, entity.tf.getCenter().x, entity.tf.getCenter().y - 2);
		}
	}

	private void drawPlayMode(Graphics2D g) {
		if (settings.demoMode) {
			try (Pen pen = new Pen(g)) {
				pen.font(theme.fnt_text());
				pen.color(Color.LIGHT_GRAY);
				pen.hcenter("Autopilot", width(), 15, Tile.SIZE);
			}
		}
	}

	private void drawActorStates(Graphics2D g) {
		world.population().ghosts().filter(world::isOnStage).forEach(ghost -> drawGhostState(g, ghost));
		drawPacManState(g, world.population().pacMan());
		drawBonusState(g, world.population().bonus());
	}

	private void drawPacManState(Graphics2D g, PacMan pacMan) {
		if (!pacMan.visible) {
			return;
		}
		if (pacMan.getState() == null) {
			return; // may happen in test applications where Pac-Man is not used
		}
		String text = pacMan.power > 0 ? String.format("POWER(%d)", pacMan.power) : pacMan.getState().name();
		if (settings.pacManImmortable) {
			text += "immortable";
		}
		drawEntityState(g, pacMan, text, Color.YELLOW);
	}

	private void drawGhostState(Graphics2D g, Ghost ghost) {
		if (!ghost.visible) {
			return;
		}
		if (ghost.getState() == null) {
			return; // may happen in test applications where not all ghosts are used
		}
		StringBuilder text = new StringBuilder();
		// show ghost name if not obvious
		text.append(ghost.is(DEAD, FRIGHTENED, ENTERING_HOUSE) ? ghost.name : "");
		// timer values
		int duration = ghost.state().getDuration();
		int remaining = ghost.state().getTicksRemaining();
		// chasing or scattering time
		if (ghostCommand != null && ghost.is(SCATTERING, CHASING)) {
			if (ghostCommand.state() != null) {
				duration = ghostCommand.state().getDuration();
				remaining = ghostCommand.state().getTicksRemaining();
			}
		}
		if (duration != Integer.MAX_VALUE) {
			text.append(String.format("(%s,%d|%d)", ghost.getState(), remaining, duration));
		} else {
			text.append(String.format("(%s,%s)", ghost.getState(), INFTY));
		}
		if (ghost.is(LEAVING_HOUSE)) {
			text.append(String.format("[->%s]", ghost.subsequentState));
		}
		drawEntityState(g, ghost, text.toString(), ghostColor(ghost));
	}

	private void drawBonusState(Graphics2D g, Bonus bonus) {
		State<BonusState> state = bonus.state();
		String text = bonus.is(BonusState.INACTIVE) ? "Bonus inactive"
				: String.format("%s,%d|%d", bonus, state.getTicksRemaining(), state.getDuration());
		drawEntityState(g, bonus, text, Color.YELLOW);
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

	private void drawActorOffTrack(Graphics2D g) {
		world.population().creatures().filter(world::isOnStage).forEach(actor -> drawActorOffTrack(actor, g));
	}

	private void drawActorOffTrack(Creature<?> actor, Graphics2D g) {
		if (!actor.visible) {
			return;
		}
		Stroke normal = g.getStroke();
		Stroke fine = new BasicStroke(0.2f);
		g.setStroke(fine);
		g.setColor(Color.RED);
		g.translate(actor.tf.x, actor.tf.y);
		int w = actor.tf.width, h = actor.tf.height;
		Direction moveDir = actor.moveDir();
		if ((moveDir == Direction.LEFT || moveDir == Direction.RIGHT) && round(actor.tf.y) % Tile.SIZE != 0) {
			g.drawLine(0, 0, w, 0);
			g.drawLine(0, h, w, h);
		}
		if ((moveDir == Direction.UP || moveDir == Direction.DOWN) && round(actor.tf.x) % Tile.SIZE != 0) {
			g.drawLine(0, 0, 0, h);
			g.drawLine(w, 0, w, h);
		}
		g.translate(-actor.tf.x, -actor.tf.y);
		g.setStroke(normal);
	}

	private void drawOneWayTiles(Graphics2D g) {
		world.oneWayTiles().forEach(oneWay -> {
			drawDirectionIndicator(g, Color.WHITE, oneWay.dir, oneWay.tile.centerX(), oneWay.tile.y());
		});
	}

	private void drawGhostSeats(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		world.population().ghosts().forEach(ghost -> {
			g.setColor(ghostColor(ghost));
			int x = ghost.bed().position.roundedX(), y = ghost.bed().position.roundedY();
			String text = String.valueOf(ghost.bed().number);
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
		g.fillPolygon(TRIANGLE);
		g.dispose();
	}

	private void drawGhostRoutes(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		world.population().ghosts().filter(world::isOnStage).forEach(ghost -> drawGhostRoute(g, ghost));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void drawGhostRoute(Graphics2D g, Ghost ghost) {
		if (ghost.steering() instanceof PathProvidingSteering && ghost.targetTile() != null) {
			drawTargetTileRubberband(g, ghost, ghost.targetTile());
			PathProvidingSteering steering = (PathProvidingSteering) ghost.steering();
			if (!steering.isPathComputed()) {
				steering.setPathComputed(true);
			}
			drawTargetTilePath(g, steering.pathToTarget(), ghostColor(ghost));
		} else if (ghost.wishDir() != null) {
			Vector2f v = ghost.wishDir().vector();
			drawDirectionIndicator(g, ghostColor(ghost), ghost.wishDir(),
					ghost.tf.getCenter().roundedX() + v.roundedX() * Tile.SIZE,
					ghost.tf.getCenter().roundedY() + v.roundedY() * Tile.SIZE);
		}
		if (ghost.targetTile() == null) {
			return;
		}
		if (ghost == world.population().inky()) {
			drawInkyChasing(g, world.population().inky());
		} else if (ghost == world.population().clyde()) {
			drawClydeChasingArea(g, world.population().clyde());
		}
	}

	private void drawTargetTilePath(Graphics2D g, List<Tile> path, Color ghostColor) {
		if (path.size() <= 1) {
			return;
		}
		g = (Graphics2D) g.create();
		g.setStroke(new BasicStroke(0.5f));
		g.setColor(alpha(ghostColor, 200));
		for (int i = 0; i < path.size() - 1; ++i) {
			Tile from = path.get(i), to = path.get(i + 1);
			g.drawLine(from.centerX(), from.centerY(), to.centerX(), to.centerY());
			if (i == path.size() - 2) {
				drawDirectionIndicator(g, ghostColor, from.dirTo(to).get(), to.centerX(), to.centerY());
			}
		}
		g.dispose();
	}

	private void drawTargetTileRubberband(Graphics2D g, Ghost ghost, Tile targetTile) {
		if (targetTile == null) {
			return;
		}
		g = (Graphics2D) g.create();

		// draw dashed line from ghost position to target tile
		Stroke dashed = new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
		int x1 = ghost.tf.getCenter().roundedX(), y1 = ghost.tf.getCenter().roundedY();
		int x2 = targetTile.centerX(), y2 = targetTile.centerY();
		g.setStroke(dashed);
		g.setColor(alpha(ghostColor(ghost), 200));
		g.drawLine(x1, y1, x2, y2);

		// draw solid rectangle indicating target tile
		g.translate(targetTile.x(), targetTile.y());
		g.setColor(ghostColor(ghost));
		g.setStroke(new BasicStroke(0.5f));
		g.fillRect(2, 2, 4, 4);
		g.translate(-targetTile.x(), -targetTile.y());

		g.dispose();
	}

	private void drawInkyChasing(Graphics2D g, Ghost inky) {
		Ghost blinky = world.population().blinky();
		PacMan pacMan = world.population().pacMan();
		if (!inky.is(CHASING) || !world.isOnStage(blinky)) {
			return;
		}
		int x1, y1, x2, y2, x3, y3;
		x1 = blinky.tile().centerX();
		y1 = blinky.tile().centerY();
		x2 = inky.targetTile().centerX();
		y2 = inky.targetTile().centerY();
		g.setColor(Color.GRAY);
		g.drawLine(x1, y1, x2, y2);
		Tile pacManTile = pacMan.tile();
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
		int cx = clyde.tile().centerX(), cy = clyde.tile().centerY();
		int r = 8 * Tile.SIZE;
		g.setColor(alpha(ghostColor, 100));
		g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
	}

	private void drawGhostHouseState(Graphics2D g) {
		if (house == null) {
			return; // test scenes can have no ghost house
		}
		drawPacManStarvingTime(g);
		drawDotCounter(g, clydeImage, house.ghostDotCount(world.population().clyde()), 1, 20,
				!house.isGlobalDotCounterEnabled() && house.isPreferredGhost(world.population().clyde()));
		drawDotCounter(g, inkyImage, house.ghostDotCount(world.population().inky()), 24, 20,
				!house.isGlobalDotCounterEnabled() && house.isPreferredGhost(world.population().inky()));
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