package de.amr.games.pacman;

import static de.amr.games.pacman.PacManGame.TS;
import static de.amr.games.pacman.PacManGame.vec;

import java.awt.Color;

public class Creature {

	public Creature(String name, Color color) {
		this.name = name;
		this.color = color;
		size = vec(TS, TS);
	}

	@Override
	public String toString() {
		return String.format("%s tile %s offset %s", name, tile, offset);
	}

	public final String name;
	public final Color color;
	public float speed;
	public V2 size;
	public V2 tile;
	public V2 offset;
	public V2 dir;
	public V2 intendedDir;
	public V2 homeTile;
	public V2 targetTile;
	public V2 scatterTile;
	public boolean tileChanged;
	public boolean stuck;
	public boolean forceTurnBack;
}