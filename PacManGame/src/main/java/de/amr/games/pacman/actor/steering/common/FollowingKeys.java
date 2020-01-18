package de.amr.games.pacman.actor.steering.common;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.MazeMoving;

public class FollowingKeys implements Steering {

	private MazeMoving actor;
	private int[] keys;

	public FollowingKeys(MazeMoving actor, int... keys) {
		this.actor = actor;
		this.keys = keys;
	}

	@Override
	public void steer() {
		Direction.dirs().filter(dir -> Keyboard.keyDown(keys[dir.ordinal()])).findAny().ifPresent(actor::setWishDir);
	}

	@Override
	public void init() {
	}

	@Override
	public void force() {
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void enableTargetPathComputation(boolean b) {
	}
}