package de.amr.games.pacman.controller.actor.steering.ghost;

import static de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State.FALLING;
import static de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State.MOVING_LEFT;
import static de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State.MOVING_RIGHT;
import static de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State.TARGET_REACHED;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.controller.actor.steering.ghost.GoingToBed.State;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.Tile;
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

	private float targetX(Bed bed) {
		return bed.center.x - Tile.SIZE / 2;
	}

	private float targetY(Bed bed) {
		return bed.center.y - Tile.SIZE / 2;
	}

	public GoingToBed(Ghost ghost, Bed bed) {
		super(State.class);
		/*@formatter:off*/
		beginStateMachine()
			.initialState(FALLING)
			.description(String.format("[%s entering house]", ghost.name()))

			.states()
			
				.state(FALLING)
					.onEntry(() -> {
						// place the ghost centered over the ghost house entry and start falling
						Vector2f houseEntry = ghost.world().theHouse().bed(0).center; 
						ghost.entity.tf.setPosition(houseEntry.x - Tile.SIZE / 2, houseEntry.y - Tile.SIZE / 2);
						ghost.setWishDir(Direction.DOWN);					
					})
					
			.transitions()
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.entity.tf.y >= targetY(bed) && ghost.entity.tf.x > targetX(bed))
					.act(() -> ghost.setWishDir(Direction.LEFT))
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.entity.tf.y >= targetY(bed) && ghost.entity.tf.x < targetX(bed))
					.act(() -> ghost.setWishDir(Direction.RIGHT))
	
				.when(FALLING).then(TARGET_REACHED)
					.condition(() -> ghost.entity.tf.y >= targetY(bed) && ghost.entity.tf.x == targetX(bed))
				
				.when(MOVING_LEFT).then(TARGET_REACHED)
					.condition(() -> ghost.entity.tf.x <= targetX(bed))
					
				.when(MOVING_RIGHT).then(TARGET_REACHED)
					.condition(() -> ghost.entity.tf.x >= targetX(bed))
					
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