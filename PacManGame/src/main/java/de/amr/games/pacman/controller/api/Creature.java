package de.amr.games.pacman.controller.api;

import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.world.api.Lifeform;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.Theme;

/**
 * A lifeform with a visual appearance.
 * 
 * @author Armin Reichert
 */
public interface Creature extends Lifeform, View {

	void setTheme(Theme theme);

	IRenderer renderer();

}