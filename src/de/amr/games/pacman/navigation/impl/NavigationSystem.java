package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.navigation.impl.FollowTargetTile.aheadOf;

import java.util.function.Supplier;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.actor.game.Ghost;
import de.amr.games.pacman.actor.game.PacMan;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.Navigation;

/**
 * Factory for navigation behaviors.
 * 
 * @author Armin Reichert
 */
public interface NavigationSystem {

	public static Navigation ambush(MazeMover victim) {
		return new FollowTargetTile(() -> aheadOf(victim, 4));
	}

	public static Navigation bounce() {
		return new Bounce();
	}

	public static Navigation chase(MazeMover victim) {
		return new FollowTargetTile(victim::getTile);
	}

	public static Navigation flee(MazeMover chaser) {
		return new Flee(chaser);
	}

	public static Navigation followKeyboard(int... nesw) {
		return new FollowKeyboard(nesw);
	}

	public static Navigation followTargetTile(Supplier<Tile> targetTileSupplier) {
		return new FollowTargetTile(targetTileSupplier);
	}

	public static Navigation forward() {
		return new Forward();
	}

	public static Navigation go(Tile target) {
		return new FollowPath(target);
	}

	public static Navigation inkyChaseBehavior(Ghost blinky, PacMan pacMan) {
		return new FollowTargetTile(() -> InkyChaseBehavior.computeTarget(blinky, pacMan));
	}

	public static Navigation clydeChaseBehavior(Ghost clyde, PacMan pacMan) {
		return new FollowTargetTile(() -> ClydeChaseBehavior.computeTarget(clyde, pacMan));
	}
	
	public static Navigation scatter(Tile scatteringTarget) {
		return new FollowTargetTile(() -> scatteringTarget);
	}

	public static Navigation stayBehind() {
		return new StayBehind();
	}
}