package de.amr.games.pacman.controller;

import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.model.Timing.sec;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.PacManGame;

/**
 * The ghost house controls when and in which order ghosts can leave.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Gamasutra</a>
 */
public class GhostHouse implements Lifecycle {

	private static final Logger LOGGER = Logger.getLogger(GhostHouse.class.getName());

	static {
		LOGGER.setLevel(Level.INFO);
	}

	private final PacManGameCast cast;
	private final PacManGame game;
	private boolean globalDotCounterEnabled;
	private int globalDotCounter;

	public GhostHouse(PacManGameCast cast) {
		this.cast = cast;
		game = cast.game();
	}

	@Override
	public void init() {
		resetGlobalDotCounter();
		resetGhostDotCounters();
		disableGlobalDotCounter();
	}

	@Override
	public void update() {
		nextCandidate().filter(this::canLeave).ifPresent(ghost -> {
			ghost.process(new GhostUnlockedEvent());
		});
	}

	public Optional<Ghost> nextCandidate() {
		return Stream.of(cast.blinky, cast.pinky, cast.inky, cast.clyde).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private int ghostDotLimit(Ghost ghost) {
		if (ghost == cast.blinky || ghost == cast.pinky) {
			return 0;
		}
		if (ghost == cast.inky) {
			return game.level().number == 1 ? 30 : 0;
		}
		if (ghost == cast.clyde) {
			return game.level().number == 1 ? 60 : game.level().number == 2 ? 50 : 0;
		}
		throw new IllegalArgumentException("Ghost must be either Blinky, Pinky, Inky or Clyde");
	}

	private int globalDotLimit(Ghost ghost) {
		return (ghost == cast.pinky) ? 7 : (ghost == cast.inky) ? 17 : (ghost == cast.clyde) ? 32 : 0;
	}

	public void updateDotCounters() {
		if (globalDotCounterEnabled) {
			globalDotCounter++;
			LOGGER.info(() -> String.format("Global dot counter: %d", globalDotCounter));
			if (globalDotCounter == 32 && cast.clyde.is(LOCKED)) {
				disableGlobalDotCounter();
				LOGGER.info(() -> "Global dot counter disabled");
			}
		} else {
			nextCandidate().ifPresent(ghost -> {
				ghost.dotCounter++;
				LOGGER.info(() -> String.format("%s's dot counter: %d", ghost.name(), ghost.dotCounter));
			});
		}
	}

	/**
	 * Determines if the given ghost can leave the ghost house.
	 * 
	 * @param ghost a ghost
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private boolean canLeave(Ghost ghost) {
		int pacManStarvingTimeLimit = game.level().number < 5 ? sec(4) : sec(3);
		if (globalDotCounterEnabled) {
			int globalDotLimit = globalDotLimit(ghost);
			if (globalDotCounter >= globalDotLimit) {
				LOGGER.info(
						() -> String.format("%s can leave house: global dot limit (%d) reached", ghost.name(), globalDotLimit));
				return true;
			}
			if (cast.pacMan.starvingTime() > pacManStarvingTimeLimit) {
				LOGGER.info(() -> String.format("%s can leave house: Pac-Man's starving time limit reached (%d ticks)",
						ghost.name(), pacManStarvingTimeLimit));
				return true;
			}
			return false;
		}
		int ghostDotLimit = ghostDotLimit(ghost);
		if (ghost.dotCounter >= ghostDotLimit) {
			LOGGER
					.info(() -> String.format("%s can leave house: ghost's dot limit (%d) reached", ghost.name(), ghostDotLimit));
			return true;
		}
		if (cast.pacMan.starvingTime() > pacManStarvingTimeLimit) {
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
		LOGGER.info(() -> "Global dot counter set to zero");
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
		cast.pinky.dotCounter = cast.inky.dotCounter = cast.clyde.dotCounter = 0;
		LOGGER.info(() -> "Ghost dot counters set to zero");
	}

	public boolean isGlobalDotCounterEnabled() {
		return globalDotCounterEnabled;
	}
}