package de.amr.games.pacman.model.world.components;

import de.amr.games.pacman.model.world.api.Direction;

/**
 * A bed is a 2x1-area in the world with a dedicated exit direction.
 * 
 * @author Armin Reichert
 */
public class Bed extends Block {

	public final Direction exitDir;

	public Bed(int col, int row, Direction exitDir) {
		super(col, row, 2, 1);
		this.exitDir = exitDir;
	}
}