package de.amr.games.pacman.model.map;

import de.amr.easy.game.model.ByteMap;
import de.amr.games.pacman.model.world.PacManWorldStructure;
import de.amr.games.pacman.model.world.Tile;

public abstract class PacManMap extends ByteMap implements PacManWorldStructure {

	//@formatter:off
	public static final byte B_WALL         = 0;
	public static final byte B_FOOD         = 1;
	public static final byte B_ENERGIZER    = 2;
	public static final byte B_EATEN        = 3;
	public static final byte B_INTERSECTION = 4;
	public static final byte B_TUNNEL				= 5;
	public static final byte B_6            = 6;
	//@formatter:on

	public PacManMap(byte[][] data) {
		super(data);
	}

	@Override
	public boolean insideMap(Tile tile) {
		return contains(tile.row, tile.col);
	}

	@Override
	public int width() {
		return numCols;
	}

	@Override
	public int height() {
		return numRows;
	}
}