package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.view.api.IGameScoreRenderer;

class LivesCounterRenderer implements IGameScoreRenderer {

	private final Image pacManLookingLeft;

	public LivesCounterRenderer() {
		ArcadeSprites arcadeSprites = ArcadeTheme.THEME.$value("sprites");
		pacManLookingLeft = arcadeSprites.tile(1, 1);
	}

	@Override
	public void render(Graphics2D g, Game game) {
		for (int i = 0, x = Tile.SIZE; i < game.level.lives; ++i, x += 2 * Tile.SIZE) {
			g.drawImage(pacManLookingLeft, x, 0, null);
		}
	}
}