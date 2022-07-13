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
package de.amr.games.pacman.controller.creatures;

import static de.amr.games.pacman.controller.creatures.ghost.Ghost.bashfulGhost;
import static de.amr.games.pacman.controller.creatures.ghost.Ghost.pokeyGhost;
import static de.amr.games.pacman.controller.creatures.ghost.Ghost.shadowGhost;
import static de.amr.games.pacman.controller.creatures.ghost.Ghost.speedyGhost;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.you;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.components.House;

/**
 * The folks from the original game with their individual behaviors.
 * 
 * @author Armin Reichert
 */
public class Folks {

	public final PacMan pacMan;
	public final Ghost blinky;
	public final Ghost pinky;
	public final Ghost inky;
	public final Ghost clyde;

	public Folks(TiledWorld world, House ghostHouse) {

		pacMan = new PacMan(world, "Pac-Man");

		blinky = shadowGhost(world, "Blinky", pacMan);
		inky = bashfulGhost(world, "Inky", pacMan);
		pinky = speedyGhost(world, "Pinky", pacMan);
		clyde = pokeyGhost(world, "Clyde", pacMan);

		ghosts().forEach(ghost -> ghost.house = ghostHouse);

		blinky.bed = ghostHouse.bed(0);
		inky.bed = ghostHouse.bed(1);
		pinky.bed = ghostHouse.bed(2);
		clyde.bed = ghostHouse.bed(3);

		// define behavior

		you(pacMan).followTheCursorKeys().ok();

		Door door = ghostHouse.door(0);
		Tile houseEntry = world.neighbor(door.tiles().findFirst().orElse(null), door.intoHouse.opposite());

		ghosts().forEach(ghost -> {
			you(ghost).when(LOCKED).bounceOnBed().ok();
			you(ghost).when(ENTERING_HOUSE).enterDoorAndGoToBed().door(door).ok();
			you(ghost).when(LEAVING_HOUSE).leaveHouse().house(ghostHouse).ok();
			you(ghost).when(FRIGHTENED).moveRandomly().ok();
			you(ghost).when(DEAD).headFor().tile(houseEntry).ok();
		});

		you(blinky).when(ENTERING_HOUSE).enterDoorAndGoToBed().door(door).bed(pinky.bed).ok();
		you(blinky).when(SCATTERING).headFor().tile(world.width() - 3, 0).ok();
		you(blinky).when(CHASING).headFor().tile(pacMan::tile).ok();

		you(inky).when(SCATTERING).headFor().tile(world.width() - 1, world.height() - 1).ok();
		you(inky).when(CHASING).headFor().tile(() -> {
			Tile b = blinky.tile();
			Tile p = pacMan.tilesAhead(2);
			return Tile.at(2 * p.col - b.col, 2 * p.row - b.row);
		}).ok();

		you(pinky).when(SCATTERING).headFor().tile(2, 0).ok();
		you(pinky).when(CHASING).headFor().tile(() -> pacMan.tilesAhead(4)).ok();

		you(clyde).when(SCATTERING).headFor().tile(0, world.height() - 1).ok();
		you(clyde).when(CHASING).headFor()
				.tile(() -> clyde.tileDistance(pacMan) > 8 ? pacMan.tile() : Tile.at(0, world.height() - 1)).ok();
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public Stream<Guy> guys() {
		return Stream.of(pacMan, blinky, pinky, inky, clyde);
	}

	public Stream<Ghost> ghostsInWorld() {
		return ghosts().filter(ghost -> ghost.world.contains(ghost));
	}

	public Stream<Guy> guysInWorld() {
		return guys().filter(guy -> guy.world.contains(guy));
	}
}