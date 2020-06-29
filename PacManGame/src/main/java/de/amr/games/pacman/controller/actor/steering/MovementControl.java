package de.amr.games.pacman.controller.actor.steering;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.steering.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.actor.steering.MovementType.WALKING;

import java.util.Objects;
import java.util.function.Supplier;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a creature.
 * 
 * @author Armin Reichert
 */
public class MovementControl extends StateMachine<MovementType, Void> {

	private Supplier<Float> fnSpeedLimit;
	private Portal portalEntered;

	public MovementControl(Creature<?> creature, Supplier<Float> fnSpeedLimit) {
		super(MovementType.class);
		this.fnSpeedLimit = Objects.requireNonNull(fnSpeedLimit);
		getTracer().setLogger(PacManStateMachineLogging.LOGGER);
		//@formatter:off
		beginStateMachine()
			.description(String.format("[%s movement]", creature.name))
			.initialState(WALKING)
			.states()
				.state(WALKING)
					.onTick(() -> {
						move(creature);
						checkIfPortalEntered(creature);
					})
			.transitions()
				.when(WALKING).then(TELEPORTING)
					.condition(() -> hasEnteredPortal())
					.act(() -> creature.visible = false)
				.when(TELEPORTING).then(WALKING)
					.onTimeout()
					.act(() -> {
						teleport(creature);
						creature.visible = true;
					})
		.endStateMachine();
		//@formatter:on
	}

	public boolean isTeleporting() {
		return is(TELEPORTING);
	}

	public void setTeleportingDuration(int ticks) {
		state(TELEPORTING).setTimer(ticks);
	}

	public boolean hasEnteredPortal() {
		return portalEntered != null;
	}

	private void checkIfPortalEntered(Creature<?> creature) {
		Tile currentTile = creature.tile();
		creature.world.portals().filter(portal -> portal.contains(currentTile)).findAny().ifPresent(portal -> {
			portalEntered = portal;
			loginfo("Entered portal at %s", currentTile);
		});
	}

	private void teleport(Creature<?> creature) {
		portalEntered.teleport(creature, creature.tile(), creature.moveDir());
		portalEntered = null;
	}

	private void move(Creature<?> creature) {
		final Tile tile = creature.tile();
		float speedLimit = fnSpeedLimit.get();
		float speed = maxSpeedToDir(creature, creature.moveDir(), speedLimit);
		if (creature.wishDir() != null && creature.wishDir() != creature.moveDir()) {
			float wishDirSpeed = maxSpeedToDir(creature, creature.wishDir(), speedLimit);
			if (wishDirSpeed > 0) {
				speed = wishDirSpeed;
				boolean curve = (creature.wishDir() == creature.moveDir().left()
						|| creature.wishDir() == creature.moveDir().right());
				if (curve && creature.steering().requiresGridAlignment()) {
					creature.placeAt(tile);
				}
				creature.setMoveDir(creature.wishDir());
			}
		}
		creature.tf.setVelocity(Vector2f.smul(speed, creature.moveDir().vector()));
		creature.tf.move();
		creature.setEnteredNewTile(!tile.equals(creature.tile()));
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction.
	 * 
	 * @param creature the moving creature
	 * @param dir      a direction
	 * @param speed    the creature's current speed
	 */
	private float maxSpeedToDir(Creature<?> creature, Direction dir, float speed) {
		if (creature.canCrossBorderTo(dir)) {
			return speed;
		}
		float offsetX = creature.tf.x - creature.tile().x();
		float offsetY = creature.tf.y - creature.tile().y();
		switch (dir) {
		case UP:
			return Math.min(offsetY, speed);
		case DOWN:
			return Math.min(-offsetY, speed);
		case LEFT:
			return Math.min(offsetX, speed);
		case RIGHT:
			return Math.min(-offsetX, speed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}
}