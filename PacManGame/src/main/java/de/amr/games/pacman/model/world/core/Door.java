package de.amr.games.pacman.model.world.core;

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.model.Direction;

/**
 * A door into a house.
 * 
 * @author Armin Reichert
 */
public class Door {

	public enum DoorState {
		OPEN, CLOSED
	}

	public final Direction intoHouse;
	public final List<Tile> tiles;
	public DoorState state;

	public Door(Direction intoHouse, Tile... tiles) {
		this.tiles = Arrays.asList(tiles);
		this.intoHouse = intoHouse;
		state = DoorState.CLOSED;
	}

	public boolean includes(Tile tile) {
		return tiles.contains(tile);
	}
}