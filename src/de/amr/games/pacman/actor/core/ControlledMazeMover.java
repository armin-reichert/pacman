package de.amr.games.pacman.actor.core;

import java.util.Map;

import de.amr.games.pacman.routing.Navigation;
import de.amr.games.pacman.routing.impl.NavigationSystem;

/**
 * A maze mover whose navigation is controlled by its state.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state identifier type
 */
public abstract class ControlledMazeMover<S> extends MazeMover {

	private final Map<S, Navigation> navigationMap;

	protected ControlledMazeMover(Map<S, Navigation> navigationMap) {
		this.navigationMap = navigationMap;
	}

	public abstract S getState();

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
}