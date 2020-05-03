package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.tiles.Pellet;

public class FoodFoundEvent extends PacManGameEvent {

	public final Pellet pellet;

	public FoodFoundEvent(Pellet pellet) {
		this.pellet = pellet;
	}

	@Override
	public String toString() {
		return String.format("FoodFound(%s)", pellet.energizer ? "Energizer" : "Pellet");
	}
}