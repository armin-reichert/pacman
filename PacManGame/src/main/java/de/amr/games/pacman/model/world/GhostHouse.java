package de.amr.games.pacman.model.world;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class GhostHouse {

	private final List<Door> doors;
	private final Set<Tile> room;
	private final List<Seat> seats;

	public GhostHouse(Set<Tile> room, List<Door> doors, List<Seat> seats) {
		this.room = room;
		this.doors = doors;
		this.seats = seats;
	}

	public Stream<Door> doors() {
		return doors.stream();
	}

	public Set<Tile> room() {
		return room;
	}

	public Stream<Seat> seats() {
		return seats.stream();
	}

	public Seat seat(int i) {
		return seats.get(i);
	}
}