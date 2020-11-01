package de.amr.games.pacman;

public class Creature {

	public Creature(String name, int homeTileX, int homeTileY) {
		this.name = name;
		this.homeTileX = homeTileX;
		this.homeTileY = homeTileY;
	}

	public final String name;
	public final int homeTileX;
	public final int homeTileY;

	public boolean visible;
	public float speed;
	public Direction dir;
	public Direction wishDir;
	public int tileX;
	public int tileY;
	public V2 offset;
	public boolean tileChanged;
	public boolean stuck;
	public boolean forcedOnTrack;
	public boolean forcedTurningBack;
	public boolean dead;

	@Override
	public String toString() {
		return String.format("%8s tile=(%d,%d) offset=%s", name, tileX, tileY, offset);
	}
}