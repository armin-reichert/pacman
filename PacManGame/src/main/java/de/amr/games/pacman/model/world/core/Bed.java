package de.amr.games.pacman.model.world.core;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.world.api.Direction;

/**
 * A bed is a place defining an exit direction.
 * 
 * @author Armin Reichert
 */
public class Bed extends Block {

	public final int number;
	public final Vector2f center;
	public final Direction exitDir;

	public Bed(int number, int col, int row, Direction dir) {
		super(col, row, 2, 1);
		this.number = number;
		center = Vector2f.of((col + 1) * Tile.SIZE, row * Tile.SIZE + Tile.SIZE / 2);
		exitDir = dir;
	}
}