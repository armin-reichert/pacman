package de.amr.games.pacman.controller.api;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.world.api.Lifeform;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.Theme;

public interface Creature extends Lifeform, View {

	Transform tf();

	boolean isVisible();

	void setVisible(boolean visible);

	void setTheme(Theme theme);

	IRenderer renderer();

	Tile location();

	void placeAt(Tile tile, float offsetX, float offsetY);

	default void placeAt(Tile tile) {
		placeAt(tile, 0, 0);
	}
}