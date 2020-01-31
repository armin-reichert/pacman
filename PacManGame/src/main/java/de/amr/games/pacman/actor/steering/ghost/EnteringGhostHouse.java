package de.amr.games.pacman.actor.steering.ghost;

import static de.amr.games.pacman.actor.steering.ghost.EnteringGhostHouse.EnteringHouseState.AT_DOOR;
import static de.amr.games.pacman.actor.steering.ghost.EnteringGhostHouse.EnteringHouseState.TARGET_REACHED;
import static de.amr.games.pacman.actor.steering.ghost.EnteringGhostHouse.EnteringHouseState.FALLING;
import static de.amr.games.pacman.actor.steering.ghost.EnteringGhostHouse.EnteringHouseState.MOVING_LEFT;
import static de.amr.games.pacman.actor.steering.ghost.EnteringGhostHouse.EnteringHouseState.MOVING_RIGHT;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.actor.steering.ghost.EnteringGhostHouse.EnteringHouseState;
import de.amr.games.pacman.model.Game;
import de.amr.statemachine.core.StateMachine;

/**
 * A state machine steering a ghost when entering the ghost house.
 * 
 * @author Armin Reichert
 */
public class EnteringGhostHouse extends StateMachine<EnteringHouseState, Void> implements Steering {

	public enum EnteringHouseState {
		AT_DOOR, FALLING, MOVING_LEFT, MOVING_RIGHT, TARGET_REACHED
	}

	public EnteringGhostHouse(Ghost ghost, Vector2f target) {
		super(EnteringHouseState.class);
		/*@formatter:off*/
		beginStateMachine()
			.initialState(AT_DOOR)
			.description(String.format("[%s entering house]", ghost.name()))

			.states()
			
				.state(AT_DOOR)
					.onEntry(() -> {
						// target tile is only used for route visualization
						ghost.setTargetTile(ghost.maze().tileAt(target.roundedX(), target.roundedY()));
						ghost.setWishDir(DOWN);
					})
					
			.transitions()
	
				.when(AT_DOOR).then(FALLING)
					.act(() -> ghost.setWishDir(DOWN))
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.tf.getY() >= target.y && ghost.tf.getX() > target.x)
					.act(() -> ghost.setWishDir(LEFT))
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.tf.getY() >= target.y && ghost.tf.getX() < target.x)
					.act(() -> ghost.setWishDir(RIGHT))
	
				.when(FALLING).then(TARGET_REACHED)
					.condition(() -> ghost.tf.getY() >= target.y && ghost.tf.getX() == target.x)
				
				.when(MOVING_LEFT).then(TARGET_REACHED)
					.condition(() -> ghost.tf.getX() <= target.x)
					
				.when(MOVING_RIGHT).then(TARGET_REACHED)
					.condition(() -> ghost.tf.getX() >= target.x)
					
		.endStateMachine();
		/*@formatter:on*/
		getTracer().setLogger(Game.FSM_LOGGER);
	}

	@Override
	public void steer() {
		update();
	}

	@Override
	public boolean isComplete() {
		return is(TARGET_REACHED);
	}

	@Override
	public boolean requiresGridAlignment() {
		return false;
	}
}