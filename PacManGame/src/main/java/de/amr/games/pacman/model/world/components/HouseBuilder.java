/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.world.components;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.lib.Direction;
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