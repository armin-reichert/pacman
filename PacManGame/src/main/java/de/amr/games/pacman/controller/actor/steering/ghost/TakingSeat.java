package de.amr.games.pacman.controller.actor.steering.ghost;

import static de.amr.games.pacman.controller.actor.steering.ghost.TakingSeat.State.FALLING;
import static de.amr.games.pacman.controller.actor.steering.ghost.TakingSeat.State.MOVING_LEFT;
import static de.amr.games.pacman.controller.actor.steering.ghost.TakingSeat.State.MOVING_RIGHT;
import static de.amr.games.pacman.controller.actor.steering.ghost.TakingSeat.State.TARGET_REACHED;

import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.controller.actor.steering.ghost.TakingSeat.State;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.Bed;
import de.amr.statemachine.core.StateMachine;

/**
 * A state machine steering a ghost standing at the ghost house door into the house.
 * 
 * @author Armin Reichert
 */
public class TakingSeat extends StateMachine<State, Void> implements Steering {

	public enum State {
		FALLING, MOVING_LEFT, MOVING_RIGHT, TARGET_REACHED
	}

	public TakingSeat(Ghost ghost, Bed seat) {
		super(State.class);
		House theHouse = ghost.world().houses().findAny().get();
		int offsetY = 3;
		/*@formatter:off*/
		beginStateMachine()
			.initialState(FALLING)
			.description(String.format("[%s entering house]", ghost.name))

			.states()
			
				.state(FALLING)
					.onEntry(() -> {
						// place the ghost exactly at the ghost house entry and start falling down
						ghost.tf.setPosition(theHouse.seat(0).position);
						ghost.setWishDir(Direction.DOWN);					
					})
					
			.transitions()
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.tf.y >= seat.position.y + offsetY && ghost.tf.x > seat.position.x)
					.act(() -> ghost.setWishDir(Direction.LEFT))
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.tf.y >= seat.position.y + offsetY && ghost.tf.x < seat.position.x)
					.act(() -> ghost.setWishDir(Direction.RIGHT))
	
				.when(FALLING).then(TARGET_REACHED)
					.condition(() -> ghost.tf.y >= seat.position.y + offsetY && ghost.tf.x == seat.position.x)
				
				.when(MOVING_LEFT).then(TARGET_REACHED)
					.condition(() -> ghost.tf.x <= seat.position.x)
					
				.when(MOVING_RIGHT).then(TARGET_REACHED)
					.condition(() -> ghost.tf.x >= seat.position.x)
					
		.endStateMachine();
		/*@formatter:on*/
		getTracer().setLogger(PacManStateMachineLogging.LOGGER);
	}

	@Override
	public void steer() {
		update();
	}

	@Override
	public boolean isComplete() {
		return is(TARGET_REACHED);
	}
}