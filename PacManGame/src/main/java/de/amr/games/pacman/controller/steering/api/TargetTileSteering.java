package de.amr.games.pacman.controller.steering.api;

import java.util.function.Supplier;

import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;

public interface TargetTileSteering<M extends MobileLifeform> extends Steering<M> {

	Tile targetTile();

	default void setTargetTile(Supplier<Tile> fnTargetTile) {
	}
}
