package de.amr.games.pacman.controller.steering.common;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.steering.common.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.steering.common.MovementType.WALKING;
import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.game.Timing;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a guy through the world and the portals.
 * 
 * @author Armin Reichert
 */
public class MovementController extends StateMachine<MovementType, Void> {

	private final Guy guy;
	private Portal activePortal;

	public MovementController(World world, Guy guy) {
		super(MovementType.class);
		this.guy = guy;
		//@formatter:off
		beginStateMachine()
			.description(guy.name + " Movement")
			.initialState(WALKING)
			.states()
				.state(WALKING)
					.onTick(() -> { 
						guy.makeStep();
						checkIfJustEnteredPortal();
					})
				.state(TELEPORTING)
					.timeoutAfter(Timing.sec(1.0f))
					.onEntry(() -> guy.visible = false)
					.onExit(() -> guy.visible = true)
			.transitions()
				.when(WALKING).then(TELEPORTING)
					.condition(this::hasEnteredPortal)
					.annotation("Enters portal")
				.when(TELEPORTING).then(WALKING)
					.onTimeout()
					.act(this::teleport)
					.annotation("Teleporting")
		.endStateMachine();
		//@formatter:on
	}

	@Override
	public void init() {
		super.init();
		activePortal = null;
	}

	private boolean hasEnteredPortal() {
		return activePortal != null;
	}

	private void checkIfJustEnteredPortal() {
		if (activePortal != null) {
			return; // already entered portal before
		}
		Tile tile = guy.tile();
		guy.world.portals().filter(portal -> portal.includes(tile)).findFirst().ifPresent(portal -> {
			if (portal.either.equals(tile) && (guy.moveDir == LEFT && guy.tileOffsetX() <= 1)
					|| (guy.moveDir == UP && guy.tileOffsetY() <= 1)) {
				setActivePortal(portal, tile);
			} else if (portal.other.equals(tile) && (guy.moveDir == RIGHT && guy.tileOffsetX() >= 7)
					|| (guy.moveDir == DOWN && guy.tileOffsetY() >= 7)) {
				setActivePortal(portal, tile);
			}
		});
	}

	private void setActivePortal(Portal portal, Tile entry) {
		activePortal = portal;
		activePortal.setPassageDir(guy.moveDir);
		loginfo("%s enters portal at %s moving %s with offsetX %.2f", guy, entry, activePortal.getPassageDir(),
				guy.tileOffsetX());
	}

	private void teleport() {
		Tile exit = activePortal.exit();
		guy.tf.setPosition(exit.x(), exit.y());
		guy.enteredNewTile = true;
		activePortal = null;
		loginfo("%s exits portal at %s", guy, guy.tile());
	}
}