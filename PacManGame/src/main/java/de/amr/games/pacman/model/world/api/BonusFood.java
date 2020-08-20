package de.amr.games.pacman.model.world.api;

import de.amr.games.pacman.model.world.components.Tile;

/**
 * Bonus food that appears for a certain time inside the world.
 * 
 * @author Armin Reichert
 */
public interface BonusFood extends Food {

	Tile location();

	int value();

	boolean isPresent();

	boolean isConsumed();

	void show();
	
	void hide();

	void consume();
}