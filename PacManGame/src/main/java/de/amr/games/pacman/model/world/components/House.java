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

import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Tile;
import de.amr.games.pacman.model.world.api.TiledArea;

/**
 * A house has a room with beds and some doors.
 * 
 * @author Armin Reichert
 */
public class House implements TiledArea {

	private final TiledArea layout;
	private final List<Door> doors;
	private final List<Bed> beds;

	public House(TiledArea layout, List<Door> doors, List<Bed> beds) {
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

	public boolean isEntry(Tile tile) {
		for (Direction dir : Direction.values()) {
			Tile neighbor = tile.towards(dir);
			if (hasDoorAt(neighbor)) {
				var door = doors().filter(d -> d.includes(neighbor)).findFirst();
				if (door.isPresent()) {
					return dir == door.get().intoHouse;
				}
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