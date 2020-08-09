package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.view.api.IGameScoreRenderer;
import de.amr.games.pacman.view.common.Rendering;

class LivesCounterRenderer implements IGameScoreRenderer {

	@Override
	public void render(Graphics2D g, Game game) {
		Rendering.smoothOn(g);
		g.setColor(Color.YELLOW);
		for (int i = 0, x = 0; i < game.lives; ++i, x += 2 * Tile.SIZE) {
			g.fillOval(x, 0, Tile.SIZE, Tile.SIZE);
		}
		Rendering.smoothOff(g);
	}
}