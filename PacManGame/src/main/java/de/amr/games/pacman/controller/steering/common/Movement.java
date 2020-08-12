package de.amr.games.pacman.controller.steering.common;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.steering.common.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.steering.common.MovementType.WALKING;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.creatures.Creature;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
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

	private final Creature<?> guy;
	private Portal activePortal;

	public Movement(Creature<?> guy) {
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
					.timeoutAfter(sec(1.0f))
					.onEntry(() -> guy.entity.visible = false)
					.onExit(() -> guy.entity.visible = true)
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
		guy.entity.enteredNewTile = true;
		guy.entity.moveDir = RIGHT;
		guy.entity.wishDir = RIGHT;
		activePortal = null;
	}

	private boolean hasEnteredPortal() {
		return activePortal != null;
	}

	private void checkIfJustEnteredPortal() {
		if (activePortal != null) {
			return; // already entered portal before
		}
		Tile tile = guy.entity.tile();
		guy.world.portals().filter(portal -> portal.includes(tile)).findFirst().ifPresent(portal -> {
			if (portal.either.equals(tile) && (guy.entity.moveDir == LEFT && guy.entity.tileOffsetX() <= 1)
					|| (guy.entity.moveDir == UP && guy.entity.tileOffsetY() <= 1)) {
				setActivePortal(portal, tile);
			} else if (portal.other.equals(tile) && (guy.entity.moveDir == RIGHT && guy.entity.tileOffsetX() >= 7)
					|| (guy.entity.moveDir == DOWN && guy.entity.tileOffsetY() >= 7)) {
				setActivePortal(portal, tile);
			}
		});
	}

	private void setActivePortal(Portal portal, Tile entry) {
		activePortal = portal;
		activePortal.setPassageDir(guy.entity.moveDir);
		loginfo("%s enters portal at %s moving %s with offsetX %.2f", guy.name, entry, activePortal.getPassageDir(),
				guy.entity.tileOffsetX());
	}

	private void teleport() {
		Tile exit = activePortal.exit();
		guy.entity.tf.setPosition(exit.x(), exit.y());
		guy.entity.enteredNewTile = true;
		activePortal = null;
		loginfo("%s exits portal at %s", guy.name, guy.entity.tile());
	}

	private void move(boolean aligned, float speed) {
		final Tile tileBeforeMove = guy.entity.tile();

		// how far can we move?
		float pixels = possibleMoveDistance(guy.entity.moveDir, speed);
		if (guy.entity.wishDir != null && guy.entity.wishDir != guy.entity.moveDir) {
			float pixelsWishDir = possibleMoveDistance(guy.entity.wishDir, speed);
			if (pixelsWishDir > 0) {
				if (guy.entity.wishDir == guy.entity.moveDir.left() || guy.entity.wishDir == guy.entity.moveDir.right()) {
					if (aligned) {
						guy.entity.placeAt(tileBeforeMove, 0, 0);
					}
				}
				guy.entity.moveDir = guy.entity.wishDir;
				pixels = pixelsWishDir;
			}
		}
		Vector2f velocity = guy.entity.moveDir.vector().times(pixels);
		guy.entity.tf.setVelocity(velocity);
		guy.entity.tf.move();
		guy.entity.enteredNewTile = !tileBeforeMove.equals(guy.entity.tile());
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
			return Math.min(guy.entity.tileOffsetY() - Tile.SIZE / 2, speed);
		case DOWN:
			return Math.min(-guy.entity.tileOffsetY() + Tile.SIZE / 2, speed);
		case LEFT:
			return Math.min(guy.entity.tileOffsetX() - Tile.SIZE / 2, speed);
		case RIGHT:
			return Math.min(-guy.entity.tileOffsetX() + Tile.SIZE / 2, speed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}
}