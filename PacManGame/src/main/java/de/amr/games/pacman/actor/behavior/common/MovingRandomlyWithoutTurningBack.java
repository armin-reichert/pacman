package de.amr.games.pacman.actor.behavior.common;

import static de.amr.datastruct.StreamUtils.permute;

import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Direction;

/**
 * A steering that causes an actor to move randomly through the maze without
 * ever turning back.
 * 
 * @author Armin Reichert
 */
public class MovingRandomlyWithoutTurningBack implements Steering {

	private final MazeMover actor;

	public MovingRandomlyWithoutTurningBack(MazeMover actor) {
		this.actor = actor;
	}

	@Override
	public void steer() {
		actor.setTargetTile(null);
		if (enabled()) {
			/*@formatter:off*/
			permute(Direction.dirs())
				.filter(dir -> dir != actor.moveDir().opposite())
				.filter(actor::canCrossBorderTo)
				.findFirst()
				.ifPresent(actor::setWishDir);
			/*@formatter:on*/
		}
	}

	@Override
	public boolean enabled() {
		return actor.enteredNewTile();
	}

	@Override
	public boolean stayOnTrack() {
		return true;
	}
}