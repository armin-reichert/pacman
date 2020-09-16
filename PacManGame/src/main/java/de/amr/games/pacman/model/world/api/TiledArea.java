package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

/**
 * An area of tiles.
 * 
 * @author Armin Reichert
 */
public interface TiledArea {

	Stream<Tile> tiles();

	boolean includes(Tile tile);
}