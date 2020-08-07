package de.amr.games.pacman.view.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.world.api.World;

public interface IWorldRenderer {

	void render(Graphics2D g, World world);
}