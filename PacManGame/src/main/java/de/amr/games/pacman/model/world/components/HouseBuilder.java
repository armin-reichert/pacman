package de.amr.games.pacman.model.world.components;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.model.world.api.TiledArea;
import de.amr.games.pacman.model.world.api.World;

public class HouseBuilder {

	private World world;
	private TiledArea layout;
	private List<Door> doors = new ArrayList<>();
	private List<Bed> beds = new ArrayList<>();

	public HouseBuilder(World world) {
		this.world = world;
	}

	public HouseBuilder layout(int col, int row, int width, int height) {
		layout = new TiledRectangle(col, row, width, height);
		return this;
	}

	public HouseBuilder door(Door door) {
		doors.add(door);
		return this;
	}

	public HouseBuilder bed(Bed bed) {
		beds.add(bed);
		return this;
	}

	public House build() {
		return new House(world, layout, doors, beds);
	}
}