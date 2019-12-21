package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.actor.PacManState.ALIVE;
import static de.amr.games.pacman.actor.PacManState.DEAD;
import static de.amr.games.pacman.actor.PacManState.HOME;
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
import de.amr.games.pacman.actor.fsm.FsmComponent;
import de.amr.games.pacman.actor.fsm.FsmContainer;
import de.amr.games.pacman.actor.fsm.FsmControlled;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends AbstractMazeMover implements FsmContainer<PacManState, PacManGameEvent> {

	public final PacManGameCast cast;
	public final FsmComponent<PacManState, PacManGameEvent> fsmComponent;
	private int ticksSinceLastMeal;
	private Steering<PacMan> steering;

	public PacMan(PacManGameCast cast) {
		super("Pac-Man");
		this.cast = cast;
		fsmComponent = buildFsmComponent();
		// no logging if simple pellet is found
		fsmComponent.publishedEventIsLogged = event -> {
			if (event instanceof FoodFoundEvent) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				return foodFound.energizer;
			}
			return true;
		};
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
	}

	@Override
	public FsmControlled<PacManState, PacManGameEvent> fsmComponent() {
		return fsmComponent;
	}

	public PacManGame game() {
		return cast.game;
	}

	@Override
	public Maze maze() {
		return cast.game.maze;
	}

	public int ticksSinceLastMeal() {
		return ticksSinceLastMeal;
	}

	public void clearTicksSinceLastMeal() {
		ticksSinceLastMeal = -1;
	}

	private FsmComponent<PacManState, PacManGameEvent> buildFsmComponent() {
		StateMachine<PacManState, PacManGameEvent> fsm = buildStateMachine();
		fsm.traceTo(PacManGame.FSM_LOGGER, () -> 60);
		return new FsmComponent<>(fsm);
	}

	private StateMachine<PacManState, PacManGameEvent> buildStateMachine() {
		return StateMachine.
		/* @formatter:off */
		beginStateMachine(PacManState.class, PacManGameEvent.class)
				
			.description(String.format("[%s]", name()))
			.initialState(HOME)
	
			.states()
	
				.state(HOME)
					.onEntry(() -> {
						placeAtTile(maze().pacManHome, Tile.SIZE / 2, 0);
						setMoveDir(RIGHT);
						setNextDir(RIGHT);
						sprites.forEach(Sprite::resetAnimation);
						sprites.select("full");
						clearTicksSinceLastMeal();
					})
	
				.state(ALIVE)
					.impl(new AliveState())
					
			.transitions()
	
				.when(HOME).then(ALIVE)
				
				.stay(ALIVE)
					.on(PacManGainsPowerEvent.class)
					.act(() -> {
						state().setConstantTimer(sec(game().level.pacManPowerSeconds));
						cast.theme().snd_waza().loop();
						FSM_LOGGER.info(() -> String.format("%s has power for %d ticks (%.2f sec)", 
								fsmComponent.name(), state().getDuration(), state().getDuration() / 60f));
					})
					
				.when(ALIVE).then(DEAD)
					.on(PacManKilledEvent.class)
	
		.endStateMachine();
		/* @formatter:on */
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

	// Movement

	@Override
	public float maxSpeed() {
		switch (getState()) {
		case HOME:
			return 0;
		case ALIVE:
			return hasPower() ? speed(game().level.pacManPowerSpeed) : speed(game().level.pacManSpeed);
		case DEAD:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public Steering<PacMan> steering() {
		return steering;
	}

	public void steering(Steering<PacMan> steering) {
		this.steering = steering;
		steering.triggerSteering(this);
	}

	/**
	 * NOTE: If the application property <code>overflowBug</code> is
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
		if (moveDir() == UP && app().settings.getAsBoolean("overflowBug")) {
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

	// TODO when exactly is Pac-Man's power starting to vanish in the original game?

	public boolean hasPower() {
		return is(ALIVE) && state().getDuration() != State.ENDLESS && state().getTicksRemaining() > 0;
	}

	private boolean startsLosingPower() {
		return hasPower() && state().getTicksConsumed() == state().getDuration() * 66 / 100;
	}

	public boolean isLosingPower() {
		return hasPower() && state().getTicksConsumed() >= state().getDuration() * 66 / 100;
	}

	/**
	 * Subclass implementing the ALIVE state.
	 */
	private class AliveState extends State<PacManState, PacManGameEvent> {

		/** Ticks Pac-Man must rest after eating a pellet or energizer. */
		private int digestion;

		@Override
		public void onEntry() {
			digestion = 0;
		}

		@Override
		public void onTick() {
			if (startsLosingPower()) {
				fsmComponent.publish(new PacManGettingWeakerEvent());
			} else if (getTicksRemaining() == 1) {
				// power-mode is ending right now
				cast.theme().snd_waza().stop();
				setConstantTimer(State.ENDLESS);
				fsmComponent.publish(new PacManLostPowerEvent());
			} else if (digestion > 0) {
				digestion -= 1;
			} else {
				steering().steer(PacMan.this);
				step();
				sprites.select("walking-" + moveDir());
				sprites.current().get().enableAnimation(canMoveForward());
				findSomethingInteresting().ifPresent(fsmComponent::publish);
			}
		}

		private Optional<PacManGameEvent> findSomethingInteresting() {
			Tile pacManTile = tile();

			if (!maze().insideBoard(pacManTile) || !visible) {
				return Optional.empty(); // when teleporting no events are triggered
			}

			/*@formatter:off*/
			Optional<PacManGameEvent> ghostCollision = cast.ghostsOnStage()
				.filter(Ghost::visible)
				.filter(ghost -> ghost.tile().equals(pacManTile))
				.filter(ghost -> ghost.is(CHASING, SCATTERING, FRIGHTENED))
				.findFirst()
				.map(PacManGhostCollisionEvent::new);
			/*@formatter:on*/

			if (ghostCollision.isPresent()) {
				return ghostCollision;
			}

			/*@formatter:off*/
			Optional<PacManGameEvent> bonusEaten = cast.bonus()
				.filter(bonus -> pacManTile == maze().bonusTile)
				.filter(bonus -> bonus.is(ACTIVE))
				.map(bonus -> new BonusFoundEvent(bonus.symbol, bonus.value));
			/*@formatter:on*/

			if (bonusEaten.isPresent()) {
				return bonusEaten;
			}

			if (pacManTile.containsFood()) {
				++ticksSinceLastMeal;
				boolean energizer = pacManTile.containsEnergizer();
				digestion = energizer ? DIGEST_ENERGIZER_TICKS : DIGEST_PELLET_TICKS;
				return Optional.of(new FoodFoundEvent(pacManTile, energizer));
			}

			return Optional.empty();
		}
	}
}