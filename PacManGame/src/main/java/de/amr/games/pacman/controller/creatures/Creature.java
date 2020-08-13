package de.amr.games.pacman.controller.creatures;

import java.util.Map;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.Movement;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Mover;
import de.amr.statemachine.core.StateMachine;

/**
 * A creature is a named entity that can move through the world and has "intelligence", i.e. it can
 * make decisions changing its behavior.
 * <p>
 * The physical size is one tile, however the visual appearance may be larger.
 * 
 * @param <S> state (identifier) type
 * 
 * @author Armin Reichert
 */
public abstract class Creature<S> implements Lifecycle {

	public final String name;
	public final World world;
	public final Mover entity;
	public final StateMachine<S, PacManGameEvent> ai;
	public final Map<S, Steering> steeringsMap;
	public final Movement movement;

	public Steering previousSteering;
	public Game game;
	public boolean enabled;

	public Creature(String name, World world, Map<S, Steering> steeringsMap) {
		this.name = name;
		this.world = world;
		this.entity = new Mover();
		this.entity.tf.width = entity.tf.height = Tile.SIZE;
		this.ai = buildAI();
		this.steeringsMap = steeringsMap;
		this.movement = new Movement(this);
		this.enabled = true;
	}

	protected abstract StateMachine<S, PacManGameEvent> buildAI();

	@Override
	public void init() {
		previousSteering = null;
		movement.init();
		ai.init();
	}

	/**
	 * @return speed in pixels/ticks
	 */
	public abstract float getSpeed();

	/**
	 * @param tile some tile, not necessary the current tile
	 * @param a    neighbor tile of the tile
	 * @return {@code true} if this creature can move between the given tiles
	 */
	public abstract boolean canMoveBetween(Tile tile, Tile neighbor);

	/**
	 * @param dir a direction
	 * @return {@code true} if this creature can cross the border to the given direction
	 */
	public boolean canCrossBorderTo(Direction dir) {
		Tile currentTile = entity.tile(), neighbor = world.neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	/**
	 * @return the state machines of this creature
	 */
	public Stream<StateMachine<?, ?>> machines() {
		return Stream.of(ai, movement);
	}

	/**
	 * @return the current steering
	 */
	public Steering steering() {
		Steering currentSteering = steeringsMap.getOrDefault(ai.getState(), mover -> {
		});
		if (previousSteering != currentSteering) {
			currentSteering.init();
			currentSteering.force(); //TODO correct?
			previousSteering = currentSteering;
		}
		return currentSteering;
	}

	/**
	 * Returns the steering for the given state.
	 * 
	 * @param state state ID
	 * @return steering defined for this state
	 */
	public Steering steering(S state) {
		if (steeringsMap.containsKey(state)) {
			return steeringsMap.get(state);
		}
		throw new IllegalArgumentException(String.format("%s: No steering found for state %s", this, state));
	}

	/**
	 * Defines the steering for the given state.
	 * 
	 * @param state    state
	 * @param steering steering defined for this state
	 */
	public void behavior(S state, Steering steering) {
		steeringsMap.put(state, steering);
	}

	/**
	 * Forces the creature to move to the given direction.
	 * 
	 * @param dir direction
	 */
	public void forceMoving(Direction dir) {
		entity.wishDir = dir;
		movement.update();
	}

	/**
	 * Forces the creature to reverse its direction.
	 */
	public void reverseDirection() {
		forceMoving(entity.moveDir.opposite());
	}
}