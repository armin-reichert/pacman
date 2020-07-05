package de.amr.games.pacman.view.theme.arcade;

import static de.amr.games.pacman.model.Direction.RIGHT;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.ghosthouse.GhostHouseAccessControl;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.api.Population;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.common.Rendering;

public class GhostHouseStateRenderer {

	private final World world;
	private final Image inkyImage, clydeImage, pacManImage;
	private GhostHouseAccessControl houseAccessControl;

	public GhostHouseStateRenderer(World world) {
		this.world = world;
		inkyImage = ArcadeSprites.BUNDLE.spr_ghostColored(Population.CYAN_GHOST, Direction.RIGHT).frame(0);
		clydeImage = ArcadeSprites.BUNDLE.spr_ghostColored(Population.ORANGE_GHOST, Direction.RIGHT).frame(0);
		pacManImage = ArcadeSprites.BUNDLE.spr_pacManWalking(RIGHT).frame(0);
	}

	public void setHouseAccessControl(GhostHouseAccessControl houseAccessControl) {
		this.houseAccessControl = houseAccessControl;
	}

	public void draw(Graphics2D g) {
		drawGhostHouseState(g, houseAccessControl);
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

	private void drawPacManStarvingTime(Graphics2D g, GhostHouseAccessControl houseAccessControl) {
		int col = 1, row = 14;
		int time = houseAccessControl.pacManStarvingTicks();
		g.drawImage(pacManImage, col * Tile.SIZE, row * Tile.SIZE, 10, 10, null);
		try (Pen pen = new Pen(g)) {
			pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
			pen.color(Color.WHITE);
			pen.smooth(() -> pen.drawAtGridPosition(time == -1 ? Rendering.INFTY : String.format("%d", time), col + 2, row,
					Tile.SIZE));
		}
	}
}