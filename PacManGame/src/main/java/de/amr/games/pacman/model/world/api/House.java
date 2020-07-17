package de.amr.games.pacman.model.world.api;

import java.util.List;
import java.util.stream.Stream;

/**
 * A house has a room with beds and some doors.
 * 
 * @author Armin Reichert
 */
public class House implements Area {

	private final Area room;
	private final List<Door> doors;
	private final List<Bed> beds;

	public House(Area room, List<Door> doors, List<Bed> beds) {
		this.room = room;
		this.doors = doors;
		this.beds = beds;
	}

	public Stream<Door> doors() {
		return doors.stream();
	}

	public Area room() {
		return room;
	}

	public Stream<Bed> seats() {
		return beds.stream();
	}

	public Bed bed(int i) {
		return beds.get(i);
	}

	@Override
	public boolean includes(Tile tile) {
		return room.includes(tile);
	}
}