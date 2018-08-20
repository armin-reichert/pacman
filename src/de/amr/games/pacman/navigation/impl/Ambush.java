package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Tile;

/**
 * Ambush the victim in the maze.
 */
class Ambush extends FollowTargetTile {

	private static Tile aheadOf(MazeMover mover, int n) {
		Tile moverTile = mover.getTile();
		int moverDir = mover.getCurrentDir();
		Tile target = new Tile(moverTile.col + 4 * NESW.dx(moverDir), moverTile.row + 4 * NESW.dy(moverDir));
		return mover.getMaze().isValidTile(target) ? target : moverTile;
	}

	public Ambush(MazeMover victim) {
		super(victim.getMaze(), () -> aheadOf(victim, 4));
	}
}