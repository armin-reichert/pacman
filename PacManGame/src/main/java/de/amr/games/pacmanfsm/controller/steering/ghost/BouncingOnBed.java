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

import static de.amr.games.pacmanfsm.lib.Direction.DOWN;
import static de.amr.games.pacmanfsm.lib.Direction.UP;

import de.amr.games.pacmanfsm.controller.creatures.Guy;
import de.amr.games.pacmanfsm.controller.steering.api.Steering;
import de.amr.games.pacmanfsm.model.world.components.Bed;

/**
 * Lets a guy bounce on its bed.
 * 
 * @author Armin Reichert
 */
public class BouncingOnBed implements Steering {

	private final float bedCenterY;

	public BouncingOnBed(Bed bed) {
		bedCenterY = bed.center().y();
	}

	@Override
	public void steer(Guy guy) {
		float dy = guy.tf.y + guy.tf.height / 2 - bedCenterY;
		if (dy < -4) {
			guy.wishDir = DOWN;
		} else if (dy > 5) {
			guy.wishDir = UP;
		}
	}
}