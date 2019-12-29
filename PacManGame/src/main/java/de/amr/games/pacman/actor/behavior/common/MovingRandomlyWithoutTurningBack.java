package de.amr.games.pacman.actor.behavior.common;

import static de.amr.datastruct.StreamUtils.permute;

import java.util.Collections;

import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Direction;

/**
 * A steering that causes an actor to move randomly through the maze without
 * ever turning back.
 * 
 * @author Armin Reichert
 *
 * @param <T> actor type
 */
public class MovingRandomlyWithoutTurningBack<T extends MazeMover> implements Steering<T> {

	@Override
	public void steer(T actor) {
		actor.setTargetPath(Collections.emptyList());
		actor.setTargetTile(null);
		if (actor.enteredNewTile()) {
			/*@formatter:off*/
			permute(Direction.dirs())
				.filter(dir -> dir != actor.moveDir().opposite())
				.filter(actor::canCrossBorderTo)
				.findFirst()
				.ifPresent(actor::setNextDir);
			/*@formatter:on*/
		}
	}

	@Override
	public boolean onTrack() {
		return true;
	}
}