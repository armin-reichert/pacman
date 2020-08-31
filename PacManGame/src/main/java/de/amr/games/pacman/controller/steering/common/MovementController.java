package de.amr.games.pacman.controller.steering.common;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.game.Timing.sec;
import static de.amr.games.pacman.controller.steering.common.MovementType.INSIDE_PORTAL;
import static de.amr.games.pacman.controller.steering.common.MovementType.OUTSIDE_PORTAL;
import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a guy through the world and through portals.
 * 
 * @author Armin Reichert
 */
public class MovementController extends StateMachine<MovementType, Void> {

	private final Guy<?> guy;
	private Portal portalEntered;
	private Tile portalExitTile;

	public MovementController(Guy<?> guy) {
		super(MovementType.class);
		this.guy = guy;
		//@formatter:off
		beginStateMachine()
			.description(guy.name + " Movement")
			.initialState(OUTSIDE_PORTAL)
			.states()
				.state(OUTSIDE_PORTAL)
					.onTick(() -> { 
						guy.makeStep();
						checkPortalEntered();
					})
				.state(INSIDE_PORTAL)
					.timeoutAfter(sec(1.0f))
					.onEntry(() -> guy.visible = false)
					.onExit(() -> guy.visible = true)
			.transitions()
				.when(OUTSIDE_PORTAL).then(INSIDE_PORTAL)
					.condition(this::insidePortal)
					.annotation("Entered portal")
				.when(INSIDE_PORTAL).then(OUTSIDE_PORTAL)
					.onTimeout()
					.act(this::teleport)
					.annotation("Teleporting")
		.endStateMachine();
		//@formatter:on
	}

	@Override
	public void init() {
		portalEntered = null;
		portalExitTile = null;
		super.init();
	}

	private boolean insidePortal() {
		return portalEntered != null;
	}

	private void checkPortalEntered() {
		Tile tile = guy.tile();
		guy.world.portals().filter(portal -> portal.includes(tile)).findFirst().ifPresent(portal -> {
			if (portal.either.equals(tile) && (guy.moveDir == LEFT && guy.tileOffsetX() <= 1)
					|| (guy.moveDir == UP && guy.tileOffsetY() <= 1)) {
				portalEntered = portal;
				portalExitTile = portal.other;
			} else if (portal.other.equals(tile) && (guy.moveDir == RIGHT && guy.tileOffsetX() >= 7)
					|| (guy.moveDir == DOWN && guy.tileOffsetY() >= 7)) {
				portalEntered = portal;
				portalExitTile = portal.either;
			}
		});
		if (portalEntered != null) {
			loginfo("%s entered portal at %s moving %s with offsetX %.2f", guy.name, tile, guy.moveDir, guy.tileOffsetX());
		}
	}

	private void teleport() {
		guy.placeAt(portalExitTile, 0, 0);
		guy.enteredNewTile = true;
		portalEntered = null;
		loginfo("%s left portal at %s", guy.name, guy.tile());
	}
}