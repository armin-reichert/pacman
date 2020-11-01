package de.amr.games.pacman;

import java.awt.Color;

public class Ghost extends Creature {

	public final Color color;
	public final V2 scatterTile;
	public V2 targetTile;
	public boolean frightened;
	public boolean enteringHouse;
	public boolean leavingHouse;
	public int bounty;
	public long bountyTimer;

	public Ghost(String name, Color color, V2 homeTile, V2 scatterTile) {
		super(name, homeTile);
		this.color = color;
		this.scatterTile = scatterTile;
	}

	@Override
	public String toString() {
		return String.format("%8s tile=%s offset=%s target=%s", name, tile, offset, targetTile);
	}
}