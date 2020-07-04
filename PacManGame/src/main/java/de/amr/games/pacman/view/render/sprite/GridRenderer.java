package de.amr.games.pacman.view.render.sprite;

import static de.amr.games.pacman.view.render.sprite.Rendering.drawDirectionIndicator;
import static de.amr.games.pacman.view.render.sprite.Rendering.ghostColor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.api.IRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class GridRenderer implements IRenderer {

	private final World world;
	private final Theme theme;
	private final Image gridImage;

	public GridRenderer(World world, Theme theme) {
		this.world = world;
		this.theme = theme;
		gridImage = createGridPatternImage(world.width(), world.height());

	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(gridImage, 0, 0, null);
		drawGhostBeds(g);
		drawOneWayTiles(g);
	}

	private BufferedImage createGridPatternImage(int cols, int rows) {
		int width = cols * Tile.SIZE, height = rows * Tile.SIZE + 1;
		BufferedImage img = Assets.createBufferedImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D g = img.createGraphics();
		g.setColor(Rendering.GRID_PATTERN[0]);
		g.fillRect(0, 0, width, height);
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col) {
				int i = Rendering.patternIndex(col, row);
				if (i != 0) {
					g.setColor(Rendering.GRID_PATTERN[i]);
					g.fillRect(col * Tile.SIZE, row * Tile.SIZE, Tile.SIZE, Tile.SIZE);
				}
			}
		}
		g.dispose();
		return img;
	}

	public void drawOneWayTiles(Graphics2D g) {
		world.oneWayTiles().forEach(oneWay -> {
			drawDirectionIndicator(g, Color.WHITE, false, oneWay.dir, oneWay.tile.centerX(), oneWay.tile.y());
		});
	}

	public void drawGhostBeds(Graphics2D g2) {
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
}
