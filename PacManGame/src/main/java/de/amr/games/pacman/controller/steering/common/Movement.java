package de.amr.games.pacman.controller.steering.common;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.steering.common.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.steering.common.MovementType.WALKING;
import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.creatures.SmartGuy;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a lifeform through the world and the portals.
 * 
 * @author Armin Reichert
 */
public class Movement extends StateMachine<MovementType, Void> {

	private final SmartGuy<?> guy;
	private Portal activePortal;

	public Movement(SmartGuy<?> guy) {
		super(MovementType.class);
		this.guy = guy;
		String description = guy.name;
		if (guy instanceof Ghost) {
			description = "Ghost " + guy.name + " Movement";
		} else if (guy instanceof PacMan) {
			description = "Pac-Man Movement";
		}
		//@formatter:off
		beginStateMachine()
			.description(description)
			.initialState(WALKING)
			.states()
				.state(WALKING)
					.onTick(() -> move(guy.steering().requiresGridAlignment(), guy.getSpeed()))
				.state(TELEPORTING)
					.timeoutAfter(GameController.sec(1.0f))
					.onEntry(() -> guy.body.visible = false)
					.onExit(() -> guy.body.visible = true)
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
		guy.body.enteredNewTile = true;
		guy.body.moveDir = RIGHT;
		guy.body.wishDir = RIGHT;
		activePortal = null;
	}

	private boolean hasEnteredPortal() {
		return activePortal != null;
	}

	private void checkIfJustEnteredPortal() {
		if (activePortal != null) {
			return; // already entered portal before
		}
		Tile tile = guy.body.tile();
		guy.world.portals().filter(portal -> portal.includes(tile)).findFirst().ifPresent(portal -> {
			if (portal.either.equals(tile) && (guy.body.moveDir == LEFT && guy.body.tileOffsetX() <= 1)
					|| (guy.body.moveDir == UP && guy.body.tileOffsetY() <= 1)) {
				setActivePortal(portal, tile);
			} else if (portal.other.equals(tile) && (guy.body.moveDir == RIGHT && guy.body.tileOffsetX() >= 7)
					|| (guy.body.moveDir == DOWN && guy.body.tileOffsetY() >= 7)) {
				setActivePortal(portal, tile);
			}
		});
	}

	private void setActivePortal(Portal portal, Tile entry) {
		activePortal = portal;
		activePortal.setPassageDir(guy.body.moveDir);
		loginfo("%s enters portal at %s moving %s with offsetX %.2f", guy.name, entry, activePortal.getPassageDir(),
				guy.body.tileOffsetX());
	}

	private void teleport() {
		Tile exit = activePortal.exit();
		guy.body.tf.setPosition(exit.x(), exit.y());
		guy.body.enteredNewTile = true;
		activePortal = null;
		loginfo("%s exits portal at %s", guy.name, guy.body.tile());
	}

	private void move(boolean aligned, float speed) {
		final Tile tileBeforeMove = guy.body.tile();

		// how far can we move?
		float pixels = possibleMoveDistance(guy.body.moveDir, speed);
		if (guy.body.wishDir != null && guy.body.wishDir != guy.body.moveDir) {
			float pixelsWishDir = possibleMoveDistance(guy.body.wishDir, speed);
			if (pixelsWishDir > 0) {
				if (guy.body.wishDir == guy.body.moveDir.left() || guy.body.wishDir == guy.body.moveDir.right()) {
					if (aligned) {
						guy.body.placeAt(tileBeforeMove, 0, 0);
					}
				}
				guy.body.moveDir = guy.body.wishDir;
				pixels = pixelsWishDir;
			}
		}
		Vector2f velocity = guy.body.moveDir.vector().times(pixels);
		guy.body.tf.setVelocity(velocity);
		guy.body.tf.move();
		guy.body.enteredNewTile = !tileBeforeMove.equals(guy.body.tile());
		checkIfJustEnteredPortal();
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction.
	 * 
	 * @param dir a direction
	 * @return speed the creature's max. possible speed towards this direction
	 */
	private float possibleMoveDistance(Direction dir, float speed) {
		if (guy.canCrossBorderTo(dir)) {
			return speed;
		}
		switch (dir) {
		case UP:
			return Math.min(guy.body.tileOffsetY() - Tile.SIZE / 2, speed);
		case DOWN:
			return Math.min(-guy.body.tileOffsetY() + Tile.SIZE / 2, speed);
		case LEFT:
			return Math.min(guy.body.tileOffsetX() - Tile.SIZE / 2, speed);
		case RIGHT:
			return Math.min(-guy.body.tileOffsetX() + Tile.SIZE / 2, speed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}
}