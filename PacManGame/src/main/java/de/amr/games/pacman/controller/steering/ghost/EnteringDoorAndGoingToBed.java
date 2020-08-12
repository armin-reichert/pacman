package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State.BED_REACHED;
import static de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State.FALLING;
import static de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State.MOVING_LEFT;
import static de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State.MOVING_RIGHT;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.core.MobileLifeform;
import de.amr.statemachine.core.StateMachine;

/**
 * Steers a ghost standing at the ghost house door into its bed inside the house.
 * 
 * @author Armin Reichert
 */
public class EnteringDoorAndGoingToBed extends StateMachine<State, Void> implements Steering {

	public enum State {
		FALLING, MOVING_LEFT, MOVING_RIGHT, BED_REACHED
	}

	private final float targetX, targetY;

	public EnteringDoorAndGoingToBed(Ghost ghost, Door door, Bed bed) {
		super(State.class);
		targetX = bed.center().x - ghost.entity.tf.width / 2;
		targetY = bed.center().y - ghost.entity.tf.width / 2;
		/*@formatter:off*/
		beginStateMachine()
			.initialState(FALLING)
			.description(String.format("%s going to bed", ghost.name))

			.states()
			
				.state(FALLING)
					.onEntry(() -> {
						// place the ghost centered over the ghost house entry and start falling down
						Transform tf = ghost.entity.tf;
						Direction awayFromHouse = door.intoHouse.opposite();
						Vector2f houseEntry = door.center().add(awayFromHouse.vector().times(Tile.SIZE));
						tf.setPosition(houseEntry.x - tf.width / 2, houseEntry.y - tf.height / 2);
						ghost.entity.wishDir = Direction.DOWN;					
					})
					
				// list all states such that they appear in Graphviz file
				.state(MOVING_LEFT)
				.state(MOVING_RIGHT)
				.state(BED_REACHED)
					
			.transitions()
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.entity.tf.y >= targetY && ghost.entity.tf.x > targetX)
					.act(() -> ghost.entity.wishDir = Direction.LEFT)
					.annotation("Reached floor: move left")
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.entity.tf.y >= targetY && ghost.entity.tf.x < targetX)
					.act(() -> ghost.entity.wishDir = Direction.RIGHT)
					.annotation("Reached floor: move right")
	
				.when(FALLING).then(BED_REACHED)
					.condition(() -> ghost.entity.tf.y >= targetY && ghost.entity.tf.x == targetX)
					.annotation("Reached bed")
				
				.when(MOVING_LEFT).then(BED_REACHED)
					.condition(() -> ghost.entity.tf.x <= targetX)
					.annotation("Reached bed in left tract")
					
				.when(MOVING_RIGHT).then(BED_REACHED)
					.condition(() -> ghost.entity.tf.x >= targetX)
					.annotation("Reached bed in right tract")
					
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public void steer(MobileLifeform entity) {
		update();
	}

	@Override
	public boolean isComplete() {
		return is(BED_REACHED);
	}
}