package de.amr.games.pacman.controller.creatures;

import java.util.Map;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.steering.api.SteeredMover;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * A guy is a steered entity with "artificial intelligence", i.e. he can make decisions changing his
 * behavior.
 * 
 * @param <STATE> state (identifier) type
 * 
 * @author Armin Reichert
 */
public abstract class Guy<STATE> extends SteeredMover implements Lifecycle {

	public final StateMachine<STATE, PacManGameEvent> ai;
	public final Map<STATE, Steering> steeringsMap;

	public Steering previousSteering;
	public Game game;
	public boolean enabled;

	public Guy(World world, String name, Map<STATE, Steering> steeringsMap) {
		super(world, name);
		this.steeringsMap = steeringsMap;
		this.ai = buildAI();
		tf.width = tf.height = Tile.SIZE;
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
	 * @return all state machines of this guy
	 */
	public Stream<StateMachine<?, ?>> machines() {
		return Stream.of(ai, movement);
	}

	@Override
	public Steering steering() {
		Steering currentSteering = steeringsMap.getOrDefault(ai.getState(), Steering.STANDING_STILL);
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
}