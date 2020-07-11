package de.amr.games.pacman.controller.api;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Transform;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.api.IRenderer;
import de.amr.games.pacman.view.api.Theme;

public interface Creature extends Lifecycle {

	String name();

	Transform tf();

	void setWorld(World world);

	World world();

	void takePartIn(Game game);

	void setTheme(Theme theme);

	void setVisible(boolean visible);

	boolean isVisible();

	default boolean isInsideWorld() {
		return world().contains(this);
	}

	void placeAt(Tile tile, float offsetX, float offsetY);

	default void placeAt(Tile tile) {
		placeAt(tile, 0, 0);
	}

	IRenderer renderer();
}