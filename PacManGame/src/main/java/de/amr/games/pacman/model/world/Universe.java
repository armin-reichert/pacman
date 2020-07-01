package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.model.world.core.World;

/**
 * The universe.
 * 
 * @author Armin Reichert
 */
public interface Universe {

	public static World arcadeWorld() {
		return new ArcadeWorld();
	}
}