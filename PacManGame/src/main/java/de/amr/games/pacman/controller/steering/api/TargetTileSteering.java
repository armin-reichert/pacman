package de.amr.games.pacman.controller.steering.api;

import java.util.function.Supplier;

import de.amr.games.pacman.model.world.api.Tile;

public interface TargetTileSteering extends Steering {

	Tile targetTile();

	default void setTargetTile(Supplier<Tile> fnTargetTile) {
	}
}
