package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.world.Tile;

public class FoodFoundEvent extends PacManGameEvent {

	public final boolean energizer;
	public final Tile tile;

	public FoodFoundEvent(Tile tile, boolean energizer) {
		this.tile = tile;
		this.energizer = energizer;
	}

	@Override
	public String toString() {
		return String.format(energizer ? "FoodFound(energizer at %s)" : "FoodFound(pellet at %s)", tile);
	}
}