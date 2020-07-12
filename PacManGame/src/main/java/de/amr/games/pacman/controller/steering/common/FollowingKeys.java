package de.amr.games.pacman.controller.steering.common;

import static de.amr.games.pacman.model.world.api.Direction.dirs;

import java.util.EnumMap;
import java.util.Objects;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.api.MobileCreature;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Direction;

/**
 * Steering controlled by keyboard keys for UP, RIGHT, DOWN, LEFT direction.
 * 
 * @author Armin Reichert
 */
public class FollowingKeys implements Steering {

	private MobileCreature actor;
	private EnumMap<Direction, Integer> keys = new EnumMap<>(Direction.class);

	public FollowingKeys(MobileCreature actor, int upKey, int rightKey, int downKey, int leftKey) {
		this.actor = Objects.requireNonNull(actor);
		keys.put(Direction.UP, upKey);
		keys.put(Direction.RIGHT, rightKey);
		keys.put(Direction.DOWN, downKey);
		keys.put(Direction.LEFT, leftKey);
	}

	@Override
	public void steer() {
		dirs().filter(dir -> Keyboard.keyDown(keys.get(dir))).findAny().ifPresent(actor::setWishDir);
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}
}