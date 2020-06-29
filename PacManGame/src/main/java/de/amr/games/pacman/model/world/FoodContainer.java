package de.amr.games.pacman.model.world;

import de.amr.games.pacman.controller.actor.Bonus;

/**
 * Food-related functions.
 * 
 * @author Armin Reichert
 */
public interface FoodContainer {

	int totalFoodCount();

	void removeFood();

	void createFood();

	boolean containsFood(Tile tile);

	boolean containsEatenFood(Tile tile);

	boolean containsSimplePellet(Tile tile);

	boolean containsEnergizer(Tile tile);

	void eatFood(Tile tile);

	void restoreFood(Tile tile);

	Bonus bonus();
}