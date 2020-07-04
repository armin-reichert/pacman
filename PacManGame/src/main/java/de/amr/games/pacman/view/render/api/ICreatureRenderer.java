package de.amr.games.pacman.view.render.api;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;

public interface ICreatureRenderer extends IRenderer {

	void drawCreature(Graphics2D g, Entity creature);

}