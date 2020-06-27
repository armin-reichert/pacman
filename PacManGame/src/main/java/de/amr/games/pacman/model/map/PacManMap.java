package de.amr.games.pacman.model.map;

import de.amr.easy.game.model.ByteMap;

public abstract class PacManMap extends ByteMap implements PacManWorldStructure {

	public PacManMap(byte[][] data) {
		super(data);
	}
}