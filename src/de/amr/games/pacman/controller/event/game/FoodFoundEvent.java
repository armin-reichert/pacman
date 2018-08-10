package de.amr.games.pacman.controller.event.game;

import static de.amr.games.pacman.model.Content.PELLET;

import de.amr.games.pacman.model.Tile;

public class FoodFoundEvent extends GameEvent {

	public final Tile tile;
	public final char food;

	public FoodFoundEvent(Tile tile, char food) {
		this.tile = tile;
		this.food = food;
	}

	@Override
	public String toString() {
		return String.format("FoodFound(%s)", food == PELLET ? "Pellet" : "Energizer");
	}
}