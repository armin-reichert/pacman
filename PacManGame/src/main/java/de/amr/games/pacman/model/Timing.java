package de.amr.games.pacman.model;

/**
 * Timimg related methods.
 * 
 * @author Armin Reichert
 */
public interface Timing {

	static final int FPS = 60;

	static final float BASE_SPEED = 11f * Tile.SIZE / FPS; // 11 tiles/second

	/**
	 * @param fraction
	 *                   fraction of seconds
	 * @return ticks corresponding to given fraction of seconds
	 */
	public static int sec(float fraction) {
		return Math.round(FPS * fraction);
	}

	/**
	 * @param fraction
	 *                   fraction of base speed
	 * @return speed (pixels/tick) corresponding to given fraction of base speed
	 */
	public static float speed(float fraction) {
		return fraction * BASE_SPEED;
	}
}