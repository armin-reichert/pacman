package de.amr.games.pacman.view.theme.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.pacman.PacMan;

public interface IPacManRenderer extends IRenderer {

	void render(Graphics2D g, PacMan pacMan);
}