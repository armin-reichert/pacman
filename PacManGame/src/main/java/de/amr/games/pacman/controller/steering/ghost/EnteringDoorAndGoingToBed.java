/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State.BED_REACHED;
import static de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State.FALLING;
import static de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State.MOVING_LEFT;
import static de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State.MOVING_RIGHT;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.V2f;
import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.ghost.EnteringDoorAndGoingToBed.State;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.Door;
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

	private final float targetX;
	private final float targetY;

	public EnteringDoorAndGoingToBed(Ghost ghost, Door door, Bed bed) {
		super(State.class);
		targetX = bed.center().x() - ghost.tf.width / 2;
		targetY = bed.center().y() - ghost.tf.width / 2;
		/*@formatter:off*/
		beginStateMachine()
			.initialState(FALLING)
			.description(String.format("%s going to bed", ghost.name))

			.states()
			
				.state(FALLING)
					.onEntry(() -> {
						// place the ghost centered over the ghost house entry and start falling down
						Transform tf = ghost.tf;
						Direction awayFromHouse = door.intoHouse.opposite();
						V2f houseEntry = door.center().add(awayFromHouse.vector().times(Tile.TS));
						tf.setPosition(houseEntry.x() - tf.width / 2, houseEntry.y() - tf.height / 2);
						ghost.wishDir = Direction.DOWN;					
					})
					
				// list all states such that they appear in Graphviz file
				.state(MOVING_LEFT)
				.state(MOVING_RIGHT)
				.state(BED_REACHED)
					
			.transitions()
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.tf.y >= targetY && ghost.tf.x > targetX)
					.act(() -> ghost.wishDir = Direction.LEFT)
					.annotation("Reached floor: move left")
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.tf.y >= targetY && ghost.tf.x < targetX)
					.act(() -> ghost.wishDir = Direction.RIGHT)
					.annotation("Reached floor: move right")
	
				.when(FALLING).then(BED_REACHED)
					.condition(() -> ghost.tf.y >= targetY && Math.round(ghost.tf.x) == Math.round(targetX))
					.annotation("Reached bed")
				
				.when(MOVING_LEFT).then(BED_REACHED)
					.condition(() -> ghost.tf.x <= targetX)
					.annotation("Reached bed in left tract")
					
				.when(MOVING_RIGHT).then(BED_REACHED)
					.condition(() -> ghost.tf.x >= targetX)
					.annotation("Reached bed in right tract")
					
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public void steer(Guy guy) {
		update();
	}

	@Override
	public boolean isComplete() {
		return is(BED_REACHED);
	}
}