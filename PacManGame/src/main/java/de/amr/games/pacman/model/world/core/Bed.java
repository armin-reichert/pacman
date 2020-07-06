package de.amr.games.pacman.model.world.core;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.world.Direction;

/**
 * A bed is a place defining an exit direction.
 * 
 * @author Armin Reichert
 */
public class Bed extends Block {

	public final int number;
	public final Tile tile;
	public final Vector2f center;
	public final Direction exitDir;

	public Bed(int number, int x, int y, Direction dir) {
		super(x, y, 2, 1);
		this.number = number;
		tile = Tile.at(x, y);
		center = Vector2f.of(tile.x() + Tile.SIZE, tile.centerY());
		exitDir = dir;
	}
}