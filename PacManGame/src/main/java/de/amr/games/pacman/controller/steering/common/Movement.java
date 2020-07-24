package de.amr.games.pacman.controller.steering.common;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.steering.common.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.steering.common.MovementType.WALKING;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;

import java.util.function.Supplier;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a lifeform through the world.
 * 
 * @author Armin Reichert
 */
public class Movement extends StateMachine<MovementType, Void> {

	private final MobileLifeform mover;
	private final String moverName;

	public Supplier<Float> fnSpeed = () -> GameController.BASE_SPEED;
	public Direction moveDir;
	public Direction wishDir;
	public boolean enteredNewTile;

	private Portal portalEntered;

	public Movement(MobileLifeform mover, String moverName) {
		super(MovementType.class);
		this.mover = mover;
		this.moverName = moverName;
		//@formatter:off
		beginStateMachine()
			.description(String.format("%s movement", moverName))
			.initialState(WALKING)
			.states()
				.state(WALKING)
					.onTick(() -> {
						move();
					})
				.state(TELEPORTING)
					.timeoutAfter(sec(0.5f))
					.onEntry(() -> mover.setVisible(false))
					.onExit(() -> mover.setVisible(true))
			.transitions()
				.when(WALKING).then(TELEPORTING)
					.condition(() -> enteredPortal())
					.annotation("Portal entered")
				.when(TELEPORTING).then(WALKING)
					.onTimeout()
					.act(() -> teleport())
		.endStateMachine();
		//@formatter:on
	}

	@Override
	public void init() {
		super.init();
		enteredNewTile = true;
		moveDir = wishDir = RIGHT;
	}

	public void moveToTile(Tile tile, float xOffset, float yOffset) {
		Tile oldLocation = mover.tileLocation();
		mover.tf().setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !mover.tileLocation().equals(oldLocation);
	}

	private boolean enteredPortal() {
		return portalEntered != null;
	}

	private void teleport() {
		teleportEntity(mover, portalEntered);
		portalEntered = null;
		loginfo("%s left portal at %s", moverName, mover.tileLocation());
	}

	private void teleportEntity(MobileLifeform mover, Portal portal) {
		Tile exit = portal.exit();
		mover.tf().setPosition(exit.x(), exit.y());
	}

	private void move() {
		final Tile tileBeforeMove = mover.tileLocation();
		final float maxSpeed = fnSpeed.get();
		float speed = possibleSpeed(moveDir, maxSpeed);
		if (wishDir != null && wishDir != moveDir) {
			float wishDirSpeed = possibleSpeed(wishDir, maxSpeed);
			if (wishDirSpeed > 0) {
				speed = wishDirSpeed;
				if (mover.requiresAlignment() && (wishDir == moveDir.left() || wishDir == moveDir.right())) {
					moveToTile(tileBeforeMove, 0, 0);
				}
				moveDir = wishDir;
			}
		}
		mover.tf().setVelocity(Vector2f.smul(speed, moveDir.vector()));
		mover.tf().move();

		Tile tileAfterMove = mover.tileLocation();
		enteredNewTile = !tileBeforeMove.equals(tileAfterMove);

		checkIfEnteredPortal(tileAfterMove);
	}

	private void checkIfEnteredPortal(Tile tile) {
		if (portalEntered != null) {
			return; // already inside portal
		}
		mover.world().portals().filter(portal -> portal.includes(tile)).findFirst().ifPresent(portal -> {
			if (portal.either.equals(tile)) {
				if (mover.isMoving(Direction.LEFT) && mover.tileOffsetX() <= 1) {
					portalEntered = portal;
				}
				if (mover.isMoving(Direction.UP) && mover.tileOffsetY() <= 1) {
					portalEntered = portal;
				}
			} else if (portal.other.equals(tile)) {
				if (mover.isMoving(Direction.RIGHT) && mover.tileOffsetX() >= 7) {
					portalEntered = portal;
				}
				if (mover.isMoving(Direction.DOWN) && mover.tileOffsetY() >= 7) {
					portalEntered = portal;
				}
			}
		});
		if (portalEntered != null) {
			portalEntered.passThroughDirection = mover.moveDir();
			loginfo("%s entered portal at %s moving %s", moverName, tile, portalEntered.passThroughDirection);
		}
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction.
	 * 
	 * @param dir a direction
	 * @return speed the creature's max. possible speed towards this direction
	 */
	private float possibleSpeed(Direction dir, float speed) {
		if (mover.canCrossBorderTo(dir)) {
			return speed;
		}
		switch (dir) {
		case UP:
			return Math.min(mover.tileOffsetY() - Tile.SIZE / 2, speed);
		case DOWN:
			return Math.min(-mover.tileOffsetY() + Tile.SIZE / 2, speed);
		case LEFT:
			return Math.min(mover.tileOffsetX() - Tile.SIZE / 2, speed);
		case RIGHT:
			return Math.min(-mover.tileOffsetX() + Tile.SIZE / 2, speed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}
}