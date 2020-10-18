package de.amr.games.pacman;

import static de.amr.games.pacman.World.TS;

import java.awt.Color;

public class Creature {

	public Creature(String name, Color color, V2 homeTile) {
		this.name = name;
		this.color = color;
		this.homeTile = homeTile;
		this.size = new V2(TS, TS);
	}

	@Override
	public String toString() {
		return String.format("%s tile=%s offset=%s", name, tile, offset);
	}

	public final String name;
	public final Color color;
	public final V2 size;
	public final V2 homeTile;
	public float speed;
	public Direction dir;
	public Direction wishDir;
	public V2 tile;
	public V2 offset;
	public V2 targetTile;
	public boolean tileChanged;
	public boolean stuck;
	public boolean forceTurnBack;
	public boolean dead;
	public int bounty;
	public long bountyTimer;
	public boolean vulnerable;
	public boolean visible;
}