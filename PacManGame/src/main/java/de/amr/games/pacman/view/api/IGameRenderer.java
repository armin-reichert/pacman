package de.amr.games.pacman.view.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.game.Game;

public interface IGameRenderer extends IRenderer {

	void render(Graphics2D g, Game game);
}
