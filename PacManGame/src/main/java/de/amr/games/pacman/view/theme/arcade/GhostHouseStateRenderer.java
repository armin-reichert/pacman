package de.amr.games.pacman.view.theme.arcade;

import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Population.CYAN_GHOST;
import static de.amr.games.pacman.model.world.api.Population.ORANGE_GHOST;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.ghosthouse.GhostHouseDoorMan;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.common.Rendering;

public class GhostHouseStateRenderer implements IRenderer {

	private final World world;
	private final Image inkyImage, clydeImage, pacManImage;
	private final GhostHouseDoorMan doorMan;

	public GhostHouseStateRenderer(World world, GhostHouseDoorMan doorMan) {
		this.world = world;
		this.doorMan = doorMan;
		inkyImage = ArcadeTheme.ASSETS.makeSprite_ghostColored(CYAN_GHOST, RIGHT).frame(0);
		clydeImage = ArcadeTheme.ASSETS.makeSprite_ghostColored(ORANGE_GHOST, RIGHT).frame(0);
		pacManImage = ArcadeTheme.ASSETS.makeSprite_pacManWalking(RIGHT).frame(0);
	}

	@Override
	public void draw(Graphics2D g) {
		if (doorMan == null) {
			return; // test scenes may have no ghost house
		}
		drawPacManStarvingTime(g);
		drawDotCounter(g, clydeImage, doorMan.ghostDotCount(world.population().clyde()), 1, 20,
				!doorMan.isGlobalDotCounterEnabled() && doorMan.isPreferredGhost(world.population().clyde()));
		drawDotCounter(g, inkyImage, doorMan.ghostDotCount(world.population().inky()), 24, 20,
				!doorMan.isGlobalDotCounterEnabled() && doorMan.isPreferredGhost(world.population().inky()));
		drawDotCounter(g, null, doorMan.globalDotCount(), 24, 14, doorMan.isGlobalDotCounterEnabled());
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

	private void drawPacManStarvingTime(Graphics2D g) {
		int col = 1, row = 14;
		int time = doorMan.pacManStarvingTicks();
		g.drawImage(pacManImage, col * Tile.SIZE, row * Tile.SIZE, 10, 10, null);
		try (Pen pen = new Pen(g)) {
			pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
			pen.color(Color.WHITE);
			pen.smooth(() -> pen.drawAtGridPosition(time == -1 ? Rendering.INFTY : String.format("%d", time), col + 2, row,
					Tile.SIZE));
		}
	}
}