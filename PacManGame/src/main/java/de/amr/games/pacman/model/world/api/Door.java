package de.amr.games.pacman.model.world.api;

/**
 * A door into a house.
 * 
 * @author Armin Reichert
 */
public class Door extends Block {

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