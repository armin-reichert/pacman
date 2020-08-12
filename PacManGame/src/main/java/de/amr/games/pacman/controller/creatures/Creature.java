package de.amr.games.pacman.controller.creatures;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.Movement;
import de.amr.games.pacman.controller.steering.common.MovementType;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.MobileLifeform;
import de.amr.games.pacman.view.api.Theme;
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

	public final StateMachine<S, PacManGameEvent> ai;
	public final MobileLifeform entity;
	public final String name;
	public final Map<S, Steering> steeringsMap;
	public Game game;
	public boolean enabled;
	public Movement movement;
	public Theme theme;

	public Creature(String name, World world, Map<S, Steering> steeringsMap) {
		this.name = name;
		this.steeringsMap = steeringsMap;
		enabled = true;
		entity = new MobileLifeform(world);
		entity.tf.width = entity.tf.height = Tile.SIZE;
		movement = new Movement(this);
		ai = buildAI();
	}

	protected abstract StateMachine<S, PacManGameEvent> buildAI();

	/**
	 * @return speed in pixels/ticks
	 */
	public abstract float getSpeed();

	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		return entity.world.isAccessible(neighbor);
	}

	public boolean canCrossBorderTo(Direction dir) {
		Tile currentTile = entity.tileLocation(), neighbor = entity.world.neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	public Stream<StateMachine<?, ?>> machines() {
		return Stream.of(ai, movement);
	}

	public Steering steering() {
		return steeringsMap.getOrDefault(ai.getState(), mover -> {
		});
	}

	/**
	 * @return the optional target tile of this creature
	 */
	public Optional<Tile> targetTile() {
		return steering().targetTile();
	}

	/**
	 * Returns the steering for the given state.
	 * 
	 * @param state state
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

	public boolean requiresAlignment() {
		return steering().requiresGridAlignment();
	}

	@Override
	public void init() {
		movement.init();
		ai.init();
	}

	public void placeAt(Tile tile, float xOffset, float yOffset) {
		movement.placeAt(tile, xOffset, yOffset);
	}

	public boolean isTeleporting() {
		return movement.is(MovementType.TELEPORTING);
	}

	public void forceMoving(Direction dir) {
		entity.wishDir = dir;
		movement.update();
	}

	public void reverseDirection() {
		forceMoving(entity.moveDir.opposite());
	}
}