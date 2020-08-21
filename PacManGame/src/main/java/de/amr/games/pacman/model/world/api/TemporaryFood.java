package de.amr.games.pacman.model.world.api;

import de.amr.games.pacman.model.world.components.Tile;

/**
 * Food that appears for a certain time inside the world. There are three states:
 * <ul>
 * <li>deactivated (hidden)
 * <li>activated but not yet consumed (shown as symbol)
 * <li>activated and consumed (shown as value)
 * </ul>
 * 
 * @author Armin Reichert
 */
public interface TemporaryFood extends Food {

	Tile location();

	int value();

	boolean isActive();

	void activate();

	void deactivate();

	boolean isConsumed();

	void consume();
}