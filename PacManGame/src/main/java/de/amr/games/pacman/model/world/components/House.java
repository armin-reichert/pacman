package de.amr.games.pacman.model.world.components;

import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.TiledArea;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

/**
 * A house has a room with beds and some doors.
 * 
 * @author Armin Reichert
 */
public class House implements TiledArea {

	private final World world;
	private final TiledArea layout;
	private final List<Door> doors;
	private final List<Bed> beds;

	public static HouseBuilder world(World world) {
		return new HouseBuilder(world);
	}

	public House(World world, TiledArea layout, List<Door> doors, List<Bed> beds) {
		this.world = world;
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
		return layout.includes(tile) && world.isAccessible(tile);
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