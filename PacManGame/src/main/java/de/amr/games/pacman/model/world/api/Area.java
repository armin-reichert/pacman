package de.amr.games.pacman.model.world.api;

import de.amr.games.pacman.model.world.core.Tile;

public interface Area {

	boolean includes(Tile tile);
}
