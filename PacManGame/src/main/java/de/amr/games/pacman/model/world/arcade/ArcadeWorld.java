package de.amr.games.pacman.model.world.arcade;

import static de.amr.games.pacman.model.world.api.HouseBuilder.house;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.Bed;
import de.amr.games.pacman.model.world.api.Bonus;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Door;
import de.amr.games.pacman.model.world.api.House;
import de.amr.games.pacman.model.world.api.OneWayTile;
import de.amr.games.pacman.model.world.api.Portal;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.core.MapBasedWorld;

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

	protected Bed pacManBed;
	protected final List<House> houses = new ArrayList<>();
	protected final List<Portal> portals = new ArrayList<>();
	protected final List<OneWayTile> oneWayTiles = new ArrayList<>();
	protected Bonus bonus;
	protected boolean changing;
	protected boolean frozen;

	public ArcadeWorld() {
		super(DATA);
		pacManBed = new Bed(13, 26, Direction.RIGHT);
		houses.add(house()
		//@formatter:off
			.layout(11, 16, 6, 4)
			.door(new Door(Direction.DOWN, 13, 15, 2, 1))
			.bed(13, 14, Direction.LEFT)
			.bed(11, 17, Direction.UP) 
			.bed(13, 17, Direction.DOWN)
			.bed(15, 17, Direction.UP)
			.ok());
		//@formatter:on
		portals.add(new Portal(Tile.at(-1, 17), Tile.at(28, 17)));
		oneWayTiles.addAll(List.of(
		//@formatter:off
			new OneWayTile(12, 13, Direction.DOWN), 
			new OneWayTile(15, 13, Direction.DOWN),
			new OneWayTile(12, 25, Direction.DOWN), 
			new OneWayTile(15, 25, Direction.DOWN)
		//@formatter:on
		));
	}

	@Override
	public boolean isFrozen() {
		return frozen;
	}

	@Override
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	@Override
	public boolean isChanging() {
		return changing;
	}

	@Override
	public void setChanging(boolean changing) {
		this.changing = changing;
	}

	@Override
	protected void addPortal(Tile left, Tile right) {
		super.addPortal(left, right);
		portals.add(new Portal(Tile.at(left.col - 1, left.row), Tile.at(right.col + 1, right.row)));
	}

	@Override
	public Stream<House> houses() {
		return houses.stream();
	}

	@Override
	public Bed pacManBed() {
		return pacManBed;
	}

	@Override
	public Stream<Portal> portals() {
		return portals.stream();
	}

	@Override
	public Stream<OneWayTile> oneWayTiles() {
		return oneWayTiles.stream();
	}

	@Override
	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
	}

	@Override
	public void setBonus(Bonus bonus) {
		this.bonus = bonus;
	}

	/**
	 * There are 4 reserved rows at the top and 3 at the bottom used for displaying scores
	 * 
	 * @return the habitat tiles
	 */
	@Override
	public Stream<Tile> habitat() {
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