package de.amr.games.pacman.model.world.api;

import de.amr.games.pacman.model.world.core.Tile;

/**
 * An area is a set of tiles.
 * 
 * @author Armin Reichert
 *
 */
public interface Area {

	boolean includes(Tile tile);
}