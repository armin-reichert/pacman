package de.amr.games.pacman.controller.world.arcade;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.Area;
import de.amr.games.pacman.model.world.api.Bed;
import de.amr.games.pacman.model.world.api.Block;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Door;
import de.amr.games.pacman.model.world.api.House;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.OneWayTile;
import de.amr.games.pacman.model.world.api.Portal;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.core.MapBasedWorld;
import de.amr.games.pacman.model.world.core.WorldMap;

/**
 * Map-based Pac-Man game world implementation.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends MapBasedWorld {

	static final byte[][] DATA = {
			//@formatter:off
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 2, 2, 2, 2, 2,18, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2,18, 2, 2, 2, 2, 2, 1 },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
			{ 1, 6, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 6, 1 },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
			{ 1,18, 2, 2, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2, 2, 2,18, 1 },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
			{ 1, 2, 2, 2, 2, 2,18, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1,18, 2, 2, 2, 2, 2, 1 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 0, 0,16, 0, 0,16, 0, 0, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{32,32,32,32,32,32,18, 0, 0,16, 1, 0, 0, 0, 0, 0, 0, 1,16, 0, 0,18,32,32,32,32,32,32 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1,16, 0, 0, 0, 0, 0, 0, 0, 0,16, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
			{ 1, 2, 2, 2, 2, 2,18, 2, 2,18, 2, 2, 2, 1, 1, 2, 2, 2,18, 2, 2,18, 2, 2, 2, 2, 2, 1 },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
			{ 1, 6, 2, 2, 1, 1,18, 2, 2,18, 2, 2,18, 0, 0,18, 2, 2,18, 2, 2,18, 1, 1, 2, 2, 6, 1 },
			{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1 },
			{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1 },
			{ 1, 2, 2,18, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2,18, 2, 2, 1 },
			{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1 },
			{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1 },
			{ 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,18, 2, 2,18, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			//@formatter:on
	};

	private ArcadeWorldFolks folks;

	public ArcadeWorld() {
		map = new WorldMap(DATA);
		pacManBed = new Bed(4, 13, 26, Direction.RIGHT);
		houses.add(ghostHouse(11, 16, 6, 4));
		bonusTile = Tile.at(13, 20);
		portals.add(new Portal(Tile.at(-1, 17), Tile.at(28, 17)));
		oneWayTiles.addAll(Arrays.asList(
		//@formatter:off
			new OneWayTile(12, 13, Direction.DOWN), 
			new OneWayTile(15, 13, Direction.DOWN),
			new OneWayTile(12, 25, Direction.DOWN), 
			new OneWayTile(15, 25, Direction.DOWN)
		//@formatter:on
		));
	}

	public void setFolks(ArcadeWorldFolks folks) {
		this.folks = folks;
	}

	public ArcadeWorldFolks getFolks() {
		return folks;
	}

	private House ghostHouse(int col, int row, int w, int h) {
		Area room = new Block(col, row, w, h);
		Door door = new Door(Direction.DOWN, Tile.at(col + 2, row - 1), Tile.at(col + 3, row - 1));
		List<Bed> beds = Arrays.asList(
		//@formatter:off
			new Bed(0, col + 2, row - 2, Direction.LEFT),
			new Bed(1, col,     row + 1, Direction.UP), 
			new Bed(2, col + 2, row + 1, Direction.DOWN),
			new Bed(3, col + 4, row + 1, Direction.UP)
		//@formatter:on
		);
		return new House(room, Arrays.asList(door), beds);
	}

	public void putIntoBed(MobileLifeform creature, Bed bed) {
		creature.placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
		creature.setMoveDir(bed.exitDir);
		creature.setWishDir(bed.exitDir);
	}

	/**
	 * There are 4 reserved rows at the top and 3 at the bottom used for displaying scores
	 * 
	 * @return the habitat tiles
	 */
	@Override
	public Stream<Tile> habitatArea() {
		return IntStream.range(3 * width(), (height() + 4) * width()).mapToObj(i -> Tile.at(i % width(), i / width()));
	}

	@Override
	public Tile capeNW() {
		return Tile.at(1, 4);
	}

	@Override
	public Tile capeNE() {
		return Tile.at(width() - 2, 4);
	}

	@Override
	public Tile capeSW() {
		return Tile.at(1, height() - 4);
	}

	@Override
	public Tile capeSE() {
		return Tile.at(width() - 2, height() - 4);
	}
}