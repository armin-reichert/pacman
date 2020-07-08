package de.amr.games.pacman.model.world.api;

import de.amr.games.pacman.controller.actor.Creature;

/**
 * The Pac-man game world is a terrain and a habitat for the creatures.
 * 
 * @author Armin Reichert
 */
public interface World extends Terrain, Habitat {

	void setPopulation(Population population);
	
	void putIntoBed(Creature<?> creature);

	void setChangingLevel(boolean b);

	boolean isChangingLevel();

	void setFrozen(boolean b);

	boolean isFrozen();
}