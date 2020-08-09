package de.amr.games.pacman.controller.steering.api;

import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;

public interface TargetTileSteering<M extends MobileLifeform> extends Steering<M> {

	Tile targetTile();
}
