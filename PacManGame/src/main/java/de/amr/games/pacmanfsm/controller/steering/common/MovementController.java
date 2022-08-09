/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.controller.steering.common;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacmanfsm.controller.game.Timing.sec;
import static de.amr.games.pacmanfsm.controller.steering.common.MovementType.INSIDE_PORTAL;
import static de.amr.games.pacmanfsm.controller.steering.common.MovementType.OUTSIDE_PORTAL;
import static de.amr.games.pacmanfsm.lib.Direction.DOWN;
import static de.amr.games.pacmanfsm.lib.Direction.LEFT;
import static de.amr.games.pacmanfsm.lib.Direction.RIGHT;
import static de.amr.games.pacmanfsm.lib.Direction.UP;

import de.amr.games.pacmanfsm.controller.creatures.Guy;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.components.Portal;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a guy through the world and through portals.
 * 
 * @author Armin Reichert
 */
public class MovementController extends StateMachine<MovementType, Void> {

	private Portal portalEntered;
	private Tile portalExitTile;

	public MovementController(Guy guy) {
		super(MovementType.class);
		//@formatter:off
		beginStateMachine()
			.description(guy.name + " Movement")
			.initialState(OUTSIDE_PORTAL)
			.states()
				.state(OUTSIDE_PORTAL)
					.onEntry(() -> {
						portalEntered = null;
						portalExitTile = null;
					})
					.onTick(() -> { 
						guy.makeStep();
						checkPortalEnteredBy(guy);
					})
				.state(INSIDE_PORTAL)
					.timeoutAfter(sec(1.0f))
					.onEntry(() -> guy.visible = false)
					.onExit(() -> guy.visible = true)
			.transitions()
				.when(OUTSIDE_PORTAL).then(INSIDE_PORTAL)
					.condition(() -> portalEntered != null)
					.annotation("Entered portal")
				.when(INSIDE_PORTAL).then(OUTSIDE_PORTAL)
					.onTimeout()
					.act(() -> teleport(guy))
					.annotation("Teleporting")
		.endStateMachine();
		//@formatter:on
	}

	private void checkPortalEnteredBy(Guy guy) {
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

	private void teleport(Guy guy) {
		guy.placeAt(portalExitTile, 0, 0);
		portalEntered = null;
		loginfo("%s left portal at %s", guy.name, guy.tile());
	}
}