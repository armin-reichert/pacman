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