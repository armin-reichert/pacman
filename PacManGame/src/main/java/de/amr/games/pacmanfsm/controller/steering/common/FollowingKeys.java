/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.controller.steering.common;

import java.awt.event.KeyEvent;
import java.util.EnumMap;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacmanfsm.controller.creatures.Guy;
import de.amr.games.pacmanfsm.controller.steering.api.Steering;
import de.amr.games.pacmanfsm.lib.Direction;

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
	public void steer(Guy guy) {
		Direction.dirs().filter(dir -> Keyboard.keyDown(keys.get(dir))).findAny().ifPresent(dir -> guy.wishDir = dir);
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}
}