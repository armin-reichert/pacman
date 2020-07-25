package de.amr.games.pacman.controller.creatures.api;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.api.TargetTileSteering;
import de.amr.games.pacman.controller.steering.common.Movement;
import de.amr.games.pacman.controller.steering.common.MovementType;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.Themeable;
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
public abstract class Creature<M extends MobileLifeform, STATE> extends StateMachine<STATE, PacManGameEvent>
		implements MobileLifeform, Themeable, View {

	public final String name;
	public final Entity entity;

	protected final Map<STATE, Steering<M>> steeringsByState;
	protected final Movement movement;

	protected Theme theme;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Creature(Class<STATE> stateClass, World world, String name) {
		super(stateClass);
		this.name = name;
		steeringsByState = stateClass.isEnum() ? new EnumMap(stateClass) : new HashMap<>();
		movement = new Movement(this, name);
		entity = new Entity();
		entity.tf.width = entity.tf.height = Tile.SIZE;
	}

	public Stream<StateMachine<?, ?>> machines() {
		return Stream.of(this, movement);
	}

	@Override
	public boolean isVisible() {
		return entity.visible;
	}

	@Override
	public void setVisible(boolean visible) {
		entity.visible = visible;
	}

	@Override
	public Theme getTheme() {
		return theme;
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	/**
	 * @return this creatures' current speed (pixels/sec)
	 */
	public float speed() {
		return movement.fnSpeed.get();
	}

	/**
	 * @param fnSpeed function providing the current speed
	 */
	public void setSpeed(Supplier<Float> fnSpeed) {
		movement.fnSpeed = fnSpeed;
	}

	public Steering<M> steering() {
		return steering(this);
	}

	/**
	 * @return the current steering for this actor.
	 */
	public Steering<M> steering(MobileLifeform mover) {
		return steeringsByState.getOrDefault(getState(), m -> {
		});
	}

	public Optional<Tile> targetTile() {
		if (steering() instanceof TargetTileSteering) {
			TargetTileSteering<M> steering = (TargetTileSteering<M>) steering();
			return Optional.ofNullable(steering.targetTile());
		}
		return Optional.empty();
	}

	/**
	 * Returns the steering for the given state.
	 * 
	 * @param state state
	 * @return steering defined for this state
	 */
	public Steering<M> steering(STATE state) {
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
	public void behavior(STATE state, Steering<M> steering) {
		steeringsByState.put(state, steering);
	}

	@Override
	public boolean requiresAlignment() {
		return steering().requiresGridAlignment();
	}

	@Override
	public void init() {
		movement.init();
		super.init();
	}

	@Override
	public Transform tf() {
		return entity.tf;
	}

	@Override
	public float centerX() {
		return entity.tf.getCenter().x;
	}

	@Override
	public float centerY() {
		return entity.tf.getCenter().y;
	}

	@Override
	public void placeAt(Tile tile, float xOffset, float yOffset) {
		movement.moveToTile(tile, xOffset, yOffset);
	}

	@Override
	public float tileOffsetX() {
		return entity.tf.x - tileLocation().x() + Tile.SIZE / 2;
	}

	@Override
	public float tileOffsetY() {
		return entity.tf.y - tileLocation().y() + Tile.SIZE / 2;
	}

	public boolean isTeleporting() {
		return movement.is(MovementType.TELEPORTING);
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