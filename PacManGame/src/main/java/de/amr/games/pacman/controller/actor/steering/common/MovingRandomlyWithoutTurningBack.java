package de.amr.games.pacman.controller.actor.steering.common;

import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;

import java.util.Arrays;
import java.util.Objects;

import de.amr.games.pacman.controller.actor.MazeMover;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;

/**
 * Lets an actor move randomly but never reverse direction.
 * 
 * @author Armin Reichert
 */
public class MovingRandomlyWithoutTurningBack implements Steering {

	private MazeMover actor;
	private boolean forced;

	public MovingRandomlyWithoutTurningBack(MazeMover actor) {
		this.actor = Objects.requireNonNull(actor);
	}

	@Override
	public void force() {
		forced = true;
	}

	@Override
	public void steer() {
		actor.setTargetTile(null);
		if (forced || actor.enteredNewTile() || !actor.canCrossBorderTo(actor.moveDir())) {
			/*@formatter:off*/
			Direction[] dirs = Direction.values();
			shuffle(asList(dirs));
			Arrays.stream(dirs)
				.filter(dir -> dir != actor.moveDir().opposite())
				.filter(actor::canCrossBorderTo)
				.findFirst()
				.ifPresent(actor::setWishDir);
			/*@formatter:on*/
			forced = false;
		}
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}
}