package de.amr.games.pacman.controller.actor;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.actor.PacManState.DEAD;
import static de.amr.games.pacman.controller.actor.PacManState.RUNNING;
import static de.amr.games.pacman.controller.actor.PacManState.SLEEPING;
import static de.amr.games.pacman.model.game.Game.DIGEST_ENERGIZER_TICKS;
import static de.amr.games.pacman.model.game.Game.DIGEST_PELLET_TICKS;
import static de.amr.games.pacman.model.world.Direction.LEFT;
import static de.amr.games.pacman.model.world.Direction.UP;
import static de.amr.statemachine.core.StateMachine.beginStateMachine;

import java.util.EnumMap;
import java.util.Optional;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Creature<PacManState> {

	/** Number of ticks Pac-Man has power after eating an energizer. */
	public int power;

	/** Number of ticks Pac-Man is not moving after having eaten a pellet. */
	public int digestion;

	/** If Pac-Man is dying and collapsing. */
	public boolean collapsing;

	public PacMan() {
		super("Pac-Man", new EnumMap<>(PacManState.class));
		/*@formatter:off*/
		brain = beginStateMachine(PacManState.class, PacManGameEvent.class)

			.description(this::toString)
			.initialState(SLEEPING)

			.states()

				.state(SLEEPING)
					.onEntry(() -> {
						power = digestion = 0;
						visible = true;
						world.putIntoBed(this);
					})

				.state(RUNNING)
					.onEntry(() -> {
						digestion = 0;
					})

					.onTick(() -> {
						if (power > 0) {
							if (--power == 0) {
								publish(new PacManLostPowerEvent());
								return;
							}
						}
						if (digestion > 0) {
							--digestion;
							return;
						}
						steering().steer();
						movement.update();
						if (!isTeleporting()) {
							findSomethingInteresting().ifPresent(this::publish);
						}
					})

				.state(DEAD)
					.onEntry(() -> {
						power = digestion = 0;
					})

			.transitions()

				.when(RUNNING).then(DEAD).on(PacManKilledEvent.class)

		.endStateMachine();
		/* @formatter:on */
		brain.getTracer().setLogger(PacManStateMachineLogging.LOGGER);
		brain.setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		brain.doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent);
		brain.doNotLogEventPublishingIf(e -> e instanceof FoodFoundEvent);
	}

	@Override
	public void takePartIn(Game game) {
		this.game = game;
	}

	public void startRunning() {
		setState(RUNNING);
	}

	/**
	 * Defines the steering used in the {@link PacManState#RUNNING} state.
	 * 
	 * @param steering steering to use in every state
	 */
	public void behavior(Steering steering) {
		behavior(RUNNING, steering);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (world.isDoor(neighbor)) {
			return false;
		}
		return super.canMoveBetween(tile, neighbor);
	}

	/**
	 * NOTE: Depending on the application setting {@link PacManApp.Settings#fixOverflowBug}, this method
	 * simulates/fixes the overflow bug from the original Arcade game which causes, if Pac-Man points
	 * upwards, the wrong calculation of the position ahead of Pac-Man (namely adding the same number of
	 * tiles to the left).
	 * 
	 * @param numTiles number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of Pac-Man towards his current move
	 *         direction.
	 */
	public Tile tilesAhead(int numTiles) {
		Tile tileAhead = world.tileToDir(tile(), moveDir, numTiles);
		if (moveDir == UP && !settings.fixOverflowBug) {
			return world.tileToDir(tileAhead, LEFT, numTiles);
		}
		return tileAhead;
	}

	private Optional<PacManGameEvent> findSomethingInteresting() {
		Tile tile = tile();
		if (tile.equals(world.bonusTile())
				&& world.getBonus().filter(bonus -> bonus.state == BonusState.ACTIVE).isPresent()) {
			return Optional.of(new BonusFoundEvent());
		}
		if (world.containsFood(tile)) {
			boolean energizer = world.containsEnergizer(tile);
			digestion = energizer ? DIGEST_ENERGIZER_TICKS : DIGEST_PELLET_TICKS;
			return Optional.of(new FoodFoundEvent(tile));
		}
		return Optional.empty();
	}
}