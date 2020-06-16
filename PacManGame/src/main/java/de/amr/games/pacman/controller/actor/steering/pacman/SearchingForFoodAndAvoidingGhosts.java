package de.amr.games.pacman.controller.actor.steering.pacman;

import static java.util.Comparator.comparing;

import java.util.Arrays;
import java.util.Collections;

import de.amr.games.pacman.controller.actor.BonusState;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

/**
 * Steering used by PacMan in demo mode.
 * 
 * @author Armin Reichert
 */
public class SearchingForFoodAndAvoidingGhosts implements Steering {

	PacMan pacMan;
	Game game;

	public SearchingForFoodAndAvoidingGhosts(Game game) {
		this.game = game;
		this.pacMan = game.pacMan;
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void steer() {
		if (pacMan.canCrossBorderTo(pacMan.moveDir()) && !pacMan.enteredNewTile()) {
			return;
		}
		if (isDangerousGhostApproaching()) {
			pacMan.forceTurningBack();
			return;
		}
		int minDistance = Integer.MAX_VALUE;
		Direction[] dirs = Direction.values();
		Collections.shuffle(Arrays.asList(dirs));
		for (Direction dir : dirs) {
			if (dir == pacMan.moveDir().opposite()) {
				continue;
			}
			if (pacMan.canCrossBorderTo(dir)) {
				Tile neighbor = game.maze.neighbor(pacMan.tile(), dir);
				Tile nearestFood = preferredFood(neighbor);
				if (nearestFood != null) {
					int d = neighbor.manhattanDistance(nearestFood);
					if (d < minDistance) {
						pacMan.setWishDir(dir);
						minDistance = d;
					}
				}
			}
		}
	}

	Tile preferredFood(Tile currentLocation) {
		if (game.bonus.is(BonusState.ACTIVE)) {
			return game.maze.bonusSeat.tile;
		}
		//@formatter:off
		return game.maze.arena()
			.filter(game.maze::containsFood)
			.sorted(comparing(currentLocation::manhattanDistance))
			.findFirst().orElse(null);
		//@formatter:on
	}

	boolean isDangerousGhostApproaching() {
		Tile pacManLocation = pacMan.tile();
		Tile ahead1 = game.maze.neighbor(pacManLocation, pacMan.moveDir());
		Tile ahead2 = game.maze.tileToDir(pacManLocation, pacMan.moveDir(), 2);
		//@formatter:off
		return game.ghostsOnStage().anyMatch(
				ghost -> !ghost.is(GhostState.FRIGHTENED) 
				&& (ghost.tile().equals(ahead1) || ghost.tile().equals(ahead2))
				&& ghost.moveDir() == pacMan.moveDir().opposite());
		//@formatter:on
	}
}