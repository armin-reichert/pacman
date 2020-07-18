package de.amr.games.pacman.model.world.api;

import de.amr.easy.game.entity.Transform;

public class Portal {

	public final Tile left, right;

	public Portal(Tile left, Tile right) {
		this.left = left;
		this.right = right;
	}
	
	public Tile leftEntry() {
		return Tile.at(left.col + 1, left.row);
	}
	
	public Tile rightEntry() {
		return Tile.at(right.col - 1, right.row);
	}

	public boolean includes(Tile tile) {
		return tile.equals(left) || tile.equals(right);
	}

	public void teleport(Transform tf, Tile entryTile, Direction moveDir) {
		Tile exitTile = exitTile(entryTile, moveDir);
		if (exitTile != null) {
			if (moveDir == Direction.RIGHT) {
				tf.setPosition(exitTile.x() + Tile.SIZE, exitTile.y());
			}
			if (moveDir == Direction.LEFT) {
				tf.setPosition(exitTile.x() - Tile.SIZE, exitTile.y());
			}
		}
	}

	public Tile exitTile(Tile entryTile, Direction moveDir) {
		if (entryTile.equals(left) && moveDir == Direction.LEFT) {
			return right;
		}
		if (entryTile.equals(right) && moveDir == Direction.RIGHT) {
			return left;
		}
		return null;
	}
}