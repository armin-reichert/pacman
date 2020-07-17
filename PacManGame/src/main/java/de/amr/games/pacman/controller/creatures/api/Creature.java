package de.amr.games.pacman.controller.creatures.api;

import de.amr.easy.game.entity.Entity;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.Theme;

/**
 * A mobile lifeform with a visual appearance.
 * 
 * @author Armin Reichert
 */
public interface Creature extends MobileLifeform {

	Entity entity();

	void setTheme(Theme theme);

	IRenderer renderer();

}