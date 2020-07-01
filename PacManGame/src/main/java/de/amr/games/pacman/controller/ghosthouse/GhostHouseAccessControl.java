package de.amr.games.pacman.controller.ghosthouse;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.ghosthouse.Decision.confirmed;
import static de.amr.games.pacman.controller.ghosthouse.Decision.rejected;
import static de.amr.games.pacman.model.Game.sec;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Door;
import de.amr.games.pacman.model.world.core.House;

/**
 * This class controls when and in which order locked ghosts can leave the ghost house.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Gamasutra</a>
 */
public class GhostHouseAccessControl implements Lifecycle {

	private final Game game;
	private final World world;
	private final House house;
	private final Ghost blinky;
	private final Ghost pinky;
	private final Ghost inky;
	private final Ghost clyde;
	private final DotCounter globalCounter;
	private final int[] ghostCounters;
	private int pacManStarvingTicks;

	public GhostHouseAccessControl(Game game, World world, House house) {
		this.game = game;
		this.world = world;
		this.house = house;
		blinky = world.population().blinky();
		pinky = world.population().pinky();
		inky = world.population().inky();
		clyde = world.population().clyde();
		globalCounter = new DotCounter();
		ghostCounters = new int[4];
	}

	@Override
	public void init() {
		globalCounter.enabled = false;
		globalCounter.dots = 0;
		resetGhostDotCounters();
	}

	@Override
	public void update() {
		preferredLockedGhost().ifPresent(ghost -> {
			Decision decision = decisionAboutRelease(ghost);
			if (decision.confirmed) {
				loginfo(decision.reason);
				unlock(ghost);
			}
		});
		pacManStarvingTicks += 1;
		boolean open = world.population().ghosts().filter(world::included).count() > 0
				&& world.population().ghosts().anyMatch(ghost -> ghost.is(ENTERING_HOUSE, LEAVING_HOUSE));
		house.doors().forEach(door -> door.state = open ? Door.DoorState.OPEN : Door.DoorState.CLOSED);
	}

	/**
	 * @param ghost a ghost
	 * @return decision why ghost can leave
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private Decision decisionAboutRelease(Ghost ghost) {
		if (!ghost.is(LOCKED)) {
			return confirmed("Ghost is not locked");
		}
		if (ghost == blinky) {
			return confirmed("%s can always leave", ghost.name);
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
			if (ghostCounters[ghost.bed().number] >= personalLimit) {
				return confirmed("%s can leave house: ghost's dot limit (%d) reached", ghost.name, personalLimit);
			}
		}
		return rejected("");
	}

	public void onPacManFoundFood() {
		pacManStarvingTicks = 0;
		if (globalCounter.enabled) {
			globalCounter.dots++;
			if (globalCounter.dots == 32 && clyde.is(LOCKED)) {
				globalCounter.dots = 0;
				globalCounter.enabled = false;
				loginfo("Global dot counter reset and disabled (Clyde was locked when counter reached 32)");
			}
		} else {
			preferredLockedGhost().ifPresent(ghost -> {
				ghostCounters[ghost.bed().number] += 1;
			});
		}
	}

	public void onLevelChange() {
		resetGhostDotCounters();
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
		return ghostCounters[ghost.bed().number];
	}

	public int pacManStarvingTicks() {
		return pacManStarvingTicks;
	}

	public Optional<Ghost> preferredLockedGhost() {
		return Stream.of(blinky, pinky, inky, clyde).filter(world::included).filter(ghost -> ghost.is(LOCKED)).findFirst();
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
		return decisionAboutRelease(ghost).confirmed;
	}

	private int pacManStarvingTimeLimit() {
		return game.level.number < 5 ? sec(4) : sec(3);
	}

	public int personalDotLimit(Ghost ghost) {
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

	public int globalDotLimit(Ghost ghost) {
		if (ghost == pinky) {
			return 7;
		}
		if (ghost == inky) {
			return 17;
		}
		if (ghost == clyde) {
			return 32;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	private void resetGhostDotCounters() {
		Arrays.fill(ghostCounters, 0);
		loginfo("Ghost dot counters reset to zero");
	}
}