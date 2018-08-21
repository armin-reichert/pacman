package de.amr.games.pacman.navigation.impl;

import java.util.function.Supplier;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.actor.game.GhostName;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.Navigation;

public interface NavigationSystem {

	public static Navigation ambush(MazeMover victim) {
		return new Ambush(victim);
	}

	public static Navigation bounce() {
		return new Bounce();
	}

	public static Navigation chase(MazeMover victim) {
		return new Chase(victim);
	}

	public static Navigation flee(MazeMover chaser) {
		return new Flee(chaser);
	}

	public static Navigation followKeyboard(int... nesw) {
		return new FollowKeyboard(nesw);
	}

	public static Navigation followTargetTile(Maze maze, Supplier<Tile> targetTileSupplier) {
		return new FollowTargetTile(maze, targetTileSupplier);
	}

	public static Navigation forward() {
		return new Forward();
	}

	public static Navigation go(Tile target) {
		return new Go(target);
	}

	public static Navigation moody() {
		return new Moody();
	}

	public static Navigation scatter(Maze maze, GhostName ghostName) {
		return new Scatter(maze, ghostName);
	}

	public static Navigation stayBehind() {
		return new StayBehind();
	}
}
