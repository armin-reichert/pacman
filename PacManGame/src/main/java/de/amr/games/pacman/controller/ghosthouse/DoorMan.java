package de.amr.games.pacman.controller.ghosthouse;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.ghosthouse.Decision.confirmed;
import static de.amr.games.pacman.controller.ghosthouse.Decision.rejected;
import static de.amr.games.pacman.model.game.PacManGame.game;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.game.Timing;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.components.Door.DoorState;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.Tile;

/**
 * This class controls when and in which order locked ghosts can leave the ghost house.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Gamasutra</a>
 */
public class DoorMan implements Lifecycle {

	private final House house;
	private final Folks folks;
	private final DotCounter globalCounter;
	private final int[] ghostCounters;
	private int pacManStarvingTicks;

	public DoorMan(House house, Folks folks) {
		this.house = house;
		this.folks = folks;
		globalCounter = new DotCounter();
		ghostCounters = new int[4];
	}

	@Override
	public void init() {
		globalCounter.enabled = false;
		globalCounter.dots = 0;
		resetGhostDotCounters();
		closeDoor(house.door(0));
	}

	@Override
	public void update() {
		preferredLockedGhost().ifPresent(ghost -> {
			Decision decision = makeDecisionAboutReleasing(ghost);
			if (decision.confirmed) {
				loginfo(decision.reason);
				unlock(ghost);
			}
		});
		pacManStarvingTicks += 1;

		house.doors().forEach(this::closeDoor);
		house.doors().filter(this::isOpeningRequested).forEach(this::openDoor);
	}

	public void onPacManFoundFood() {
		pacManStarvingTicks = 0;
		if (globalCounter.enabled) {
			globalCounter.dots++;
			if (globalCounter.dots == 32 && folks.clyde.ai.is(LOCKED)) {
				globalCounter.dots = 0;
				globalCounter.enabled = false;
				loginfo("Global dot counter reset and disabled (Clyde was locked when counter reached 32)");
			}
		} else {
			preferredLockedGhost().ifPresent(ghost -> {
				ghostCounters[index(ghost)] += 1;
			});
		}
	}

	public void onLifeLost() {
		globalCounter.enabled = true;
		globalCounter.dots = 0;
		loginfo("Global dot counter enabled and set to zero");
	}

	public void onLevelChange() {
		resetGhostDotCounters();
	}

	public boolean isPreferredGhost(Ghost ghost) {
		return preferredLockedGhost().orElse(null) == ghost;
	}

	/**
	 * Determines if the given ghost can leave the ghost house.
	 * 
	 * @param ghost a ghost
	 * @return if the ghost can leave
	 */
	public boolean canLeave(Ghost ghost) {
		return makeDecisionAboutReleasing(ghost).confirmed;
	}

	public int globalDotCount() {
		return globalCounter.dots;
	}

	public boolean isGlobalDotCounterEnabled() {
		return globalCounter.enabled;
	}

	public int ghostDotCount(Ghost ghost) {
		return ghostCounters[index(ghost)];
	}

	public int personalDotLimit(Ghost ghost) {
		if (ghost == folks.pinky) {
			return 0;
		}
		if (ghost == folks.inky) {
			return game.level == 1 ? 30 : 0;
		}
		if (ghost == folks.clyde) {
			return game.level == 1 ? 60 : game.level == 2 ? 50 : 0;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	public int globalDotLimit(Ghost ghost) {
		if (ghost == folks.pinky) {
			return 7;
		}
		if (ghost == folks.inky) {
			return 17;
		}
		if (ghost == folks.clyde) {
			return 32;
		}
		throw new IllegalArgumentException("Ghost must be either Pinky, Inky or Clyde");
	}

	public int pacManStarvingTicks() {
		return pacManStarvingTicks;
	}

	public Optional<Ghost> preferredLockedGhost() {
		//@formatter:off
		return Stream.of(folks.blinky, folks.pinky, folks.inky, folks.clyde)
				.filter(ghost -> ghost.world.contains(ghost))
				.filter(ghost -> ghost.ai.is(LOCKED)).findFirst();
		//@formatter:on
	}

	public void closeDoor(Door door) {
		door.state = DoorState.CLOSED;
	}

	public void openDoor(Door door) {
		door.state = DoorState.OPEN;
	}

	private void unlock(Ghost ghost) {
		ghost.ai.process(new GhostUnlockedEvent());
	}

	private boolean isOpeningRequested(Door door) {
		//@formatter:off
		return folks.ghostsInWorld()
				.filter(ghost -> ghost.ai.is(ENTERING_HOUSE, LEAVING_HOUSE))
				.filter(ghost -> isGhostNearDoor(ghost, door))
				.findAny()
				.isPresent();
		//@formatter:on
	}

	private boolean isGhostNearDoor(Ghost ghost, Door door) {
		Tile fromGhostTowardsHouse = ghost.world.neighbor(ghost.tile(), door.intoHouse);
		Tile fromGhostAwayFromHouse = ghost.world.neighbor(ghost.tile(), door.intoHouse.opposite());
		return door.includes(ghost.tile()) || door.includes(fromGhostAwayFromHouse) || door.includes(fromGhostTowardsHouse);
	}

	private long pacManStarvingTimeLimit() {
		return game.level < 5 ? Timing.sec(4) : Timing.sec(3);
	}

	/**
	 * @param ghost a ghost
	 * @return decision why ghost can leave
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private Decision makeDecisionAboutReleasing(Ghost ghost) {
		if (!ghost.ai.is(LOCKED)) {
			return confirmed("Ghost is not locked");
		}
		if (ghost == folks.blinky) {
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
			if (ghostCounters[index(ghost)] >= personalLimit) {
				return confirmed("%s can leave house: ghost's dot limit (%d) reached", ghost.name, personalLimit);
			}
		}
		return rejected("");
	}

	private void resetGhostDotCounters() {
		Arrays.fill(ghostCounters, 0);
		loginfo("Ghost dot counters have been reset to zero");
	}

	private int index(Ghost ghost) {
		if (ghost == folks.blinky) {
			return 0;
		}
		if (ghost == folks.inky) {
			return 1;
		}
		if (ghost == folks.pinky) {
			return 2;
		}
		if (ghost == folks.clyde) {
			return 3;
		}
		throw new IllegalArgumentException();
	}
}