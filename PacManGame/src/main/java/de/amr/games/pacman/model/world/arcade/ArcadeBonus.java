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
package de.amr.games.pacman.model.world.arcade;

import de.amr.games.pacman.model.world.api.TemporaryFood;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * Symbols appearing as bonus food in the Arcade game.
 * 
 * @author Armin Reichert
 */
public class ArcadeBonus implements TemporaryFood {

	public enum Symbol {
		CHERRIES, STRAWBERRY, PEACH, APPLE, GRAPES, GALAXIAN, BELL, KEY;
	}

	private ArcadeBonus(Symbol symbol) {
		this.symbol = symbol;
	}

	public static ArcadeBonus of(String name, int value) {
		ArcadeBonus bonus = new ArcadeBonus(Symbol.valueOf(name));
		bonus.value = value;
		bonus.location = ArcadeWorld.BONUS_LOCATION;
		return bonus;
	}

	public final Symbol symbol;
	private Tile location;
	private int value;
	private boolean active;
	private boolean consumed;

	@Override
	public Tile location() {
		return location;
	}

	@Override
	public int value() {
		return value;
	}

	@Override
	public int fat() {
		return 0;
	}

	@Override
	public boolean isConsumed() {
		return consumed;
	}

	@Override
	public void consume() {
		consumed = true;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void activate() {
		active = true;
		consumed = false;
	}

	@Override
	public void deactivate() {
		active = false;
		consumed = false;
	}

	@Override
	public String toString() {
		return String.format("(%s,value=%s,active=%s,consumed=%s)", symbol, value, active, consumed);
	}
}