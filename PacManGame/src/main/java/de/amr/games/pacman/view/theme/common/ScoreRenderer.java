package de.amr.games.pacman.view.theme.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IRenderer;

public class ScoreRenderer implements IRenderer {

	private final Game game;
	private Font font;

	public ScoreRenderer(Game game) {
		this.game = game;
		font = new Font(Font.MONOSPACED, Font.PLAIN, 8);
	}

	public void setFont(Font font) {
		this.font = font;
	}

	@Override
	public void render(Graphics2D g) {
		int topMargin = 3;
		int lineOffset = 2;
		Color hilight = Color.YELLOW;
		int col;
		g.translate(0, topMargin); // margin between score and upper window border
		try (Pen pen = new Pen(g)) {
			pen.font(font);

			// Game score
			col = 1;
			pen.color(hilight);
			pen.drawAtGridPosition("Score".toUpperCase(), col, 0, Tile.SIZE);

			pen.down(lineOffset);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%7d", game.score), col, 1, Tile.SIZE);
			pen.up(lineOffset);

			// Highscore
			col = 9;
			pen.color(hilight);
			pen.drawAtGridPosition("High Score".toUpperCase(), col, 0, Tile.SIZE);
			pen.down(lineOffset);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%7d", game.hiscore.points), col, 1, Tile.SIZE);
			pen.color(Color.LIGHT_GRAY);
			pen.drawAtGridPosition(String.format("L%02d", game.hiscore.level), col + 7, 1, Tile.SIZE);
			pen.up(lineOffset);

			col = 21;
			pen.color(hilight);
			pen.drawAtGridPosition(String.format("Level".toUpperCase()), col, 0, Tile.SIZE);
			// Level number
			pen.down(lineOffset);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%02d", game.level.number), col, 1, Tile.SIZE);
			// Number of remaining pellets
			g.setColor(Color.PINK);
			g.translate(0, (topMargin + lineOffset) - 2);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.fillOval((col + 2) * Tile.SIZE + 2, Tile.SIZE, 4, 4); // dot image
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.translate(0, -(topMargin + lineOffset) + 2);
			pen.color(Color.WHITE);
			pen.drawAtGridPosition(String.format("%03d", game.level.remainingFoodCount()), col + 3, 1, Tile.SIZE);
			pen.up(lineOffset);
		}
		g.translate(0, -topMargin);
	}
}