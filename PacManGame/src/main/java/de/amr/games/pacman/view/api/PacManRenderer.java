package de.amr.games.pacman.view.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.pacman.PacMan;

public interface PacManRenderer {

	void render(Graphics2D g, PacMan pacMan);

	default void resetAnimations(PacMan pacMan) {
	}
}