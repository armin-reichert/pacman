package de.amr.games.pacman.controller.actor;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.controller.actor.PacManState.DEAD;
import static de.amr.games.pacman.controller.actor.PacManState.EATING;
import static de.amr.games.pacman.controller.actor.PacManState.SLEEPING;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Direction.dirs;
import static de.amr.games.pacman.model.Game.DIGEST_ENERGIZER_TICKS;
import static de.amr.games.pacman.model.Game.DIGEST_PELLET_TICKS;
import static de.amr.games.pacman.model.Game.speed;
import static de.amr.statemachine.core.StateMachine.beginStateMachine;

import java.util.EnumMap;
import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.theme.Theme;
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

	public PacMan(Game game) {
		super(game, "Pac-Man", new EnumMap<>(PacManState.class));
		/*@formatter:off*/
		brain = beginStateMachine(PacManState.class, PacManGameEvent.class)

			.description(this::toString)
			.initialState(SLEEPING)

			.states()

				.state(SLEEPING)
					.onEntry(() -> {
						power = digestion = 0;
						moveDir = wishDir = maze.pacManSeat.startDir;
						visible = true;
						tf.setPosition(maze.pacManSeat.position);
						sprites.forEach(Sprite::resetAnimation);
						showFull();
					})

				.state(EATING)
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
						move();
						showWalking();
						if (!isTeleporting()) {
							findSomethingInteresting(game).ifPresent(this::publish);
						}
					})

				.state(DEAD)
					.onEntry(() -> {
						power = digestion = 0;
					})

			.transitions()

				.when(EATING).then(DEAD).on(PacManKilledEvent.class)

		.endStateMachine();
		/* @formatter:on */
		brain.getTracer().setLogger(PacManStateMachineLogging.LOGGER);
		brain.setMissingTransitionBehavior(MissingTransitionBehavior.EXCEPTION);
		brain.doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent && !((FoodFoundEvent) e).energizer);
		brain.doNotLogEventPublishingIf(e -> e instanceof FoodFoundEvent && !((FoodFoundEvent) e).energizer);
	}

	@Override
	public float currentSpeed(Game game) {
		return is(EATING) ? speed(power > 0 ? game.level.pacManPowerSpeed : game.level.pacManSpeed) : 0;
	}

	public void takeClothes(Theme theme) {
		dirs().forEach(dir -> sprites.set("walking-" + dir, theme.spr_pacManWalking(dir)));
		sprites.set("dying", theme.spr_pacManDying());
		sprites.set("full", theme.spr_pacManFull());
	}

	public void showWalking() {
		sprites.select("walking-" + moveDir);
		sprites.current().get().enableAnimation(tf.getVelocity().length() > 0);
	}

	public void showDying() {
		sprites.select("dying");
	}

	public void showFull() {
		sprites.select("full");
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
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze.isDoor(neighbor)) {
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
		Tile tileAhead = maze.tileToDir(tile(), moveDir, numTiles);
		if (moveDir == UP && !settings.fixOverflowBug) {
			return maze.tileToDir(tileAhead, LEFT, numTiles);
		}
		return tileAhead;
	}

	private void move() {
		steering().steer();
		movement.update();
	}

	private Optional<PacManGameEvent> findSomethingInteresting(Game game) {
		Tile tile = tile();
		if (tile.equals(maze.bonusSeat.tile) && game.bonus.is(ACTIVE)) {
			return Optional.of(new BonusFoundEvent(game.bonus.symbol, game.bonus.value));
		}
		if (maze.containsEnergizer(tile)) {
			digestion = DIGEST_ENERGIZER_TICKS;
			return Optional.of(new FoodFoundEvent(tile, true));
		}
		if (maze.containsSimplePellet(tile)) {
			digestion = DIGEST_PELLET_TICKS;
			return Optional.of(new FoodFoundEvent(tile, false));
		}
		return Optional.empty();
	}
}