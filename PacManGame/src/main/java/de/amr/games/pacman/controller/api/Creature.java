package de.amr.games.pacman.controller.api;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.world.api.Lifeform;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.Theme;

public interface Creature extends Lifeform, View {

	String name();

	Transform tf();

	Tile location();

	void placeAt(Tile tile, float offsetX, float offsetY);

	default void placeAt(Tile tile) {
		placeAt(tile, 0, 0);
	}

	/**
	 * Euclidean distance (in tiles) between this and the other entity.
	 * 
	 * @param other other entity
	 * @return Euclidean distance measured in tiles
	 */
	default double distance(Creature other) {
		return location().distance(other.location());
	}

	void setVisible(boolean visible);

	boolean isVisible();

	IRenderer renderer();

	void setTheme(Theme theme);
}