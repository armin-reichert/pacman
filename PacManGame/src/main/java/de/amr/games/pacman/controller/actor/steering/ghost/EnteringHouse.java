package de.amr.games.pacman.controller.actor.steering.ghost;

import static de.amr.games.pacman.controller.actor.steering.ghost.EnteringHouse.State.FALLING;
import static de.amr.games.pacman.controller.actor.steering.ghost.EnteringHouse.State.MOVING_LEFT;
import static de.amr.games.pacman.controller.actor.steering.ghost.EnteringHouse.State.MOVING_RIGHT;
import static de.amr.games.pacman.controller.actor.steering.ghost.EnteringHouse.State.TARGET_REACHED;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.controller.actor.steering.ghost.EnteringHouse.State;
import de.amr.statemachine.core.StateMachine;

/**
 * A state machine steering a ghost standing at the ghost house door into the house.
 * 
 * @author Armin Reichert
 */
public class EnteringHouse extends StateMachine<State, Void> implements Steering {

	public enum State {
		FALLING, MOVING_LEFT, MOVING_RIGHT, TARGET_REACHED
	}

	/**
	 * Creates a steering for a ghost to enter the house.
	 * 
	 * @param ghost  ghost that wants to enter the house
	 * @param target target position inside house
	 */
	public EnteringHouse(Ghost ghost, Vector2f target) {
		super(State.class);
		/*@formatter:off*/
		beginStateMachine()
			.initialState(FALLING)
			.description(String.format("[%s entering house]", ghost.name))

			.states()
			
				.state(FALLING)
					.onEntry(() -> {
						// place the ghost exactly at the ghost house entry and start falling down
						ghost.tf.setPosition(ghost.maze.ghostSeats[0].position);
						ghost.setWishDir(DOWN);					})
					
			.transitions()
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.tf.y >= target.y && ghost.tf.x > target.x)
					.act(() -> ghost.setWishDir(LEFT))
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.tf.y >= target.y && ghost.tf.x < target.x)
					.act(() -> ghost.setWishDir(RIGHT))
	
				.when(FALLING).then(TARGET_REACHED)
					.condition(() -> ghost.tf.y >= target.y && ghost.tf.x == target.x)
				
				.when(MOVING_LEFT).then(TARGET_REACHED)
					.condition(() -> ghost.tf.x <= target.x)
					
				.when(MOVING_RIGHT).then(TARGET_REACHED)
					.condition(() -> ghost.tf.x >= target.x)
					
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