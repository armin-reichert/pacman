package de.amr.games.pacman;

import java.awt.Color;

public class Creature {

	public Creature(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("%s tile %s offset %s", name, tile, offset);
	}

	public String name;
	public V2 size;
	public V2 tile;
	public V2 offset;
	public V2 direction;
	public V2 intendedDirection;
	float speed;
	Color color;
}