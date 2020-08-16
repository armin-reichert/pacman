package de.amr.games.pacman.controller.steering.api;

import java.util.List;

import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.core.MovingGuy;

/**
 * Implemented by steerings that can compute the complete path to the target tile.
 * 
 * @author Armin Reichert
 */
public interface PathProvidingSteering extends Steering {

	/**
	 * @return the path from the current position of the mover to its current target tile
	 */
	List<Tile> pathToTarget(MovingGuy mover);

	/**
	 * @param enabled if {@code true} the steering computes the path to the target
	 */
	void setPathComputationEnabled(boolean enabled);

	/**
	 * @return tells if the steering computes the complete path to the target
	 */
	boolean isPathComputationEnabled();
}