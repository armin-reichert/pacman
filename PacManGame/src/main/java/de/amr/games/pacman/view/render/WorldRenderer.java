package de.amr.games.pacman.view.render;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.view.render.Rendering.drawDirectionIndicator;
import static de.amr.games.pacman.view.render.Rendering.ghostColor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.ghosthouse.GhostHouseAccessControl;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Door.DoorState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.Theme;

public class WorldRenderer {

	private static final Color[] GRID_PATTERN = { Color.BLACK, new Color(40, 40, 40) };
	private static final String INFTY = Character.toString('\u221E');
	private static final Font SMALL_FONT = new Font("Arial Narrow", Font.PLAIN, 6);

	private final World world;
	private final Theme theme;
	private final SpriteMap mazeSprites;
	private final SpriteAnimation energizerAnimation;
	private boolean showingGrid;
	private boolean showingStates;
	private boolean showingScores;
	private final Image gridImage;
	private final Image inkyImage, clydeImage, pacManImage;
	private GhostCommand ghostCommand;
	private GhostHouseAccessControl houseAccessControl;

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
		inkyImage = theme.spr_ghostColored(Theme.CYAN_GHOST, Direction.RIGHT).frame(0);
		clydeImage = theme.spr_ghostColored(Theme.ORANGE_GHOST, Direction.RIGHT).frame(0);
		pacManImage = theme.spr_pacManWalking(RIGHT).frame(0);
	}

	public void draw(Graphics2D g) {
		if (showingGrid) {
			g.drawImage(gridImage, 0, 0, null);
			drawGhostBeds(g);
		}
		mazeSprites.current().ifPresent(sprite -> {
			sprite.draw(g, 0, 3 * Tile.SIZE);
		});
		if ("maze-full".equals(mazeSprites.selectedKey())) {
			drawMazeContent(g);
		}
		if (showingGrid) {
			drawOneWayTiles(g);
		}
		if (showingStates) {
			drawActorStates(g);
			if (ghostCommand != null) {
				drawGhostHouseState(g, houseAccessControl);
			}
		}
		energizerAnimation.update();
	}

	public void setHouseAccessControl(GhostHouseAccessControl houseAccessControl) {
		this.houseAccessControl = houseAccessControl;
	}

	public void setGhostCommand(GhostCommand ghostCommand) {
		this.ghostCommand = ghostCommand;
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

	public void setShowingStates(boolean showingStates) {
		this.showingStates = showingStates;
	}

	public boolean isShowingStates() {
		return showingStates;
	}

	public void setShowingScores(boolean showingScores) {
		this.showingScores = showingScores;
	}

	public boolean isShowingScores() {
		return showingScores;
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

	private void drawActorStates(Graphics2D g) {
		world.population().ghosts().filter(world::included).forEach(ghost -> drawGhostState(g, ghost, ghostCommand));
		drawPacManState(g, world.population().pacMan());
	}

	private void drawPacManState(Graphics2D g, PacMan pacMan) {
		if (!pacMan.visible || pacMan.getState() == null) {
			return;
		}
		String text = pacMan.power > 0 ? String.format("POWER(%d)", pacMan.power) : pacMan.getState().name();
		if (settings.pacManImmortable) {
			text += " immortable";
		}
		drawEntityState(g, pacMan, text, Color.YELLOW);
	}

	private void drawEntityState(Graphics2D g, Entity entity, String text, Color color) {
		try (Pen pen = new Pen(g)) {
			pen.color(color);
			pen.font(SMALL_FONT);
			pen.drawCentered(text, entity.tf.getCenter().x, entity.tf.getCenter().y - 2);
		}
	}

	private void drawGhostState(Graphics2D g, Ghost ghost, GhostCommand ghostCommand) {
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

	private void drawPacManStarvingTime(Graphics2D g, GhostHouseAccessControl houseAccessControl) {
		int col = 1, row = 14;
		int time = houseAccessControl.pacManStarvingTicks();
		g.drawImage(pacManImage, col * Tile.SIZE, row * Tile.SIZE, 10, 10, null);
		try (Pen pen = new Pen(g)) {
			pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
			pen.color(Color.WHITE);
			pen.smooth(() -> pen.drawAtGridPosition(time == -1 ? INFTY : String.format("%d", time), col + 2, row, Tile.SIZE));
		}
	}

	private void drawGhostHouseState(Graphics2D g, GhostHouseAccessControl houseAccessControl) {
		if (houseAccessControl == null) {
			return; // test scenes may have no ghost house
		}
		drawPacManStarvingTime(g, houseAccessControl);
		drawDotCounter(g, clydeImage, houseAccessControl.ghostDotCount(world.population().clyde()), 1, 20,
				!houseAccessControl.isGlobalDotCounterEnabled()
						&& houseAccessControl.isPreferredGhost(world.population().clyde()));
		drawDotCounter(g, inkyImage, houseAccessControl.ghostDotCount(world.population().inky()), 24, 20,
				!houseAccessControl.isGlobalDotCounterEnabled()
						&& houseAccessControl.isPreferredGhost(world.population().inky()));
		drawDotCounter(g, null, houseAccessControl.globalDotCount(), 24, 14,
				houseAccessControl.isGlobalDotCounterEnabled());
	}

	private void drawDotCounter(Graphics2D g, Image image, int value, int col, int row, boolean emphasized) {
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