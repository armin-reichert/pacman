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
package de.amr.games.pacman.controller.steering.pacman;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static java.util.Comparator.comparingInt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.TemporaryFood;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.model.world.arcade.ArcadeFood;
import de.amr.games.pacman.model.world.graph.WorldGraph;
import de.amr.games.pacman.model.world.graph.WorldGraph.PathFinder;

/**
 * Steering used by PacMan in demo mode.
 * 
 * @author Armin Reichert
 */
public class SearchingForFoodAndAvoidingGhosts implements Steering {

	private static final Random rnd = new Random();

	static class Target {

		Direction dir;
		Tile tile;

		Target(Direction dir) {
			this.dir = dir;
		}
	}

	private final Guy<?> guy;
	private final Folks folks;
	private final TiledWorld world;
	private final WorldGraph graph;
	private Tile target;

	public SearchingForFoodAndAvoidingGhosts(TiledWorld world, Guy<?> guy, Folks folks) {
		this.world = world;
		this.guy = guy;
		this.folks = folks;
		graph = new WorldGraph(world);
		graph.setPathFinder(PathFinder.ASTAR);
	}

	@Override
	public void steer(Guy<?> guy_) {
		if (!guy.enteredNewTile && guy.canMoveTo(guy.moveDir)) {
			return;
		}
		boolean acted = avoidTouchingGhostAhead() || avoidOncomingGhost() || chaseFrightenedGhost(10);
		if (!acted) {
			turnTowardsNearestFood(guy.tile());
		}
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public boolean isPathComputed() {
		return target != null;
	}

	@Override
	public void setPathComputed(boolean enabled) {
	}

	@Override
	public List<Tile> pathToTarget() {
		return target != null ? graph.findPath(guy.tile(), target) : Collections.emptyList();
	}

	private void flee(Ghost enemy) {
		Tile here = guy.tile(), enemyTile = enemy.tile();
		if (world.isIntersection(here)) {
			double maxDistance = -1;
			Iterable<Direction> dirs = Stream.of(guy.moveDir.opposite(), guy.moveDir.left(), guy.moveDir.right())
					.filter(guy::canMoveTo)::iterator;
			for (Direction dir : dirs) {
				Tile neighbor = world.neighbor(here, dir);
				double distanceToEnemy = neighbor.distance(enemyTile);
				if (distanceToEnemy > maxDistance) {
					maxDistance = distanceToEnemy;
					guy.moveDir = dir;
				}
			}
			guy.forceMoving(guy.moveDir);
		} else {
			guy.reverseDirection();
		}
	}

	private boolean avoidTouchingGhostAhead() {
		// is dangerous ghost just in front of pacMan and is moving in the same direction?
		Ghost enemy = dangerousGhostsInRange(2).filter(ghost -> guy.moveDir == ghost.moveDir).findAny().orElse(null);
		if (enemy != null) {
			flee(enemy);
			return true;
		}
		return false;
	}

	private boolean avoidOncomingGhost() {
		// is dangerous ghost coming directly towards pacMan?
		Ghost enemy = dangerousGhostsInRange(4).filter(ghost -> guy.moveDir == ghost.moveDir.opposite()).findAny()
				.orElse(null);
		if (enemy != null) {
			flee(enemy);
			return true;
		}
		return false;
	}

	private boolean chaseFrightenedGhost(int range) {
		Optional<Ghost> frightenedGhost = ghostsInRange(range).filter(this::isGhostFrightened).findAny();
		if (frightenedGhost.isPresent()) {
			Optional<Direction> dir = frightenedGhost.flatMap(this::directionTowards);
			if (dir.isPresent()) {
				guy.wishDir = dir.get();
				target = frightenedGhost.get().tile();
				return true;
			}
		}
		return false;
	}

	private void turnTowardsNearestFood(Tile here) {
		double minFoodDistance = Double.MAX_VALUE;
		Iterable<Direction> dirs = aheadThenLeftOrRight().filter(guy::canMoveTo)::iterator;
		for (Direction dir : dirs) {
			Tile neighbor = world.neighbor(here, dir);
			Optional<Tile> foodLocation = preferredFoodLocationFrom(neighbor);
			if (foodLocation.isPresent()) {
				double foodDistance = neighbor.distance(foodLocation.get());
				if (foodDistance < minFoodDistance) {
					guy.wishDir = dir;
					target = foodLocation.get();
					minFoodDistance = foodDistance;
				}
			}
		}
	}

	private Stream<Tile> foodTiles() {
		return world.tiles().filter(world::hasFood);
	}

	private Optional<Tile> preferredFoodLocationFrom(Tile here) {
		double nearestEnemyDist = distanceToNearestEnemy(here);
		if (nearestEnemyDist == Double.MAX_VALUE) {
			return activeBonusAtMostAway(here, 30).or(() -> nearestFoodFrom(here));
		}
		//@formatter:off
		return activeBonusAtMostAway(here, 10)
			.or(() -> energizerAtMostAway(here, (int) nearestEnemyDist))
			.or(() -> nearestFoodFrom(here));
		//@formatter:on
	}

	private Optional<Tile> activeBonusAtMostAway(Tile here, int maxDistance) {
		if (world.temporaryFood().isPresent()) {
			TemporaryFood bonus = world.temporaryFood().get();
			if (bonus.isActive() && !bonus.isConsumed()) {
				int dist = here.manhattanDistance(bonus.location());
				if (dist <= maxDistance) {
					return Optional.of(bonus.location());
				}
			}
		}
		return Optional.empty();
	}

	private Optional<Tile> energizerAtMostAway(Tile here, int distance) {
		//@formatter:off
		return foodTiles()
			.filter(tile -> world.hasFood(ArcadeFood.ENERGIZER, tile))
			.filter(energizer -> here.manhattanDistance(energizer) <= distance)
			.findFirst();
		//@formatter:on
	}

	private Optional<Tile> nearestFoodFrom(Tile here) {
		//@formatter:off
		return foodTiles()
			.sorted(comparingInt(food -> here.manhattanDistance(food)))
			.findFirst();
		//@formatter:on
	}

	private boolean isGhostFrightened(Ghost ghost) {
		return ghost.ai.is(FRIGHTENED);
	}

	private boolean isGhostDangerous(Ghost ghost) {
		return ghost.ai.is(CHASING, SCATTERING);
	}

	private boolean isGhostInRange(Ghost ghost, int numTiles) {
		return shortestPathLength(ghost.tile(), guy.tile()) <= numTiles;
	}

	private Stream<Ghost> dangerousGhosts() {
		return folks.ghostsInWorld().filter(this::isGhostDangerous);
	}

	private Stream<Ghost> dangerousGhostsInRange(int numTiles) {
		return ghostsInRange(numTiles).filter(this::isGhostDangerous);
	}

	private double distanceToNearestEnemy(Tile here) {
		return dangerousGhosts().map(ghost -> here.distance(ghost.tile())).min(Double::compareTo).orElse(Double.MAX_VALUE);
	}

	private Stream<Ghost> ghostsInRange(int numTiles) {
		return folks.ghostsInWorld().filter(ghost -> isGhostInRange(ghost, numTiles));
	}

	private Stream<Direction> aheadThenLeftOrRight() {
		return rnd.nextBoolean() ? Stream.of(guy.moveDir, guy.moveDir.right(), guy.moveDir.left())
				: Stream.of(guy.moveDir, guy.moveDir.left(), guy.moveDir.right());
	}

	private int shortestPathLength(Tile from, Tile to) {
		return graph.findPath(from, to).size();
	}

	private Optional<Direction> directionTowards(Ghost enemy) {
		Direction result = null;
		double minDist = Integer.MAX_VALUE;
		for (Direction dir : Direction.values()) {
			if (guy.canMoveTo(dir)) {
				List<Tile> path = graph.findPath(guy.tile(), enemy.tile());
				int dist = path.size();
				if (dist < minDist && path.size() >= 2) {
					minDist = dist;
					result = path.get(0).dirTo(path.get(1)).get();
				}
			}
		}
		return Optional.ofNullable(result);
	}
}