/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.controller.creatures.pacman;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.AWAKE;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.COLLAPSING;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.DEAD;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.IN_BED;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.POWERFUL;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.SLEEPING;
import static de.amr.games.pacman.model.game.PacManGame.game;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
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
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.api.TemporaryFood;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.model.world.arcade.ArcadeFood;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * Hunting ghosty girls, eating, sleeping - a Pac-Man's life.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Guy<PacManState> {

	public final StateMachine<PacManState, PacManGameEvent> ai;
	private Steering walkingBehavior;
	private int weight;

	public PacMan(TiledWorld world, String name) {
		super(world, name);
		ai = buildAI();
		tf.width = tf.height = Tile.SIZE;
	}

	private StateMachine<PacManState, PacManGameEvent> buildAI() {
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
						weight = 0;
						movement.init();
					})

				.state(SLEEPING)

				.state(AWAKE)
					.onTick(() -> {
						move();
						searchForFood().ifPresent(ai::publish);
					})
					
				.state(POWERFUL)
					.onTick(() -> {
						move();
						searchForFood().ifPresent(ai::publish);
					})
					
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
	public Stream<StateMachine<?, ?>> machines() {
		return Stream.of(ai, movement);
	}

	@Override
	public void setSteering(PacManState state, Steering steering) {
		if (state == AWAKE || state == POWERFUL) {
			walkingBehavior = steering;
		}
	}

	@Override
	public Steering getSteering() {
		return ai.is(AWAKE) || ai.is(POWERFUL) ? walkingBehavior : Steering.STANDING_STILL;
	}

	@Override
	public void init() {
		ai.init();
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
		if (ai.getState() == null || !PacManGame.started()) {
			return 0;
		}
		if (ai.is(IN_BED, SLEEPING, DEAD, COLLAPSING)) {
			return 0;
		} else if (ai.is(POWERFUL)) {
			return Timing.speed(game.pacManPowerSpeed);
		} else if (ai.is(AWAKE)) {
			return Timing.speed(game.pacManSpeed);
		}
		throw new IllegalStateException("Illegal Pac-Man state: " + ai.getState());
	}

	@Override
	public void move() {
		if (weight > 0) {
			--weight;
			Application.loginfo("Pac-Man lost weight, remaining %d", weight);
		} else {
			super.move();
		}
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

	private Optional<PacManGameEvent> searchForFood() {
		Tile location = tile();
		TemporaryFood consumableBonus = world.temporaryFood().filter(bonus -> bonus.isActive() && !bonus.isConsumed())
				.filter(bonus -> bonus.location().equals(location)).orElse(null);
		if (consumableBonus != null) {
			return Optional.of(new BonusFoundEvent(location, consumableBonus));
		}
		if (world.hasFood(ArcadeFood.ENERGIZER, location)) {
			weight = ArcadeFood.ENERGIZER.fat();
			return Optional.of(new FoodFoundEvent(location, ArcadeFood.ENERGIZER));
		}
		if (world.hasFood(ArcadeFood.PELLET, location)) {
			weight = ArcadeFood.PELLET.fat();
			return Optional.of(new FoodFoundEvent(location, ArcadeFood.PELLET));
		}
		return Optional.empty();
	}
}