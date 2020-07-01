package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.core.Tile;

public class FoodFoundEvent implements PacManGameEvent {

	public final Tile tile;

	public FoodFoundEvent(Tile tile) {
		this.tile = tile;
	}

	@Override
	public String toString() {
		return String.format("FoodFound at %s", tile);
	}
}