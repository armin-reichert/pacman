package de.amr.games.pacman.model.world.core;

/**
 * The Pac-man game world is a terrain and a habitat for the creatures.
 * 
 * @author Armin Reichert
 */
public interface World extends Terrain, Habitat {

	void accept(Population aliens);
}