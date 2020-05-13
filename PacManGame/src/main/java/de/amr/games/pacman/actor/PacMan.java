package de.amr.games.pacman.actor;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.PacManState.DEAD;
import static de.amr.games.pacman.actor.PacManState.EATING;
import static de.amr.games.pacman.actor.PacManState.SLEEPING;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Game.DIGEST_ENERGIZER_TICKS;
import static de.amr.games.pacman.model.Game.DIGEST_PELLET_TICKS;
import static de.amr.games.pacman.model.Timing.relSpeed;

import java.util.EnumMap;
import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.PacManAppSettings;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.actor.steering.common.SteeredMazeMover;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends MovingActor<PacManState> implements SteeredMazeMover {

	public int powerTicks;
	public int digestionTicks;

	public PacMan(Game game) {
		super(game, "Pac-Man");
		steerings = new EnumMap<>(PacManState.class);
		/*@formatter:off*/
		brain = StateMachine.beginStateMachine(PacManState.class, PacManGameEvent.class)

			.description(this::toString)
			.initialState(SLEEPING)

			.states()

				.state(SLEEPING)
					.onEntry(() -> {
						powerTicks = 0;
						digestionTicks = 0;
						moveDir = wishDir = RIGHT;
						visible = true;
						sprites.forEach(Sprite::resetAnimation);
						sprites.select("full");
						tf.setPosition(maze().pacManHome.centerX(), maze().pacManHome.y());
					})

				.state(EATING)
					.onEntry(() -> {
						digestionTicks = 0;
					})

					.onTick(() -> {
						if (powerTicks > 0) {
							powerTicks -= 1;
							if (powerTicks == 0) {
								publish(new PacManLostPowerEvent());
								return;
							}
						}
						if (digestionTicks > 0) {
							--digestionTicks;
							return;
						}
						move();
						if (!isTeleporting()) {
							findSomethingInteresting().ifPresent(this::publish);
						}
					})

				.state(DEAD)
					.onEntry(() -> {
						powerTicks = 0;
						digestionTicks = 0;
					})

			.transitions()

				.when(EATING).then(DEAD).on(PacManKilledEvent.class)

		.endStateMachine();
		/* @formatter:on */
		brain.getTracer().setLogger(PacManStateMachineLogging.LOG);
		brain.setMissingTransitionBehavior(MissingTransitionBehavior.EXCEPTION);
		brain.doNotLogEventProcessingIf(PacManGameEvent::isTrivial);
		brain.doNotLogEventPublishingIf(PacManGameEvent::isTrivial);
	}

	/**
	 * Defines the steering used in every state.
	 * 
	 * @param steering steering to use in every state
	 */
	public void behavior(Steering steering) {
		for (PacManState state : PacManState.values()) {
			behavior(state, steering);
		}
	}

	@Override
	public float speed(Tile tile, PacManState state) {
		switch (state) {
		case SLEEPING:
			return 0;
		case EATING:
			return relSpeed(powerTicks > 0 ? game.level.pacManPowerSpeed : game.level.pacManSpeed);
		case DEAD:
			return 0;
		default:
			throw new IllegalStateException("Illegal Pac-Man state: " + state);
		}
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze().isDoor(neighbor)) {
			return false;
		}
		return super.canMoveBetween(tile, neighbor);
	}

	/**
	 * NOTE: If the application property {@link PacManAppSettings#overflowBug} is
	 * <code>true</code>, this method simulates the bug from the original Arcade
	 * game where, if Pac-Man points upwards, the position ahead of Pac-Man is
	 * wrongly calculated by adding the same number of tiles to the left.
	 * 
	 * @param numTiles number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of Pac-Man towards
	 *         his current move direction.
	 */
	public Tile tilesAhead(int numTiles) {
		Tile tileAhead = maze().tileToDir(tile(), moveDir(), numTiles);
		if (moveDir() == UP && settings.overflowBug) {
			return maze().tileToDir(tileAhead, LEFT, numTiles);
		}
		return tileAhead;
	}

	private void move() {
		steering().steer();
		movement.update();
		sprites.select("walking-" + moveDir());
		sprites.current().get().enableAnimation(tf.getVelocity().length() > 0);
	}

	private Optional<PacManGameEvent> findSomethingInteresting() {
		Tile tile = tile();
		if (tile.equals(maze().bonusTile) && game.bonus.is(ACTIVE)) {
			return Optional.of(new BonusFoundEvent(game.bonus.symbol(), game.bonus.value()));
		}
		if (maze().isEnergizer(tile)) {
			digestionTicks = DIGEST_ENERGIZER_TICKS;
			return Optional.of(new FoodFoundEvent(tile));
		}
		if (maze().isSimplePellet(tile)) {
			digestionTicks = DIGEST_PELLET_TICKS;
			return Optional.of(new FoodFoundEvent(tile));
		}
		return Optional.empty();
	}
}