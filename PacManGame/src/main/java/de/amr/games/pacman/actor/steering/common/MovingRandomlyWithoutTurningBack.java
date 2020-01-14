package de.amr.games.pacman.actor.steering.common;

import de.amr.datastruct.StreamUtils;
import de.amr.games.pacman.actor.core.MazeMoving;
import de.amr.games.pacman.actor.steering.core.Steering;
import de.amr.games.pacman.model.Direction;

public class MovingRandomlyWithoutTurningBack implements Steering {

	private MazeMoving actor;

	public MovingRandomlyWithoutTurningBack(MazeMoving actor) {
		this.actor = actor;
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
	public void init() {
	}

	@Override
	public void force() {
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void enableTargetPathComputation(boolean b) {
	}
}