package de.amr.games.pacman.controller.steering.api;

import java.util.function.Supplier;

import de.amr.games.pacman.controller.api.MobileCreature;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.common.HeadingForTargetTile;
import de.amr.games.pacman.controller.steering.common.RandomMovement;
import de.amr.games.pacman.controller.steering.ghost.BouncingOnBed;
import de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed;
import de.amr.games.pacman.controller.steering.ghost.LeavingHouse;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.House;
import de.amr.games.pacman.model.world.core.Tile;

public class SteeringBuilder {

	public static class BouncesOnBedBuilder {
	
		private Ghost ghost;
		private Bed bed;
	
		public BouncesOnBedBuilder bed(Bed bed) {
			this.bed = bed;
			return this;
		}
	
		public Steering ok() {
			return new BouncingOnBed(ghost, bed);
		}
	}

	public static class EntersHouseAndGoesToBedBuilder {
	
		private Ghost ghost;
		private Bed bed;
	
		public EntersHouseAndGoesToBedBuilder bed(Bed bed) {
			this.bed = bed;
			return this;
		}
	
		public Steering ok() {
			return new EnteringHouseAndGoingToBed(ghost, bed);
		}
	}

	public static class HeadsForTargetTileBuilder {

		private MobileCreature creature;
		private Supplier<Tile> fnTargetTile;

		public HeadsForTargetTileBuilder tile(Supplier<Tile> fnTargetTile) {
			this.fnTargetTile = fnTargetTile;
			return this;
		}

		public HeadsForTargetTileBuilder tile(Tile targetTile) {
			return tile(() -> targetTile);
		}

		public HeadsForTargetTileBuilder tile(int col, int row) {
			return tile(Tile.at(col, row));
		}

		public Steering ok() {
			return new HeadingForTargetTile(creature, fnTargetTile);
		}
	}

	public static class LeavesHouseBuilder {
	
		private Ghost ghost;
		private House house;
	
		public LeavesHouseBuilder house(House house) {
			this.house = house;
			return this;
		}
	
		public Steering ok() {
			return new LeavingHouse(ghost, house);
		}
	}

	public static class MovesRandomlyBuilder {
		private MobileCreature creature;

		public Steering ok() {
			return new RandomMovement(creature);
		}
	}

	public static BouncesOnBedBuilder bouncesOnBed(Ghost ghost) {
		BouncesOnBedBuilder builder = new BouncesOnBedBuilder();
		builder.ghost = ghost;
		return builder;
	}

	public static EntersHouseAndGoesToBedBuilder entersHouseAndGoesToBed(Ghost ghost) {
		EntersHouseAndGoesToBedBuilder builder = new EntersHouseAndGoesToBedBuilder();
		builder.ghost = ghost;
		return builder;
	}

	public static HeadsForTargetTileBuilder headsForTargetTile(Ghost ghost) {
		HeadsForTargetTileBuilder builder = new HeadsForTargetTileBuilder();
		builder.creature = ghost;
		return builder;
	}

	public static LeavesHouseBuilder leavesHouse(Ghost ghost) {
		LeavesHouseBuilder builder = new LeavesHouseBuilder();
		builder.ghost = ghost;
		return builder;
	}

	public static MovesRandomlyBuilder movesRandomly(MobileCreature ghost) {
		MovesRandomlyBuilder builder = new MovesRandomlyBuilder();
		builder.creature = ghost;
		return builder;
	}
}
