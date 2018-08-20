package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Tile;

/**
 * Ambush the victim in the maze.
 */
class Ambush extends FollowTargetTile {

	private static Tile aheadOf(MazeMover mover, int n) {
		Tile moverLocation = mover.getTile();
		int currentDir = mover.getCurrentDir();
		Tile target = new Tile(moverLocation.col + 4 * NESW.dx(currentDir), moverLocation.row + 4 * NESW.dy(currentDir));
		return mover.getMaze().isValidTile(target) ? target : moverLocation;
	}

	public Ambush(MazeMover victim) {
		super(victim.getMaze(), () -> aheadOf(victim, 4));
	}
}