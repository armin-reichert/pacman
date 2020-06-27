package de.amr.games.pacman.model.map;

public abstract class PacManMap extends GameMap implements PacManWorldStructure {

	public PacManMap(byte[][] data) {
		super(data);
	}
}