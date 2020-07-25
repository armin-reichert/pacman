package de.amr.games.pacman.controller.steering.common;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.steering.common.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.steering.common.MovementType.WALKING;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import java.util.function.Supplier;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a lifeform through the world and the portals.
 * 
 * @author Armin Reichert
 */
public class Movement extends StateMachine<MovementType, Void> {

	private final MobileLifeform mover;
	private final String moverName;

	public Supplier<Float> fnSpeed = () -> GameController.BASE_SPEED;
	public boolean enteredNewTile;
	private Portal activePortal;

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
					.onTick(this::move)
				.state(TELEPORTING)
					.timeoutAfter(sec(0.5f))
					.onEntry(() -> mover.setVisible(false))
					.onExit(() -> mover.setVisible(true))
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
		enteredNewTile = true;
		mover.setMoveDir(RIGHT);
		mover.setWishDir(RIGHT);
		activePortal = null;
	}

	public void moveToTile(Tile tile, float xOffset, float yOffset) {
		Tile oldLocation = mover.tileLocation();
		mover.tf().setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !mover.tileLocation().equals(oldLocation);
	}

	private boolean hasEnteredPortal() {
		return activePortal != null;
	}

	private void checkPortalEntered() {
		Tile tile = mover.tileLocation();
		mover.world().portals().filter(portal -> portal.includes(tile)).findFirst().ifPresent(portal -> {
			if (portal.either.equals(tile) && (mover.isMoving(LEFT) && mover.tileOffsetX() <= 1)
					|| (mover.isMoving(UP) && mover.tileOffsetY() <= 1)) {
				activePortal = portal;
			} else if (portal.other.equals(tile) && (mover.isMoving(RIGHT) && mover.tileOffsetX() >= 7)
					|| (mover.isMoving(DOWN) && mover.tileOffsetY() >= 7)) {
				activePortal = portal;
			}
		});
		if (activePortal != null) {
			activePortal.setPassageDir(mover.moveDir());
			loginfo("%s enters portal at %s moving %s", moverName, tile, activePortal.getPassageDir());
		}
	}

	private void teleport() {
		Tile exit = activePortal.exit();
		mover.tf().setPosition(exit.x(), exit.y());
		activePortal = null;
		loginfo("%s exits portal at %s", moverName, mover.tileLocation());
	}

	private void move() {
		final Tile tileBeforeMove = mover.tileLocation();
		final float maxSpeed = fnSpeed.get();
		float speed = possibleSpeed(mover.moveDir(), maxSpeed);
		if (mover.wishDir() != null && mover.wishDir() != mover.moveDir()) {
			float wishDirSpeed = possibleSpeed(mover.wishDir(), maxSpeed);
			if (wishDirSpeed > 0) {
				speed = wishDirSpeed;
				if (mover.requiresAlignment()
						&& (mover.wishDir() == mover.moveDir().left() || mover.wishDir() == mover.moveDir().right())) {
					moveToTile(tileBeforeMove, 0, 0);
				}
				mover.setMoveDir(mover.wishDir());
			}
		}
		mover.tf().setVelocity(Vector2f.smul(speed, mover.moveDir().vector()));
		mover.tf().move();

		Tile tileAfterMove = mover.tileLocation();
		enteredNewTile = !tileBeforeMove.equals(tileAfterMove);
		if (activePortal == null) {
			checkPortalEntered();
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