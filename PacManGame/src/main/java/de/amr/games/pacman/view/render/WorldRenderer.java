package de.amr.games.pacman.view.render;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static java.lang.Math.PI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.List;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.steering.PathProvidingSteering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Door.DoorState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.Theme;

public class WorldRenderer {

	private static final Color[] GRID_PATTERN = { Color.BLACK, new Color(40, 40, 40) };
	private static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);

	private final World world;
	private final Theme theme;
	private final SpriteMap mazeSprites;
	private final SpriteAnimation energizerAnimation;
	private boolean showingGrid;
	private boolean showingRoutes;
	private final Image gridImage;

	public WorldRenderer(World world, Theme theme) {
		this.world = world;
		this.theme = theme;
		mazeSprites = new SpriteMap();
		mazeSprites.set("maze-full", theme.spr_fullMaze());
		mazeSprites.set("maze-empty", theme.spr_emptyMaze());
		mazeSprites.set("maze-flashing", theme.spr_flashingMaze());
		energizerAnimation = new CyclicAnimation(2);
		energizerAnimation.setFrameDuration(150);
		energizerAnimation.setEnabled(false);
		gridImage = createGridPatternImage(world.width(), world.height());
	}

	public void draw(Graphics2D g) {
		if (showingGrid) {
			g.drawImage(gridImage, 0, 0, null);
			drawOneWayTiles(g);
			drawGhostBeds(g);
		}
		mazeSprites.current().ifPresent(sprite -> {
			sprite.draw(g, 0, 3 * Tile.SIZE);
		});
		if ("maze-full".equals(mazeSprites.selectedKey())) {
			drawMazeContent(g);
		}
		if (showingRoutes) {
			drawGhostRoutes(g);
		}
		energizerAnimation.update();
	}

	public void selectSprite(String spriteKey) {
		mazeSprites.select(spriteKey);
	}

	public void enableSpriteAnimation(boolean enabled) {
		mazeSprites.current().ifPresent(sprite -> {
			sprite.enableAnimation(enabled);
		});
	}

	public void setShowingGrid(boolean showingGrid) {
		this.showingGrid = showingGrid;
	}

	public boolean isShowingGrid() {
		return showingGrid;
	}

	public void setShowingRoutes(boolean showingRoutes) {
		this.showingRoutes = showingRoutes;
	}

	public boolean isShowingRoutes() {
		return showingRoutes;
	}

	private void drawMazeContent(Graphics2D g) {
		// hide eaten food
		world.habitatTiles().filter(world::containsEatenFood).forEach(tile -> {
			g.setColor(tileColor(tile));
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// simulate energizer blinking animation
		if (energizerAnimation.isEnabled() && energizerAnimation.currentFrameIndex() == 1) {
			world.habitatTiles().filter(world::containsEnergizer).forEach(tile -> {
				g.setColor(tileColor(tile));
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		// draw bonus when active or consumed
		world.getBonus().filter(bonus -> bonus.state != BonusState.INACTIVE).ifPresent(bonus -> {
			Sprite sprite = bonus.state == BonusState.CONSUMED ? theme.spr_number(bonus.value)
					: theme.spr_bonusSymbol(bonus.symbol);
			g.drawImage(sprite.frame(0), world.bonusTile().x(), world.bonusTile().y() - Tile.SIZE / 2, null);
		});
		// draw doors depending on their state
		world.theHouse().doors().filter(door -> door.state == DoorState.OPEN).forEach(door -> {
			g.setColor(Color.BLACK);
			door.tiles.forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE));
		});
	}

	public void letEnergizersBlink(boolean enabled) {
		energizerAnimation.setEnabled(enabled);
	}

	protected Color tileColor(Tile tile) {
		return showingGrid ? GRID_PATTERN[patternIndex(tile.col, tile.row)] : Color.BLACK;
	}

	private Color alpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private int patternIndex(int col, int row) {
		return (col + row) % GRID_PATTERN.length;
	}

	private BufferedImage createGridPatternImage(int cols, int rows) {
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

	private void drawOneWayTiles(Graphics2D g) {
		world.oneWayTiles().forEach(oneWay -> {
			drawDirectionIndicator(g, Color.WHITE, false, oneWay.dir, oneWay.tile.centerX(), oneWay.tile.y());
		});
	}

	public Color ghostColor(Ghost ghost) {
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

	private void drawGhostBeds(Graphics2D g2) {
		Graphics2D g = (Graphics2D) g2.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		world.population().ghosts().forEach(ghost -> {
			Bed bed = ghost.bed();
			int x = bed.center.roundedX() - Tile.SIZE, y = bed.center.roundedY() - Tile.SIZE / 2;
			g.setColor(ghostColor(ghost));
			g.drawRoundRect(x, y, 2 * Tile.SIZE, Tile.SIZE, 2, 2);
			try (Pen pen = new Pen(g)) {
				pen.color(Color.WHITE);
				pen.font(new Font(Font.MONOSPACED, Font.BOLD, 6));
				pen.drawCentered("" + bed.number, bed.center.roundedX(), bed.center.roundedY() + Tile.SIZE);
			}
		});
		g.dispose();
	}

	public void drawDirectionIndicator(Graphics2D g, Color color, boolean fill, Direction dir, int x, int y) {
		g = (Graphics2D) g.create();
		g.setStroke(new BasicStroke(0.1f));
		g.translate(x, y);
		g.rotate((dir.ordinal() - 2) * (PI / 2));
		g.setColor(color);
		if (fill) {
			g.fillPolygon(TRIANGLE);
		} else {
			g.drawPolygon(TRIANGLE);
		}
		g.dispose();
	}

	private void drawGhostRoutes(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		world.population().ghosts().filter(world::included).forEach(ghost -> drawGhostRoute(g, ghost));
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
			drawDirectionIndicator(g, ghostColor(ghost), true, ghost.wishDir(),
					ghost.tf.getCenter().roundedX() + v.roundedX() * Tile.SIZE,
					ghost.tf.getCenter().roundedY() + v.roundedY() * Tile.SIZE);
		}
		if (ghost.targetTile() == null) {
			return;
		}
		if (ghost == world.population().inky()) {
			drawInkyChasing(g, ghost);
		} else if (ghost == world.population().clyde()) {
			drawClydeChasingArea(g, ghost);
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
				drawDirectionIndicator(g, ghostColor, true, from.dirTo(to).get(), to.centerX(), to.centerY());
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
		if (!inky.is(CHASING) || !world.included(blinky)) {
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

}