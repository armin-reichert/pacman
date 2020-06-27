package de.amr.games.pacman.model.world;

import java.util.List;

public class GhostHouse {

	private final List<Door> doors;
	private final List<Tile> room;
	private final List<Seat> seats;

	public GhostHouse(List<Tile> room, List<Door> doors, List<Seat> seats) {
		this.room = room;
		this.doors = doors;
		this.seats = seats;
	}

	public List<Door> doors() {
		return doors;
	}

	public List<Tile> room() {
		return room;
	}

	public List<Seat> seats() {
		return seats;
	}

	public Seat seat(int i) {
		return seats.get(i);
	}
}