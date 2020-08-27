package de.amr.games.pacman.controller.creatures.pacman;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.AWAKE;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.COLLAPSING;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.DEAD;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.IN_BED;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.POWERFUL;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.SLEEPING;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import java.util.EnumMap;
import java.util.Optional;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManFallAsleepEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.PacManWakeUpEvent;
import de.amr.games.pacman.controller.game.Timing;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.MovementType;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.api.TemporaryFood;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeFood;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * Hunting ghosty girls, eating, sleeping - a Pac-Man's life.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Guy<PacManState> {

	private int fat;

	public PacMan(World world, String name) {
		super(world, name, new EnumMap<>(PacManState.class));
	}

	@Override
	protected StateMachine<PacManState, PacManGameEvent> buildAI() {
		StateMachine<PacManState, PacManGameEvent> fsm = StateMachine
		/*@formatter:off*/
		.beginStateMachine(PacManState.class, PacManGameEvent.class, TransitionMatchStrategy.BY_CLASS)
			.description(name)
			.initialState(IN_BED)

			.states()
			
				.state(IN_BED)
					.onEntry(() -> {
						putIntoBed(world.pacManBed());
						visible = true;
						fat = 0;
					})

				.state(SLEEPING)

				.state(AWAKE)
					.onTick(this::wander)
					
				.state(POWERFUL)
					.onTick(this::wander)
					
				.state(DEAD)
					.timeoutAfter(Timing.sec(2.5f))

				.state(COLLAPSING)

			.transitions()

				.when(IN_BED).then(SLEEPING)
					.annotation("What else without Ms. Pac-Man?")

				.when(IN_BED).then(AWAKE).on(PacManWakeUpEvent.class)
				
				.when(SLEEPING).then(AWAKE).on(PacManWakeUpEvent.class)
				
				.when(AWAKE).then(POWERFUL).on(PacManGainsPowerEvent.class).act(this::setPowerTimer)

				.when(AWAKE).then(SLEEPING).on(PacManFallAsleepEvent.class)
				
				.when(AWAKE).then(DEAD).on(PacManKilledEvent.class)
				
				.stay(POWERFUL).on(PacManGainsPowerEvent.class).act(this::setPowerTimer)
				
				.when(POWERFUL).then(DEAD).on(PacManKilledEvent.class)
				
				.when(POWERFUL).then(SLEEPING).on(PacManFallAsleepEvent.class)
				
				.when(POWERFUL).then(AWAKE).onTimeout().act(() -> ai.publish(new PacManLostPowerEvent()))
					.annotation("Lost power")
					
				.when(DEAD).then(COLLAPSING).onTimeout()	

		.endStateMachine();
		/* @formatter:on */
		fsm.setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		fsm.doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent);
		fsm.doNotLogEventPublishingIf(e -> e instanceof FoodFoundEvent);
		return fsm;
	}

	@Override
	public void update() {
		ai.update();
	}

	public void wakeUp() {
		ai.process(new PacManWakeUpEvent());
	}

	public void fallAsleep() {
		ai.process(new PacManFallAsleepEvent());
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (world.houses().flatMap(House::doors).anyMatch(door -> door.includes(neighbor))) {
			return false;
		}
		return world.isAccessible(neighbor);
	}

	/**
	 * NOTE: Depending on the application setting {@link PacManApp.Settings#fixOverflowBug}, this method
	 * simulates/fixes the overflow bug from the original Arcade game which causes, if Pac-Man points
	 * upwards, the wrong calculation of the position ahead of Pac-Man (namely adding the same number of
	 * tiles to the left).
	 * 
	 * @param nTiles number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of Pac-Man towards his current move
	 *         direction.
	 */
	public Tile tilesAhead(int nTiles) {
		Tile tileAhead = world.tileToDir(tile(), moveDir, nTiles);
		if (moveDir == UP && !settings.fixOverflowBug) {
			tileAhead = world.tileToDir(tileAhead, LEFT, nTiles);
		}
		return tileAhead;
	}

	@Override
	public float getSpeed() {
		if (ai.getState() == null || PacManGame.level == null) {
			return 0;
		}
		if (ai.is(IN_BED, SLEEPING, DEAD, COLLAPSING)) {
			return 0;
		} else if (ai.is(POWERFUL)) {
			return Timing.speed(fat > 0 ? PacManGame.level.pacManPowerDotsSpeed : PacManGame.level.pacManPowerSpeed);
		} else if (ai.is(AWAKE)) {
			return Timing.speed(fat > 0 ? PacManGame.level.pacManDotsSpeed : PacManGame.level.pacManSpeed);
		}
		throw new IllegalStateException("Illegal Pac-Man state: " + ai.getState());
	}

	/**
	 * Defines the steering used in the {@link PacManState#AWAKE} and {@link PacManState#POWERFUL}
	 * states.
	 * 
	 * @param steering steering to use
	 */
	public void setWalkingBehavior(Steering steering) {
		behavior(AWAKE, steering);
		behavior(POWERFUL, steering);
	}

	private void putIntoBed(Bed bed) {
		if (bed != null) {
			placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
			moveDir = wishDir = bed.exitDir;
		}
	}

	private void setPowerTimer(PacManGameEvent e) {
		PacManGainsPowerEvent powerEvent = (PacManGainsPowerEvent) e;
		ai.state(POWERFUL).setTimer(powerEvent.duration);
		ai.state(POWERFUL).resetTimer();
	}

	private void wander() {
		steering().steer(this);
		movement.update();
		if (enteredNewTile) {
			fat = Math.max(0, fat - 1);
		}
		if (!movement.is(MovementType.TELEPORTING)) {
			searchForFood(tile()).ifPresent(ai::publish);
		}
	}

	private Optional<PacManGameEvent> searchForFood(Tile location) {
		if (world.temporaryFood().filter(bonusFood -> !bonusFood.isConsumed()).isPresent()) {
			TemporaryFood bonus = world.temporaryFood().get();
			if (bonus.isActive() && bonus.location().equals(location)) {
				fat += PacManGame.FAT_ENERGIZER;
				return Optional.of(new BonusFoundEvent(location, bonus));
			}
		}
		if (world.hasFood(ArcadeFood.ENERGIZER, location)) {
			fat += PacManGame.FAT_ENERGIZER;
			return Optional.of(new FoodFoundEvent(location, ArcadeFood.ENERGIZER));
		}
		if (world.hasFood(ArcadeFood.PELLET, location)) {
			fat += PacManGame.FAT_PELLET;
			return Optional.of(new FoodFoundEvent(location, ArcadeFood.PELLET));
		}
		return Optional.empty();
	}
}