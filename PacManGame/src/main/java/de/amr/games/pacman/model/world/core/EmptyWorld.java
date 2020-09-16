package de.amr.games.pacman.model.world.core;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.Food;
import de.amr.games.pacman.model.world.api.TemporaryFood;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.model.world.components.Portal;

public class EmptyWorld extends AbstractTiledWorld {

	public EmptyWorld(int horizontalTiles, int verticalTiles) {
		super(horizontalTiles, verticalTiles);
	}

	@Override
	public boolean isIntersection(Tile tile) {
		return true;
	}

	@Override
	public boolean isAccessible(Tile tile) {
		return true;
	}

	@Override
	public boolean isTunnel(Tile tile) {
		return false;
	}

	@Override
	public Bed pacManBed() {
		return null;
	}

	@Override
	public Stream<House> houses() {
		return Stream.empty();
	}

	@Override
	public Optional<House> house(int i) {
		return Optional.empty();
	}

	@Override
	public Stream<Portal> portals() {
		return Stream.empty();
	}

	@Override
	public Stream<OneWayTile> oneWayTiles() {
		return Stream.empty();
	}

	@Override
	public int totalFoodCount() {
		return 0;
	}

	@Override
	public void restoreFood() {
	}

	@Override
	public Optional<Food> foodAt(Tile location) {
		return Optional.empty();
	}

	@Override
	public void removeFood(Tile location) {
	}

	@Override
	public boolean hasEatenFood(Tile location) {
		return false;
	}

	@Override
	public Optional<TemporaryFood> temporaryFood() {
		return Optional.empty();
	}

	@Override
	public void showTemporaryFood(TemporaryFood food) {
	}

	@Override
	public void hideTemporaryFood() {
	}
}