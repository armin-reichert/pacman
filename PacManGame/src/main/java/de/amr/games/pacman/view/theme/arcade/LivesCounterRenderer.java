package de.amr.games.pacman.view.theme.arcade;

import static de.amr.games.pacman.model.world.api.Direction.LEFT;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.view.api.IGameRenderer;

public class LivesCounterRenderer implements IGameRenderer {

	private final Image pacManLookingLeft;

	public LivesCounterRenderer() {
		ArcadeThemeSprites arcadeSprites = ArcadeTheme.THEME.$value("sprites");
		pacManLookingLeft = arcadeSprites.makeSprite_pacManWalking(LEFT).frame(1);
	}

	@Override
	public void render(Graphics2D g, Game game) {
		for (int i = 0, x = Tile.SIZE; i < game.lives; ++i, x += 2 * Tile.SIZE) {
			g.drawImage(pacManLookingLeft, x, 0, null);
		}
	}
}