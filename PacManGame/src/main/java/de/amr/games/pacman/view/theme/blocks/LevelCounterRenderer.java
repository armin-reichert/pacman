package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IRenderer;

class LevelCounterRenderer implements IRenderer {

	private final Game game;

	public LevelCounterRenderer(Game game) {
		this.game = game;
	}

	@Override
	public void render(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int max = 7;
		int n = Math.min(max, game.levelCounter.size());
		int y = -2 * Tile.SIZE;
		for (int i = 0, x = -3 * Tile.SIZE; i < n; ++i, x -= 2 * Tile.SIZE) {
			g.setColor(Color.GREEN);
			g.drawOval(x, y, Tile.SIZE, Tile.SIZE);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}