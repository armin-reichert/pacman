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
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
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

	public boolean enteredNewTile;
	private Portal activePortal;

	public Movement(MobileLifeform mover, String moverName) {
		super(MovementType.class);
		this.mover = mover;
		this.moverName = moverName;
		String description = mover instanceof Ghost ? "Ghost " + moverName + " Movement" : "Pac-Man Movement";
		//@formatter:off
		beginStateMachine()
			.description(description)
			.initialState(WALKING)
			.states()
				.state(WALKING)
					.onTick(this::move)
				.state(TELEPORTING)
					.timeoutAfter(sec(1.0f))
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

	private void checkIfJustEnteredPortal() {
		if (activePortal != null) {
			return; // already entered portal before
		}
		Tile tile = mover.tileLocation();
		mover.world().portals().filter(portal -> portal.includes(tile)).findFirst().ifPresent(portal -> {
			if (portal.either.equals(tile) && (mover.isMoving(LEFT) && mover.tileOffsetX() <= 1)
					|| (mover.isMoving(UP) && mover.tileOffsetY() <= 1)) {
				setActivePortal(portal, tile);
			} else if (portal.other.equals(tile) && (mover.isMoving(RIGHT) && mover.tileOffsetX() >= 7)
					|| (mover.isMoving(DOWN) && mover.tileOffsetY() >= 7)) {
				setActivePortal(portal, tile);
			}
		});
	}

	private void setActivePortal(Portal portal, Tile entry) {
		activePortal = portal;
		activePortal.setPassageDir(mover.moveDir());
		loginfo("%s enters portal at %s moving %s with offsetX %.2f", moverName, entry, activePortal.getPassageDir(),
				mover.tileOffsetX());
	}

	private void teleport() {
		Tile exit = activePortal.exit();
		mover.tf().setPosition(exit.x(), exit.y());
		activePortal = null;
		loginfo("%s exits portal at %s", moverName, mover.tileLocation());
	}

	private void move() {
		final Tile tileBeforeMove = mover.tileLocation();
		final Direction wishDir = mover.wishDir();
		final float speed = mover.getSpeed();

		// how far can we move?
		float pixels = possibleMoveDistance(mover.moveDir(), speed);
		if (wishDir != null && wishDir != mover.moveDir()) {
			float pixelsWishDir = possibleMoveDistance(wishDir, speed);
			if (pixelsWishDir > 0) {
				if (wishDir == mover.moveDir().left() || wishDir == mover.moveDir().right()) {
					if (mover.requiresAlignment()) {
						moveToTile(tileBeforeMove, 0, 0);
					}
				}
				mover.setMoveDir(wishDir);
				pixels = pixelsWishDir;
			}
		}

		Vector2f velocity = mover.moveDir().vector().times(pixels);
		mover.tf().setVelocity(velocity);
		mover.tf().move();

		Tile tileAfterMove = mover.tileLocation();
		enteredNewTile = !tileBeforeMove.equals(tileAfterMove);
		checkIfJustEnteredPortal();
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction.
	 * 
	 * @param dir a direction
	 * @return speed the creature's max. possible speed towards this direction
	 */
	private float possibleMoveDistance(Direction dir, float speed) {
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