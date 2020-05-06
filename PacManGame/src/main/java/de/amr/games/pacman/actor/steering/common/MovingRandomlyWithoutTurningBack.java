package de.amr.games.pacman.actor.steering.common;

import java.util.Objects;

import de.amr.datastruct.StreamUtils;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;

/**
 * Lets an actor move randomly but never reverse direction.
 * 
 * @author Armin Reichert
 */
public class MovingRandomlyWithoutTurningBack implements Steering {

	private MazeMover actor;

	public MovingRandomlyWithoutTurningBack(MazeMover actor) {
		this.actor = Objects.requireNonNull(actor);
	}

	@Override
	public void steer() {
		actor.setTargetTile(null);
		if (actor.enteredNewTile() || !actor.canCrossBorderTo(actor.moveDir())) {
			/*@formatter:off*/
			StreamUtils.permute(Direction.dirs())
				.filter(dir -> dir != actor.moveDir().opposite())
				.filter(actor::canCrossBorderTo)
				.findFirst()
				.ifPresent(actor::setWishDir);
			/*@formatter:on*/
		}
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}
}