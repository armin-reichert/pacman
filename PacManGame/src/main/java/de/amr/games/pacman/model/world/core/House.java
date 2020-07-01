package de.amr.games.pacman.model.world.core;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A house has a room with beds and some doors.
 * 
 * @author Armin Reichert
 */
public class House {

	private final Set<Tile> room;
	private final List<Door> doors;
	private final List<Bed> beds;

	public House(Set<Tile> room, List<Door> doors, List<Bed> beds) {
		this.room = room;
		this.doors = doors;
		this.beds = beds;
	}

	public Stream<Door> doors() {
		return doors.stream();
	}

	public Set<Tile> room() {
		return Collections.unmodifiableSet(room);
	}

	public Stream<Bed> seats() {
		return beds.stream();
	}

	public Bed bed(int i) {
		return beds.get(i);
	}
}