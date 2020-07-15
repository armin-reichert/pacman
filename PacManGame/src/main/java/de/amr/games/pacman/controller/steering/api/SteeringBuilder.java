package de.amr.games.pacman.controller.steering.api;

import java.util.function.Supplier;

import de.amr.games.pacman.controller.creatures.Animal;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.steering.common.HeadingForTargetTile;
import de.amr.games.pacman.controller.steering.common.RandomMovement;
import de.amr.games.pacman.controller.steering.ghost.BouncingOnBed;
import de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed;
import de.amr.games.pacman.controller.steering.ghost.LeavingHouse;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.House;
import de.amr.games.pacman.model.world.core.Tile;

public class SteeringBuilder {

	private Animal<?> animal;
	private GhostState ghostState;

	public static SteeringBuilder ghost(Ghost ghost) {
		SteeringBuilder builder = new SteeringBuilder();
		builder.animal = ghost;
		return builder;
	}

	public static SteeringBuilder pacMan(PacMan pacMan) {
		SteeringBuilder builder = new SteeringBuilder();
		builder.animal = pacMan;
		return builder;
	}

	public SteeringBuilder when(GhostState state) {
		this.ghostState = state;
		return this;
	}

	public HeadsForTargetTileBuilder headsFor() {
		return new HeadsForTargetTileBuilder();
	}

	public BouncesOnBedBuilder bouncesOnBed() {
		return new BouncesOnBedBuilder();
	}

	public EntersHouseAndGoesToBedBuilder entersHouseAndGoesToBed() {
		return new EntersHouseAndGoesToBedBuilder();
	}

	public LeavesHouseBuilder leavesHouse() {
		return new LeavesHouseBuilder();
	}

	public MovesRandomlyBuilder movesRandomly() {
		return new MovesRandomlyBuilder();
	}

	public class BouncesOnBedBuilder {

		private Bed bed;

		public BouncesOnBedBuilder bed(Bed bed) {
			this.bed = bed;
			return this;
		}

		public Steering ok() {
			Ghost ghost = (Ghost) animal;
			Steering steering = new BouncingOnBed(ghost, bed);
			ghost.behavior(ghostState, steering);
			return steering;
		}
	}

	public class EntersHouseAndGoesToBedBuilder {

		private Bed bed;

		public EntersHouseAndGoesToBedBuilder bed(Bed bed) {
			this.bed = bed;
			return this;
		}

		public Steering ok() {
			Ghost ghost = (Ghost) animal;
			Steering steering = new EnteringHouseAndGoingToBed(ghost, bed);
			ghost.behavior(ghostState, steering);
			return steering;
		}
	}

	public class HeadsForTargetTileBuilder {

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
			if (animal instanceof Ghost) {
				Ghost ghost = (Ghost) animal;
				Steering steering = new HeadingForTargetTile(ghost, fnTargetTile);
				ghost.behavior(ghostState, steering);
				return steering;
			} else if (animal instanceof PacMan) {
				PacMan pacMan = (PacMan) animal;
				Steering steering = new HeadingForTargetTile(pacMan, fnTargetTile);
				pacMan.behavior(PacManState.RUNNING, steering);
				return steering;
			}
			throw new IllegalStateException();
		}
	}

	public class LeavesHouseBuilder {

		private House house;

		public LeavesHouseBuilder house(House house) {
			this.house = house;
			return this;
		}

		public Steering ok() {
			Ghost ghost = (Ghost) animal;
			Steering steering = new LeavingHouse(ghost, house);
			ghost.behavior(ghostState, steering);
			return steering;
		}
	}

	public class MovesRandomlyBuilder {

		public Steering ok() {
			Steering steering = new RandomMovement(animal);
			if (animal instanceof Ghost) {
				Ghost ghost = (Ghost) animal;
				ghost.behavior(ghostState, steering);
			} else if (animal instanceof PacMan) {
				PacMan pacMan = (PacMan) animal;
				pacMan.behavior(PacManState.RUNNING, steering);
			} else {
				throw new IllegalStateException();
			}
			return steering;
		}
	}
}