package de.amr.games.pacman.model;

/**
 * Timimg related methods.
 * 
 * @author Armin Reichert
 */
public interface Timing {

	static final int FPS = 60;

	/**
	 * I am still not sure about the correct base speed.
	 * <p>
	 * In Shaun Williams' Pac-Man remake
	 * (https://github.com/masonicGIT/pacman/blob/master/src/Actor.js) there is a
	 * speed table giving the number of steps (=pixels?) Pac-Man is moving in 16
	 * frames. In level 5 this gives 4*2 + 12 = 20 steps in 16 frames, which gives
	 * 1.25 pixels / frame.
	 * <p>
	 * The table from Gamasutra ({@link Game#levels}) states that this corresponds
	 * to 100% base speed for Pac-Man at level 5. Therefore I use 1.25 pixel/frame.
	 * 
	 */
	static final float BASE_SPEED = 1.25f;

	/**
	 * @param fraction fraction of seconds
	 * @return ticks corresponding to given fraction of seconds
	 */
	static int sec(float fraction) {
		return Math.round(FPS * fraction);
	}

	/**
	 * @param fraction fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}
}