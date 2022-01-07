/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.controller.ghosthouse;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.game.Timing.sec;
import static de.amr.games.pacman.controller.ghosthouse.Decision.confirmed;
import static de.amr.games.pacman.controller.ghosthouse.Decision.rejected;
import static de.amr.games.pacman.model.game.PacManGame.game;

import java.util.Arrays;
import java.util.Optional;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.components.Door.DoorState;
import de.amr.games.pacman.model.world.components.House;

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
	private final Ghost[] ghost_preference;
	private final DotCounter globalCounter;
	private final int[] ghostCounters;
	private int pacManStarvingTicks;

	public DoorMan(House house, Folks folks) {
		this.house = house;
		this.folks = folks;
		ghost_preference = new Ghost[] { folks.blinky, folks.pinky, folks.inky, folks.clyde };
		globalCounter = new DotCounter();
		ghostCounters = new int[4];
	}

	@Override
	public void init() {
		globalCounter.enabled = false;
		globalCounter.dots = 0;
		resetGhostDotCounters();
		house.doors().forEach(this::closeDoor);
	}

	@Override
	public void update() {
		preferredLockedGhost().ifPresent(ghost -> {
			Decision decision = decideIfGhostCanLeaveHouse(ghost);
			if (decision.confirmed) {
				loginfo(decision.reason);
				unlock(ghost);
			}
		});
		pacManStarvingTicks += 1;
		house.doors().forEach(this::closeDoor);
		house.doors().filter(this::isOpeningDoorRequested).forEach(this::openDoor);
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

	public void onPacManLostLife() {
		globalCounter.enabled = true;
		globalCounter.dots = 0;
		loginfo("Global dot counter enabled and set to zero (Pac-Man lost life)");
	}

	public void onLevelChange() {
		resetGhostDotCounters();
	}

	public boolean isPreferredLockedGhost(Ghost ghost) {
		return preferredLockedGhost().orElse(null) == ghost;
	}

	/**
	 * Determines if the given ghost can leave the ghost house.
	 * 
	 * @param ghost a ghost
	 * @return if the ghost can leave
	 */
	public boolean canLeaveHouse(Ghost ghost) {
		return decideIfGhostCanLeaveHouse(ghost).confirmed;
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
		return Arrays.stream(ghost_preference)
			.filter(ghost -> ghost.world.contains(ghost))
			.filter(ghost -> ghost.ai.is(LOCKED))
			.findFirst();
		//@formatter:on
	}

	public void closeDoor(Door door) {
		door.state = DoorState.CLOSED;
	}

	public void openDoor(Door door) {
		door.state = DoorState.OPEN;
	}

	private void resetGhostDotCounters() {
		Arrays.fill(ghostCounters, 0);
		loginfo("Ghost dot counters have been reset to zero");
	}

	private int index(Ghost ghost) {
		return ghost.personality.ordinal();
	}

	private void unlock(Ghost ghost) {
		ghost.ai.process(new GhostUnlockedEvent());
	}

	private boolean isOpeningDoorRequested(Door door) {
		//@formatter:off
		return folks.ghostsInWorld()
			.filter(ghost -> ghost.ai.is(ENTERING_HOUSE, LEAVING_HOUSE))
			.anyMatch(ghost -> isGhostNearDoor(ghost, door));
		//@formatter:on
	}

	private boolean isGhostNearDoor(Ghost ghost, Door door) {
		Tile fromGhostTowardsHouse = ghost.world.neighbor(ghost.tile(), door.intoHouse);
		Tile fromGhostAwayFromHouse = ghost.world.neighbor(ghost.tile(), door.intoHouse.opposite());
		return door.includes(ghost.tile()) || door.includes(fromGhostAwayFromHouse) || door.includes(fromGhostTowardsHouse);
	}

	private long pacManStarvingTimeLimit() {
		return game.level < 5 ? sec(4) : sec(3);
	}

	/**
	 * @param ghost a ghost
	 * @return decision why ghost can leave
	 * 
	 * @see <a href=
	 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=4">Pac-Man
	 *      Dossier</a>
	 */
	private Decision decideIfGhostCanLeaveHouse(Ghost ghost) {
		if (!ghost.ai.is(LOCKED)) {
			return confirmed("Ghost is not locked, can leave house");
		}
		if (ghost == folks.blinky) {
			return confirmed("%s can always leave house", ghost.name);
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
}