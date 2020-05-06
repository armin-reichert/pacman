package de.amr.games.pacman.actor.steering.common;

import static de.amr.games.pacman.model.Direction.dirs;

import java.util.Objects;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.steering.Steering;

/**
 * Steering controlled by keyboard keys for UP, RIGHT, DOWN, LEFT direction.
 * 
 * @author Armin Reichert
 */
public class FollowingKeys implements Steering {

	private MazeMover actor;
	private int[] keys;

	public FollowingKeys(MazeMover actor, int up, int right, int down, int left) {
		this.actor = Objects.requireNonNull(actor);
		this.keys = new int[] { up, right, down, left };
	}

	@Override
	public void steer() {
		dirs().filter(dir -> Keyboard.keyDown(keys[dir.ordinal()])).findAny().ifPresent(actor::setWishDir);
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}
}