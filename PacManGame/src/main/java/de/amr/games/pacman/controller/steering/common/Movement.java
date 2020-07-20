package de.amr.games.pacman.controller.steering.common;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.steering.common.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.steering.common.MovementType.WALKING;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;

import java.util.function.Supplier;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.game.GameSpeed;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Portal;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a lifeform through the world.
 * 
 * @author Armin Reichert
 */
public class Movement extends StateMachine<MovementType, Void> {

	private final MobileLifeform mover;
	private final String moverName;

	public Supplier<Float> fnSpeed = () -> GameSpeed.BASE_SPEED;
	public Direction moveDir;
	public Direction wishDir;
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
					.onTick(() -> {
						move();
					})
				.state(TELEPORTING)
					.timeoutAfter(sec(0.5f))
					.onEntry(() -> mover.setVisible(false))
					.onExit(() -> mover.setVisible(true))
			.transitions()
				.when(WALKING).then(TELEPORTING)
					.condition(() -> insidePortal())
				.when(TELEPORTING).then(WALKING)
					.onTimeout()
					.act(() -> teleport())
		.endStateMachine();
		//@formatter:on
		PacManApp.fsm_register(this);
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

	private boolean insidePortal() {
		return activePortal != null;
	}

	private void teleport() {
		activePortal.teleport(mover.tf(), mover.tileLocation(), moveDir);
		activePortal = null;
		loginfo("%s left portal at %s", moverName, mover.tileLocation());
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
		// new tile entered?
		Tile tileAfterMove = mover.tileLocation();
		enteredNewTile = !tileBeforeMove.equals(tileAfterMove);
		// portal entered?
		mover.world().portals().filter(p -> p.includes(tileAfterMove)).findFirst().ifPresent(p -> {
			if (p.vertical) {
				// TODO fine tuning
				activePortal = p;
				loginfo("%s entered vertical portal at %s", moverName, tileAfterMove);
			} else {
				if (mover.moveDir() == Direction.RIGHT && mover.tileOffsetX() > 2
						|| mover.moveDir() == Direction.LEFT && mover.tileOffsetX() < 2) {
					activePortal = p;
					loginfo("%s entered horizontal portal at %s", moverName, tileAfterMove);
				}
			}
		});
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction.
	 * 
	 * @param mover the moving creature
	 * @param dir   a direction
	 * @param speed the creature's current speed
	 */
	private float possibleSpeed(Direction dir, float speed) {
		// in tunnel no turn is allowed
		if (mover.world().isTunnel(mover.tileLocation())) {
			if (dir == mover.moveDir().left() || dir == mover.moveDir().right()) {
				return 0;
			}
		}
		if (mover.canCrossBorderTo(dir)) {
			return speed;
		}
		switch (dir) {
		case UP:
			return Math.min(mover.tileOffsetY(), speed);
		case DOWN:
			return Math.min(-mover.tileOffsetY(), speed);
		case LEFT:
			return Math.min(mover.tileOffsetX(), speed);
		case RIGHT:
			return Math.min(-mover.tileOffsetX(), speed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}
}