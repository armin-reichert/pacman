package de.amr.games.pacman.controller;

import static de.amr.games.pacman.controller.actor.PacManState.EATING;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.GameLevel;

public class SpeedLimits {
	/**
	 * In Shaun William's <a href="https://github.com/masonicGIT/pacman">Pac-Man remake</a> there is a
	 * speed table giving the number of steps (=pixels?) which Pac-Man is moving in 16 frames. In level
	 * 5, he uses 4 * 2 + 12 = 20 steps in 16 frames, which is 1.25 pixels/frame. The table from
	 * Gamasutra ({@link Game#LEVELS}) states that this corresponds to 100% base speed for Pac-Man at
	 * level 5. Therefore I use 1.25 pixel/frame for 100% speed.
	 */
	public static final float BASE_SPEED = 1.25f;

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	public static float ghostSpeedLimit(Ghost ghost, Game game) {
		GameLevel level = game.level;
		switch (ghost.getState()) {
		case LOCKED:
			return speed(ghost.isInsideHouse() ? level.ghostSpeed / 2 : 0);
		case LEAVING_HOUSE:
			return speed(level.ghostSpeed / 2);
		case ENTERING_HOUSE:
			return speed(level.ghostSpeed);
		case CHASING:
		case SCATTERING:
			if (ghost.world.isTunnel(ghost.tile())) {
				return speed(level.ghostTunnelSpeed);
			}
			switch (ghost.sanity.getState()) {
			case ELROY1:
				return speed(level.elroy1Speed);
			case ELROY2:
				return speed(level.elroy2Speed);
			case INFECTABLE:
			case IMMUNE:
				return speed(level.ghostSpeed);
			default:
				throw new IllegalArgumentException("Illegal ghost sanity state: " + ghost.sanity.getState());
			}
		case FRIGHTENED:
			return speed(ghost.world.isTunnel(ghost.tile()) ? level.ghostTunnelSpeed : level.ghostFrightenedSpeed);
		case DEAD:
			return speed(2 * level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s", ghost.getState()));
		}
	}

	public static float pacManSpeedLimit(PacMan pacMan, Game game) {
		GameLevel level = game.level;
		return pacMan.is(EATING) ? speed(pacMan.power > 0 ? level.pacManPowerSpeed : level.pacManSpeed) : 0;
	}
}