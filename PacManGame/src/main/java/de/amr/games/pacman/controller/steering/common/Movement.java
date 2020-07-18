package de.amr.games.pacman.controller.steering.common;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.steering.common.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.steering.common.MovementType.WALKING;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;

import java.util.function.Supplier;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.game.GameSpeed;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Portal;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.statemachine.core.StateMachine;

/**
 * Controls the movement of a lifeform through the world.
 * 
 * @author Armin Reichert
 */
public class Movement extends StateMachine<MovementType, Void> {

	public final World world;
	public final MobileLifeform movedLifeform;
	private final String lifeformName;
	public final Transform tf;
	public Supplier<Float> fnSpeed = () -> GameSpeed.BASE_SPEED;
	public Direction moveDir;
	public Direction wishDir;
	public boolean enteredNewTile;
	private Portal portal;

	public Movement(World world, MobileLifeform movedLifeform, String lifeformName, Transform tf) {
		super(MovementType.class);
		this.world = world;
		this.movedLifeform = movedLifeform;
		this.lifeformName = lifeformName;
		this.tf = tf;
		PacManApp.fsm_register(this);
		//@formatter:off
		beginStateMachine()
			.description(String.format("[%s movement]", lifeformName))
			.initialState(WALKING)
			.states()
				.state(WALKING)
					.onTick(() -> {
						move();
					})
				.state(TELEPORTING)
					.timeoutAfter(sec(0.5f))
					.onEntry(() -> movedLifeform.setVisible(false))
					.onExit(() -> movedLifeform.setVisible(true))
			.transitions()
				.when(WALKING).then(TELEPORTING)
					.condition(() -> insidePortal())
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
		Tile oldLocation = movedLifeform.tileLocation();
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !movedLifeform.tileLocation().equals(oldLocation);
	}

	private boolean insidePortal() {
		return portal != null;
	}

	private void teleport() {
		portal.teleport(tf, movedLifeform.tileLocation(), moveDir);
		portal = null;
		loginfo("%s left portal at %s", lifeformName, movedLifeform.tileLocation());
	}

	private void move() {
		final Tile tileBeforeMove = movedLifeform.tileLocation();
		final float maxSpeed = fnSpeed.get();
		float speed = possibleSpeed(moveDir, maxSpeed);
		if (wishDir != null && wishDir != moveDir) {
			float wishDirSpeed = possibleSpeed(wishDir, maxSpeed);
			if (wishDirSpeed > 0) {
				speed = wishDirSpeed;
				if (movedLifeform.requiresAlignment() && (wishDir == moveDir.left() || wishDir == moveDir.right())) {
					moveToTile(tileBeforeMove, 0, 0);
				}
				moveDir = wishDir;
			}
		}
		tf.setVelocity(Vector2f.smul(speed, moveDir.vector()));
		tf.move();
		// new tile entered?
		Tile tileAfterMove = movedLifeform.tileLocation();
		enteredNewTile = !tileBeforeMove.equals(tileAfterMove);
		// portal entered?
		world.portals().filter(p -> p.includes(tileAfterMove)).findFirst().ifPresent(p -> {
			portal = p;
			loginfo("%s entered portal at %s", lifeformName, tileAfterMove);
		});
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction.
	 * 
	 * @param movedLifeform the moving creature
	 * @param dir           a direction
	 * @param speed         the creature's current speed
	 */
	private float possibleSpeed(Direction dir, float speed) {
		if (movedLifeform.canCrossBorderTo(dir)) {
			return speed;
		}
		switch (dir) {
		case UP:
			return Math.min(movedLifeform.tileOffsetY(), speed);
		case DOWN:
			return Math.min(-movedLifeform.tileOffsetY(), speed);
		case LEFT:
			return Math.min(movedLifeform.tileOffsetX(), speed);
		case RIGHT:
			return Math.min(-movedLifeform.tileOffsetX(), speed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}
}