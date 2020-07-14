package de.amr.games.pacman.controller.creatures;

import static de.amr.games.pacman.model.world.api.Direction.RIGHT;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.api.MobileCreature;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.FollowingKeys;
import de.amr.games.pacman.controller.steering.common.HeadingForTargetTile;
import de.amr.games.pacman.controller.steering.common.MovementControl;
import de.amr.games.pacman.controller.steering.common.MovementType;
import de.amr.games.pacman.controller.steering.common.RandomMovement;
import de.amr.games.pacman.controller.steering.common.TakingFixedPath;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.statemachine.core.StateMachine;

/**
 * An entity with a visual appearance that can move through the world and with a behavior defined by
 * a finite-state machine. The appearance is exchangeable via theming. The physical size is one tile
 * by default. The visual size however is normally larger. Depending on its state, an animal has a
 * specific steering.
 * 
 * @param <STATE> state (identifier) type
 * 
 * @author Armin Reichert
 */
public abstract class Animal<STATE> extends StateMachine<STATE, PacManGameEvent> implements MobileCreature {

	public final Entity entity = new Entity();

	protected String name;
	protected World world;
	protected Map<STATE, Steering> steeringMap;
	protected MovementControl movement;
	protected Direction moveDir;
	protected Direction wishDir;
	protected Tile targetTile;
	protected boolean enteredNewTile;
	protected Theme theme;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Animal(Class<STATE> stateClass, String name) {
		super(stateClass);
		this.name = name;
		entity.tf.width = entity.tf.height = Tile.SIZE;
		movement = new MovementControl(this);
		steeringMap = stateClass.isEnum() ? new EnumMap(stateClass) : new HashMap<>();
	}

	public String name() {
		return name;
	}

	@Override
	public Transform tf() {
		return entity.tf;
	}

	@Override
	public boolean isVisible() {
		return entity.visible;
	}

	@Override
	public void setVisible(boolean visible) {
		entity.visible = visible;
	}

	public Theme getTheme() {
		return theme;
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	/**
	 * Euclidean distance (in tiles) between this and the other animal.
	 * 
	 * @param other other animal
	 * @return Euclidean distance measured in tiles
	 */
	public double distance(Animal<?> other) {
		return location().distance(other.location());
	}

	/**
	 * @return how fast (px/s) this creature can move at most
	 */
	public float speedLimit() {
		return movement.getSpeedLimit();
	}

	public void setSpeedLimit(Supplier<Float> fnSpeedLimit) {
		movement.setSpeedLimit(fnSpeedLimit);
	}

	/**
	 * @return the current steering for this actor.
	 */
	public Steering steering() {
		return steeringMap.getOrDefault(getState(), () -> {
			// do nothing
		});
	}

	/**
	 * Returns the steering for the given state.
	 * 
	 * @param state state
	 * @return steering defined for this state
	 */
	public Steering steering(STATE state) {
		if (steeringMap.containsKey(state)) {
			return steeringMap.get(state);
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
		steeringMap.put(state, steering);
	}

	@Override
	public World world() {
		return world;
	}

	@Override
	public String toString() {
		Tile tile = location();
		return String.format("(%s, col:%d, row:%d, %s)", name, tile.col, tile.row, getState());
	}

	@Override
	public void init() {
		moveDir = wishDir = RIGHT;
		targetTile = null;
		enteredNewTile = true;
		movement.init();
		super.init();
	}

	@Override
	public void placeAt(Tile tile, float xOffset, float yOffset) {
		Tile oldTile = location();
		entity.tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !location().equals(oldTile);
	}

	public boolean isTeleporting() {
		return movement.is(MovementType.TELEPORTING);
	}

	@Override
	public Tile location() {
		Vector2f center = entity.tf.getCenter();
		int col = (int) (center.x >= 0 ? center.x / Tile.SIZE : Math.floor(center.x / Tile.SIZE));
		int row = (int) (center.y >= 0 ? center.y / Tile.SIZE : Math.floor(center.y / Tile.SIZE));
		return Tile.at(col, row);
	}

	@Override
	public boolean enteredNewTile() {
		return enteredNewTile;
	}

	public void setEnteredNewTile(boolean enteredNewTile) {
		this.enteredNewTile = enteredNewTile;
	}

	@Override
	public Direction moveDir() {
		return moveDir;
	}

	@Override
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
		Tile currentTile = location(), neighbor = world.neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		return world.isAccessible(neighbor);
	}

	@Override
	public void forceMoving(Direction dir) {
		setWishDir(dir);
		movement.update();
	}

	/**
	 * @param up    key for moving up
	 * @param right key for moving right
	 * @param down  key for moving down
	 * @param left  key for moving left
	 * 
	 * @return steering using the given keys
	 */
	public Steering followingKeys(int up, int right, int down, int left) {
		return new FollowingKeys(this, up, right, down, left);
	}

	/**
	 * Lets the actor move randomly through the maze while respecting the maze structure (for example,
	 * chasing and scattering ghost may not move upwards at dedicated tiles. Also reversing the
	 * direction is never allowed.
	 * 
	 * @return random move behavior
	 */
	public Steering movingRandomly() {
		return new RandomMovement(this);
	}

	/**
	 * Lets the actor head for a (probably unreachable) target tile by taking the "best" direction at
	 * every intersection.
	 * 
	 * @return behavior where the actor tries to reach the target tile
	 */
	public Steering headingFor(Supplier<Tile> fnTargetTile) {
		return new HeadingForTargetTile(this, fnTargetTile);
	}

	/**
	 * Lets the actor follow a fixed path to the target. As the rules for accessing tiles are not
	 * checked, the actor may get stuck.
	 * 
	 * @param path the path to follow
	 * 
	 * @return behavior where actor follows the given path
	 */
	public Steering takingFixedPath(List<Tile> path) {
		if (path.isEmpty()) {
			throw new IllegalArgumentException("Path must not be empty");
		}
		return new TakingFixedPath(this, path);
	}
}