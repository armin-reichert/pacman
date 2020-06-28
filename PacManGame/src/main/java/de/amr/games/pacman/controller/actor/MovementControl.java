package de.amr.games.pacman.controller.actor;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.actor.MovementControl.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.actor.MovementControl.MovementType.WALKING;

import de.amr.games.pacman.controller.PacManStateMachineLogging;
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
						creature.moveInsideMaze();
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
}