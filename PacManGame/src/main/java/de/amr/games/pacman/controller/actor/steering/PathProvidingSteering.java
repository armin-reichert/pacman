package de.amr.games.pacman.controller.actor.steering;

import java.util.List;

import de.amr.games.pacman.model.Tile;

/**
 * Implemented by steerings that can compute the complete path to the target tile.
 * 
 * @author Armin Reichert
 */
public interface PathProvidingSteering extends Steering {

	List<Tile> pathToTarget();

	void setPathComputationEnabled(boolean enabled);

	boolean isPathComputationEnabled();
}