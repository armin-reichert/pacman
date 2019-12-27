package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.PacManState.ALIVE;
import static de.amr.games.pacman.actor.PacManState.DEAD;
import static de.amr.games.pacman.actor.PacManState.SLEEPING;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.PacManGame.DIGEST_ENERGIZER_TICKS;
import static de.amr.games.pacman.model.PacManGame.DIGEST_PELLET_TICKS;
import static de.amr.games.pacman.model.PacManGame.FSM_LOGGER;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.model.Timing.speed;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.AbstractMazeMover;
import de.amr.games.pacman.actor.core.PacManGameActor;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.model.Timing;
import de.amr.statemachine.client.FsmComponent;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends AbstractMazeMover implements PacManGameActor<PacManState> {

	public final SpriteMap sprites = new SpriteMap();
	private final PacManGameCast cast;
	private final FsmComponent<PacManState, PacManGameEvent> brain;
	private Steering<PacMan> steering;
	private boolean kicking;
	private boolean tired;
	private int digestionTicks;
	private int starvingTicks;

	public PacMan(PacManGameCast cast) {
		super("Pac-Man");
		this.cast = cast;
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
		brain = buildBrain();
		brain.fsm().traceTo(FSM_LOGGER, () -> Timing.FPS);
		brain.doNotLog(event -> event instanceof FoodFoundEvent && !((FoodFoundEvent) event).energizer);
	}

	@Override
	public PacManGameCast cast() {
		return cast;
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
						clearStarvingTime();
						placeHalfRightOf(maze().pacManHome);
						setMoveDir(RIGHT);
						setNextDir(RIGHT);
						sprites.forEach(Sprite::resetAnimation);
						sprites.select("full");
						show();
					})

				.state(ALIVE)
					.onEntry(() -> {
						digestionTicks = 0;
					})

					.onTick(() -> {
						steering().steer(PacMan.this);
						if (digestionTicks > 0) {
							--digestionTicks;
							return;
						}
						if (kicking) {
							if (state().getTicksConsumed() == state().getDuration() * 75 / 100) {
								tired = true;
							}
							else if (state().getTicksRemaining() == 0) {
								cast.theme().snd_waza().stop();
								// "disable timer"
								state().setConstantTimer(State.ENDLESS);
								kicking = tired = false;
								publish(new PacManLostPowerEvent());
								return;
							}
						}
						step();
						sprites.select("walking-" + moveDir());
						sprites.current().get().enableAnimation(canMoveForward());
						if (!isTeleporting()) {
							inspect(tile()).ifPresent(brain::publish);
						}
					})

				.state(DEAD)
					.onEntry(() -> {
						kicking = tired = false;
						digestionTicks = 0;
						clearStarvingTime();
					})

			.transitions()

				.stay(ALIVE) // Ah, ha, ha, ha, stayin' alive
					.on(PacManGainsPowerEvent.class).act(() -> {
						kicking = true;
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
	public void draw(Graphics2D g) {
		sprites.current().ifPresent(sprite -> {
			float dx = centerX() - sprite.getWidth() / 2;
			float dy = centerY() - sprite.getHeight() / 2;
			sprite.draw(g, dx, dy);
		});
	}

	@Override
	public Steering<PacMan> steering() {
		return steering;
	}

	public void steering(Steering<PacMan> steering) {
		this.steering = steering;
		steering.triggerSteering(this);
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
			throw new IllegalStateException();
		}
	}

	/**
	 * NOTE: If the application property <code>PacMan.overflowBug</code> is <code>true</code>, this
	 * method simulates the bug in the original Arcade game which occurs if Pac-Man points upwards. In
	 * that case the same number of tiles to the left is added.
	 * 
	 * @param numTiles
	 *                   number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of the actor towards his current move
	 *         direction.
	 */
	@Override
	public Tile tilesAhead(int numTiles) {
		Tile tileAhead = maze().tileToDir(tile(), moveDir(), numTiles);
		if (moveDir() == UP && app().settings.getAsBoolean("PacMan.overflowBug")) {
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

	public int starvingTime() {
		return starvingTicks;
	}

	public void clearStarvingTime() {
		starvingTicks = -1;
	}

	private Optional<PacManGameEvent> inspect(Tile tile) {
		if (tile == maze().bonusTile) {
			Optional<PacManGameEvent> activeBonusFound = cast.bonus().filter(bonus -> bonus.is(ACTIVE))
					.map(bonus -> new BonusFoundEvent(bonus.symbol, bonus.value));
			if (activeBonusFound.isPresent()) {
				return activeBonusFound;
			}
		}
		if (tile.containsFood()) {
			starvingTicks = 0;
			if (tile.containsEnergizer()) {
				digestionTicks = DIGEST_ENERGIZER_TICKS;
				return Optional.of(new FoodFoundEvent(tile, true));
			}
			else {
				digestionTicks = DIGEST_PELLET_TICKS;
				return Optional.of(new FoodFoundEvent(tile, false));
			}
		}
		++starvingTicks;
		return Optional.empty();
	}
}