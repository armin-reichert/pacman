package de.amr.games.pacman.actor.behavior.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;

/**
 * Lets a ghost jump up and down.
 * 
 * @author Armin Reichert
 */
public class JumpingUpAndDown implements Steering<Ghost> {

	private final int baseY;
	private final int amplitude;

	public JumpingUpAndDown(Tile baseTile, int amplitude) {
		this.baseY = baseTile.row * Tile.SIZE;
		this.amplitude = amplitude;
	}

	@Override
	public void steer(Ghost ghost) {
		float ghostY = ghost.tf.getPosition().y;
		if (ghostY < baseY - amplitude) {
			ghost.setNextDir(Direction.DOWN);
		}
		else if (ghostY > baseY + amplitude) {
			ghost.setNextDir(Direction.UP);
		}
		else {
			ghost.setNextDir(ghost.moveDir());
		}
	}
}