package de.amr.games.pacman.model.world;

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