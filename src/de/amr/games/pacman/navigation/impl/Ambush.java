package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Tile;

/**
 * Ambush the victim in the maze.
 */
class Ambush extends FollowTargetTile {

	private static Tile aheadOf(MazeMover mover, int n) {
		Tile tile = mover.getTile();
		int dir = mover.getCurrentDir();
		Tile target = new Tile(tile.col + n * NESW.dx(dir), tile.row + n * NESW.dy(dir));
		return mover.getMaze().isValidTile(target) ? target : tile;
	}

	public Ambush(MazeMover victim) {
		super(victim.getMaze(), () -> aheadOf(victim, 4));
	}
}