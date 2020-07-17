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
 * Controls the movement of a creature through the world.
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

	public void placeCreatureAt(Tile tile, float xOffset, float yOffset) {
		Tile oldLocation = tileLocation();
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !tileLocation().equals(oldLocation);
	}

	private boolean insidePortal() {
		return portal != null;
	}

	private void teleport() {
		portal.teleport(tf, tileLocation(), moveDir);
		portal = null;
		loginfo("%s left portal at %s", lifeformName, tileLocation());
	}

	private void move() {
		final Tile tileBeforeMove = tileLocation();
		float speedLimit = fnSpeed.get();
		float speed = maxSpeedToDir(moveDir, speedLimit);
		if (wishDir != null && wishDir != moveDir) {
			float wishDirSpeed = maxSpeedToDir(wishDir, speedLimit);
			if (wishDirSpeed > 0) {
				speed = wishDirSpeed;
				boolean curve = wishDir == moveDir.left() || wishDir == moveDir.right();
				if (curve && movedLifeform.requiresGridAlignment()) {
					placeCreatureAt(tileBeforeMove, 0, 0);
				}
				moveDir = wishDir;
			}
		}
		tf.setVelocity(Vector2f.smul(speed, moveDir.vector()));
		tf.move();
		// new tile entered?
		Tile tileAfterMove = tileLocation();
		enteredNewTile = !tileBeforeMove.equals(tileAfterMove);
		// portal entered?
		world.portals().filter(p -> p.includes(tileAfterMove)).findAny().ifPresent(p -> {
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
	private float maxSpeedToDir(Direction dir, float speed) {
		if (movedLifeform.canCrossBorderTo(dir)) {
			return speed;
		}
		float offsetX = tf.x - tileLocation().x();
		float offsetY = tf.y - tileLocation().y();
		switch (dir) {
		case UP:
			return Math.min(offsetY, speed);
		case DOWN:
			return Math.min(-offsetY, speed);
		case LEFT:
			return Math.min(offsetX, speed);
		case RIGHT:
			return Math.min(-offsetX, speed);
		default:
			throw new IllegalArgumentException("Illegal move direction: " + dir);
		}
	}

	public Tile tileLocation() {
		Vector2f center = tf.getCenter();
		int col = (int) (center.x >= 0 ? center.x / Tile.SIZE : Math.floor(center.x / Tile.SIZE));
		int row = (int) (center.y >= 0 ? center.y / Tile.SIZE : Math.floor(center.y / Tile.SIZE));
		return Tile.at(col, row);
	}
}