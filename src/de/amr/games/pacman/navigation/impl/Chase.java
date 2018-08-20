package de.amr.games.pacman.navigation.impl;

import de.amr.games.pacman.actor.core.MazeMover;

/**
 * Chasing a victim through the maze.
 */
class Chase extends FollowTargetTile {

	public Chase(MazeMover victim) {
		super(victim.getMaze(), victim::getTile);
	}
}