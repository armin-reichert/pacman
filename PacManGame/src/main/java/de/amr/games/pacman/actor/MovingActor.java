package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.MovingActor.Movement.MOVING_INSIDE_MAZE;
import static de.amr.games.pacman.actor.MovingActor.Movement.TELEPORTING;
import static de.amr.games.pacman.model.Direction.RIGHT;

import java.util.Map;
import java.util.Objects;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.model.Timing;
import de.amr.statemachine.api.Fsm;
import de.amr.statemachine.api.FsmContainer;
import de.amr.statemachine.core.StateMachine;

/**
 * Base class for actors moving through the maze and controlled by a
 * finite-state machine.
 * 
 * @param <STATE> state identifier type
 * 
 * @author Armin Reichert
 */
public abstract class MovingActor<STATE> extends Entity implements FsmContainer<STATE, PacManGameEvent>, MazeMover {

	enum Movement {
		MOVING_INSIDE_MAZE, TELEPORTING;
	}

	public final Game game;
	public final String name;
	public final SpriteMap sprites = new SpriteMap();

	protected Fsm<STATE, PacManGameEvent> brain;
	protected Map<STATE, Steering> steerings;
	protected StateMachine<Movement, ?> movement;
	protected Direction moveDir;
	protected Direction wishDir;
	protected Tile targetTile;
	protected boolean enteredNewTile;

	public MovingActor(Game game, String name) {
		this.game = game;
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
						.act(() -> teleport())
			.endStateMachine();
		//@formatter:on
		setTeleportingDuration(Timing.sec(0.5f));
		movement.getTracer().setLogger(PacManStateMachineLogging.LOG);
	}

	/**
	 * @return the current steering for this actor depending on its state etc.
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

	/**
	 * @param tile in the maze
	 * @parm state actor state
	 * @return the actor's speed at this tile in this state (in pixels/tick)
	 */
	public abstract float speed(Tile tile, STATE state);

	@Override
	public Maze maze() {
		return game.maze;
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
		Tile currentTile = tile(), neighbor = maze().neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze().isWall(neighbor)) {
			return false;
		}
		if (maze().isTunnel(neighbor)) {
			return true; // includes portal tiles
		}
		return maze().insideBoard(neighbor);
	}

	protected void forceMoving(Direction dir) {
		setWishDir(dir);
		movement.update();
	}

	protected void forceTurningBack() {
		forceMoving(moveDir().opposite());
	}

	/**
	 * Computes how many pixels this entity can move towards the given direction
	 * without entering an inaccessible neighbor tile.
	 * 
	 * @param tile tile from where to move
	 * @param dir  move direction
	 */
	private float maxMoveDistance(Tile tile, Direction dir) {
		if (canCrossBorderTo(dir)) {
			return speed(tile, getState());
		}
		float offsetX = tf.x - tile.x(), offsetY = tf.y - tile.y();
		switch (dir) {
		case UP:
			return Math.min(offsetY, speed(tile, getState()));
		case DOWN:
			return Math.min(-offsetY, speed(tile, getState()));
		case LEFT:
			return Math.min(offsetX, speed(tile, getState()));
		case RIGHT:
			return Math.min(-offsetX, speed(tile, getState()));
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

	private void teleport() {
		if (enteredRightPortal()) {
			placeAt(maze().portalLeft);
		} else if (enteredLeftPortal()) {
			placeAt(maze().portalRight);
		}
	}

	private boolean enteredLeftPortal() {
		return tf.getPosition().x < maze().portalLeft.x();
	}

	private boolean enteredRightPortal() {
		return tf.getPosition().x > maze().portalRight.x();
	}
}