package de.amr.games.pacman.controller.steering.api;

import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.MovingEntity;

/**
 * @author Armin Reichert
 */
public abstract class SteeredMover extends MovingEntity {

	public SteeredMover(World world) {
		super(world);
	}

	public abstract Steering steering();

}