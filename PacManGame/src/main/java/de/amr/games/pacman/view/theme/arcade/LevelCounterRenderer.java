package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.view.api.IGameScoreRenderer;

class LevelCounterRenderer implements IGameScoreRenderer {

	private final Map<ArcadeBonus, Image> bonusImages = new HashMap<ArcadeBonus, Image>();

	public LevelCounterRenderer() {
		ArcadeSprites arcadeSprites = ArcadeTheme.THEME.$value("sprites");
		for (ArcadeBonus symbol : ArcadeBonus.values()) {
			bonusImages.put(symbol, arcadeSprites.makeSprite_bonusSymbol(symbol.name()).frame(0));
		}
	}

	@Override
	public void render(Graphics2D g, PacManGame game) {
		int max = 7;
		int first = Math.max(0, game.levelCounter.size() - max);
		int n = Math.min(max, game.levelCounter.size());
		int size = 2 * Tile.SIZE; // image size
		for (int i = 0, x = -2 * size; i < n; ++i, x -= size) {
			ArcadeBonus symbol = ArcadeBonus.valueOf(game.levelCounter.get(first + i));
			g.drawImage(bonusImages.get(symbol), x, 0, size, size, null);
		}
	}
}