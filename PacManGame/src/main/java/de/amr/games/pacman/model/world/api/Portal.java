package de.amr.games.pacman.model.world.api;

import de.amr.easy.game.entity.Transform;

public class Portal {

	/** left resp. top tile */
	public final Tile either;

	/** right resp. bottom tile */
	public final Tile other;

	public final boolean vertical;

	public Portal(Tile either, Tile other, boolean vertical) {
		this.either = either;
		this.other = other;
		this.vertical = vertical;
	}

	/** left resp. top entry tile */
	public Tile eitherEntry() {
		return vertical ? Tile.at(either.col, either.row + 1) : Tile.at(either.col + 1, either.row);
	}

	/** right resp. bottom entry tile */
	public Tile otherEntry() {
		return vertical ? Tile.at(other.col, other.row - 1) : Tile.at(other.col - 1, other.row);
	}

	public boolean includes(Tile tile) {
		return tile.equals(either) || tile.equals(other);
	}

	public void teleport(Transform tf, Tile entered, Direction moveDir) {
		if (vertical) {
			Tile leave = verticalExit(moveDir);
			int offsetY = moveDir == Direction.DOWN ? 0 : 0;
			tf.setPosition(leave.x(), leave.y() + offsetY);
		} else {
			Tile leave = horizontalExit(moveDir);
			int offsetX = moveDir == Direction.RIGHT ? 4 : -4;
			tf.setPosition(leave.x() + offsetX, leave.y());
		}
	}

	private Tile horizontalExit(Direction moveDir) {
		if (moveDir == Direction.RIGHT) {
			return either;
		}
		if (moveDir == Direction.LEFT) {
			return other;
		}
		throw new IllegalArgumentException();
	}

	private Tile verticalExit(Direction moveDir) {
		if (moveDir == Direction.DOWN) {
			return either;
		}
		if (moveDir == Direction.UP) {
			return other;
		}
		throw new IllegalArgumentException();
	}
}