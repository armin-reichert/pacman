package de.amr.games.pacman.model.world.api;

import de.amr.easy.game.controller.Lifecycle;

public interface Lifeform extends Lifecycle {

	World world();

	default boolean isInsideWorld() {
		return world().contains(this);
	}
}