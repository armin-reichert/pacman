package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.model.world.components.Tile;

/**
 * An area of tiles.
 * 
 * @author Armin Reichert
 */
public interface Area {

	Stream<Tile> tiles();

	boolean includes(Tile tile);
}