package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.arcade.Symbol;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.IRenderer;

public class LevelCounterRenderer implements IRenderer {

	private final Game game;
	private final Map<Symbol, Image> bonusImages = new HashMap<Symbol, Image>();

	public LevelCounterRenderer(Game game) {
		this.game = game;
		for (Symbol symbol : Symbol.values()) {
			bonusImages.put(symbol, ArcadeTheme.ASSETS.makeSprite_bonusSymbol(symbol.name()).frame(0));
		}
	}

	@Override
	public void render(Graphics2D g) {
		int max = 7;
		int first = Math.max(0, game.levelCounter.size() - max);
		int n = Math.min(max, game.levelCounter.size());
		int size = 2 * Tile.SIZE; // image size
		for (int i = 0, x = -2 * size; i < n; ++i, x -= size) {
			Symbol symbol = game.levelCounter.get(first + i);
			g.drawImage(bonusImages.get(symbol), x, 0, size, size, null);
		}
	}
}