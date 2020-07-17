package de.amr.games.pacman.controller.creatures.api;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import de.amr.easy.game.entity.Entity;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.Movement;
import de.amr.games.pacman.controller.steering.common.MovementType;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.statemachine.core.StateMachine;

/**
 * A creature can move through the world and has "intelligence", i.e. it can make decisions changing
 * its behavior.
 * <p>
 * The physical size is one tile, however the visual appearance may be larger.
 * 
 * @param <STATE> state (identifier) type
 * 
 * @author Armin Reichert
 */
public abstract class Creature<STATE> extends StateMachine<STATE, PacManGameEvent> implements MobileLifeform {

	public final Entity entity;
	public final String name;

	protected final Map<STATE, Steering> steeringsByState;
	protected final Movement movement;

	protected Theme theme;
	protected Tile targetTile;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Creature(Class<STATE> stateClass, World world, String name) {
		super(stateClass);
		this.name = name;
		entity = new Entity();
		entity.tf.width = entity.tf.height = Tile.SIZE;
		movement = new Movement(world, this, entity.tf, name);
		steeringsByState = stateClass.isEnum() ? new EnumMap(stateClass) : new HashMap<>();
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

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	public abstract IRenderer renderer();

	/**
	 * Euclidean distance (in tiles) between this and the other animal.
	 * 
	 * @param other other animal
	 * @return Euclidean distance measured in tiles
	 */
	public double distance(Creature<?> other) {
		return tileLocation().distance(other.tileLocation());
	}

	/**
	 * @return upper bound (px/s) for this creatures' speed
	 */
	public float speedLimit() {
		return movement.fnSpeedLimit.get();
	}

	/**
	 * @param fnSpeedLimit function providing the speed limit
	 */
	public void setSpeedLimit(Supplier<Float> fnSpeedLimit) {
		movement.fnSpeedLimit = fnSpeedLimit;
	}

	/**
	 * @return the current steering for this actor.
	 */
	public Steering steering() {
		return steeringsByState.getOrDefault(getState(), () -> {
		});
	}

	/**
	 * Returns the steering for the given state.
	 * 
	 * @param state state
	 * @return steering defined for this state
	 */
	public Steering steering(STATE state) {
		if (steeringsByState.containsKey(state)) {
			return steeringsByState.get(state);
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
		steeringsByState.put(state, steering);
	}

	@Override
	public boolean requiresGridAlignment() {
		return steering().requiresGridAlignment();
	}

	@Override
	public String toString() {
		Tile tile = tileLocation();
		return String.format("(%s, col:%d, row:%d, %s)", name, tile.col, tile.row, getState());
	}

	@Override
	public void init() {
		targetTile = null;
		movement.init();
		super.init();
	}

	@Override
	public void placeAt(Tile tile, float xOffset, float yOffset) {
		movement.placeCreatureAt(tile, xOffset, yOffset);
	}

	public boolean isTeleporting() {
		return movement.is(MovementType.TELEPORTING);
	}

	@Override
	public Tile tileLocation() {
		return movement.currentTile();
	}

	@Override
	public boolean enteredNewTile() {
		return movement.enteredNewTile;
	}

	@Override
	public Direction moveDir() {
		return movement.moveDir;
	}

	@Override
	public void setMoveDir(Direction dir) {
		movement.moveDir = Objects.requireNonNull(dir);
	}

	@Override
	public Direction wishDir() {
		return movement.wishDir;
	}

	@Override
	public void setWishDir(Direction dir) {
		movement.wishDir = dir;
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
		Tile currentTile = tileLocation(), neighbor = world().neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		return world().isAccessible(neighbor);
	}

	@Override
	public void forceMoving(Direction dir) {
		setWishDir(dir);
		movement.update();
	}
}