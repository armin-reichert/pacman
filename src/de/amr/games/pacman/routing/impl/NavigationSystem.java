package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.routing.Navigation;

public interface NavigationSystem {

	public static Navigation ambush(MazeMover<?> victim) {
		return new Ambush(victim);
	}

	public static Navigation bounce() {
		return new Bounce();
	}

	public static Navigation chase(MazeMover<?> victim) {
		return new Chase(victim);
	}

	public static Navigation flee(MazeMover<?> chaser) {
		return new Flee(chaser);
	}

	public static Navigation followKeyboard(int... nesw) {
		return new FollowKeyboard(nesw);
	}

	public static Navigation forward() {
		return new Forward();
	}

	public static Navigation goHome() {
		return new GoHome();
	}

	public static Navigation moody() {
		return new Moody();
	}

	public static Navigation stayBehind() {
		return new StayBehind();
	}
}
