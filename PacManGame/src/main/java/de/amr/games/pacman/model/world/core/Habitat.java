package de.amr.games.pacman.model.world.core;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Creature;

/**
 * Where the creatures live.
 * 
 * @author Armin Reichert
 */
public interface Habitat extends FoodContainer {

	Stream<Tile> habitatTiles();

	Population population();

	boolean isOnStage(Creature<?> creature);

	void putOnStage(Creature<?> creature, boolean onStage);
}