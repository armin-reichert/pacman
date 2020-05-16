package de.amr.games.pacman.actor.steering.pacman;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import de.amr.games.pacman.actor.BonusState;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

/**
 * Steering used by PacMan in demo mode.
 * 
 * @author Armin Reichert
 */
public class DemoModeMovement implements Steering {

	static int manhattanDistance(Tile t1, Tile t2) {
		int dx = Math.abs(t1.col - t2.col), dy = Math.abs(t1.row - t2.row);
		return dx + dy;
	}

	static double euclideanDistance(Tile t1, Tile t2) {
		int dx = t1.col - t2.col, dy = t1.row - t2.row;
		return Math.sqrt(dx * dx + dy * dy);
	}

	PacMan pacMan;
	Game game;

	public DemoModeMovement(Game game, PacMan actor) {
		this.game = game;
		this.pacMan = actor;
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void steer() {
		Tile currentLocation = pacMan.tile();
		Tile ahead = game.maze.neighbor(currentLocation, pacMan.moveDir());
		if (pacMan.canMoveBetween(currentLocation, ahead) && !pacMan.enteredNewTile()) {
			return;
		}
		if (isDangerousGhostInsight()) {
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
			Tile neighbor = game.maze.neighbor(currentLocation, dir);
			if (pacMan.canMoveBetween(currentLocation, neighbor)) {
				Tile nearestFood = nearestFood(neighbor);
				if (nearestFood != null) {
					int d = manhattanDistance(neighbor, nearestFood);
					if (d < minDistance) {
						pacMan.setWishDir(dir);
						minDistance = d;
					}
				}
			}
		}
	}

	Tile nearestFood(Tile tile) {
		if (game.bonus.is(BonusState.ACTIVE)) {
			return game.maze.bonusTile;
		}
		//@formatter:off
		return game.maze.playingArea()
			.filter(t -> game.maze.isEnergizer(t) || game.maze.isSimplePellet(t))
			.sorted(Comparator.comparing(foodLocation -> euclideanDistance(tile, foodLocation)))
			.findFirst().orElse(null);
		//@formatter:on
	}

	boolean isDangerousGhostInsight() {
		Tile ahead = game.maze.neighbor(pacMan.tile(), pacMan.moveDir());
		Tile twoAhead = game.maze.tileToDir(pacMan.tile(), pacMan.moveDir(), 2);
		return game.ghostsOnStage().filter(ghost -> !ghost.is(GhostState.FRIGHTENED))
				.anyMatch(ghost -> ghost.tile().equals(ahead) || ghost.tile().equals(twoAhead));
	}
}