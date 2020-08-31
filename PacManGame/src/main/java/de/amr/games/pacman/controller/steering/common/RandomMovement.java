package de.amr.games.pacman.controller.steering.common;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Direction;

/**
 * Lets a creature move randomly but never reverse its direction.
 * 
 * @author Armin Reichert
 */
public class RandomMovement implements Steering {

	private boolean forced;

	@Override
	public void steer(Guy<?> guy) {
		if (forced || !guy.canCrossBorderTo(guy.moveDir) || guy.enteredNewTile && guy.world.isIntersection(guy.tile())) {
			/*@formatter:off*/
			Direction.dirsShuffled()
				.filter(dir -> dir != guy.moveDir.opposite())
				.filter(guy::canCrossBorderTo)
				.findFirst()
				.ifPresent(dir -> guy.wishDir = dir);
			/*@formatter:on*/
			forced = false;
		}
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void force() {
		forced = true;
	}
}