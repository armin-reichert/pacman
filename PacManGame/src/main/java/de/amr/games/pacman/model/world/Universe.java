package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;

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