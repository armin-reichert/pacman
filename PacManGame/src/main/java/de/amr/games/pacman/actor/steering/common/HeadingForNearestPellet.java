package de.amr.games.pacman.actor.steering.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import de.amr.games.pacman.actor.BonusState;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

public class HeadingForNearestPellet implements Steering {

	MazeMover actor;
	Game game;

	public HeadingForNearestPellet(Game game, MazeMover actor) {
		this.game = game;
		this.actor = actor;
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void steer() {
		if (!actor.enteredNewTile()) {
			return;
		}
		Tile actorLocation = actor.tile();
		int minDistance = Integer.MAX_VALUE;
		Direction[] dirs = Direction.values();
		Collections.shuffle(Arrays.asList(dirs));
		for (Direction dir : dirs) {
			if (dir == actor.moveDir().opposite()) {
				continue;
			}
			Tile neighbor = game.maze.neighbor(actorLocation, dir);
			if (actor.canMoveBetween(actorLocation, neighbor)) {
				int d = manhattanDistance(neighbor, nearestPellet(neighbor));
				if (d < minDistance) {
					actor.setWishDir(dir);
					minDistance = d;
				}
			}
		}
	}

	Tile nearestPellet(Tile tile) {
		if (game.bonus.is(BonusState.ACTIVE)) {
			return game.maze.bonusTile;
		}
		return game.maze.playingArea()
		//@formatter:off
			.filter(t -> game.maze.isEnergizer(t) || game.maze.isSimplePellet(t))
			.sorted(Comparator.comparing(foodTile -> manhattanDistance(tile, foodTile)))
			.findFirst().orElse(null);
		//@formatter:on
	}

	int manhattanDistance(Tile t1, Tile t2) {
		int dx = Math.abs(t1.col - t2.col), dy = Math.abs(t1.row - t2.row);
		return dx + dy;
	}
}