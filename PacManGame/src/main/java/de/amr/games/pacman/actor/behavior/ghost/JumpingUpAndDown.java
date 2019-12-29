package de.amr.games.pacman.actor.behavior.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;

/**
 * Lets a ghost jump up and down at its place in the house.
 * 
 * @author Armin Reichert
 */
public class JumpingUpAndDown implements Steering<Ghost> {

	private final int placeY;

	public JumpingUpAndDown(Maze maze, int place) {
		placeY = maze.ghostHouseSeats[place].y();
	}

	@Override
	public void steer(Ghost ghost) {
		float dy = ghost.tf.getPosition().y - placeY;
		if (dy < -4) {
			ghost.setWishDir(Direction.DOWN);
		} else if (dy > 3) {
			ghost.setWishDir(Direction.UP);
		}
	}

	@Override
	public boolean stayOnTrack() {
		return false;
	}
}