package de.amr.games.pacman.navigation.impl;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.actor.game.Ghost;
import de.amr.games.pacman.actor.game.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

/**
 * Clyde's chase behavior as described
 * <a href="http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>.
 * 
 * <P>
 * <cite> The unique feature of Clyde’s targeting is that it has two separate modes which he
 * constantly switches back and forth between, based on his proximity to Pac-Man. Whenever Clyde
 * needs to determine his target tile, he first calculates his distance from Pac-Man. If he is
 * farther than eight tiles away, his targeting is identical to Blinky’s, using Pac-Man’s current
 * tile as his target. However, as soon as his distance to Pac-Man becomes less than eight tiles,
 * Clyde’s target is set to the same tile as his fixed one in Scatter mode, just outside the
 * bottom-left corner of the maze.</cite>
 * </p>
 * 
 * <p>
 * <cite> The combination of these two methods has the overall effect of Clyde alternating between
 * coming directly towards Pac-Man, and then changing his mind and heading back to his corner
 * whenever he gets too close. On the diagram above, the X marks on the path represent the points
 * where Clyde’s mode switches. If Pac-Man somehow managed to remain stationary in that position,
 * Clyde would indefinitely loop around that T-shaped area. As long as the player is not in the
 * lower-left corner of the maze, Clyde can be avoided completely by simply ensuring that you do not
 * block his “escape route” back to his corner. While Pac-Man is within eight tiles of the
 * lower-left corner, Clyde’s path will end up in exactly the same loop as he would eventually
 * maintain in Scatter mode. </cite>
 * </p>
 */
public class ClydeChaseBehavior {

	static Tile computeTarget(Ghost clyde, PacMan pacMan) {
		double d = Vector2f.dist(clyde.getCenter(), pacMan.getCenter());
		return d >= 8 * Game.TS ? pacMan.getTile() : clyde.getMaze().getClydeScatteringTarget();
	}
}