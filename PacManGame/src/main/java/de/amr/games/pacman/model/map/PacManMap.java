package de.amr.games.pacman.model.map;

import de.amr.easy.game.model.ByteMap;
import de.amr.games.pacman.model.world.PacManWorldStructure;

public abstract class PacManMap extends ByteMap implements PacManWorldStructure {

	//@formatter:off
	public static final byte B_WALL         = 0;
	public static final byte B_FOOD         = 1;
	public static final byte B_ENERGIZER    = 2;
	public static final byte B_EATEN        = 3;
	public static final byte B_INTERSECTION = 4;
	public static final byte B_5 						= 5;
	public static final byte B_TUNNEL       = 6;
	//@formatter:on

	public PacManMap(byte[][] data) {
		super(data);
	}
}