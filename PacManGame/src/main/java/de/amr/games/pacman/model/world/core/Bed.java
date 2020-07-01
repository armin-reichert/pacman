package de.amr.games.pacman.model.world.core;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Direction;

/**
 * A bed is a maze position defining an initial move direction for the creature residing at that
 * position.
 * 
 * @author Armin Reichert
 */
public class Bed {

	public final int number;
	public final Tile tile;
	public final Vector2f position;
	public final Direction startDir;

	public Bed(int number, int col, int row, Direction dir) {
		this.number = number;
		tile = Tile.at(col, row);
		position = Vector2f.of(tile.centerX(), tile.y());
		startDir = dir;
	}
}