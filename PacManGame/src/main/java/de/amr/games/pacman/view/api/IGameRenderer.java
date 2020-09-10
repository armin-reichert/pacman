package de.amr.games.pacman.view.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.game.PacManGame;

@FunctionalInterface
public interface IGameRenderer {

	void render(Graphics2D g, PacManGame game);
}
