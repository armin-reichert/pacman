package de.amr.games.pacman.controller.steering.api;

import java.util.List;

import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * Implemented by steerings that can compute the complete path to the target tile.
 * 
 * @author Armin Reichert
 */
public interface PathProvidingSteering<M extends MobileLifeform> extends Steering<M> {

	/**
	 * @return the path from the current position of the mover to its current target tile
	 */
	List<Tile> pathToTarget(MobileLifeform mover);

	/**
	 * @param enabled if {@code true} the steering computes the path to the target
	 */
	void setPathComputationEnabled(boolean enabled);

	/**
	 * @return tells if the steering computes the complete path to the target
	 */
	boolean isPathComputationEnabled();
}