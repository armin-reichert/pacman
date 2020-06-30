package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.world.map.ArcadeWorldMap;
import de.amr.games.pacman.model.world.map.CustomArcadeWorldMap;

/**
 * The universe.
 * 
 * @author Armin Reichert
 */
public interface Universe {

	public static PacManWorld arcadeWorld() {
		return new PacManWorldUsingMap(new ArcadeWorldMap());
	}

	public static PacManWorld customWorld() {
		return new PacManWorldUsingMap(new CustomArcadeWorldMap());
	}
}