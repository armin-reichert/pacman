package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.world.map.ArcadeMap;
import de.amr.games.pacman.model.world.map.CustomMap;

/**
 * The universe.
 * 
 * @author Armin Reichert
 */
public interface Universe {

	public static PacManWorld arcadeWorld() {
		return new PacManWorldImpl(new ArcadeMap());
	}

	public static PacManWorld customWorld() {
		return new PacManWorldImpl(new CustomMap());
	}
}