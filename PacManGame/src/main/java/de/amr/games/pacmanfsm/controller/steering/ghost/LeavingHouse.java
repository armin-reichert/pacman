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
package de.amr.games.pacmanfsm.controller.steering.ghost;

import static de.amr.games.pacmanfsm.lib.Direction.LEFT;
import static de.amr.games.pacmanfsm.lib.Direction.RIGHT;
import static de.amr.games.pacmanfsm.lib.Direction.UP;

import de.amr.games.pacmanfsm.controller.creatures.Guy;
import de.amr.games.pacmanfsm.controller.steering.api.Steering;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.components.House;

public class LeavingHouse implements Steering {

	private final House house;

	public LeavingHouse(House house) {
		this.house = house;
	}

	@Override
	public void steer(Guy guy) {
		Tile exit = Tile.at(house.bed(0).minX(), house.bed(0).minY());
		int targetX = exit.centerX();
		int targetY = exit.y();
		if (guy.tf.y <= targetY) {
			guy.tf.y = targetY;
		} else if (Math.round(guy.tf.x) == targetX) {
			guy.tf.x = targetX;
			guy.wishDir = UP;
		} else if (guy.tf.x < targetX) {
			guy.wishDir = RIGHT;
		} else if (guy.tf.x > targetX) {
			guy.wishDir = LEFT;
		}
	}
}