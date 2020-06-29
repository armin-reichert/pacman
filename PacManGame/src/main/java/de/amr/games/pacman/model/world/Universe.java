package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.world.map.ArcadeTerrain;
import de.amr.games.pacman.model.world.map.CustomTerrain;

/**
 * The universe.
 * 
 * @author Armin Reichert
 */
public interface Universe {

	public static PacManWorld arcadeWorld() {
		return new PacManWorldUsingMap(new ArcadeTerrain());
	}

	public static PacManWorld customWorld() {
		return new PacManWorldUsingMap(new CustomTerrain());
	}
}