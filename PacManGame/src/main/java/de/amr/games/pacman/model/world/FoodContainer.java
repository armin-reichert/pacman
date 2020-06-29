package de.amr.games.pacman.model.world;

import de.amr.games.pacman.controller.actor.Bonus;

public interface FoodContainer {

	int totalFoodCount();

	void eatFood();

	void restoreFood();

	boolean containsFood(Tile tile);

	boolean containsEatenFood(Tile tile);

	boolean isEnergizer(Tile tile);

	boolean containsSimplePellet(Tile tile);

	boolean containsEnergizer(Tile tile);

	void eatFood(Tile tile);

	void restoreFood(Tile tile);

	Bonus bonus();
}