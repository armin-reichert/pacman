package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.view.api.IGameRenderer;
import de.amr.games.pacman.view.common.Rendering;

class LivesCounterRenderer implements IGameRenderer {

	@Override
	public void render(Graphics2D g, PacManGame level) {
		Rendering.smoothOn(g);
		g.setColor(Color.YELLOW);
		for (int i = 0, x = 0; i < level.lives; ++i, x += 2 * Tile.SIZE) {
			g.fillOval(x, 0, Tile.SIZE, Tile.SIZE);
		}
		Rendering.smoothOff(g);
	}
}