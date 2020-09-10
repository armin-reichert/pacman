package de.amr.games.pacman.view.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;

public interface GhostRenderer {

	void render(Graphics2D g, Ghost ghost);
}
