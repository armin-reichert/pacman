package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.Tile;

public class FoodFoundEvent extends PacManGameEvent {

	public final Tile tile;

	public FoodFoundEvent(Tile tile) {
		this.tile = tile;
	}

	@Override
	public String toString() {
		return String.format("FoodFound(%s)", tile);
	}
}