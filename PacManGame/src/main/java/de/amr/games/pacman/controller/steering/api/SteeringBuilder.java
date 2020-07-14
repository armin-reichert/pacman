package de.amr.games.pacman.controller.steering.api;

import java.util.function.Supplier;

import de.amr.games.pacman.controller.api.MobileCreature;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.common.HeadingForTargetTile;
import de.amr.games.pacman.controller.steering.common.RandomMovement;
import de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.Tile;

public class SteeringBuilder {

	public static class HeadingForTargetTileBuilder {

		private MobileCreature creature;
		private Supplier<Tile> fnTargetTile;

		public HeadingForTargetTileBuilder tile(Supplier<Tile> fnTargetTile) {
			this.fnTargetTile = fnTargetTile;
			return this;
		}

		public HeadingForTargetTileBuilder tile(Tile targetTile) {
			return tile(() -> targetTile);
		}

		public HeadingForTargetTileBuilder tile(int col, int row) {
			return tile(Tile.at(col, row));
		}

		public Steering build() {
			return new HeadingForTargetTile(creature, fnTargetTile);
		}
	}

	public static HeadingForTargetTileBuilder headingForTargetTile(Ghost ghost) {
		HeadingForTargetTileBuilder builder = new HeadingForTargetTileBuilder();
		builder.creature = ghost;
		return builder;
	}

	public static class RandomMovementBuilder {
		private MobileCreature creature;

		public Steering build() {
			return new RandomMovement(creature);
		}
	}

	public static RandomMovementBuilder randomMovement(MobileCreature ghost) {
		RandomMovementBuilder builder = new RandomMovementBuilder();
		builder.creature = ghost;
		return builder;
	}

	public static class EnteringHouseAndGoingToBedBuilder {

		private Ghost ghost;
		private Bed bed;

		public EnteringHouseAndGoingToBedBuilder bed(Bed bed) {
			this.bed = bed;
			return this;
		}

		public Steering build() {
			return new EnteringHouseAndGoingToBed(ghost, bed);
		}

	}

	public static EnteringHouseAndGoingToBedBuilder enteringHouseAndGoingToBed(Ghost ghost) {
		EnteringHouseAndGoingToBedBuilder builder = new EnteringHouseAndGoingToBedBuilder();
		builder.ghost = ghost;
		return builder;
	}
}
