package de.amr.games.pacman.controller.creatures.pacman;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.AWAKE;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.COLLAPSING;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.DEAD;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.IN_BED;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.POWERFUL;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.SLEEPING;
import static de.amr.games.pacman.controller.game.GameController.speed;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import java.util.EnumMap;
import java.util.Optional;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.creatures.Creature;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManFallAsleepEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.PacManWakeUpEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.MovementType;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.Pellet;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * Hunting ghosty girls, eating, sleeping - a Pac-Man's life.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Creature<PacManState> {

	private int fat;

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
						putIntoBed();
						entity.visible = true;
						enabled = true;
						fat = 0;
					})

				.state(SLEEPING)

				.state(AWAKE)
					.onTick(this::wander)
					
				.state(POWERFUL)
					.onTick(this::wander)
					
				.state(DEAD)
					.timeoutAfter(sec(2.5f))

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

	public PacMan(World world) {
		super("Pac-Man", world, new EnumMap<>(PacManState.class));
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
		if (entity.world.houses().anyMatch(house -> house.hasDoorAt(neighbor))) {
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
	 * @param nTiles number of tiles
	 * @return the tile located <code>numTiles</code> tiles ahead of Pac-Man towards his current move
	 *         direction.
	 */
	public Tile tilesAhead(int nTiles) {
		Tile tileAhead = entity.world.tileToDir(entity.tileLocation(), entity.moveDir, nTiles);
		if (entity.moveDir == UP && !settings.fixOverflowBug) {
			tileAhead = entity.world.tileToDir(tileAhead, LEFT, nTiles);
		}
		return tileAhead;
	}

	@Override
	public float getSpeed() {
		if (ai.getState() == null || game == null) {
			return 0;
		}
		if (ai.is(IN_BED, SLEEPING, DEAD, COLLAPSING)) {
			return 0;
		} else if (ai.is(POWERFUL)) {
			return speed(fat > 0 ? game.level.pacManPowerDotsSpeed : game.level.pacManPowerSpeed);
		} else if (ai.is(AWAKE)) {
			return speed(fat > 0 ? game.level.pacManDotsSpeed : game.level.pacManSpeed);
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

	private void putIntoBed() {
		Bed bed = entity.world.pacManBed();
		entity.placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
		entity.moveDir = bed.exitDir;
		entity.wishDir = bed.exitDir;
	}

	private void setPowerTimer(PacManGameEvent e) {
		PacManGainsPowerEvent powerEvent = (PacManGainsPowerEvent) e;
		ai.state(POWERFUL).setTimer(powerEvent.duration);
		ai.state(POWERFUL).resetTimer();
	}

	private void wander() {
		steering().steer(entity);
		movement.update();
		enabled = entity.tf.vx != 0 || entity.tf.vy != 0;
		if (entity.enteredNewTile) {
			fat = Math.max(0, fat - 1);
		}
		if (!movement.is(MovementType.TELEPORTING)) {
			searchForFood(entity.tileLocation()).ifPresent(ai::publish);
		}
	}

	private Optional<PacManGameEvent> searchForFood(Tile location) {
		if (entity.world.bonusFood().isPresent()) {
			BonusFood bonus = entity.world.bonusFood().get();
			if (bonus.isPresent() && bonus.location().equals(location)) {
				fat += Game.BIG_MEAL_FAT;
				return Optional.of(new BonusFoundEvent(bonus));
			}
		}
		if (entity.world.hasFood(Pellet.ENERGIZER, location)) {
			fat += Game.BIG_MEAL_FAT;
			return Optional.of(new FoodFoundEvent(location));
		}
		if (entity.world.hasFood(Pellet.SNACK, location)) {
			fat += Game.SNACK_FAT;
			return Optional.of(new FoodFoundEvent(location));
		}
		return Optional.empty();
	}
}