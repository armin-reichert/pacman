package de.amr.games.pacman.controller.creatures;

import java.util.Map;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.Movement;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.model.world.core.MovingGuy;
import de.amr.statemachine.core.StateMachine;

/**
 * A moving entity with "artificial intelligence", i.e. he/she can make decisions influencing its
 * behavior.
 * 
 * @param <STATE> state (identifier) type
 * 
 * @author Armin Reichert
 */
public abstract class SmartGuy<STATE> implements Lifecycle {

	public final String name;
	public final World world;
	public final MovingGuy body;
	public final StateMachine<STATE, PacManGameEvent> ai;
	public final Map<STATE, Steering> steeringsMap;
	public Movement movement;
	public Steering previousSteering;
	public Game game;
	public boolean enabled;

	public SmartGuy(String name, World world, Map<STATE, Steering> steeringsMap) {
		this.name = name;
		this.world = world;
		this.body = new MovingGuy();
		this.ai = buildAI();
		this.steeringsMap = steeringsMap;
	}

	@Override
	public void init() {
		previousSteering = null;
		enabled = true;
		movement.init();
		ai.init();
	}

	protected abstract StateMachine<STATE, PacManGameEvent> buildAI();

	/**
	 * @return speed in pixels/ticks
	 */
	public abstract float getSpeed();

	/**
	 * @param tile some tile, not necessary the current tile
	 * @param a    neighbor tile of the tile
	 * @return {@code true} if this guy can move between the given tiles
	 */
	public abstract boolean canMoveBetween(Tile tile, Tile neighbor);

	/**
	 * @param dir a direction
	 * @return {@code true} if this guy can cross the border to the given direction
	 */
	public boolean canCrossBorderTo(Direction dir) {
		Tile currentTile = body.tile(), neighbor = world.neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	/**
	 * @return all state machines of this guy
	 */
	public Stream<StateMachine<?, ?>> machines() {
		return Stream.of(ai, movement);
	}

	/**
	 * @return the current steering of this guy. If the steering has changed since the last access it
	 *         gets initialized.
	 */
	public Steering steering() {
		Steering currentSteering = steeringsMap.getOrDefault(ai.getState(), guy -> {
		});
		if (previousSteering != currentSteering) {
			currentSteering.init();
			currentSteering.force();
			previousSteering = currentSteering;
		}
		return currentSteering;
	}

	/**
	 * Defines the steering for the given state.
	 * 
	 * @param state    state
	 * @param steering steering defined for this state
	 */
	public void behavior(STATE state, Steering steering) {
		steeringsMap.put(state, steering);
	}

	/**
	 * Forces this guy to move to the given direction.
	 * 
	 * @param dir direction
	 */
	public void forceMoving(Direction dir) {
		body.wishDir = dir;
		movement.update();
	}

	/**
	 * Forces this guy to reverse its direction.
	 */
	public void reverseDirection() {
		forceMoving(body.moveDir.opposite());
	}
}