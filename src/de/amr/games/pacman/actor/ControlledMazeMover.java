package de.amr.games.pacman.actor;

import java.util.Map;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.Navigation;
import de.amr.games.pacman.routing.impl.NavigationSystem;
import de.amr.statemachine.StateMachine;

public abstract class ControlledMazeMover<S, E> extends TileWorldMover {

	private final Map<S, Navigation> navigationMap;

	public ControlledMazeMover(Maze maze, Tile homeTile, Map<S, Navigation> navigationMap) {
		super(maze, homeTile);
		this.navigationMap = navigationMap;
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

	public void processEvent(E e) {
		getStateMachine().enqueue(e);
		getStateMachine().update();
	}

	public void setNavigation(S state, Navigation navigation) {
		navigationMap.put(state, navigation);
	}

	public Navigation getNavigation() {
		return navigationMap.getOrDefault(getState(), NavigationSystem.forward());
	}

	@Override
	public int computeNextDir() {
		return getNavigation().computeRoute(this).getDirection();
	}
}