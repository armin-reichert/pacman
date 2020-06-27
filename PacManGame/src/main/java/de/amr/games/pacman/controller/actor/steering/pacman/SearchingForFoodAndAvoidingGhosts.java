package de.amr.games.pacman.controller.actor.steering.pacman;

import static de.amr.games.pacman.model.world.Tile.manhattanDistance;
import static java.util.Comparator.comparingInt;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.actor.BonusState;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Tile;

/**
 * Steering used by PacMan in demo mode.
 * 
 * @author Armin Reichert
 */
public class SearchingForFoodAndAvoidingGhosts implements Steering {

	final PacMan pacMan;
	final Game game;
	final PacManWorld world;

	public SearchingForFoodAndAvoidingGhosts(Game game) {
		this.game = game;
		this.world = game.world;
		this.pacMan = game.pacMan;
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void steer() {
		if (pacMan.canCrossBorderTo(pacMan.moveDir()) && !pacMan.enteredNewTile()) {
			return; // no decision necessary, move on
		}
		if (isDangerousGhostApproaching()) {
			pacMan.reverseDirection();
			return;
		}
		Direction[] dirsShuffled = Direction.values();
		Collections.shuffle(Arrays.asList(dirsShuffled));
		int minDistance = Integer.MAX_VALUE;
		for (Direction dir : dirsShuffled) {
			if (dir == pacMan.moveDir().opposite()) {
				continue;
			}
			if (pacMan.canCrossBorderTo(dir)) {
				Tile neighbor = game.world.neighbor(pacMan.tile(), dir);
				Optional<Tile> foodLocation = preferredFoodLocationFrom(neighbor);
				if (foodLocation.isPresent()) {
					int d = neighbor.manhattanDistance(foodLocation.get());
					if (d < minDistance) {
						pacMan.setWishDir(dir);
						minDistance = d;
					}
				}
			}
		}
	}

	Stream<Tile> foodTiles() {
		return world.mapTiles().filter(world::containsFood);
	}

	Optional<Tile> preferredFoodLocationFrom(Tile here) {
		return activeBonusAtMostAway(here, 20).or(() -> energizerAtMostAwayFrom(here, 10)).or(() -> nearestFoodFrom(here));
	}

	Optional<Tile> activeBonusAtMostAway(Tile here, int maxDistance) {
		return game.bonus.is(BonusState.ACTIVE) && here.manhattanDistance(world.bonusTile()) <= maxDistance
				? Optional.of(world.bonusTile())
				: Optional.empty();
	}

	Optional<Tile> energizerAtMostAwayFrom(Tile here, int maxDistance) {
		//@formatter:off
		return foodTiles()
				.filter(world::containsEnergizer)
				.filter(energizer -> here.manhattanDistance(energizer) <= maxDistance)
				.findAny();
		//@formatter:on
	}

	Optional<Tile> nearestFoodFrom(Tile here) {
		return foodTiles().sorted(comparingInt(food -> manhattanDistance(here, food))).findFirst();
	}

	boolean isDangerousGhostApproaching() {
		Tile pacManLocation = pacMan.tile();
		Tile ahead1 = world.neighbor(pacManLocation, pacMan.moveDir());
		Tile ahead2 = world.tileToDir(pacManLocation, pacMan.moveDir(), 2);
		//@formatter:off
		return game.ghostsOnStage().anyMatch(
				ghost -> !ghost.is(GhostState.FRIGHTENED) 
				&& (ghost.tile().equals(ahead1) || ghost.tile().equals(ahead2))
				&& ghost.moveDir() == pacMan.moveDir().opposite());
		//@formatter:on
	}
}