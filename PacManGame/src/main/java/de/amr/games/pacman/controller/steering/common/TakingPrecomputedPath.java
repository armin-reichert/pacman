package de.amr.games.pacman.controller.steering.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

/**
 * Lets an actor follow a pre-computed path.
 * 
 * @author Armin Reichert
 */
public abstract class TakingPrecomputedPath implements PathProvidingSteering {

	static Tile first(List<Tile> list) {
		return list.isEmpty() ? null : list.get(0);
	}

	static Tile last(List<Tile> list) {
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}

	protected final MobileLifeform actor;
	protected final World world;
	protected final Supplier<Tile> fnTargetTile;
	protected List<Tile> targetPath;

	public TakingPrecomputedPath(MobileLifeform actor, Supplier<Tile> fnTargetTile) {
		this.actor = Objects.requireNonNull(actor);
		this.world = actor.world();
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
		this.targetPath = new ArrayList<>();
	}

	@Override
	public void steer() {
		Tile targetTile = fnTargetTile.get();
		if (targetTile == null) {
			actor.setTargetTile(null);
			targetPath = Collections.emptyList();
			return;
		}
		for (int i = 0; i < targetPath.indexOf(actor.tileLocation()); ++i) {
			targetPath.remove(0);
		}
		if (isPathInvalid(actor)) {
			targetPath = pathToTarget(actor, targetTile);
			actor.setTargetTile(last(targetPath));
		}
		actor.setWishDir(alongPath(targetPath).orElse(null));
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public List<Tile> pathToTarget() {
		return targetPath;
	}

	@Override
	public boolean isPathComputed() {
		return true;
	}

	@Override
	public void setPathComputed(boolean enabled) {
	}

	protected abstract List<Tile> pathToTarget(MobileLifeform actor, Tile targetTile);

	protected boolean isPathInvalid(MobileLifeform actor) {
		return actor.wishDir() == null || targetPath.size() == 0 || !first(targetPath).equals(actor.tileLocation())
				|| !last(targetPath).equals(actor.targetTile());
	}

	protected Optional<Direction> alongPath(List<Tile> path) {
		return path.size() < 2 ? Optional.empty() : path.get(0).dirTo(path.get(1));
	}
}