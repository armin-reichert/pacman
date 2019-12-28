package de.amr.games.pacman.controller;

import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.model.Timing.sec;

import java.util.Arrays;
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

	private final Logger logger = Logger.getLogger(getClass().getName());
	private final PacManGameCast cast;
	private boolean globalDotCounterEnabled;
	private int ghostDotCountersBySeat[] = new int[4];
	private int globalDotCounter;

	public GhostHouse(PacManGameCast cast) {
		this.cast = cast;
		logger.setLevel(Level.INFO);
	}

	public PacManGame game() {
		return cast.game();
	}

	@Override
	public void init() {
		disableGlobalDotCounter();
		resetGlobalDotCounter();
		resetGhostDotCounters();
	}

	@Override
	public void update() {
		nextCandidate().filter(this::canLeave).ifPresent(ghost -> {
			game().clearPacManStarvingTime();
			ghost.process(new GhostUnlockedEvent());
		});
	}

	public Optional<Ghost> nextCandidate() {
		return Stream.of(cast.blinky, cast.pinky, cast.inky, cast.clyde).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private int personalDotLimit(Ghost ghost) {
		if (ghost == cast.pinky) {
			return 0;
		}
		if (ghost == cast.inky) {
			return game().level().number == 1 ? 30 : 0;
		}
		if (ghost == cast.clyde) {
			return game().level().number == 1 ? 60 : game().level().number == 2 ? 50 : 0;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	private int globalDotLimit(Ghost ghost) {
		if (ghost == cast.pinky) {
			return 7;
		}
		if (ghost == cast.inky) {
			return 17;
		}
		if (ghost == cast.clyde) {
			return 32;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	public void updateDotCounters() {
		if (globalDotCounterEnabled) {
			globalDotCounter++;
			logger.info(() -> String.format("Global dot counter: %d", globalDotCounter));
			if (globalDotCounter == 32 && cast.clyde.is(LOCKED)) {
				disableGlobalDotCounter();
				logger.info(() -> "Global dot counter disabled (Clyde still locked when counter reached 32)");
			}
		} else {
			nextCandidate().ifPresent(ghost -> {
				ghostDotCountersBySeat[ghost.seat] += 1;
				logger.info(() -> String.format("%s's dot counter: %d", ghost.name(), ghostDotCountersBySeat[ghost.seat]));
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
		if (ghost == cast.blinky) {
			return true;
		}
		int pacManStarvingTimeLimit = game().level().number < 5 ? sec(4) : sec(3);
		if (globalDotCounterEnabled) {
			int globalDotLimit = globalDotLimit(ghost);
			if (globalDotCounter >= globalDotLimit) {
				logger.info(
						() -> String.format("%s can leave house: global dot limit (%d) reached", ghost.name(), globalDotLimit));
				return true;
			}
			if (game().pacManStarvingTicks >= pacManStarvingTimeLimit) {
				logger.info(() -> String.format("%s can leave house: Pac-Man's starving time limit (%d ticks) reached",
						ghost.name(), pacManStarvingTimeLimit));
				return true;
			}
			return false;
		}
		int ghostDotLimit = personalDotLimit(ghost);
		if (ghostDotCountersBySeat[ghost.seat] >= ghostDotLimit) {
			logger
					.info(() -> String.format("%s can leave house: ghost's dot limit (%d) reached", ghost.name(), ghostDotLimit));
			return true;
		}
		if (game().pacManStarvingTicks > pacManStarvingTimeLimit) {
			logger.info(() -> String.format("%s can leave house: Pac-Man's starving time limit (%d ticks) reached",
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
		logger.info(() -> "Global dot counter set to zero");
	}

	public void enableAndResetGlobalDotCounter() {
		globalDotCounterEnabled = true;
		globalDotCounter = 0;
		logger.info(() -> "Global dot counter enabled and set to zero");
	}

	public void disableGlobalDotCounter() {
		globalDotCounterEnabled = false;
		logger.info(() -> "Global dot counter disabled (not reset)");
	}

	public void resetGhostDotCounters() {
		Arrays.fill(ghostDotCountersBySeat, 0);
		logger.info(() -> "Ghost dot counters reset to zero");
	}

	public int ghostDotCounter(int seat) {
		return ghostDotCountersBySeat[seat];
	}

	public boolean isGlobalDotCounterEnabled() {
		return globalDotCounterEnabled;
	}
}