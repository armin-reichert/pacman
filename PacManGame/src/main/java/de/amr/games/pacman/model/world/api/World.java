package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.model.world.core.Tile;

/**
 * The Pac-man game world is a terrain and a habitat for the creatures.
 * 
 * @author Armin Reichert
 */
public interface World extends Territory, FoodContainer {

	void setPopulation(Population population);

	Population population();

	Stream<Tile> habitatTiles();

	boolean contains(Lifeform creature);

	void bringIn(Lifeform creature);

	void takeOut(Lifeform creature);

	void putIntoBed(Lifeform creature);

	void setChangingLevel(boolean b);

	boolean isChangingLevel();

	void setFrozen(boolean b);

	boolean isFrozen();
}