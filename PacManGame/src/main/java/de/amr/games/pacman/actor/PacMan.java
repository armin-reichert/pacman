package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.BonusState.ACTIVE;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.actor.PacManState.DEAD;
import static de.amr.games.pacman.actor.PacManState.DYING;
import static de.amr.games.pacman.actor.PacManState.HOME;
import static de.amr.games.pacman.actor.PacManState.HUNGRY;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.PacManGame.DIGEST_ENERGIZER_TICKS;
import static de.amr.games.pacman.model.PacManGame.DIGEST_PELLET_TICKS;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.model.Timing.speed;

import java.util.Optional;
import java.util.logging.Logger;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.AbstractMazeMover;
import de.amr.games.pacman.actor.fsm.FsmComponent;
import de.amr.games.pacman.actor.fsm.FsmContainer;
import de.amr.games.pacman.actor.fsm.FsmControlled;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
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
public class PacMan extends AbstractMazeMover implements FsmContainer<PacManState> {

	public final PacManGameCast cast;
	public final PacManGame game;
	public final FsmComponent<PacManState> fsmComponent;
	public int ticksSinceLastMeal;
	protected Steering<PacMan> steering;

	public PacMan(PacManGameCast cast) {
		this.cast = cast;
		this.game = cast.game;
		fsmComponent = buildFsmComponent("Pac-Man");
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
	}

	@Override
	public Maze maze() {
		return cast.game.maze;
	}

	private FsmComponent<PacManState> buildFsmComponent(String name) {
		StateMachine<PacManState, PacManGameEvent> fsm = buildStateMachine();
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		FsmComponent<PacManState> component = new FsmComponent<>(name, fsm);
		component.publishedEventIsLogged = event -> {
			// do not write log entry when normal pellet is found
			if (event instanceof FoodFoundEvent) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				return foodFound.energizer;
			}
			return true;
		};
		return component;
	}

	private StateMachine<PacManState, PacManGameEvent> buildStateMachine() {
		return StateMachine.
		/* @formatter:off */
		beginStateMachine(PacManState.class, PacManGameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(HOME)
	
			.states()
	
				.state(HOME)
					.onEntry(() -> {
						placeAtTile(maze().pacManHome, Tile.SIZE / 2, 0);
						setMoveDir(RIGHT);
						setNextDir(RIGHT);
						sprites.forEach(Sprite::resetAnimation);
						sprites.select("full");
						ticksSinceLastMeal = 0;
					})
	
				.state(HUNGRY)
					.impl(new HungryState())
					
				.state(DYING)
					.timeoutAfter(sec(4f))
					.onEntry(() -> {
						sprites.select("full");
					})
	
			.transitions()
	
				.when(HOME).then(HUNGRY)
				
				.stay(HUNGRY)
					.on(PacManGainsPowerEvent.class)
					.act(() -> {
						state().setConstantTimer(sec(game.level.pacManPowerSeconds));
						cast.theme.snd_waza().loop();
						LOGGER.info(() -> String.format("Pac-Man got power for %d ticks (%d sec)", 
								state().getDuration(), state().getDuration() / 60));
					})
					
				.when(HUNGRY).then(DYING)
					.on(PacManKilledEvent.class)
	
				.when(DYING).then(DEAD)
					.on(PacManDiedEvent.class)
	
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

	@Override
	public FsmControlled<PacManState> fsmComponent() {
		return fsmComponent;
	}

	// Movement

	public void setSteering(Steering<PacMan> steering) {
		this.steering = steering;
		steering.triggerSteering(this);
	}

	@Override
	public float maxSpeed() {
		switch (getState()) {
		case HUNGRY:
			return hasPower() ? speed(game.level.pacManPowerSpeed) : speed(game.level.pacManSpeed);
		default:
			return 0;
		}
	}

	@Override
	protected void move() {
		super.move();
		sprites.select("walking-" + moveDir());
		sprites.current().ifPresent(sprite -> sprite.enableAnimation(canMoveForward()));
	}

	@Override
	public void steer() {
		steering.steer(this);
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

	// State machine

	public boolean isLosingPower() {
		// TODO how was the timing in the original game?
		return hasPower() && state().getTicksRemaining() <= state().getDuration() * 33 / 100;
	}

	private boolean startsLosingPower() {
		return hasPower() && state().getTicksRemaining() == state().getDuration() * 33 / 100;
	}

	public boolean hasPower() {
		return is(HUNGRY) && state().getTicksRemaining() > 0;
	}

	/*
	 * Subclass implementing the HUNGRY state.
	 */
	private class HungryState extends State<PacManState, PacManGameEvent> {

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
				setConstantTimer(0);
				cast.theme.snd_waza().stop();
				fsmComponent.publish(new PacManLostPowerEvent());
			} else if (mustDigest()) {
				digest();
			} else {
				steer();
				move();
				findSomethingInteresting().ifPresent(fsmComponent::publish);
			}
		}

		private boolean mustDigest() {
			return digestion > 0;
		}

		private void digest() {
			digestion -= 1;
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
				.filter(ghost -> ghost.is(CHASING) || ghost.is(SCATTERING) || ghost.is(FRIGHTENED))
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
				ticksSinceLastMeal = 0;
				boolean energizer = pacManTile.containsEnergizer();
				digestion = energizer ? DIGEST_ENERGIZER_TICKS : DIGEST_PELLET_TICKS;
				return Optional.of(new FoodFoundEvent(pacManTile, energizer));
			} else {
				ticksSinceLastMeal += 1;
			}

			return Optional.empty();
		}
	}
}