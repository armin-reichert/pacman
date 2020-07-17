package de.amr.games.pacman.view.theme.arcade;

import static de.amr.games.pacman.model.world.api.Direction.LEFT;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.view.theme.api.IRenderer;

public class LiveCounterRenderer implements IRenderer {

	private final Game game;
	private final Image pacManLookingLeft;

	public LiveCounterRenderer(Game game) {
		this.game = game;
		ArcadeThemeSprites arcadeSprites = ArcadeTheme.IT.$value("sprites");
		pacManLookingLeft = arcadeSprites.makeSprite_pacManWalking(LEFT).frame(1);
	}

	@Override
	public void render(Graphics2D g) {
		for (int i = 0, x = Tile.SIZE; i < game.lives; ++i, x += 2 * Tile.SIZE) {
			g.drawImage(pacManLookingLeft, x, 0, null);
		}
	}
}