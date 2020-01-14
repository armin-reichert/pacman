package de.amr.games.pacman.actor.core;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities that reside in a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeResiding {

	/**
	 * @return the maze where this entity is residing
	 */
	Maze maze();

	/**
	 * @return the tile where this entity is located
	 */
	Tile tile();

	/**
	 * @param other other actor
	 * @return if both actors occupy the same tile
	 */
	default boolean onSameTileAs(MazeResiding other) {
		return tile().equals(other.tile());
	}
}