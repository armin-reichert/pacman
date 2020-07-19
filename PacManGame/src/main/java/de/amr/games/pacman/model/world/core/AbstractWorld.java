package de.amr.games.pacman.model.world.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Door;
import de.amr.games.pacman.model.world.api.House;
import de.amr.games.pacman.model.world.api.Life;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

/**
 * World base class.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractWorld implements World {

	private int distFromCornerNW(Tile t1, Tile t2) {
		return Integer.compare(t1.col + t1.row, t2.col + t2.row);
	}

	private int distFromCornerNE(Tile t1, Tile t2) {
		return Integer.compare(width() - t1.col + t1.row, width() - t2.col + t2.row);
	}

	@Override
	public List<Tile> capes() {
		Tile capeNW = habitat().filter(this::isAccessible).min((t1, t2) -> distFromCornerNW(t1, t2)).get();
		Tile capeNE = habitat().filter(this::isAccessible).min((t1, t2) -> distFromCornerNE(t1, t2)).get();
		Tile capeSE = habitat().filter(this::isAccessible).max((t1, t2) -> distFromCornerNW(t1, t2)).get();
		Tile capeSW = habitat().filter(this::isAccessible).max((t1, t2) -> distFromCornerNE(t1, t2)).get();
		return List.of(capeNW, capeNE, capeSE, capeSW);
	}

	private final Collection<Life> excluded = new HashSet<>();

	@Override
	public int col() {
		return 0;
	}

	@Override
	public int row() {
		return 0;
	}

	@Override
	public boolean includes(Tile tile) {
		return 0 <= tile.row && tile.row < height() && 0 <= tile.col && tile.col < width();
	}

	@Override
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		//@formatter:off
		return portals()
			.filter(portal -> portal.includes(tile))
			.findAny()
			.map(portal -> portal.otherEntry())
			.orElse(Tile.at(tile.col + n * dir.vector().roundedX(), tile.row + n * dir.vector().roundedY()));
		//@formatter:on
	}

	@Override
	public boolean insideHouseOrDoor(Tile tile) {
		return isDoor(tile) || houses().map(House::layout).anyMatch(layout -> layout.includes(tile));
	}

	@Override
	public boolean isDoor(Tile tile) {
		return houses().flatMap(House::doors).anyMatch(door -> door.includes(tile));
	}

	@Override
	public boolean isHouseEntry(Tile tile) {
		for (Direction dir : Direction.values()) {
			Tile neighbor = neighbor(tile, dir);
			if (isDoor(neighbor)) {
				Door door = houses().flatMap(House::doors).filter(d -> d.includes(neighbor)).findFirst().get();
				return door.intoHouse == dir;
			}
		}
		return false;
	}

	@Override
	public void include(Life life) {
		excluded.remove(life);
		life.setVisible(true);
	}

	@Override
	public void exclude(Life life) {
		excluded.add(life);
		life.setVisible(false);
	}

	@Override
	public boolean contains(Life life) {
		return !excluded.contains(life);
	}
}