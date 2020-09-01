package de.amr.games.pacman.controller.steering.api;

import java.awt.event.KeyEvent;
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
import de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed;
import de.amr.games.pacman.controller.steering.ghost.LeavingHouse;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.Tile;

/**
 * A builder for assigning steerings to Pac-Man and the ghosts. Allows nice formulations like
 * 
 * <pre>
 * you(ghost).when(FRIGHTENED).fleeToSafeTile().ok();
 * </pre>
 * 
 * @author Armin Reichert
 */
public class SteeringBuilder {

	protected Ghost ghost;
	protected PacMan pacMan;
	protected GhostState ghostState;

	protected void ensureGhost() {
		if (ghost == null) {
			throw new IllegalStateException("Who do you mean?");
		}
	}

	protected void ensureGhostState() {
		if (ghostState == null) {
			throw new IllegalStateException("When should I do that?");
		}
	}

	protected void ensureAny() {
		if (pacMan == null && ghost == null) {
			throw new IllegalStateException("Who do you mean?");
		}
	}

	public static SteeringBuilder you(Ghost ghost) {
		SteeringBuilder master = new SteeringBuilder();
		master.ghost = Objects.requireNonNull(ghost);
		return master;
	}

	public static SteeringBuilder you(PacMan pacMan) {
		SteeringBuilder master = new SteeringBuilder();
		master.pacMan = Objects.requireNonNull(pacMan);
		return master;
	}

	public SteeringBuilder when(GhostState state) {
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

	public EnteringDoorAndGoingToBedBuilder enterDoorAndGoToBed() {
		ensureGhost();
		return new EnteringDoorAndGoingToBedBuilder().bed(ghost.bed);
	}

	public LeavesHouseBuilder leaveHouse() {
		ensureGhost();
		return new LeavesHouseBuilder();
	}

	public MovesRandomlyBuilder moveRandomly() {
		ensureAny();
		return new MovesRandomlyBuilder();
	}

	public FollowsKeysBuilder followTheKeys() {
		ensureAny();
		return new FollowsKeysBuilder();
	}

	public FollowsKeysBuilder followTheCursorKeys() {
		ensureAny();
		return new FollowsKeysBuilder().keys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT);
	}

	public class BouncesOnBedBuilder {

		private Bed bed;

		public BouncesOnBedBuilder bed(Bed bed) {
			this.bed = Objects.requireNonNull(bed);
			return this;
		}

		public Steering ok() {
			ensureGhost();
			ensureGhostState();
			Steering steering = new BouncingOnBed(bed != null ? bed : ghost.bed);
			ghost.setSteering(ghostState, steering);
			return steering;
		}
	}

	public class EnteringDoorAndGoingToBedBuilder {

		private Door door;
		private Bed bed;

		public EnteringDoorAndGoingToBedBuilder door(Door door) {
			this.door = Objects.requireNonNull(door);
			return this;
		}

		public EnteringDoorAndGoingToBedBuilder bed(Bed bed) {
			this.bed = Objects.requireNonNull(bed);
			return this;
		}

		public Steering ok() {
			ensureGhost();
			ensureGhostState();
			if (door == null) {
				throw new IllegalStateException(String.format("Which door should %s enter?", ghost.name));
			}
			if (bed == null) {
				throw new IllegalStateException(String.format("Which bed should %s go into", ghost.name));
			}
			Steering steering = new EnteringDoorAndGoingToBed(ghost, door, bed);
			ghost.setSteering(ghostState, steering);
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

		public Steering ok() {
			if (ghost != null) {
				ensureGhostState();
				ghost.setSteering(ghostState, new HeadingForTargetTile(fnTargetTile));
				return ghost.getSteering();
			} else if (pacMan != null) {
				pacMan.setSteering(PacManState.AWAKE, new HeadingForTargetTile(fnTargetTile));
				return pacMan.getSteering();
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

		public Steering ok() {
			ensureGhost();
			ensureGhostState();
			ghost.setSteering(ghostState, new LeavingHouse(house));
			return ghost.getSteering();
		}
	}

	public class MovesRandomlyBuilder {

		public Steering ok() {
			if (ghost != null) {
				ensureGhostState();
				ghost.setSteering(ghostState, new RandomMovement());
				return ghost.getSteering();
			} else if (pacMan != null) {
				pacMan.setSteering(PacManState.AWAKE, new RandomMovement());
				return pacMan.getSteering();
			}
			throw new IllegalStateException();
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
			if (ghost != null) {
				ensureGhostState();
				ghost.setSteering(ghostState, new FollowingKeys(up, right, down, left));
				return ghost.getSteering();
			} else if (pacMan != null) {
				pacMan.setSteering(PacManState.AWAKE, new FollowingKeys(up, right, down, left));
				return pacMan.getSteering();
			}
			throw new IllegalStateException();
		}
	}
}