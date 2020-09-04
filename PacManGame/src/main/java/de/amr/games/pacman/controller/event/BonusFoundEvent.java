package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.api.Food;
import de.amr.games.pacman.model.world.components.Tile;

public class BonusFoundEvent extends FoodFoundEvent {

	public BonusFoundEvent(Tile tile, Food food) {
		super(tile, food);
	}

	@Override
	public String toString() {
		return String.format("BonusFoundEvent(%s,%s)", location, food);
	}
}