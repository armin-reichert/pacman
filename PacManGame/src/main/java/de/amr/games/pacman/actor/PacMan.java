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
import static de.amr.games.pacman.model.Timing.sec;

import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.PacManAppSettings;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.actor.steering.common.SteeredMazeMover;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.api.Fsm;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends MovingActor<PacManState> implements SteeredMazeMover {

	public final SpriteMap sprites = new SpriteMap();
	private final Fsm<PacManState, PacManGameEvent> brain;
	private Steering steering;
	private int powerTicksRemaining;
	private int digestionTicks;

	public PacMan(Game game) {
		super(game, "Pac-Man");
		/*@formatter:off*/
		brain = StateMachine.beginStateMachine(PacManState.class, PacManGameEvent.class)

			.description(PacMan.this::toString)
			.initialState(SLEEPING)

			.states()

				.state(SLEEPING)
					.onEntry(() -> {
						powerTicksRemaining = 0;
						digestionTicks = 0;
						tf.setPosition(maze().pacManHome.centerX(), maze().pacManHome.y());
						setMoveDir(RIGHT);
						setWishDir(RIGHT);
						visible = true;
						sprites.forEach(Sprite::resetAnimation);
						sprites.select("full");
					})

				.state(EATING)
					.onEntry(() -> {
						digestionTicks = 0;
					})

					.onTick(() -> {
						if (powerTicksRemaining > 0) {
							powerTicksRemaining -= 1;
							if (powerTicksRemaining == 0) {
								publish(new PacManLostPowerEvent());
								return;
							}
						}
						if (digestionTicks > 0) {
							--digestionTicks;
							return;
						}
						makeStep();
						if (!isTeleporting()) {
							findSomethingInteresting().ifPresent(this::publish);
						}
					})

				.state(DEAD)
					.onEntry(() -> {
						powerTicksRemaining = 0;
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

	@Override
	public void init() {
		super.init();
		brain.init();
	}

	@Override
	public Fsm<PacManState, PacManGameEvent> fsm() {
		return brain;
	}

	public void startEating() {
		if (getState() == SLEEPING) {
			setState(EATING);
		} else
			throw new IllegalStateException();
	}

	public void gainPower() {
		powerTicksRemaining = sec(game.level.pacManPowerSeconds);
		game.ghostsOnStage().forEach(ghost -> ghost.process(new PacManGainsPowerEvent()));
	}

	public boolean hasPower() {
		return powerTicksRemaining > 0;
	}

	public int powerTicks() {
		return powerTicksRemaining;
	}

	private Optional<PacManGameEvent> findSomethingInteresting() {
		Tile tile = tile();
		if (tile == maze().bonusTile && game.bonus.is(ACTIVE)) {
			return Optional.of(new BonusFoundEvent(game.bonus.symbol(), game.bonus.value()));
		}
		if (tile.containsFood()) {
			if (tile.containsEnergizer()) {
				digestionTicks = DIGEST_ENERGIZER_TICKS;
				return Optional.of(new FoodFoundEvent(tile, true));
			} else {
				digestionTicks = DIGEST_PELLET_TICKS;
				return Optional.of(new FoodFoundEvent(tile, false));
			}
		}
		return Optional.empty();
	}

	public void makeStep() {
		steering().steer();
		move();
		sprites.select("walking-" + moveDir());
		sprites.current().get().enableAnimation(tf.getVelocity().length() > 0);
	}

	@Override
	public Steering steering() {
		return steering;
	}

	public void behavior(Steering steering) {
		this.steering = steering;
	}

	@Override
	public float speed() {
		switch (getState()) {
		case SLEEPING:
			return 0;
		case EATING:
			return relSpeed(hasPower() ? game.level.pacManPowerSpeed : game.level.pacManSpeed);
		case DEAD:
			return 0;
		default:
			throw new IllegalStateException("Illegal Pac-Man state: " + getState());
		}
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

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze().isDoor(neighbor)) {
			return false;
		}
		return super.canMoveBetween(tile, neighbor);
	}
}