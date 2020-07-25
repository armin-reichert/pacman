package de.amr.games.pacman.controller.steering.api;

import java.util.function.Supplier;

import de.amr.games.pacman.controller.creatures.api.Creature;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.steering.common.FollowingKeys;
import de.amr.games.pacman.controller.steering.common.HeadingForTargetTile;
import de.amr.games.pacman.controller.steering.common.RandomMovement;
import de.amr.games.pacman.controller.steering.ghost.BouncingOnBed;
import de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed;
import de.amr.games.pacman.controller.steering.ghost.FleeingToSafeTile;
import de.amr.games.pacman.controller.steering.ghost.LeavingHouse;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;

public class AnimalMaster {

	private Creature<?, ?> animal;
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

	public FleeToSafeTileBuilder fleeToSafeTile() {
		return new FleeToSafeTileBuilder();
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

		public Steering<Ghost> ok() {
			Ghost ghost = (Ghost) animal;
			Steering<Ghost> steering = new BouncingOnBed(bed != null ? bed : ghost.bed());
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

		public Steering<Ghost> ok() {
			Ghost ghost = (Ghost) animal;
			Steering<Ghost> steering = new EnteringHouseAndGoingToBed(ghost, bed != null ? bed : ghost.bed());
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

		public Steering<?> ok() {
			if (animal instanceof Ghost) {
				Ghost ghost = (Ghost) animal;
				Steering<Ghost> steering = new HeadingForTargetTile<>(ghost, fnTargetTile);
				ghost.behavior(ghostState, steering);
				return steering;
			} else if (animal instanceof PacMan) {
				PacMan pacMan = (PacMan) animal;
				Steering<PacMan> steering = new HeadingForTargetTile<>(pacMan, fnTargetTile);
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

		public Steering<Ghost> ok() {
			Ghost ghost = (Ghost) animal;
			Steering<Ghost> steering = new LeavingHouse(house);
			ghost.behavior(ghostState, steering);
			return steering;
		}
	}

	public class MovesRandomlyBuilder {

		public Steering<?> ok() {
			if (animal instanceof Ghost) {
				Ghost ghost = (Ghost) animal;
				ghost.behavior(ghostState, new RandomMovement<>());
				return ghost.steering();
			} else if (animal instanceof PacMan) {
				PacMan pacMan = (PacMan) animal;
				pacMan.behavior(PacManState.RUNNING, new RandomMovement<>());
				return pacMan.steering();
			} else {
				throw new IllegalStateException();
			}
		}
	}

	public class FleeToSafeTileBuilder {

		private MobileLifeform attacker;

		public FleeToSafeTileBuilder from(MobileLifeform attacker) {
			this.attacker = attacker;
			return this;
		}

		public Steering<Ghost> ok() {
			Ghost ghost = (Ghost) animal;
			Steering<Ghost> steering = new FleeingToSafeTile(ghost, attacker);
			ghost.behavior(ghostState, steering);
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

		public Steering<?> ok() {
			if (animal instanceof Ghost) {
				Ghost ghost = (Ghost) animal;
				ghost.behavior(ghostState, new FollowingKeys<>(up, right, down, left));
				return ghost.steering();
			} else if (animal instanceof PacMan) {
				PacMan pacMan = (PacMan) animal;
				pacMan.behavior(PacManState.RUNNING, new FollowingKeys<>(up, right, down, left));
				return pacMan.steering();
			} else {
				throw new IllegalStateException();
			}
		}
	}
}