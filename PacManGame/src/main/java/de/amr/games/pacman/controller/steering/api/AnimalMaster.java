package de.amr.games.pacman.controller.steering.api;

import java.util.Objects;
import java.util.function.Supplier;

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

	private Ghost ghost;
	private PacMan pacMan;
	private GhostState ghostState;

	private void ensureGhost() {
		if (ghost == null) {
			throw new IllegalStateException("Who do you mean?");
		}
	}

	private void ensureGhostState() {
		if (ghostState == null) {
			throw new IllegalStateException("When should I do that?");
		}
	}

	private void ensureAny() {
		if (pacMan == null && ghost == null) {
			throw new IllegalStateException("Who do you mean?");
		}
	}

	public static AnimalMaster you(Ghost ghost) {
		AnimalMaster master = new AnimalMaster();
		master.ghost = Objects.requireNonNull(ghost);
		return master;
	}

	public static AnimalMaster you(PacMan pacMan) {
		AnimalMaster master = new AnimalMaster();
		master.pacMan = Objects.requireNonNull(pacMan);
		return master;
	}

	public AnimalMaster when(GhostState state) {
		ensureGhost();
		ghostState = Objects.requireNonNull(state);
		return this;
	}

	public HeadsForTargetTileBuilder headFor() {
		ensureAny();
		return new HeadsForTargetTileBuilder();
	}

	public BouncesOnBedBuilder bounceOnBed() {
		ensureGhost();
		return new BouncesOnBedBuilder();
	}

	public EntersHouseAndGoesToBedBuilder enterHouseAndGoToBed() {
		ensureGhost();
		return new EntersHouseAndGoesToBedBuilder();
	}

	public LeavesHouseBuilder leaveHouse() {
		ensureGhost();
		return new LeavesHouseBuilder();
	}

	public MovesRandomlyBuilder moveRandomly() {
		ensureAny();
		return new MovesRandomlyBuilder();
	}

	public FleeToSafeTileBuilder fleeToSafeTile() {
		ensureGhost();
		return new FleeToSafeTileBuilder();
	}

	public FollowsKeysBuilder followTheKeys() {
		ensureAny();
		return new FollowsKeysBuilder();
	}

	public class BouncesOnBedBuilder {

		private Bed bed;

		public BouncesOnBedBuilder bed(Bed bed) {
			this.bed = Objects.requireNonNull(bed);
			return this;
		}

		public Steering<Ghost> ok() {
			ensureGhost();
			ensureGhostState();
			Steering<Ghost> steering = new BouncingOnBed(bed != null ? bed : ghost.bed());
			ghost.behavior(ghostState, steering);
			return steering;
		}
	}

	public class EntersHouseAndGoesToBedBuilder {

		private Bed bed;

		public EntersHouseAndGoesToBedBuilder bed(Bed bed) {
			this.bed = Objects.requireNonNull(bed);
			return this;
		}

		public Steering<Ghost> ok() {
			ensureGhost();
			ensureGhostState();
			Steering<Ghost> steering = new EnteringHouseAndGoingToBed(ghost, bed != null ? bed : ghost.bed());
			ghost.behavior(ghostState, steering);
			return steering;
		}
	}

	public class HeadsForTargetTileBuilder {

		private Supplier<Tile> fnTargetTile;

		public HeadsForTargetTileBuilder tile(Supplier<Tile> fnTargetTile) {
			this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
			return this;
		}

		public HeadsForTargetTileBuilder tile(Tile targetTile) {
			return tile(() -> targetTile);
		}

		public HeadsForTargetTileBuilder tile(int col, int row) {
			return tile(Tile.at(col, row));
		}

		public Steering<?> ok() {
			if (ghost != null) {
				ensureGhostState();
				ghost.behavior(ghostState, new HeadingForTargetTile<>(ghost, fnTargetTile));
				return ghost.steering();
			} else if (pacMan != null) {
				pacMan.behavior(PacManState.RUNNING, new HeadingForTargetTile<>(pacMan, fnTargetTile));
				return pacMan.steering();
			}
			throw new IllegalStateException();
		}
	}

	public class LeavesHouseBuilder {

		private House house;

		public LeavesHouseBuilder house(House house) {
			this.house = Objects.requireNonNull(house);
			return this;
		}

		public Steering<Ghost> ok() {
			ensureGhost();
			ensureGhostState();
			ghost.behavior(ghostState, new LeavingHouse(house));
			return ghost.steering();
		}
	}

	public class MovesRandomlyBuilder {

		public Steering<?> ok() {
			if (ghost != null) {
				ensureGhostState();
				ghost.behavior(ghostState, new RandomMovement<>());
				return ghost.steering();
			} else if (pacMan != null) {
				pacMan.behavior(PacManState.RUNNING, new RandomMovement<>());
				return pacMan.steering();
			}
			throw new IllegalStateException();
		}
	}

	public class FleeToSafeTileBuilder {

		private MobileLifeform attacker;

		public FleeToSafeTileBuilder from(MobileLifeform attacker) {
			this.attacker = Objects.requireNonNull(attacker);
			return this;
		}

		public Steering<Ghost> ok() {
			ensureGhost();
			ensureGhostState();
			ghost.behavior(ghostState, new FleeingToSafeTile(ghost, attacker));
			return ghost.steering();
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
			if (ghost != null) {
				ensureGhostState();
				ghost.behavior(ghostState, new FollowingKeys<>(up, right, down, left));
				return ghost.steering();
			} else if (pacMan != null) {
				pacMan.behavior(PacManState.RUNNING, new FollowingKeys<>(up, right, down, left));
				return pacMan.steering();
			}
			throw new IllegalStateException();
		}
	}
}