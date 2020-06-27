package de.amr.games.pacman.model.world;

import de.amr.easy.game.entity.Entity;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;

public class Portal {

	public Portal(Tile left, Tile right) {
		this.left = left;
		this.right = right;
	}

	public final Tile left, right;

	public boolean contains(Tile tile) {
		return tile.equals(left) || tile.equals(right);
	}

	public void teleport(Entity entity, Tile entryTile, Direction moveDir) {
		Tile exitTile = exitTile(entryTile, moveDir);
		if (exitTile != null) {
			if (moveDir == Direction.RIGHT) {
				entity.tf.setPosition(exitTile.x() + Tile.SIZE, exitTile.y());
			}
			if (moveDir == Direction.LEFT) {
				entity.tf.setPosition(exitTile.x() - Tile.SIZE, exitTile.y());
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