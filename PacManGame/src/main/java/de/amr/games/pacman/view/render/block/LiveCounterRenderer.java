package de.amr.games.pacman.view.render.block;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.IRenderer;

public class LiveCounterRenderer implements IRenderer {

	private final World world;
	private final Game game;

	public LiveCounterRenderer(World world, Game game) {
		this.world = world;
		this.game = game;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int sz = 2 * Tile.SIZE;
		for (int i = 0, x = sz; i < game.lives; ++i, x += sz) {
			g.setColor(Color.YELLOW);
			g.fillOval(x, world.height() * Tile.SIZE - sz, Tile.SIZE, Tile.SIZE);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}