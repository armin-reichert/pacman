package de.amr.games.pacman.model.world.components;

import de.amr.games.pacman.model.world.api.Direction;

/**
 * A door into a house.
 * 
 * @author Armin Reichert
 */
public class Door extends TiledRectangle {

	public enum DoorState {
		OPEN, CLOSED
	}

	public final Direction intoHouse;
	public DoorState state;

	public Door(Direction intoHouse, int col, int row, int width, int height) {
		super(col, row, width, height);
		this.intoHouse = intoHouse;
		state = DoorState.CLOSED;
	}
}