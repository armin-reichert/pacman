package de.amr.games.pacman.controller.actor;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.MovementControl.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.actor.MovementControl.MovementType.WALKING;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a creature.
 * 
 * @author Armin Reichert
 */
public class MovementControl extends StateMachine<MovementControl.MovementType, Void> {

	public enum MovementType {
		WALKING, TELEPORTING;
	}

	private Portal portalEntered;

	public MovementControl(Creature<?> creature) {
		super(MovementControl.MovementType.class);
		getTracer().setLogger(PacManStateMachineLogging.LOGGER);
		//@formatter:off
		beginStateMachine()
			.description(String.format("[%s movement]", creature.name))
			.initialState(WALKING)
			.states()
				.state(WALKING)
					.onTick(() -> {
						moveInsideMaze(creature);
						checkIfPortalEntered(creature);
					})
			.transitions()
				.when(WALKING).then(TELEPORTING).condition(() -> hasEnteredPortal())
				.when(TELEPORTING).then(WALKING).onTimeout().act(() -> teleport(creature))
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
			creature.visible = false;
		});
	}

	private void teleport(Creature<?> creature) {
		portalEntered.teleport(creature, creature.tile(), creature.moveDir);
		portalEntered = null;
		creature.visible = true;
	}

	private void moveInsideMaze(Creature<?> creature) {
		Tile tile = creature.tile();
		float speed = maxMoveDistance(creature, tile, creature.moveDir);
		if (creature.wishDir != null && creature.wishDir != creature.moveDir) {
			float wishDirSpeed = maxMoveDistance(creature, tile, creature.wishDir);
			if (wishDirSpeed > 0) {
				boolean corner = (creature.wishDir == creature.moveDir.left() || creature.wishDir == creature.moveDir.right());
				if (corner && creature.steering().requiresGridAlignment()) {
					creature.placeAt(tile);
				}
				creature.moveDir = creature.wishDir;
				speed = wishDirSpeed;
			}
		}
		creature.tf.setVelocity(Vector2f.smul(speed, creature.moveDir.vector()));
		creature.tf.move();
		creature.enteredNewTile = !tile.equals(creature.tile());
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction at its current speed
	 * before entering an inaccessible neighbor tile.
	 * 
	 * @param tile tile from where to move
	 * @param dir  move direction
	 */
	private float maxMoveDistance(Creature<?> creature, Tile tile, Direction dir) {
		float speed = creature.currentSpeed(creature.game);
		if (creature.canCrossBorderTo(dir)) {
			return speed;
		}
		float offsetX = creature.tf.x - tile.x(), offsetY = creature.tf.y - tile.y();
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