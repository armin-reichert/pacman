package de.amr.games.pacman.actor.steering.ghost;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.steering.common.SteerableMazeMover;
import de.amr.games.pacman.actor.steering.core.Steering;
import de.amr.games.pacman.model.MazeMoving;

/**
 * Interface with ghost-specific steerings that can be used by any ghost.
 * 
 * @author Armin Reichert
 */
public interface SteerableGhost extends SteerableMazeMover {

	default Ghost thisGhost() {
		return (Ghost) this;
	}

	/**
	 * Lets the ghost jump up and down at its own seat in the house.
	 * 
	 * @return behavior which lets the ghost jump
	 */
	default Steering isJumpingUpAndDown(Vector2f seatPosition) {
		return new JumpingUpAndDown(thisGhost(), seatPosition.y);
	}

	/**
	 * Lets the actor avoid the attacker's path by walking to a "safe" maze corner.
	 * 
	 * @param attacker the attacking actor
	 * 
	 * @return behavior where actor flees to a "safe" maze corner
	 */
	default Steering isFleeingToSafeCorner(MazeMoving attacker) {
		return new FleeingToSafeCorner(thisGhost(), attacker);
	}

	/**
	 * Lets a ghost enter the ghost house and move to the seat with the given
	 * number.
	 * 
	 * @param seat seat number
	 * 
	 * @return behavior which lets a ghost enter the house and take its seat
	 */
	default Steering isTakingSeat(Vector2f seatPosition) {
		return new EnteringGhostHouse(thisGhost(), seatPosition);
	}

	/**
	 * Lets a ghost leave the ghost house.
	 * 
	 * @return behavior which lets a ghost leave the ghost house
	 */
	default Steering isLeavingGhostHouse() {
		return new LeavingGhostHouse(thisGhost());
	}
}