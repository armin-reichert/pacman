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