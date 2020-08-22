package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.api.Food;
import de.amr.games.pacman.model.world.components.Tile;

public class FoodFoundEvent implements PacManGameEvent {

	public final Tile location;
	public final Food food;

	public FoodFoundEvent(Tile location, Food food) {
		this.location = location;
		this.food = food;
	}

	@Override
	public String toString() {
		return String.format("FoodFoundEvent(%s,%s)", location, food);
	}
}