package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.IRenderer;

class LiveCounterRenderer implements IRenderer {

	private final Game game;

	public LiveCounterRenderer(Game game) {
		this.game = game;
	}

	@Override
	public void render(Graphics2D g) {
		smoothDrawingOn(g);
		g.setColor(Color.YELLOW);
		for (int i = 0, x = 0; i < game.lives; ++i, x += 2 * Tile.SIZE) {
			g.fillOval(x, 0, Tile.SIZE, Tile.SIZE);
		}
		smoothDrawingOff(g);
	}
}