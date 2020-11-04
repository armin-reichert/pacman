package de.amr.games.pacman;

public class Creature {

	public final String name;
	public final V2i homeTile;

	public boolean visible;
	public float speed;
	public Direction dir;
	public Direction wishDir;
	public V2i tile;
	public V2f offset;
	public boolean changedTile;
	public boolean stuck;
	public boolean forcedOnTrack;
	public boolean forcedTurningBack;
	public boolean dead;

	public Creature(String name, V2i homeTile) {
		this.name = name;
		this.homeTile = homeTile;
	}

	public boolean at(V2i tile) {
		return this.tile.equals(tile);
	}

	@Override
	public String toString() {
		return String.format("%-8s tile=%s offset=%s", name, tile, offset);
	}
}