package de.amr.games.pacman.controller.steering.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * Lets a lifeform follow a path.
 * 
 * @author Armin Reichert
 */
public abstract class FollowingPath<M extends MobileLifeform> implements PathProvidingSteering<M> {

	protected final MobileLifeform mover;
	protected List<Tile> path;
	protected int pathIndex;

	public FollowingPath(MobileLifeform mover) {
		this(mover, Collections.emptyList());
	}

	public FollowingPath(MobileLifeform mover, List<Tile> initialPath) {
		this.mover = Objects.requireNonNull(mover);
		setPath(initialPath);
	}

	@Override
	public void steer(M mover) {
		if (mover.enteredNewTile() || pathIndex == -1) {
			++pathIndex;
			dirAlongPath().ifPresent(dir -> {
				if (dir != mover.wishDir()) {
					mover.setWishDir(dir);
				}
			});
		}
	}

	@Override
	public boolean isComplete() {
		return pathIndex == path.size() - 1;
	}

	@Override
	public Optional<Tile> targetTile() {
		return Optional.ofNullable(last(path));
	}

	public void setPath(List<Tile> path) {
		this.path = new ArrayList<>(path);
		pathIndex = -1;
	}

	@Override
	public List<Tile> pathToTarget(MobileLifeform mover) {
		return Collections.unmodifiableList(path);
	}

	@Override
	public boolean isPathComputationEnabled() {
		return true;
	}

	@Override
	public void setPathComputationEnabled(boolean enabled) {
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	protected Tile first(List<Tile> list) {
		return list.isEmpty() ? null : list.get(0);
	}

	protected Tile last(List<Tile> list) {
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}

	protected Optional<Direction> dirAlongPath() {
		if (path.size() < 2 || pathIndex >= path.size() - 1) {
			return Optional.empty();
		}
		return path.get(pathIndex).dirTo(path.get(pathIndex + 1));
	}
}