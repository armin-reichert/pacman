package de.amr.games.pacman.theme.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.game.PacManGame;

@FunctionalInterface
public interface GameRenderer {

	void render(Graphics2D g, PacManGame game);
}
