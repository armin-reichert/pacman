package de.amr.games.pacman.model.world.components;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.TiledArea;

public class HouseBuilder {

	private TiledArea layout;
	private List<Door> doors = new ArrayList<>();
	private List<Bed> beds = new ArrayList<>();

	public HouseBuilder layout(int col, int row, int width, int height) {
		layout = new TiledRectangle(col, row, width, height);
		return this;
	}

	public HouseBuilder door(Door door) {
		doors.add(door);
		return this;
	}

	public HouseBuilder door(Direction intoHouse, int col, int row, int width, int height) {
		doors.add(new Door(intoHouse, col, row, width, height));
		return this;
	}

	public HouseBuilder bed(Bed bed) {
		beds.add(bed);
		return this;
	}

	public HouseBuilder bed(int col, int row, Direction exitDir) {
		beds.add(new Bed(col, row, exitDir));
		return this;
	}

	public House build() {
		return new House(layout, doors, beds);
	}
}