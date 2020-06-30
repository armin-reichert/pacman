package de.amr.games.pacman.model.world;

import java.util.Optional;

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

	void removeFood(Tile tile);

	void createFood(Tile tile);

	void setBonus(Bonus bonus);

	Optional<Bonus> getBonus();
}