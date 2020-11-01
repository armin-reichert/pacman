package de.amr.games.pacman;

import java.awt.Color;

public class Ghost extends Creature {

	public final Color color;
	public final int scatterTileX;
	public final int scatterTileY;
	public int targetTileX;
	public int targetTileY;
	public boolean frightened;
	public boolean enteringHouse;
	public boolean leavingHouse;
	public int bounty;
	public long bountyTimer;

	public Ghost(String name, Color color, int homeTileX, int homeTileY, int scatterTileX, int scatterTileY) {
		super(name, homeTileX, homeTileY);
		this.color = color;
		this.scatterTileX = scatterTileX;
		this.scatterTileY = scatterTileY;
	}

	@Override
	public String toString() {
		return String.format("%8s tile=(%d,%d) offset=(%.2f,%.2f) target=(%d,%d)", name, tileX, tileY, offsetX, offsetY,
				targetTileX, targetTileY);
	}
}