package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IRenderer;

class LiveCounterRenderer implements IRenderer {

	private final Game game;

	public LiveCounterRenderer(Game game) {
		this.game = game;
	}

	@Override
	public void render(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int sz = 2 * Tile.SIZE;
		for (int i = 0, x = sz; i < game.lives; ++i, x += sz) {
			g.setColor(Color.YELLOW);
			g.fillOval(x, -sz, Tile.SIZE, Tile.SIZE);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}