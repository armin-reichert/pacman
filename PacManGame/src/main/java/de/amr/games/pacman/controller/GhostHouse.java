package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.model.PacManGame.sec;

import java.util.stream.Stream;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;

/**
 * Implements the logic when ghosts can leave their house.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
 *      Dossier</a>
 */
public class GhostHouse {

	public final PacManGameCast cast;

	public GhostHouse(PacManGameCast cast) {
		this.cast = cast;
	}

	/**
	 * The first control used to evaluate when the ghosts leave home is a personal counter each ghost
	 * retains for tracking the number of dots Pac-Man eats. Each ghost's "dot counter" is reset to zero
	 * when a level begins and can only be active when inside the ghost house, but only one ghost's
	 * counter can be active at any given time regardless of how many ghosts are inside.
	 * 
	 * <p>
	 * The order of preference for choosing which ghost's counter to activate is: Pinky, then Inky, and
	 * then Clyde. For every dot Pac-Man eats, the preferred ghost in the house (if any) gets its dot
	 * counter increased by one. Each ghost also has a "dot limit" associated with his counter, per
	 * level.
	 * 
	 * <p>
	 * If the preferred ghost reaches or exceeds his dot limit, it immediately exits the house and its
	 * dot counter is deactivated (but not reset). The most-preferred ghost still waiting inside the
	 * house (if any) activates its timer at this point and begins counting dots.
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	public boolean canLeave(Ghost ghost) {
		if (ghost == cast.blinky) {
			return true;
		}
		/*@formatter:off*/
		Ghost nextGhost = Stream.of(cast.pinky, cast.inky, cast.clyde)
				.filter(g -> g.getState() == GhostState.LOCKED)
				.findFirst()
				.orElse(null);
		/*@formatter:on*/

		if (ghost != nextGhost) {
			return false;
		}
		if (ghost.foodCount >= foodLimit(ghost)) {
			return true;
		}
		if (cast.game.globalFoodCounterEnabled && cast.game.globalFoodCount >= globalFoodCounterLimit(ghost)) {
			return true;
		}
		int timeout = cast.game.level.number < 5 ? sec(4) : sec(3);
		if (cast.pacMan.ticksSinceLastMeal > timeout) {
			LOGGER.info(() -> String.format("Releasing ghost %s (Pac-Man eat timer expired)", ghost.name()));
			return true;
		}
		return false;
	}

	public void updateFoodCounter() {
		if (cast.game.globalFoodCounterEnabled) {
			cast.game.globalFoodCount++;
			LOGGER.fine(() -> String.format("Global Food Counter=%d", cast.game.globalFoodCount));
			if (cast.game.globalFoodCount == 32 && cast.clyde.getState() == GhostState.LOCKED) {
				cast.game.globalFoodCounterEnabled = false;
				cast.game.globalFoodCount = 0;
			}
			return;
		}
		/*@formatter:off*/
		Stream.of(cast.pinky, cast.inky, cast.clyde)
			.filter(ghost -> ghost.getState() == GhostState.LOCKED)
			.findFirst()
			.ifPresent(preferredGhost -> {
				preferredGhost.foodCount += 1;
				LOGGER.fine(() -> String.format("Food Counter[%s]=%d", preferredGhost.name(), preferredGhost.foodCount));
		});
		/*@formatter:on*/
	}

	/**
	 * Pinky's dot limit is always set to zero, causing him to leave home immediately when every level
	 * begins. For the first level, Inky has a limit of 30 dots, and Clyde has a limit of 60. This
	 * results in Pinky exiting immediately which, in turn, activates Inky's dot counter. His counter
	 * must then reach or exceed 30 dots before he can leave the house.
	 * 
	 * <p>
	 * Once Inky starts to leave, Clyde's counter (which is still at zero) is activated and starts
	 * counting dots. When his counter reaches or exceeds 60, he may exit. On the second level, Inky's
	 * dot limit is changed from 30 to zero, while Clyde's is changed from 60 to 50. Inky will exit the
	 * house as soon as the level begins from now on.
	 * 
	 * <p>
	 * Starting at level three, all the ghosts have a dot limit of zero for the remainder of the game
	 * and will leave the ghost house immediately at the start of every level.
	 * 
	 * @param ghost
	 *                a ghost
	 * @return the ghosts's current food limit
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private int foodLimit(Ghost ghost) {
		if (ghost == cast.pinky) {
			return 0;
		}
		if (ghost == cast.inky) {
			return cast.game.level.number == 1 ? 30 : 0;
		}
		if (ghost == cast.clyde) {
			return cast.game.level.number == 1 ? 60 : cast.game.level.number == 2 ? 50 : 0;
		}
		return 0;
	}

	private int globalFoodCounterLimit(Ghost ghost) {
		return (ghost == cast.pinky) ? 7 : (ghost == cast.inky) ? 17 : (ghost == cast.clyde) ? 32 : 0;
	}
}