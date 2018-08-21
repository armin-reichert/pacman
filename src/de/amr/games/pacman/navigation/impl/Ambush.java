package de.amr.games.pacman.navigation.impl;

import de.amr.games.pacman.actor.core.MazeMover;

/**
 * Ambush the victim in the maze.
 */
class Ambush extends FollowTargetTile {

	public Ambush(MazeMover victim) {
		super(victim.getMaze(), () -> aheadOf(victim, 4));
	}
}