package de.amr.games.pacman.model.world.api;

import de.amr.easy.game.math.Vector2f;

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

	public Vector2f center() {
		return Vector2f.of((col() + 1) * Tile.SIZE, row() * Tile.SIZE + Tile.SIZE / 2);
	}
}