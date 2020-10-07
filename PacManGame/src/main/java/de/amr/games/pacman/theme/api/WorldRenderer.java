package de.amr.games.pacman.theme.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.world.api.TiledWorld;

public interface WorldRenderer {

	void render(Graphics2D g, TiledWorld world);
}