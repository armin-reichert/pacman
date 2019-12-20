package de.amr.games.pacman.actor.behavior.common;

import java.util.Collections;

import de.amr.datastruct.StreamUtils;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Direction;

public class MovingRandomlyWithoutTurningBack<T extends MazeMover> implements Steering<T> {

	@Override
	public void steer(T actor) {
		actor.setTargetPath(Collections.emptyList());
		actor.setTargetTile(null);
		StreamUtils.permute(Direction.dirs()).filter(dir -> actor.enteredNewTile())
				.filter(dir -> dir != actor.moveDir().opposite()).filter(actor::canCrossBorderTo).findFirst()
				.ifPresent(actor::setNextDir);
	}

	@Override
	public boolean onTrack() {
		return true;
	}
}
