package de.amr.games.pacman.model.world.api;

/**
 * An area is a set of tiles.
 * 
 * @author Armin Reichert
 *
 */
public interface Area {

	boolean includes(Tile tile);
}