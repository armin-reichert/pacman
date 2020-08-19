package de.amr.games.pacman.model.world.components;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.model.world.api.Area;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Territory;

public class HouseBuilder {

	private Territory territory;
	private Area layout;
	private List<Door> doors = new ArrayList<>();
	private List<Bed> beds = new ArrayList<>();

	public HouseBuilder(Territory territory) {
		this.territory = territory;
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
		return new House(territory, layout, doors, beds);
	}
}