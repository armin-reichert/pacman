package de.amr.games.pacman;

import java.awt.Color;
import java.awt.Point;

public class Ghost extends Creature {

	public final Color color;
	public final Point scatterTile;
	public Point targetTile;
	public boolean frightened;
	public boolean enteringHouse;
	public boolean leavingHouse;
	public int bounty;
	public long bountyTimer;

	public Ghost(String name, Color color, Point homeTile, Point scatterTile) {
		super(name, homeTile);
		this.color = color;
		this.scatterTile = scatterTile;
	}

	@Override
	public String toString() {
		return String.format("%8s tile=%s offset=(%.2f,%.2f) target=%s", name, tile, offsetX, offsetY, targetTile);
	}
}