package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.Decision.confirmed;
import static de.amr.games.pacman.controller.Decision.rejected;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.model.Game.sec;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.Habitat;

/**
 * This class controls when and in which order locked ghosts can leave the ghost house.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Gamasutra</a>
 */
public class GhostHouseAccess {

	private final Game game;
	private final Habitat world;
	private final DotCounter globalCounter;
	private final int[] ghostDotCount;
	private int pacManStarvingTicks;

	public GhostHouseAccess(Game game, Habitat world) {
		this.game = game;
		this.world = world;
		globalCounter = new DotCounter();
		ghostDotCount = new int[4];
	}

	public void init() {
		globalCounter.enabled = false;
		globalCounter.dots = 0;
		resetGhostDotCount();
	}

	public void update() {
		if (world.takesPart(world.blinky()) && world.blinky().is(LOCKED)) {
			unlock(world.blinky());
		}
		Ghost nextToLeave = preferredLockedGhost().orElse(null);
		if (nextToLeave != null) {
			Decision decision = decideIfGhostCanLeave(nextToLeave);
			if (decision.confirmed) {
				loginfo(decision.reason);
				unlock(nextToLeave);
			}
		}
		pacManStarvingTicks += 1;
	}

	public void onPacManFoundFood() {
		pacManStarvingTicks = 0;
		if (globalCounter.enabled) {
			globalCounter.dots++;
			if (globalCounter.dots == 32 && world.clyde().is(LOCKED)) {
				globalCounter.dots = 0;
				globalCounter.enabled = false;
				loginfo("Global dot counter reset and disabled (Clyde was locked when counter reached 32)");
			}
		} else {
			preferredLockedGhost().ifPresent(ghost -> {
				ghostDotCount[ghost.seat().number] += 1;
			});
		}
	}

	public void onLevelChange() {
		resetGhostDotCount();
	}

	public void onLifeLost() {
		globalCounter.enabled = true;
		globalCounter.dots = 0;
		loginfo("Global dot counter enabled and set to zero");
	}

	public boolean isPreferredGhost(Ghost ghost) {
		return preferredLockedGhost().orElse(null) == ghost;
	}

	public int globalDotCount() {
		return globalCounter.dots;
	}

	public boolean isGlobalDotCounterEnabled() {
		return globalCounter.enabled;
	}

	public int ghostDotCount(Ghost ghost) {
		return ghostDotCount[ghost.seat().number];
	}

	public int pacManStarvingTicks() {
		return pacManStarvingTicks;
	}

	public Optional<Ghost> preferredLockedGhost() {
		return Stream.of(world.pinky(), world.inky(), world.clyde()).filter(world::takesPart).filter(ghost -> ghost.is(LOCKED))
				.findFirst();
	}

	private void unlock(Ghost ghost) {
		ghost.process(new GhostUnlockedEvent());
	}

	/**
	 * Determines if the given ghost can leave the ghost house.
	 * 
	 * @param ghost a ghost
	 * @return if the ghost can leave
	 */
	public boolean canLeave(Ghost ghost) {
		return decideIfGhostCanLeave(ghost).confirmed;
	}

	/**
	 * @param ghost a ghost
	 * @return decision why ghost can leave
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private Decision decideIfGhostCanLeave(Ghost ghost) {
		if (!ghost.is(LOCKED)) {
			return confirmed("Ghost is not locked");
		}
		if (pacManStarvingTicks >= pacManStarvingTimeLimit()) {
			pacManStarvingTicks = 0;
			return confirmed("%s can leave house: Pac-Man's starving time limit (%d ticks) reached", ghost.name,
					pacManStarvingTimeLimit());
		}
		if (globalCounter.enabled) {
			int globalLimit = globalDotLimit(ghost);
			if (globalCounter.dots >= globalLimit) {
				return confirmed("%s can leave house: global dot limit (%d) reached", ghost.name, globalLimit);
			}
		} else {
			int personalLimit = personalDotLimit(ghost);
			if (ghostDotCount[ghost.seat().number] >= personalLimit) {
				return confirmed("%s can leave house: ghost's dot limit (%d) reached", ghost.name, personalLimit);
			}
		}
		return rejected("");
	}

	private int pacManStarvingTimeLimit() {
		return game.level.number < 5 ? sec(4) : sec(3);
	}

	public int personalDotLimit(Ghost ghost) {
		if (ghost == world.pinky()) {
			return 0;
		}
		if (ghost == world.inky()) {
			return game.level.number == 1 ? 30 : 0;
		}
		if (ghost == world.clyde()) {
			return game.level.number == 1 ? 60 : game.level.number == 2 ? 50 : 0;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	public int globalDotLimit(Ghost ghost) {
		if (ghost == world.pinky()) {
			return 7;
		}
		if (ghost == world.inky()) {
			return 17;
		}
		if (ghost == world.clyde()) {
			return 32;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	private void resetGhostDotCount() {
		Arrays.fill(ghostDotCount, 0);
		loginfo("Ghost dot counters reset to zero");
	}
}

class Decision {
	public boolean confirmed;
	public String reason;

	static Decision confirmed(String msg, Object... args) {
		Decision d = new Decision();
		d.confirmed = true;
		d.reason = String.format(msg, args);
		return d;
	}

	static Decision rejected(String msg, Object... args) {
		Decision d = new Decision();
		d.confirmed = false;
		d.reason = String.format(msg, args);
		return d;
	}
}

class DotCounter {
	int dots;
	boolean enabled;
}