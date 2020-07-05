package de.amr.games.pacman.view.render.sprite;

import static de.amr.games.pacman.model.Direction.LEFT;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.IRenderer;

public class LiveCounterRenderer implements IRenderer {

	private final Game game;
	private final Image pacManLookingLeft;

	public LiveCounterRenderer(Game game) {
		this.game = game;
		pacManLookingLeft = ArcadeSprites.BUNDLE.spr_pacManWalking(LEFT).frame(1);
	}

	@Override
	public void draw(Graphics2D g) {
		int sz = 2 * Tile.SIZE;
		for (int i = 0, x = sz; i < game.lives; ++i, x += sz) {
			g.drawImage(pacManLookingLeft, x, -sz, null);
		}
	}
}