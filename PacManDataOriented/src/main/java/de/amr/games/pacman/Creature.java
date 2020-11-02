package de.amr.games.pacman;

import java.awt.Point;

public class Creature {

	public Creature(String name, Point homeTile) {
		this.name = name;
		this.homeTile = homeTile;
	}

	public final String name;
	public final Point homeTile;

	public boolean visible;
	public float speed;
	public Direction dir;
	public Direction wishDir;
	public Point tile;
	public float offsetX;
	public float offsetY;
	public boolean tileChanged;
	public boolean stuck;
	public boolean forcedOnTrack;
	public boolean forcedTurningBack;
	public boolean dead;

	@Override
	public String toString() {
		return String.format("%8s tile=%s offset=(%.2f,%.2f)", name, tile, offsetX, offsetY);
	}
}