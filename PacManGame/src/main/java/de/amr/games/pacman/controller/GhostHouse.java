package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.model.Timing.sec;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.PacManGame;

/**
 * Implements the rules when the ghosts can leave the ghost house.
 * 
 * @author Armin Reichert
 */
public class GhostHouse {

	private final PacManGameCast cast;
	private final PacManGame game;

	public GhostHouse(PacManGameCast cast) {
		this.cast = cast;
		game = cast.game;
		game.globalDotCounterEnabled = false;
	}

	/**
	 * Determines if the given ghost can leave the ghost house.
	 * 
	 * @param ghost
	 *                      a ghost
	 * @param levelNumber
	 *                      the level number
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	public boolean canLeaveHouse(Ghost ghost, int levelNumber) {
		if (ghost == cast.blinky) {
			return true;
		}
		Optional<Ghost> nextGhostToLeaveHouse = preferredLockedGhost();
		if (!nextGhostToLeaveHouse.isPresent() || nextGhostToLeaveHouse.get() != ghost) {
			return false;
		}
		int ghostDotLimit = ghostDotLimit(ghost, levelNumber);
		if (ghost.dotCounter >= ghostDotLimit) {
			LOGGER.info(() -> String.format("%s can leave house: ghost's dot limit (%d) reached", ghost.name(),
					ghostDotLimit));
			return true;
		}
		if (game.globalDotCounterEnabled) {
			int globalDotLimit = globalDotLimit(ghost);
			if (game.globalDotCounter >= globalDotLimit) {
				LOGGER.info(() -> String.format("%s can leave house: global dot limit (%d) reached", ghost.name(),
						globalDotLimit));
				return true;
			}
		}
		int timeout = levelNumber < 5 ? sec(4) : sec(3);
		if (cast.pacMan.ticksSinceLastMeal() > timeout) {
			LOGGER.info(() -> String.format("%s can leave house: Pac-Man's eat timeout (%d ticks) reached",
					ghost.name(), timeout));
			return true;
		}
		return false;
	}

	private Optional<Ghost> preferredLockedGhost() {
		return Stream.of(cast.pinky, cast.inky, cast.clyde).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	public void enableGlobalDotCounter() {
		game.globalDotCounterEnabled = true;
		game.globalDotCounter = 0;
		LOGGER.info(() -> "Global dot counter enabled and set to zero");
	}

	public void disableGlobalDotCounter() {
		game.globalDotCounterEnabled = false;
		LOGGER.info(() -> "Global dot counter disabled (not reset)");
	}

	public void resetGhostDotCounters() {
		cast.ghosts().forEach(ghost -> ghost.dotCounter = 0);
		LOGGER.info(() -> "Ghost dot counters enabled and set to zero");
	}

	public void updateDotCounters() {
		if (game.globalDotCounterEnabled) {
			game.globalDotCounter++;
			LOGGER.fine(() -> String.format("Global dot counter: %d", game.globalDotCounter));
			if (game.globalDotCounter == 32 && cast.clyde.is(LOCKED)) {
				game.globalDotCounterEnabled = false;
				game.globalDotCounter = 0;
				LOGGER.info(() -> "Global dot counter reset to zero");
			}
		}
		else {
			preferredLockedGhost().ifPresent(ghost -> {
				ghost.dotCounter++;
				LOGGER.fine(() -> String.format("%s's dot counter: %d", ghost.name(), ghost.dotCounter));
			});
		}
	}

	private int ghostDotLimit(Ghost ghost, int levelNumber) {
		if (ghost == cast.pinky) {
			return 0;
		}
		if (ghost == cast.inky) {
			return levelNumber == 1 ? 30 : 0;
		}
		if (ghost == cast.clyde) {
			return levelNumber == 1 ? 60 : levelNumber == 2 ? 50 : 0;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	private int globalDotLimit(Ghost ghost) {
		return (ghost == cast.pinky) ? 7 : (ghost == cast.inky) ? 17 : (ghost == cast.clyde) ? 32 : 0;
	}
}