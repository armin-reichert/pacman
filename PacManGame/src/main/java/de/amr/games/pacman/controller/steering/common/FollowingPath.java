package de.amr.games.pacman.controller.steering.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.components.Tile;

/**
 * Lets a guy follow a path.
 * 
 * @author Armin Reichert
 */
public abstract class FollowingPath implements Steering {

	protected final Guy guy;
	protected List<Tile> path;
	protected int pathIndex;

	public FollowingPath(Guy guy) {
		this(guy, Collections.emptyList());
	}

	public FollowingPath(Guy guy, List<Tile> initialPath) {
		this.guy = Objects.requireNonNull(guy);
		setPath(initialPath);
	}

	@Override
	public void steer(Guy guy) {
		if (!guy.canCrossBorderTo(guy.moveDir) || guy.enteredNewTile || pathIndex == -1) {
			++pathIndex;
			dirAlongPath().ifPresent(dir -> {
				if (dir != guy.wishDir) {
					guy.wishDir = dir;
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
	public List<Tile> pathToTarget() {
		return Collections.unmodifiableList(path);
	}

	@Override
	public boolean isPathComputed() {
		return true;
	}

	@Override
	public void setPathComputed(boolean enabled) {
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