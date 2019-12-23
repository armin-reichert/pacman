package de.amr.games.pacman.controller;

import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.model.Timing.sec;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.PacManGame;

/**
 * Implements the rules controlling when the ghosts can leave their house.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Gamasutra</a>
 */
public class GhostHouse {

	private static final Logger LOGGER = Logger.getLogger(GhostHouse.class.getName());

	static {
		LOGGER.setLevel(Level.INFO);
	}

	private final PacManGame game;
	private final PacMan pacMan;
	private final Ghost blinky, pinky, inky, clyde;
	private boolean globalDotCounterEnabled;
	private int globalDotCounter;

	public GhostHouse(PacManGameCast cast) {
		game = cast.game;
		pacMan = cast.pacMan;
		blinky = cast.blinky;
		pinky = cast.pinky;
		inky = cast.inky;
		clyde = cast.clyde;
		disableGlobalDotCounter();
	}

	public Optional<Ghost> preferredLockedGhost() {
		return Stream.of(pinky, inky, clyde).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private int ghostDotLimit(Ghost ghost) {
		if (ghost == pinky) {
			return 0;
		}
		if (ghost == inky) {
			return game.level.number == 1 ? 30 : 0;
		}
		if (ghost == clyde) {
			return game.level.number == 1 ? 60 : game.level.number == 2 ? 50 : 0;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	private int globalDotLimit(Ghost ghost) {
		return (ghost == pinky) ? 7 : (ghost == inky) ? 17 : (ghost == clyde) ? 32 : 0;
	}

	public void updateDotCounters() {
		if (globalDotCounterEnabled) {
			globalDotCounter++;
			LOGGER.info(() -> String.format("Global dot counter: %d", globalDotCounter));
			if (globalDotCounter == 32 && clyde.is(LOCKED)) {
				disableGlobalDotCounter();
				LOGGER.info(() -> "Global dot counter disabled");
			}
		}
		else {
			preferredLockedGhost().ifPresent(ghost -> {
				ghost.dotCounter++;
				LOGGER.info(() -> String.format("%s's dot counter: %d", ghost.name(), ghost.dotCounter));
			});
		}
	}

	/**
	 * Determines if the given ghost can leave the ghost house.
	 * 
	 * @param ghost
	 *                a ghost
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	public boolean isReleasing(Ghost ghost) {
		if (ghost == blinky) {
			return true;
		}
		Optional<Ghost> ghostToRelease = preferredLockedGhost();
		if (!ghostToRelease.isPresent() || ghostToRelease.get() != ghost) {
			return false;
		}
		int pacManStarvingTimeLimit = game.level.number < 5 ? sec(4) : sec(3);
		if (globalDotCounterEnabled) {
			int globalDotLimit = globalDotLimit(ghost);
			if (globalDotCounter >= globalDotLimit) {
				LOGGER.info(() -> String.format("%s can leave house: global dot limit (%d) reached", ghost.name(),
						globalDotLimit));
				return true;
			}
			if (pacMan.starvingTime() > pacManStarvingTimeLimit) {
				LOGGER
						.info(() -> String.format("%s can leave house: Pac-Man's starving time limit reached (%d ticks)",
								ghost.name(), pacManStarvingTimeLimit));
				return true;
			}
			return false;
		}
		int ghostDotLimit = ghostDotLimit(ghost);
		if (ghost.dotCounter >= ghostDotLimit) {
			LOGGER.info(() -> String.format("%s can leave house: ghost's dot limit (%d) reached", ghost.name(),
					ghostDotLimit));
			return true;
		}
		if (pacMan.starvingTime() > pacManStarvingTimeLimit) {
			LOGGER.info(() -> String.format("%s can leave house: Pac-Man's starving time limit reached (%d ticks)",
					ghost.name(), pacManStarvingTimeLimit));
			return true;
		}
		return false;
	}

	public int globalDotCounter() {
		return globalDotCounter;
	}

	public void resetGlobalDotCounter() {
		globalDotCounter = 0;
	}

	public void enableGlobalDotCounter() {
		globalDotCounterEnabled = true;
		globalDotCounter = 0;
		LOGGER.info(() -> "Global dot counter enabled and set to zero");
	}

	public void disableGlobalDotCounter() {
		globalDotCounterEnabled = false;
		LOGGER.info(() -> "Global dot counter disabled (not reset)");
	}

	public void resetGhostDotCounters() {
		pinky.dotCounter = inky.dotCounter = clyde.dotCounter = 0;
		LOGGER.info(() -> "Ghost dot counters set to zero");
	}

	public boolean isGlobalDotCounterEnabled() {
		return globalDotCounterEnabled;
	}
}