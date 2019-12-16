package de.amr.games.pacman.actor.behavior.ghost;

import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.AT_DOOR;
import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.FALLING;
import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.MOVING_LEFT;
import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.MOVING_RIGHT;
import static de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState.AT_PLACE;

import java.util.Collections;

import de.amr.easy.game.Application;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.behavior.ghost.EnteringGhostHouse.EnteringHouseState;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.StateMachine;

/**
 * A state machine steering a ghost when entering the ghost house.
 * 
 * @author Armin Reichert
 */
public class EnteringGhostHouse extends StateMachine<EnteringHouseState, Void> implements Steering<Ghost> {

	public enum EnteringHouseState {
		AT_DOOR, FALLING, MOVING_LEFT, MOVING_RIGHT, AT_PLACE
	}

	public EnteringGhostHouse(Maze maze, Ghost ghost, int place) {
		super(EnteringHouseState.class);
		int targetX = maze.ghostHome[place].col * Tile.SIZE + Tile.SIZE / 2;
		int targetY = maze.ghostHome[place].row * Tile.SIZE;
		/*@formatter:off*/
		beginStateMachine()
			.initialState(AT_DOOR)
			.description(String.format("[%s entering house]", ghost.name()))

			.states()
			
				.state(AT_DOOR)
					.onEntry(() -> {
						ghost.setTargetTile(maze.ghostHome[place]); // only for visualization
						ghost.setTargetPath(Collections.emptyList());
					})
					
				.state(AT_PLACE)
					.onEntry(() -> {
						ghost.setNextDir(null);
					})
					
			.transitions()
	
				.when(AT_DOOR).then(FALLING)
					.act(() -> ghost.setNextDir(Direction.DOWN))
	
				.when(FALLING).then(MOVING_LEFT)
					.condition(() -> ghost.tf.getY() >= targetY && ghost.tf.getX() > targetX)
					.act(() -> ghost.setNextDir(Direction.LEFT))
				
				.when(FALLING).then(MOVING_RIGHT)
					.condition(() -> ghost.tf.getY() >= targetY && ghost.tf.getX() < targetX)
					.act(() -> ghost.setNextDir(Direction.RIGHT))
	
				.when(FALLING).then(AT_PLACE)
					.condition(() -> ghost.tf.getY() >= targetY && ghost.tf.getX() == targetX)
				
				.when(MOVING_LEFT).then(AT_PLACE)
					.condition(() -> ghost.tf.getX() <= targetX)
					
				.when(MOVING_RIGHT).then(AT_PLACE)
					.condition(() -> ghost.tf.getX() >= targetX)
					
		.endStateMachine();
		/*@formatter:on*/
		traceTo(Application.LOGGER, () -> 60);
	}

	@Override
	public void steer(Ghost ghost) {
		if (getState() == null || is(AT_PLACE)) {
			init();
		}
		else {
			update();
		}
	}
}