package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.components.Tile;

public class FoodFoundEvent implements PacManGameEvent {

	public final Tile location;

	public FoodFoundEvent(Tile tile) {
		this.location = tile;
	}

	@Override
	public String toString() {
		return String.format("FoodFound at %s", location);
	}
}