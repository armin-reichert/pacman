package de.amr.games.pacman.model.world.api;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides food-related functionality.
 * 
 * @author Armin Reichert
 */
public interface FoodSource {

	Stream<Food> food();

	Optional<Food> foodAt(Tile location);

	int totalFoodCount();

	void clearFood();

	void fillFood();

	void clearFood(Tile location);

	void fillFood(Tile location);

	void setFood(Food food, Tile location);

	default boolean hasFood(Tile location) {
		return foodAt(location).isPresent();
	}

	default boolean hasFood(Food food, Tile location) {
		return foodAt(location).filter(f -> f.equals(food)).isPresent();
	}

	boolean hasEatenFood(Tile location);

	Optional<BonusFood> bonusFood();

	void addBonusFood(BonusFood bonusFood);

	void clearBonusFood();
}