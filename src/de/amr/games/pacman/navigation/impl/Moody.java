package de.amr.games.pacman.navigation.impl;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.actor.game.Ghost;
import de.amr.games.pacman.actor.game.PacMan;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

/**
 * Inky's behaviour as described
 * <a href="http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>.
 * 
 * <p>
 * <cite> The blue ghost is nicknamed Inky, and remains inside the ghost house for a short time on
 * the first level, not joining the chase until Pac-Man has managed to consume at least 30 of the
 * dots. His English personality description is bashful, while in Japanese he is referred to as 気紛れ,
 * kimagure, or “whimsical”.
 * </p>
 * 
 * <p>
 * <cite>Inky is difficult to predict, because he is the only one of the ghosts that uses a factor
 * other than Pac-Man’s position/orientation when determining his target tile. Inky actually uses
 * both Pac-Man’s position/facing as well as Blinky’s (the red ghost’s) position in his calculation.
 * To locate Inky’s target, we first start by selecting the position two tiles in front of Pac-Man
 * in his current direction of travel, similar to Pinky’s targeting method. From there, imagine
 * drawing a vector from Blinky’s position to this tile, and then doubling the length of the vector.
 * The tile that this new, extended vector ends on will be Inky’s actual target.</cite>
 * </p>
 */
class Moody implements Navigation {

	private final Ghost inky;
	private final Ghost blinky;
	private final PacMan pacMan;

	public Moody(Ghost inky, Ghost blinky, PacMan pacMan) {
		this.inky = inky;
		this.blinky = blinky;
		this.pacMan = pacMan;
	}

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute result = new MazeRoute();

		return result;
	}
}