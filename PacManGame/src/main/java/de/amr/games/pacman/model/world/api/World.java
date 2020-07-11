package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.api.Creature;
import de.amr.games.pacman.controller.api.MobileCreature;
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

	boolean contains(Creature creature);

	void bringIn(Creature creature);

	void takeOut(Creature creature);

	void putIntoBed(MobileCreature creature);

	void setChangingLevel(boolean b);

	boolean isChangingLevel();

	void setFrozen(boolean b);

	boolean isFrozen();
}