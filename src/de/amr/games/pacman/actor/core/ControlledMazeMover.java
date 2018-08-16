package de.amr.games.pacman.actor.core;

import java.util.Map;

import de.amr.games.pacman.routing.Navigation;
import de.amr.games.pacman.routing.impl.NavigationSystem;
import de.amr.statemachine.StateMachine;

/**
 * A maze mover that is controlled by a state machine.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state identifier type
 * @param <E>
 *          event type
 */
public abstract class ControlledMazeMover<S, E> extends MazeMover {

	private final Map<S, Navigation> navigationMap;

	protected ControlledMazeMover(Map<S, Navigation> navigationMap) {
		this.navigationMap = navigationMap;
	}

	public Navigation getNavigation() {
		return navigationMap.getOrDefault(getState(), NavigationSystem.forward());
	}

	public void setNavigation(S state, Navigation navigation) {
		navigationMap.put(state, navigation);
	}

	@Override
	public int supplyIntendedDir() {
		return getNavigation().computeRoute(this).dir;
	}

	protected abstract StateMachine<S, E> getStateMachine();

	public S getState() {
		return getStateMachine().currentState();
	}

	@Override
	public void init() {
		getStateMachine().init();
	}

	@Override
	public void update() {
		getStateMachine().update();
	}

	public void processEvent(E event) {
		getStateMachine().enqueue(event);
		getStateMachine().update();
	}
}