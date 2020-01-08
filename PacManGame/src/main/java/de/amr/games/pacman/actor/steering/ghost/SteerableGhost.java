package de.amr.games.pacman.actor.steering.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.actor.steering.core.Steering;

/**
 * Interface with ghost-specific steerings that can be used by any ghost.
 * 
 * @author Armin Reichert
 */
public interface SteerableGhost {

	default Ghost thisGhost() {
		return (Ghost) this;
	}

	/**
	 * Lets the ghost jump up and down at its own seat in the house.
	 * 
	 * @return behavior which lets the ghost jump
	 */
	default Steering isJumpingUpAndDown() {
		return new JumpingUpAndDown(thisGhost(), thisGhost().seat());
	}

	/**
	 * Lets the actor avoid the attacker's path by walking to a "safe" maze corner.
	 * 
	 * @param attacker the attacking actor
	 * 
	 * @return behavior where actor flees to a "safe" maze corner
	 */
	default Steering isFleeingToSafeCorner(MazeMover attacker) {
		return new FleeingToSafeCorner(thisGhost(), attacker);
	}

	/**
	 * Lets a ghost enter the ghost house and move to its seat.
	 * 
	 * @return behavior which lets a ghost enter the house and take its seat
	 */
	default Steering isTakingSeat() {
		return new EnteringGhostHouse(thisGhost(), thisGhost().seat());
	}

	/**
	 * Lets a ghost enter the ghost house and move to the seat with the given
	 * number.
	 * 
	 * @param seat seat number
	 * 
	 * @return behavior which lets a ghost enter the house and take its seat
	 */
	default Steering isTakingSeat(int seat) {
		return new EnteringGhostHouse(thisGhost(), seat);
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