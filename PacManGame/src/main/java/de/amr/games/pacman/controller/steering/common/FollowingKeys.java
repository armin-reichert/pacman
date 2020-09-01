package de.amr.games.pacman.controller.steering.common;

import java.awt.event.KeyEvent;
import java.util.EnumMap;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Direction;

/**
 * Steers a mover using keyboard keys.
 * 
 * @author Armin Reichert
 */
public class FollowingKeys implements Steering {

	private EnumMap<Direction, Integer> keys = new EnumMap<>(Direction.class);

	/**
	 * Defines a steering using the virtual key codes as defined in class {@link KeyEvent}.
	 * 
	 * @param up    key code for moving up
	 * @param right key code for moving right
	 * @param down  key code for moving down
	 * @param left  key code for moving left
	 */
	public FollowingKeys(int up, int right, int down, int left) {
		keys.put(Direction.UP, up);
		keys.put(Direction.RIGHT, right);
		keys.put(Direction.DOWN, down);
		keys.put(Direction.LEFT, left);
	}

	@Override
	public void steer(Guy<?> guy) {
		Direction.dirs().filter(dir -> Keyboard.keyDown(keys.get(dir))).findAny().ifPresent(dir -> guy.wishDir = dir);
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}
}