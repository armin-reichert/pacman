package de.amr.games.pacman.model.world.core;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.Food;
import de.amr.games.pacman.model.world.api.TemporaryFood;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.components.Tile;

public class EmptyWorld extends AbstractWorld {

	public EmptyWorld(int width, int height) {
		super(width, height);
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
	public House house(int i) {
		return null;
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
	public void eatFood(Tile location) {
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