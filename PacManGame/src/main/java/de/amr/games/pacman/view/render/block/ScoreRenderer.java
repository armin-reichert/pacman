package de.amr.games.pacman.view.render.block;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.api.IScoreRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class ScoreRenderer implements IScoreRenderer {

	private final World world;
	private final Theme theme;

	public ScoreRenderer(World world, Theme theme) {
		this.world = world;
		this.theme = theme;
	}

	@Override
	public void draw(Graphics2D g, Game game) {
		drawScores(g, game);
		drawLives(g, game);
		drawLevelCounter(g, game);
	}

	protected void drawScores(Graphics2D g, Game game) {
		int topMargin = 3;
		int lineOffset = 2;
		Color hilight = Color.YELLOW;
		int col;
		g.translate(0, topMargin); // margin between score and upper window border
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());

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

	protected void drawLives(Graphics2D g, Game game) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int sz = 2 * Tile.SIZE;
		for (int i = 0, x = sz; i < game.lives; ++i, x += sz) {
			g.setColor(Color.YELLOW);
			g.fillOval(x, world.height() * Tile.SIZE - sz, Tile.SIZE, Tile.SIZE);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	protected void drawLevelCounter(Graphics2D g, Game game) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int max = 7;
//		int first = Math.max(0, game.levelCounter.size() - max);
		int n = Math.min(max, game.levelCounter.size());
		int y = (world.height() - 2) * Tile.SIZE;
		for (int i = 0, x = (world.width() - 3) * Tile.SIZE; i < n; ++i, x -= 2 * Tile.SIZE) {
//			Symbol symbol = game.levelCounter.get(first + i);
			g.setColor(Color.GREEN);
			g.drawOval(x, y, Tile.SIZE, Tile.SIZE);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}