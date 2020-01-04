package de.amr.games.pacman.actor.core;

import static de.amr.games.pacman.actor.core.MazeMovement.MazeMovementState.MOVING;
import static de.amr.games.pacman.actor.core.MazeMovement.MazeMovementState.TELEPORTING;

import de.amr.games.pacman.actor.core.MazeMovement.MazeMovementState;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.core.StateMachine;

public class MazeMovement extends StateMachine<MazeMovementState, Void> {

	public enum MazeMovementState {
		MOVING, TELEPORTING
	}

	AbstractMazeMover mover;
	Maze maze;
	Tile rightPortal;
	Tile leftPortal;

	public MazeMovement() {
		super(MazeMovementState.class);
		leftPortal = maze.tileToDir(maze.tunnelExitLeft, Direction.LEFT);
		rightPortal = maze.tileToDir(maze.tunnelExitRight, Direction.RIGHT);
		//@formatter:off
		beginStateMachine()
		.description("MazeMovement")
		.initialState(MOVING)
		.states()
			.state(MOVING)
				.onTick(mover::step)
			.state(TELEPORTING)
				.onEntry(mover::hide)
				.onExit(mover::show)
		.transitions()
			.when(MOVING).then(TELEPORTING)
				.condition(this::enteredPortal)
				.act(this::placeAtExitTile)
			.when(TELEPORTING).then(MOVING)
				.onTimeout()
		.endStateMachine();
		//@formatter:on
	}

	boolean enteredPortal() {
		return mover.enteredNewTile() && isPortal(mover.tile());
	}

	boolean isPortal(Tile tile) {
		return tile.equals(leftPortal) || tile.equals(rightPortal);
	}

	void placeAtExitTile() {
		Tile tile = mover.tile();
		if (tile.equals(leftPortal)) {
			mover.placeAt(rightPortal);
		} else if (tile.equals(rightPortal)) {
			mover.placeAt(leftPortal);
		}
	}
}