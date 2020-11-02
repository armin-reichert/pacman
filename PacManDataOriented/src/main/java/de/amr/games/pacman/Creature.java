package de.amr.games.pacman;

public class Creature {

	public Creature(String name, V2i homeTile) {
		this.name = name;
		this.homeTile = homeTile;
	}

	public final String name;
	public final V2i homeTile;

	public boolean visible;
	public float speed;
	public Direction dir;
	public Direction wishDir;
	public V2i tile;
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

	public boolean at(V2i tile) {
		return this.tile.equals(tile);
	}
}