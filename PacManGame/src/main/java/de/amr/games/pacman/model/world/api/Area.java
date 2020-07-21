package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

/**
 * An area is composed of tiles.
 * 
 * @author Armin Reichert
 */
public interface Area {

	Stream<Tile> tiles();

	boolean includes(Tile tile);
}