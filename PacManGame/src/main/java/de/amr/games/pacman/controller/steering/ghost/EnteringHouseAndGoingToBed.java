package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed.State.BED_REACHED;
import static de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed.State.FALLING;
import static de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed.State.MOVING_LEFT;
import static de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed.State.MOVING_RIGHT;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.ghost.EnteringHouseAndGoingToBed.State;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.statemachine.core.StateMachine;

/**
 * A state machine steering a ghost standing at the ghost house door into its bed inside the house.
 * 
 * @author Armin Reichert
 */
public class EnteringHouseAndGoingToBed extends StateMachine<State, Void> implements Steering<Ghost> {

	public enum State {
		FALLING, MOVING_LEFT, MOVING_RIGHT, BED_REACHED
	}

	private float targetX(Bed bed) {
		return bed.center().x - Tile.SIZE / 2;
	}

	private float targetY(Bed bed) {
		return bed.center().y - Tile.SIZE / 2;
	}

	public EnteringHouseAndGoingToBed(Ghost ghost, Bed bed) {
		super(State.class);
		/*@formatter:off*/
		beginStateMachine()
			.initialState(FALLING)
			.description(String.format("%s entering house", ghost.name))

			.states()
			
				.state(FALLING)
					.onEntry(() -> {
						// place the ghost centered over the ghost house entry and start falling
						Vector2f houseEntry = ghost.world().house(0).bed(0).center(); 
						ghost.entity.tf.setPosition(houseEntry.x - Tile.SIZE / 2, houseEntry.y - Tile.SIZE / 2);
						ghost.setWishDir(Direction.DOWN);					
					})
					
				// listen all states such that they appear in Graphviz file
				.state(MOVING_LEFT)
				.state(MOVING_RIGHT)
				.state(BED_REACHED)
					
			.transitions()
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.entity.tf.y >= targetY(bed) && ghost.entity.tf.x > targetX(bed))
					.act(() -> ghost.setWishDir(Direction.LEFT))
					.annotation("Reached ghost house floor")
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.entity.tf.y >= targetY(bed) && ghost.entity.tf.x < targetX(bed))
					.act(() -> ghost.setWishDir(Direction.RIGHT))
					.annotation("Reached ghost house floor")
	
				.when(FALLING).then(BED_REACHED)
					.condition(() -> ghost.entity.tf.y >= targetY(bed) && ghost.entity.tf.x == targetX(bed))
					.annotation("Reached bed inside ghost house")
				
				.when(MOVING_LEFT).then(BED_REACHED)
					.condition(() -> ghost.entity.tf.x <= targetX(bed))
					.annotation("Reached bed inside ghost house")
					
				.when(MOVING_RIGHT).then(BED_REACHED)
					.condition(() -> ghost.entity.tf.x >= targetX(bed))
					.annotation("Reached bed inside ghost house")
					
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public void steer(Ghost ghost) {
		update();
	}

	@Override
	public boolean isComplete() {
		return is(BED_REACHED);
	}
}