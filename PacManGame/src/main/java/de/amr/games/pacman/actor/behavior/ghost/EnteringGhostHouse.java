package de.amr.games.pacman.actor.behavior.ghost;

import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.AT_DOOR;
import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.AT_PLACE;
import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.FALLING;
import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.MOVING_LEFT;
import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.MOVING_RIGHT;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * A state machine steering a ghost when entering the ghost house.
 * 
 * @author Armin Reichert
 */
public class EnteringGhostHouse extends StateMachine<EnteringHouseState, Void> implements Steering {

	public enum EnteringHouseState {
		AT_DOOR, FALLING, MOVING_LEFT, MOVING_RIGHT, AT_PLACE
	}

	public EnteringGhostHouse(Ghost ghost, int seatNumber) {
		super(EnteringHouseState.class);
		Tile seat = ghost.maze().ghostHouseSeats[seatNumber];
		int targetX = seat.centerX(), targetY = seat.y();
		/*@formatter:off*/
		beginStateMachine()
			.initialState(AT_DOOR)
			.description(String.format("[%s entering house]", ghost.name()))

			.states()
			
				.state(AT_DOOR)
					.onEntry(() -> {
						ghost.setTargetTile(seat); // only for visualization
					})
					
				.state(AT_PLACE)
					.onEntry(() -> {
						ghost.setWishDir(null);
					})
					
			.transitions()
	
				.when(AT_DOOR).then(FALLING)
					.act(() -> ghost.setWishDir(Direction.DOWN))
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.tf.getY() >= targetY && ghost.tf.getX() > targetX)
					.act(() -> ghost.setWishDir(Direction.LEFT))
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.tf.getY() >= targetY && ghost.tf.getX() < targetX)
					.act(() -> ghost.setWishDir(Direction.RIGHT))
	
				.when(FALLING).then(AT_PLACE)
					.condition(() -> ghost.tf.getY() >= targetY && ghost.tf.getX() == targetX)
				
				.when(MOVING_LEFT).then(AT_PLACE)
					.condition(() -> ghost.tf.getX() <= targetX)
					
				.when(MOVING_RIGHT).then(AT_PLACE)
					.condition(() -> ghost.tf.getX() >= targetX)
					
		.endStateMachine();
		/*@formatter:on*/
		setLogger(Game.FSM_LOGGER);
	}

	@Override
	public void steer() {
		if (getState() == null || is(AT_PLACE)) {
			init();
		} else {
			update();
		}
	}

	@Override
	public boolean stayOnTrack() {
		return false;
	}
}