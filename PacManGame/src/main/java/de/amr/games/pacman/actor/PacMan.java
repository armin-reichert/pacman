package de.amr.games.pacman.actor;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.PacManState.ALIVE;
import static de.amr.games.pacman.actor.PacManState.DEAD;
import static de.amr.games.pacman.actor.PacManState.SLEEPING;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Game.DIGEST_ENERGIZER_TICKS;
import static de.amr.games.pacman.model.Game.DIGEST_PELLET_TICKS;
import static de.amr.games.pacman.model.Game.FSM_LOGGER;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.model.Timing.speed;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.PacManAppSettings;
import de.amr.games.pacman.actor.core.AbstractMazeMover;
import de.amr.games.pacman.actor.core.Actor;
import de.amr.games.pacman.actor.steering.common.SteerableMazeMover;
import de.amr.games.pacman.actor.steering.core.Steering;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.client.FsmComponent;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends AbstractMazeMover implements SteerableMazeMover, Actor<PacManState> {

	public final SpriteMap sprites = new SpriteMap();
	private final Cast cast;
	private final FsmComponent<PacManState, PacManGameEvent> brain;
	private Steering steering;
	private boolean kicking;
	private boolean tired;
	private int digestionTicks;

	public PacMan(Cast cast) {
		super("Pac-Man");
		this.cast = cast;
		brain = new FsmComponent<>(buildFsm());
		brain.fsm().setLogger(Game.FSM_LOGGER);
		brain.fsm().setMissingTransitionBehavior(MissingTransitionBehavior.EXCEPTION);
		brain.fsm().doNotLogEventProcessingIf(PacManGameEvent::isTrivial);
		brain.doNotLogEventPublishingIf(PacManGameEvent::isTrivial);
	}

	@Override
	public Cast cast() {
		return cast;
	}

	@Override
	public Maze maze() {
		return cast.maze();
	}

	public boolean isKicking() {
		return kicking;
	}

	public boolean isTired() {
		return tired;
	}

	@Override
	public FsmComponent<PacManState, PacManGameEvent> fsmComponent() {
		return brain;
	}

	@Override
	public StateMachine<PacManState, PacManGameEvent> buildFsm() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(PacManState.class, PacManGameEvent.class)

			.description(String.format("[%s]", name()))
			.initialState(SLEEPING)

			.states()

				.state(SLEEPING)
					.onEntry(() -> {
						kicking = tired = false;
						digestionTicks = 0;
						game().clearPacManStarvingTime();
						state().setConstantTimer(State.ENDLESS);
						placeHalfRightOf(maze().pacManHome);
						setMoveDir(RIGHT);
						setWishDir(RIGHT);
						sprites.forEach(Sprite::resetAnimation);
						sprites.select("full");
						show();
					})

				.state(ALIVE)
					.onEntry(() -> {
						digestionTicks = 0;
					})

					.onTick(() -> {
						if (digestionTicks > 0) {
							--digestionTicks;
							return;
						}
						if (kicking) {
							int t = state().getTicksConsumed();
							if (t == state().getDuration() * 75 / 100) {
								tired = true;
							}
							else if (t == state().getDuration()) {
								cast.theme().snd_waza().stop();
								state().setConstantTimer(State.ENDLESS);
								kicking = tired = false;
								publish(new PacManLostPowerEvent());
								return;
							}
						}
						step();
						if (!isTeleporting()) {
							findSomethingInteresting().ifPresent(brain::publish);
						}
					})

				.state(DEAD)
					.onEntry(() -> {
						kicking = tired = false;
						digestionTicks = 0;
						game().clearPacManStarvingTime();
					})

			.transitions()

				.stay(ALIVE) // Ah, ha, ha, ha, stayin' alive
					.on(PacManGainsPowerEvent.class).act(() -> {
						kicking = true;
						tired = false;
						// set and start power timer
						state().setConstantTimer(sec(game().level().pacManPowerSeconds));
						cast.theme().snd_waza().loop();
						FSM_LOGGER.info(() -> String.format("Pac-Man gaining power for %d ticks (%.2f sec)",
								state().getDuration(), state().getDuration() / 60f));
					})

				.when(ALIVE).then(DEAD).on(PacManKilledEvent.class)

		.endStateMachine();
		/* @formatter:on */
	}

	@Override
	public void init() {
		super.init();
		brain.init();
	}

	@Override
	public void update() {
		brain.update();
	}

	@Override
	public void step() {
		steering().steer();
		super.step();
		sprites.select("walking-" + moveDir());
		sprites.current().get().enableAnimation(tf.getVelocity().length() > 0);
	}

	@Override
	public void draw(Graphics2D g) {
		if (visible()) {
			sprites.current().ifPresent(sprite -> {
				float x = tf.getCenter().x - sprite.getWidth() / 2;
				float y = tf.getCenter().y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	@Override
	public Steering steering() {
		return steering;
	}

	public void steering(Steering steering) {
		this.steering = steering;
	}

	@Override
	public float maxSpeed() {
		switch (getState()) {
		case SLEEPING:
			return 0;
		case ALIVE:
			return speed(kicking ? game().level().pacManPowerSpeed : game().level().pacManSpeed);
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

	private Optional<PacManGameEvent> findSomethingInteresting() {
		Tile tile = tile();
		if (tile == maze().bonusTile) {
			Optional<PacManGameEvent> activeBonusFound = cast.bonus().filter(bonus -> bonus.is(ACTIVE))
					.map(bonus -> new BonusFoundEvent(bonus.symbol(), bonus.value()));
			if (activeBonusFound.isPresent()) {
				return activeBonusFound;
			}
		}
		if (tile.containsFood()) {
			game().pacManStarvingTicks = 0;
			if (tile.containsEnergizer()) {
				digestionTicks = DIGEST_ENERGIZER_TICKS;
				return Optional.of(new FoodFoundEvent(tile, true));
			} else {
				digestionTicks = DIGEST_PELLET_TICKS;
				return Optional.of(new FoodFoundEvent(tile, false));
			}
		}
		++game().pacManStarvingTicks;
		return Optional.empty();
	}
}