package de.amr.games.pacman.controller.steering.common;

import de.amr.games.pacman.controller.creatures.SmartGuy;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.core.MovingGuy;

/**
 * Lets a creature move randomly but never reverse its direction.
 * 
 * @author Armin Reichert
 */
public class RandomMovement implements Steering {

	private SmartGuy<?> guy;
	private boolean forced;

	public RandomMovement(SmartGuy<?> guy) {
		this.guy = guy;
	}

	@Override
	public void steer(MovingGuy entity) {
		if (forced || !guy.canCrossBorderTo(entity.moveDir)
				|| entity.enteredNewTile && guy.world.isIntersection(entity.tile())) {
			/*@formatter:off*/
			Direction.dirsShuffled()
				.filter(dir -> dir != entity.moveDir.opposite())
				.filter(guy::canCrossBorderTo)
				.findFirst()
				.ifPresent(dir -> entity.wishDir = dir);
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