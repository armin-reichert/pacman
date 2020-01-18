package de.amr.games.pacman.actor.steering.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.MazeMoving;
import de.amr.games.pacman.model.Tile;

/**
 * Steering using a precomputed path.
 * 
 * @param <MazeMoving>
 *          type of steered actor
 *
 * @author Armin Reichert
 */
public abstract class TakingPrecomputedPath implements Steering {

	static Tile first(List<Tile> list) {
		return list.isEmpty() ? null : list.get(0);
	}

	static Tile last(List<Tile> list) {
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}

	protected final MazeMoving actor;
	protected final Maze maze;
	protected final Supplier<Tile> fnTargetTile;
	protected List<Tile> targetPath;

	public TakingPrecomputedPath(MazeMoving actor, Supplier<Tile> fnTargetTile) {
		this.actor = actor;
		this.maze = actor.maze();
		this.fnTargetTile = fnTargetTile;
		this.targetPath = new ArrayList<>();
	}

	@Override
	public void init() {
	}

	@Override
	public void force() {
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void enableTargetPathComputation(boolean b) {
	}

	@Override
	public List<Tile> targetPath() {
		return targetPath;
	}

	protected abstract List<Tile> pathToTarget(MazeMoving actor, Tile targetTile);

	protected boolean isPathInvalid(MazeMoving actor) {
		return actor.wishDir() == null || targetPath.size() == 0 || first(targetPath) != actor.tile()
				|| last(targetPath) != actor.targetTile();
	}

	protected Optional<Direction> alongPath(List<Tile> path) {
		return path.size() < 2 ? Optional.empty() : maze.direction(path.get(0), path.get(1));
	}

	@Override
	public void steer() {
		Tile targetTile = fnTargetTile.get();
		if (targetTile == null) {
			actor.setTargetTile(null);
			targetPath = Collections.emptyList();
			return;
		}
		for (int i = 0; i < targetPath.indexOf(actor.tile()); ++i) {
			targetPath.remove(0);
		}
		if (isPathInvalid(actor)) {
			targetPath = pathToTarget(actor, targetTile);
			actor.setTargetTile(last(targetPath));
		}
		actor.setWishDir(alongPath(targetPath).orElse(null));
	}
}