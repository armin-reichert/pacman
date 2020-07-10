package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.model.world.core.Tile;

/**
 * The Pac-man game world is a terrain and a habitat for the creatures.
 * 
 * @author Armin Reichert
 */
public interface World extends Terrain, FoodContainer {

	void setPopulation(Population population);

	Population population();

	Stream<Tile> habitatTiles();

	boolean included(Creature<?> creature);

	void include(Creature<?> creature);

	void exclude(Creature<?> creature);

	void putIntoBed(Creature<?> creature);

	void setChangingLevel(boolean b);

	boolean isChangingLevel();

	void setFrozen(boolean b);

	boolean isFrozen();
}