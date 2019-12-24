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

import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.AbstractMazeMover;
import de.amr.games.pacman.actor.core.PacManGameActor;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLosingPowerEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.client.FsmComponent;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends AbstractMazeMover implements PacManGameActor<PacManState> {

	public final PacManGameCast cast;
	public final FsmComponent<PacManState, PacManGameEvent> fsmComponent;
	private Steering<PacMan> steering;
	private boolean kicking;
	private boolean losingPower;
	private int digestionTicks;
	private int ticksSinceLastMeal;

	public PacMan(PacManGameCast cast) {
		super("Pac-Man");
		this.cast = cast;
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
		fsmComponent = buildFsmComponent();
	}

	private StateMachine<PacManState, PacManGameEvent> buildStateMachine() {
		return StateMachine.
		/* @formatter:off */
		beginStateMachine(PacManState.class, PacManGameEvent.class)
				
			.description(String.format("[%s]", name()))
			.initialState(SLEEPING)
	
			.states()
	
				.state(SLEEPING)
					.onEntry(() -> {
						kicking = losingPower = false;
						placeAtTile(maze().pacManHome, Tile.SIZE / 2, 0);
						setMoveDir(RIGHT);
						setNextDir(RIGHT);
						sprites.forEach(Sprite::resetAnimation);
						sprites.select("full");
						clearStarvingTime();
					})
	
				.state(ALIVE)
					.onEntry(() -> {
						digestionTicks = 0;
						kicking = losingPower = false;
					})
	
					.onTick(() -> {
						if (digestionTicks > 0) {
							--digestionTicks;
							return;
						}
						if (kicking) {
							// power ending?
							if (state().getTicksConsumed() == state().getDuration() * 75 / 100) {
								losingPower = true;
								publish(new PacManLosingPowerEvent());
								return;
							}
							// power lost?
							if (state().getTicksRemaining() == 0) {
								cast.theme().snd_waza().stop();
								// "disable timer"
								state().setConstantTimer(State.ENDLESS);
								kicking = losingPower = false;
								publish(new PacManLostPowerEvent());
								return;
							}
						}
						steering().steer(PacMan.this);
						step();
						sprites.select("walking-" + moveDir());
						sprites.current().get().enableAnimation(canMoveForward());
						if (!teleporting.is(true)) {
							inspect(tile()).ifPresent(fsmComponent::publish);
						}
				})
					
				.state(DEAD)
					.onEntry(() -> {
						kicking = losingPower = false;
						digestionTicks = 0;
					})
					
			.transitions()
	
				.stay(ALIVE) // Ah, ha, ha, ha, stayin' alive
					.on(PacManGainsPowerEvent.class)
					.act(() -> {
						kicking = true;
						// set and start power timer
						state().setConstantTimer(sec(game().level.pacManPowerSeconds));
						FSM_LOGGER.info(() -> String.format("Pac-Man gaining power for %d ticks (%.2f sec)", 
								state().getDuration(), state().getDuration() / 60f));
						cast.theme().snd_waza().loop();
					})
					
				.when(ALIVE).then(DEAD)
					.on(PacManKilledEvent.class)
	
		.endStateMachine();
		/* @formatter:on */
	}

	private FsmComponent<PacManState, PacManGameEvent> buildFsmComponent() {
		StateMachine<PacManState, PacManGameEvent> fsm = buildStateMachine();
		fsm.traceTo(PacManGame.FSM_LOGGER, () -> 60);
		return new FsmComponent<>(fsm);
	}

	@Override
	public void init() {
		super.init();
		fsmComponent.init();
	}

	@Override
	public void update() {
		super.update();
		fsmComponent.update();
	}

	@Override
	public FsmComponent<PacManState, PacManGameEvent> fsmComponent() {
		return fsmComponent;
	}

	public PacManGame game() {
		return cast.game;
	}

	@Override
	public Maze maze() {
		return cast.game.maze;
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
			return speed(kicking ? game().level.pacManPowerSpeed : game().level.pacManSpeed);
		case DEAD:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * NOTE: If the application property <code>PacMan.overflowBug</code> is
	 * <code>true</code>, this method simulates the bug in the original Arcade game
	 * which occurs if Pac-Man points upwards. In that case the same number of tiles
	 * to the left is added.
	 * 
	 * @param numTiles number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of the actor
	 *         towards his current move direction.
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
		if (neighbor.isDoor()) {
			return false;
		}
		return super.canMoveBetween(tile, neighbor);
	}

	public int starvingTime() {
		return ticksSinceLastMeal;
	}

	public void clearStarvingTime() {
		ticksSinceLastMeal = -1;
	}

	public boolean hasPower() {
		return kicking;
	}

	public boolean isLosingPower() {
		return losingPower;
	}

	private Optional<PacManGameEvent> inspect(Tile tile) {

		/*@formatter:off*/
		Optional<PacManGameEvent> activeBonus = cast.bonus()
			.filter(bonus -> tile == maze().bonusTile)
			.filter(bonus -> bonus.is(ACTIVE))
			.map(bonus -> new BonusFoundEvent(bonus.symbol, bonus.value));
		/*@formatter:on*/

		if (activeBonus.isPresent()) {
			return activeBonus;
		}

		if (tile.containsFood()) {
			ticksSinceLastMeal = 0;
			if (tile.containsEnergizer()) {
				digestionTicks = DIGEST_ENERGIZER_TICKS;
				return Optional.of(new FoodFoundEvent(tile, true));
			} else {
				digestionTicks = DIGEST_PELLET_TICKS;
				return Optional.of(new FoodFoundEvent(tile, false));
			}
		}
		++ticksSinceLastMeal;
		return Optional.empty();
	}
}