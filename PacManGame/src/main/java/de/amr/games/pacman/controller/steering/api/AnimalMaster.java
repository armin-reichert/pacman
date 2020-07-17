package de.amr.games.pacman.controller.steering.api;

import java.util.function.Supplier;

import de.amr.games.pacman.controller.creatures.Animal;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.steering.common.FollowingKeys;
import de.amr.games.pacman.controller.steering.common.HeadingForTargetTile;
import de.amr.games.pacman.controller.steering.common.RandomMovement;
import de.amr.games.pacman.controller.steering.ghost.BouncingOnBed;
import de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed;
import de.amr.games.pacman.controller.steering.ghost.LeavingHouse;
import de.amr.games.pacman.model.world.api.Bed;
import de.amr.games.pacman.model.world.api.House;
import de.amr.games.pacman.model.world.api.Tile;

public class AnimalMaster {

	private Animal<?> animal;
	private GhostState ghostState;

	public static AnimalMaster you(Ghost ghost) {
		AnimalMaster builder = new AnimalMaster();
		builder.animal = ghost;
		return builder;
	}

	public static AnimalMaster you(PacMan pacMan) {
		AnimalMaster builder = new AnimalMaster();
		builder.animal = pacMan;
		return builder;
	}

	public AnimalMaster when(GhostState state) {
		this.ghostState = state;
		return this;
	}

	public HeadsForTargetTileBuilder headFor() {
		return new HeadsForTargetTileBuilder();
	}

	public BouncesOnBedBuilder bounceOnBed() {
		return new BouncesOnBedBuilder();
	}

	public EntersHouseAndGoesToBedBuilder enterHouseAndGoToBed() {
		return new EntersHouseAndGoesToBedBuilder();
	}

	public LeavesHouseBuilder leaveHouse() {
		return new LeavesHouseBuilder();
	}

	public MovesRandomlyBuilder moveRandomly() {
		return new MovesRandomlyBuilder();
	}

	public FollowsKeysBuilder followTheKeys() {
		return new FollowsKeysBuilder();
	}

	public class BouncesOnBedBuilder {

		private Bed bed;

		public BouncesOnBedBuilder bed(Bed bed) {
			this.bed = bed;
			return this;
		}

		public Steering ok() {
			Ghost ghost = (Ghost) animal;
			Steering steering = new BouncingOnBed(ghost, bed != null ? bed : ghost.bed());
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
			Steering steering = new EnteringHouseAndGoingToBed(ghost, bed != null ? bed : ghost.bed());
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

	public class FollowsKeysBuilder {

		private int up, right, down, left;

		public FollowsKeysBuilder keys(int up, int right, int down, int left) {
			this.up = up;
			this.right = right;
			this.down = down;
			this.left = left;
			return this;
		}

		public Steering ok() {
			Steering steering = new FollowingKeys(animal, up, right, down, left);
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