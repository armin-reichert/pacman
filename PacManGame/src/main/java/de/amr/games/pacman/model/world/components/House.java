package de.amr.games.pacman.model.world.components;

import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.Area;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Territory;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * A house has a room with beds and some doors.
 * 
 * @author Armin Reichert
 */
public class House implements Area {

	private final Territory territory;
	private final Area layout;
	private final List<Door> doors;
	private final List<Bed> beds;

	public static HouseBuilder world(Territory territory) {
		return new HouseBuilder(territory);
	}

	public House(Territory territory, Area layout, List<Door> doors, List<Bed> beds) {
		this.territory = territory;
		this.layout = layout;
		this.doors = doors;
		this.beds = beds;
	}

	public Stream<Door> doors() {
		return doors.stream();
	}

	public boolean hasDoorAt(Tile location) {
		return doors().anyMatch(door -> door.includes(location));
	}

	public Door door(int i) {
		return doors.get(i);
	}

	public Stream<Bed> beds() {
		return beds.stream();
	}

	public Bed bed(int i) {
		return beds.get(i);
	}

	public boolean isInsideOrDoor(Tile tile) {
		return layout.includes(tile) && territory.isAccessible(tile);
	}

	public boolean isEntry(Tile tile) {
		for (Direction dir : Direction.values()) {
			Tile neighbor = tile.towards(dir);
			if (hasDoorAt(neighbor)) {
				Door door = doors().filter(d -> d.includes(neighbor)).findFirst().get();
				return dir == door.intoHouse;
			}
		}
		return false;
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