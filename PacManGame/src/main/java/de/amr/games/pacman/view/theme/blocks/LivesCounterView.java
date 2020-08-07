package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.view.theme.api.IRenderer;

class LivesCounterView implements View, IRenderer {

	private final Game game;

	public LivesCounterView(Game game) {
		this.game = game;
	}

	@Override
	public void draw(Graphics2D g) {
		smoothOn(g);
		g.setColor(Color.YELLOW);
		for (int i = 0, x = 0; i < game.lives; ++i, x += 2 * Tile.SIZE) {
			g.fillOval(x, 0, Tile.SIZE, Tile.SIZE);
		}
		smoothOff(g);
	}
}