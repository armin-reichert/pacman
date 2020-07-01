package de.amr.games.pacman.controller.actor.steering.ghost;

import static de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State.FALLING;
import static de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State.MOVING_LEFT;
import static de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State.MOVING_RIGHT;
import static de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State.TARGET_REACHED;

import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.statemachine.core.StateMachine;

/**
 * A state machine steering a ghost standing at the ghost house door into the house.
 * 
 * @author Armin Reichert
 */
public class GoingToBed extends StateMachine<State, Void> implements Steering {

	public enum State {
		FALLING, MOVING_LEFT, MOVING_RIGHT, TARGET_REACHED
	}

	public GoingToBed(Ghost ghost, Bed bed) {
		super(State.class);
		int offsetY = 3;
		/*@formatter:off*/
		beginStateMachine()
			.initialState(FALLING)
			.description(String.format("[%s entering house]", ghost.name))

			.states()
			
				.state(FALLING)
					.onEntry(() -> {
						// place the ghost exactly at the ghost house entry and start falling down
						ghost.tf.setPosition(ghost.world().theHouse().bed(0).position);
						ghost.setWishDir(Direction.DOWN);					
					})
					
			.transitions()
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.tf.y >= bed.position.y + offsetY && ghost.tf.x > bed.position.x)
					.act(() -> ghost.setWishDir(Direction.LEFT))
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.tf.y >= bed.position.y + offsetY && ghost.tf.x < bed.position.x)
					.act(() -> ghost.setWishDir(Direction.RIGHT))
	
				.when(FALLING).then(TARGET_REACHED)
					.condition(() -> ghost.tf.y >= bed.position.y + offsetY && ghost.tf.x == bed.position.x)
				
				.when(MOVING_LEFT).then(TARGET_REACHED)
					.condition(() -> ghost.tf.x <= bed.position.x)
					
				.when(MOVING_RIGHT).then(TARGET_REACHED)
					.condition(() -> ghost.tf.x >= bed.position.x)
					
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