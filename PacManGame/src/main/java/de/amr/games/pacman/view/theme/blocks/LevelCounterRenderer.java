package de.amr.games.pacman.view.theme.blocks;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.arcade.Symbol;
import de.amr.games.pacman.view.api.IGameRenderer;
import de.amr.games.pacman.view.common.Rendering;

class LevelCounterRenderer implements IGameRenderer {

	@Override
	public void render(Graphics2D g, Game game) {
		Rendering.smoothOn(g);
		int levels = game.levelCounter.size();
		for (int i = 0, x = -2 * Tile.SIZE; i < Math.min(7, levels); ++i, x -= 2 * Tile.SIZE) {
			Symbol symbol = game.levelCounter.get(levels > 7 ? levels - 7 + i : i);
			g.setColor(BlocksTheme.THEME.symbolColor(symbol.name()));
			g.drawOval(x, 0, Tile.SIZE, Tile.SIZE);
		}
		Rendering.smoothOff(g);
	}
}