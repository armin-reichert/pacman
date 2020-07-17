package de.amr.games.pacman.controller.steering.common;

import static de.amr.games.pacman.controller.steering.common.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.steering.common.MovementType.WALKING;
import static de.amr.games.pacman.model.game.Game.sec;

import java.util.function.Supplier;

import de.amr.easy.game.Application;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.creatures.api.Creature;
import de.amr.games.pacman.controller.game.SpeedLimits;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Portal;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a creature.
 * 
 * @author Armin Reichert
 */
public class MovementControl extends StateMachine<MovementType, Void> {

	protected Supplier<Float> fnSpeedLimit = () -> SpeedLimits.BASE_SPEED;
	private Portal portalEntered;

	public MovementControl(Creature<?> creature) {
		super(MovementType.class);
		PacManApp.fsm_register(this);
		//@formatter:off
		beginStateMachine()
			.description(String.format("[%s movement]", creature.name()))
			.initialState(WALKING)
			.states()
				.state(WALKING)
					.onTick(() -> {
						move(creature);
						checkIfPortalEntered(creature);
					})
				.state(TELEPORTING)
					.timeoutAfter(sec(0.5f))
			.transitions()
				.when(WALKING).then(TELEPORTING)
					.condition(() -> hasEnteredPortal())
					.act(() -> creature.entity.visible = false)
				.when(TELEPORTING).then(WALKING)
					.onTimeout()
					.act(() -> {
						teleport(creature);
						creature.entity.visible = true;
					})
		.endStateMachine();
		//@formatter:on
	}

	public float getSpeedLimit() {
		return fnSpeedLimit.get();
	}

	public void setSpeedLimit(Supplier<Float> fnSpeedLimit) {
		this.fnSpeedLimit = fnSpeedLimit;
	}

	public boolean hasEnteredPortal() {
		return portalEntered != null;
	}

	private void checkIfPortalEntered(Creature<?> creature) {
		Tile currentTile = creature.location();
		creature.world().portals().filter(portal -> portal.includes(currentTile)).findAny().ifPresent(portal -> {
			portalEntered = portal;
			Application.loginfo("Entered portal at %s", currentTile);
		});
	}

	private void teleport(Creature<?> creature) {
		portalEntered.teleport(creature.entity, creature.location(), creature.moveDir());
		portalEntered = null;
	}

	private void move(Creature<?> creature) {
		final Tile tile = creature.location();
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
		creature.entity.tf.setVelocity(Vector2f.smul(speed, creature.moveDir().vector()));
		creature.entity.tf.move();
		creature.setEnteredNewTile(!tile.equals(creature.location()));
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
		float offsetX = creature.entity.tf.x - creature.location().x();
		float offsetY = creature.entity.tf.y - creature.location().y();
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