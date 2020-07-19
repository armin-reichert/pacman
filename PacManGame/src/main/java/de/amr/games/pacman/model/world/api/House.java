package de.amr.games.pacman.model.world.api;

import java.util.List;
import java.util.stream.Stream;

/**
 * A house has a room with beds and some doors.
 * 
 * @author Armin Reichert
 */
public class House implements Area {

	private final Area layout;
	private final List<Door> doors;
	private final List<Bed> beds;

	public House(Area layout, List<Door> doors, List<Bed> beds) {
		this.layout = layout;
		this.doors = doors;
		this.beds = beds;
	}

	public Stream<Door> doors() {
		return doors.stream();
	}

	public Door door(int i) {
		return doors.get(i);
	}

	public Area layout() {
		return layout;
	}

	public Stream<Bed> beds() {
		return beds.stream();
	}

	public Bed bed(int i) {
		return beds.get(i);
	}

	@Override
	public boolean includes(Tile tile) {
		return layout.includes(tile);
	}

	@Override
	public Stream<Tile> tiles() {
		return layout.tiles();
	}
}