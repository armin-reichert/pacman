package de.amr.games.pacman.controller;

import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.model.Timing.sec;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;

/**
 * This class controls when and in which order locked ghosts can leave the ghost house.
 * 
 * @author Armin Reichert
 * 
 * @see <a href= "https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Gamasutra</a>
 */
public class House implements Lifecycle {

	private static class DotCounter {

		int dots;
		boolean enabled;
	}

	private final Logger logger;
	private final Cast cast;
	private final DotCounter globalCounter;
	private final int[] ghostCounters;

	public House(Cast cast) {
		this.cast = cast;
		globalCounter = new DotCounter();
		ghostCounters = new int[4];
		logger = Logger.getLogger(getClass().getName());
		logger.setLevel(Level.INFO);
	}

	@Override
	public void init() {
		globalCounter.enabled = false;
		globalCounter.dots = 0;
		resetGhostCounters();
	}

	@Override
	public void update() {
		if (cast.blinky.is(LOCKED)) {
			unlock(cast.blinky);
		}
		nextCandidate().filter(this::canLeave).ifPresent(ghost -> {
			game().clearPacManStarvingTime();
			unlock(ghost);
		});
	}

	public void onFoodFound(FoodFoundEvent e) {
		if (globalCounter.enabled) {
			globalCounter.dots++;
			if (globalCounter.dots == 32 && cast.clyde.is(LOCKED)) {
				globalCounter.enabled = false;
				logger.info(() -> "Global dot counter disabled (Clyde still locked when counter reached 32)");
			}
		}
		else {
			nextCandidate().ifPresent(ghost -> {
				ghostCounters[ghost.seat()] += 1;
			});
		}
	}

	public void onLevelChange() {
		resetGhostCounters();
	}

	public boolean isPreferredGhost(Ghost ghost) {
		return nextCandidate().map(next -> next == ghost).orElse(false);
	}

	public int globalDotCount() {
		return globalCounter.dots;
	}

	public boolean isGlobalDotCounterEnabled() {
		return globalCounter.enabled;
	}

	public void enableAndResetGlobalDotCounter() {
		globalCounter.enabled = true;
		globalCounter.dots = 0;
		logger.info(() -> "Global dot counter enabled and set to zero");
	}

	public int ghostDotCounter(int seat) {
		return ghostCounters[seat];
	}

	private Game game() {
		return cast.game();
	}

	private Optional<Ghost> nextCandidate() {
		return Stream.of(cast.pinky, cast.inky, cast.clyde).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private void unlock(Ghost ghost) {
		ghost.process(new GhostUnlockedEvent());
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

	private void resetGhostCounters() {
		Arrays.fill(ghostCounters, 0);
		logger.info(() -> "Ghost dot counters reset to zero");
	}

	/**
	 * Determines if the given ghost can leave the ghost house.
	 * 
	 * @param ghost
	 *                a ghost
	 * 
	 * @see <a href= "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man Dossier</a>
	 */
	private boolean canLeave(Ghost ghost) {
		int pacManStarvingTimeLimit = game().level().number < 5 ? sec(4) : sec(3);
		if (game().pacManStarvingTicks > pacManStarvingTimeLimit) {
			logger.info(() -> String.format("%s can leave house: Pac-Man's starving time limit (%d ticks) reached",
					ghost.name(), pacManStarvingTimeLimit));
			return true;
		}
		if (globalCounter.enabled) {
			int globalDotLimit = globalDotLimit(ghost);
			if (globalCounter.dots >= globalDotLimit) {
				logger.info(
						() -> String.format("%s can leave house: global dot limit (%d) reached", ghost.name(), globalDotLimit));
				return true;
			}
		}
		int ghostDotLimit = personalDotLimit(ghost);
		if (ghostCounters[ghost.seat()] >= ghostDotLimit) {
			logger
					.info(() -> String.format("%s can leave house: ghost's dot limit (%d) reached", ghost.name(), ghostDotLimit));
			return true;
		}
		return false;
	}
}