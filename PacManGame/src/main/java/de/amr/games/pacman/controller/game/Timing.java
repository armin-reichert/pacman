package de.amr.games.pacman.controller.game;

import de.amr.games.pacman.model.game.Game;

public interface Timing {

	/**
	 * In Shaun William's <a href="https://github.com/masonicGIT/pacman">Pac-Man remake</a> there is a
	 * speed table giving the number of steps (=pixels?) which Pac-Man is moving in 16 frames. In level
	 * 5, he uses 4 * 2 + 12 = 20 steps in 16 frames, which is 1.25 pixels/frame. The table from
	 * Gamasutra ({@link Game#LEVEL_DATA}) states that this corresponds to 100% base speed for Pac-Man
	 * at level 5. Therefore I use 1.25 pixel/frame for 100% speed.
	 */
	float BASE_SPEED = 1.25f;

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}

	/**
	 * Returns the number of ticks corresponding to the given time (in seconds) for a framerate of 60
	 * ticks/sec.
	 * 
	 * @param seconds seconds
	 * @return ticks corresponding to given number of seconds
	 */
	static long sec(float seconds) {
		return Math.round(60 * seconds);
	}
}
