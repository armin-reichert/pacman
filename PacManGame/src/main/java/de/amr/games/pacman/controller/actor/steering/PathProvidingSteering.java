package de.amr.games.pacman.controller.actor.steering;

import java.util.List;

import de.amr.games.pacman.model.world.core.Tile;

/**
 * Implemented by steerings that can compute the complete path to the target tile.
 * 
 * @author Armin Reichert
 */
public interface PathProvidingSteering extends Steering {

	/**
	 * @return the path from the current position of the actor to its current target tile
	 */
	List<Tile> pathToTarget();

	/**
	 * @param enabled if {@code true} the steering computes the complete path to the target
	 */
	void setPathComputed(boolean enabled);

	/**
	 * @return tells if the steering computes the complete path to the target
	 */
	boolean isPathComputed();
}