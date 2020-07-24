package de.amr.games.pacman.model.world.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Life;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Block;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.Portal;

/**
 * World base class.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractWorld extends Block implements World {

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

	public AbstractWorld(int width, int height) {
		super(0, 0, width, height);
	}

	@Override
	public boolean includes(Tile tile) {
		return 0 <= tile.row && tile.row < height() && 0 <= tile.col && tile.col < width();
	}

	@Override
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		if (n < 0) {
			dir = dir.opposite();
		}
		if (n == 0) {
			return tile;
		}
		Vector2f dirVector = dir.vector();
		int dx = dirVector.roundedX(), dy = dirVector.roundedY();
		int col = tile.col, row = tile.row;
		while (n-- > 0) {
			Tile t = Tile.at(col, row);
			if (isPortalAt(t)) {
				Portal portal = portals().filter(p -> p.includes(t)).findAny().get();
				if (portal.vertical) {
					if (t.equals(portal.either) && dir == Direction.UP) {
						col = portal.other.col;
					} else if (t.equals(portal.other) && dir == Direction.DOWN) {
						col = portal.either.col;
					} else {
						col += dx;
						row += dy;
					}
				} else {
					if (t.equals(portal.either) && dir == Direction.LEFT) {
						col = portal.other.col;
					} else if (t.equals(portal.other) && dir == Direction.RIGHT) {
						col = portal.either.col;
					} else {
						col += dx;
						row += dy;
					}
				}
			} else {
				col += dx;
				row += dy;
			}
		}
		return Tile.at(col, row);
	}

	@Override
	public boolean insideHouseOrDoor(Tile tile) {
		return isDoorAt(tile) || houses().map(House::layout).anyMatch(layout -> layout.includes(tile));
	}

	@Override
	public boolean isDoorAt(Tile tile) {
		return houses().flatMap(House::doors).anyMatch(door -> door.includes(tile));
	}

	@Override
	public boolean isHouseEntry(Tile tile) {
		for (Direction dir : Direction.values()) {
			Tile neighbor = neighbor(tile, dir);
			if (isDoorAt(neighbor)) {
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