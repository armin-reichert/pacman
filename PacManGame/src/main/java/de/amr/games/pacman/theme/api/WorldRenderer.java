package de.amr.games.pacman.theme.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.world.api.World;

public interface WorldRenderer {

	void render(Graphics2D g, World world);
}