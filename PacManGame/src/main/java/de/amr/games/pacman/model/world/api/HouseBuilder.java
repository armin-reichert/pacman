package de.amr.games.pacman.model.world.api;

import java.util.ArrayList;
import java.util.List;

public class HouseBuilder {

	private Area layout;
	private List<Door> doors = new ArrayList<>();
	private List<Bed> beds = new ArrayList<>();

	public static HouseBuilder house() {
		return new HouseBuilder();
	}

	public HouseBuilder layout(int col, int row, int width, int height) {
		layout = new Block(col, row, width, height);
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

	public HouseBuilder bed(int col, int row, Direction dir) {
		Bed bed = new Bed(col, row, dir);
		beds.add(bed);
		return this;
	}

	public House build() {
		return new House(layout, doors, beds);
	}
}