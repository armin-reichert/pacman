package de.amr.games.pacman.controller.steering.common;

import static de.amr.games.pacman.controller.steering.common.MovementType.TELEPORTING;
import static de.amr.games.pacman.controller.steering.common.MovementType.WALKING;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;

import java.util.function.Supplier;

import de.amr.easy.game.Application;
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
	public final MobileLifeform creature;
	public final Transform tf;
	public Direction moveDir;
	public Direction wishDir;
	public boolean enteredNewTile;
	public Supplier<Float> fnSpeed = () -> GameSpeed.BASE_SPEED;
	private Portal portalEntered;

	public Movement(World world, MobileLifeform creature, Transform tf, String name) {
		super(MovementType.class);
		this.world = world;
		this.creature = creature;
		this.tf = tf;
		PacManApp.fsm_register(this);
		//@formatter:off
		beginStateMachine()
			.description(String.format("[%s movement]", name))
			.initialState(WALKING)
			.states()
				.state(WALKING)
					.onTick(() -> {
						move();
						checkIfPortalEntered();
					})
				.state(TELEPORTING)
					.timeoutAfter(sec(0.5f))
			.transitions()
				.when(WALKING).then(TELEPORTING)
					.condition(() -> hasEnteredPortal())
					.act(() -> creature.setVisible(false))
				.when(TELEPORTING).then(WALKING)
					.onTimeout()
					.act(() -> {
						teleport();
						creature.setVisible(true);
					})
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
		Tile oldLocation = currentTile();
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !currentTile().equals(oldLocation);
	}

	public boolean hasEnteredPortal() {
		return portalEntered != null;
	}

	private void checkIfPortalEntered() {
		Tile currentTile = currentTile();
		world.portals().filter(portal -> portal.includes(currentTile)).findAny().ifPresent(portal -> {
			portalEntered = portal;
			Application.loginfo("Entered portal at %s", currentTile);
		});
	}

	private void teleport() {
		portalEntered.teleport(tf, currentTile(), moveDir);
		portalEntered = null;
	}

	private void move() {
		final Tile tileBeforeMove = currentTile();
		float speedLimit = fnSpeed.get();
		float speed = maxSpeedToDir(moveDir, speedLimit);
		if (wishDir != null && wishDir != moveDir) {
			float wishDirSpeed = maxSpeedToDir(wishDir, speedLimit);
			if (wishDirSpeed > 0) {
				speed = wishDirSpeed;
				boolean curve = wishDir == moveDir.left() || wishDir == moveDir.right();
				if (curve && creature.requiresGridAlignment()) {
					placeCreatureAt(tileBeforeMove, 0, 0);
				}
				moveDir = wishDir;
			}
		}
		tf.setVelocity(Vector2f.smul(speed, moveDir.vector()));
		tf.move();
		enteredNewTile = !tileBeforeMove.equals(currentTile());
	}

	/**
	 * Computes how many pixels this creature can move towards the given direction.
	 * 
	 * @param creature the moving creature
	 * @param dir      a direction
	 * @param speed    the creature's current speed
	 */
	private float maxSpeedToDir(Direction dir, float speed) {
		if (creature.canCrossBorderTo(dir)) {
			return speed;
		}
		float offsetX = tf.x - currentTile().x();
		float offsetY = tf.y - currentTile().y();
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

	public Tile currentTile() {
		Vector2f center = tf.getCenter();
		int col = (int) (center.x >= 0 ? center.x / Tile.SIZE : Math.floor(center.x / Tile.SIZE));
		int row = (int) (center.y >= 0 ? center.y / Tile.SIZE : Math.floor(center.y / Tile.SIZE));
		return Tile.at(col, row);
	}
}