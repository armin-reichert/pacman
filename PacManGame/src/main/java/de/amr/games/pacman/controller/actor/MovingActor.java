package de.amr.games.pacman.controller.actor;

import static de.amr.games.pacman.controller.actor.MovingActor.Movement.MOVING_INSIDE_MAZE;
import static de.amr.games.pacman.controller.actor.MovingActor.Movement.TELEPORTING;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Game.sec;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.api.Fsm;
import de.amr.statemachine.api.FsmContainer;
import de.amr.statemachine.core.StateMachine;

/**
 * Base class for actors moving through the maze and controlled by a finite-state machine.
 * 
 * @param <STATE> state identifier type
 * 
 * @author Armin Reichert
 */
public abstract class MovingActor<STATE> extends Entity implements FsmContainer<STATE, PacManGameEvent>, MazeMover {

	enum Movement {
		MOVING_INSIDE_MAZE, TELEPORTING;
	}

	public final Maze maze;
	public final String name;
	public final SpriteMap sprites = new SpriteMap();

	public BiFunction<Tile, STATE, Float> fnSpeed = (tile, state) -> 0f;

	protected Fsm<STATE, PacManGameEvent> brain;
	protected Map<STATE, Steering> steerings;
	protected StateMachine<Movement, Void> movement;
	protected Direction moveDir;
	protected Direction wishDir;
	protected Tile targetTile;
	protected boolean enteredNewTile;

	public MovingActor(Maze maze, String name) {
		this.maze = maze;
		this.name = name;
		tf.width = Tile.SIZE;
		tf.height = Tile.SIZE;
		movement = StateMachine
		//@formatter:off
			.beginStateMachine(Movement.class, Void.class)
				.description(String.format("[%s movement]", name))
				.initialState(MOVING_INSIDE_MAZE)
				.states()
					.state(MOVING_INSIDE_MAZE)
						.onTick(() -> moveInsideMaze())
					.state(TELEPORTING)
						.onEntry(() -> visible = false)
						.onExit(() -> visible = true)
				.transitions()
					.when(MOVING_INSIDE_MAZE).then(TELEPORTING)
						.condition(() -> enteredLeftPortal() || enteredRightPortal())
					.when(TELEPORTING).then(MOVING_INSIDE_MAZE)
						.onTimeout()
						.act(() -> placeAt(enteredRightPortal() ? maze.portalLeft : maze.portalRight))
			.endStateMachine();
		//@formatter:on
		setTeleportingDuration(sec(0.5f));
		movement.getTracer().setLogger(PacManStateMachineLogging.LOGGER);
	}

	private boolean enteredLeftPortal() {
		return tf.getPosition().x < maze.portalLeft.x();
	}

	private boolean enteredRightPortal() {
		return tf.getPosition().x > maze.portalRight.x();
	}

	/**
	 * @return the current steering for this actor.
	 */
	public Steering steering() {
		return steerings.get(getState());
	}

	/**
	 * Returns the steering for the given state.
	 * 
	 * @param state state
	 * @return steering defined for this state
	 */
	public Steering steering(STATE state) {
		if (steerings.containsKey(state)) {
			return steerings.get(state);
		}
		throw new IllegalArgumentException(String.format("%s: No steering found for state %s", this, state));
	}

	/**
	 * Defines the steering for the given state.
	 * 
	 * @param state    state
	 * @param steering steering defined for this state
	 */
	public void behavior(STATE state, Steering steering) {
		steerings.put(state, steering);
	}

	@Override
	public Maze maze() {
		return maze;
	}

	@Override
	public String toString() {
		return String.format("(%s, col:%d, row:%d, %s)", name, tile().col, tile().row, getState());
	}

	@Override
	public Fsm<STATE, PacManGameEvent> fsm() {
		return brain;
	}

	@Override
	public void init() {
		moveDir = wishDir = RIGHT;
		targetTile = null;
		enteredNewTile = true;
		brain.init();
		movement.init();
	}

	public void placeAt(Tile tile) {
		placeAt(tile, 0, 0);
	}

	public void placeAt(Tile tile, float xOffset, float yOffset) {
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !tile.equals(tile());
	}

	public boolean isTeleporting() {
		return movement.is(TELEPORTING);
	}

	public void setTeleportingDuration(int ticks) {
		movement.state(TELEPORTING).setTimer(ticks);
	}

	@Override
	public Tile tile() {
		Vector2f center = tf.getCenter();
		return new Tile(center.roundedX() / Tile.SIZE, center.roundedY() / Tile.SIZE);
	}

	@Override
	public boolean enteredNewTile() {
		return enteredNewTile;
	}

	@Override
	public Direction moveDir() {
		return moveDir;
	}

	public void setMoveDir(Direction dir) {
		moveDir = Objects.requireNonNull(dir);
	}

	@Override
	public Direction wishDir() {
		return wishDir;
	}

	@Override
	public void setWishDir(Direction dir) {
		wishDir = dir;
	}

	@Override
	public Tile targetTile() {
		return targetTile;
	}

	@Override
	public void setTargetTile(Tile tile) {
		targetTile = tile;
	}

	@Override
	public boolean canCrossBorderTo(Direction dir) {
		Tile currentTile = tile(), neighbor = maze.neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze.isWall(neighbor)) {
			return false;
		}
		if (maze.isTunnel(neighbor)) {
			return true; // includes portal tiles
		}
		return maze.insideBoard(neighbor);
	}

	@Override
	public void forceMoving(Direction dir) {
		setWishDir(dir);
		movement.update();
	}

	@Override
	public void forceTurningBack() {
		forceMoving(moveDir().opposite());
	}

	/**
	 * Computes how many pixels this entity can move towards the given direction without entering an
	 * inaccessible neighbor tile.
	 * 
	 * @param tile tile from where to move
	 * @param dir  move direction
	 */
	private float maxMoveDistance(Tile tile, Direction dir) {
		float speed = fnSpeed.apply(tile, getState());
		if (canCrossBorderTo(dir)) {
			return speed;
		}
		float offsetX = tf.x - tile.x(), offsetY = tf.y - tile.y();
		switch (dir) {
		case UP:
			return Math.min(offsetY, speed);
		case DOWN:
			return Math.min(-offsetY, speed);
		case LEFT:
			return Math.min(offsetX, speed);
		case RIGHT:
			return Math.min(-offsetX, speed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}

	private void moveInsideMaze() {
		Tile tile = tile();
		float speed = maxMoveDistance(tile, moveDir);
		if (wishDir != null && wishDir != moveDir) {
			float wishDirSpeed = maxMoveDistance(tile, wishDir);
			if (wishDirSpeed > 0) {
				boolean corner = (wishDir == moveDir.left() || wishDir == moveDir.right());
				if (corner && steering().requiresGridAlignment()) {
					placeAt(tile);
				}
				moveDir = wishDir;
				speed = wishDirSpeed;
			}
		}
		tf.setVelocity(Vector2f.smul(speed, moveDir.vector()));
		tf.move();
		enteredNewTile = !tile.equals(tile());
	}
}