package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.Tile;

public class FoodFoundEvent extends PacManGameEvent {

	public final Tile tile;
	public final boolean energizer;

	public FoodFoundEvent(Tile tile, boolean energizer) {
		this.tile = tile;
		this.energizer = energizer;
	}

	@Override
	public String toString() {
		return String.format("FoodFound(%s)", energizer ? "Energizer" : "Pellet");
	}
}