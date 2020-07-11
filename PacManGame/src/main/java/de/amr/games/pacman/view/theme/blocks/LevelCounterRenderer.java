package de.amr.games.pacman.view.theme.blocks;

import static de.amr.games.pacman.view.theme.blocks.BlocksTheme.symbolColor;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.arcade.Symbol;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.api.IRenderer;

class LevelCounterRenderer implements IRenderer {

	private final Game game;

	public LevelCounterRenderer(Game game) {
		this.game = game;
	}

	@Override
	public void render(Graphics2D g) {
		smoothDrawingOn(g);
		int levels = game.levelCounter.size();
		for (int i = 0, x = -2 * Tile.SIZE; i < Math.min(7, levels); ++i, x -= 2 * Tile.SIZE) {
			Symbol symbol = game.levelCounter.get(levels > 7 ? levels - 7 + i : i);
			g.setColor(symbolColor(symbol.name()));
			g.drawOval(x, 0, Tile.SIZE, Tile.SIZE);
		}
		smoothDrawingOff(g);
	}
}