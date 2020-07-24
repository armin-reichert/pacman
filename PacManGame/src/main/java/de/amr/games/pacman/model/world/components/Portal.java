package de.amr.games.pacman.model.world.components;

import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;

public class Portal {

	/** left resp. top tile */
	public final Tile either;

	/** right resp. bottom tile */
	public final Tile other;

	public final boolean vertical;

	public Direction passThroughDirection;

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

	public Tile exit() {
		if (passThroughDirection == Direction.RIGHT || passThroughDirection == Direction.DOWN) {
			return either;
		}
		if (passThroughDirection == Direction.LEFT || passThroughDirection == Direction.UP) {
			return other;
		}
		throw new IllegalArgumentException("Illegal direction for passing through portal: " + passThroughDirection);
	}
}