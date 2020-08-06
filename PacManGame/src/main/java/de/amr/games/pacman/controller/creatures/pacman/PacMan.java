package de.amr.games.pacman.controller.creatures.pacman;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.AWAKE;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.COLLAPSING;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.DEAD;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.IN_BED;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.POWERFUL;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.SLEEPING;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import java.awt.Graphics2D;
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
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.Pellet;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.api.Theme;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Creature<PacMan, PacManState> {

	private int foodWeight;
	private IPacManRenderer renderer;

	public PacMan(World world) {
		super(PacManState.class, world, "Pac-Man");
		/*@formatter:off*/
		beginStateMachine()

			.description(name)
			.initialState(IN_BED)

			.states()
			
				.state(IN_BED)
					.onEntry(() -> {
						goToBed();
						setVisible(true);
						foodWeight = 0;
					})

				.state(SLEEPING)

				.state(AWAKE)
					.onTick(this::wander)
					
				.state(POWERFUL)
					.onTick(this::wander)
					
				.state(DEAD)
					.timeoutAfter(sec(1.5f))

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
				
				.when(POWERFUL).then(AWAKE).onTimeout().act(() -> publish(new PacManLostPowerEvent()))
					.annotation("Lost power")
					
				.when(DEAD).then(COLLAPSING).onTimeout()	

		.endStateMachine();
		/* @formatter:on */
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent);
		doNotLogEventPublishingIf(e -> e instanceof FoodFoundEvent);
	}

	@Override
	public void getReadyToRumble(Game game) {
		setSpeed(() -> GameController.pacManSpeed(this, game.level));
		init();
	}

	/**
	 * Defines the steering used in the {@link PacManState#AWAKE} and {@link PacManState#POWERFUL}
	 * states.
	 * 
	 * @param steering steering to use
	 */
	public void behavior(Steering<PacMan> steering) {
		behavior(AWAKE, steering);
		behavior(POWERFUL, steering);
	}

	@Override
	public void draw(Graphics2D g) {
		renderer.render(g, this);
	}

	private void setPowerTimer(PacManGameEvent e) {
		PacManGainsPowerEvent powerEvent = (PacManGainsPowerEvent) e;
		state(POWERFUL).setTimer(powerEvent.duration);
		state(POWERFUL).resetTimer();
	}

	private void wander() {
		steering().steer(this);
		movement.update();
		if (enteredNewTile()) {
			foodWeight = Math.max(0, foodWeight - 1);
		}
		if (isTeleporting()) {
			return;
		}
		searchForFood().ifPresent(this::publish);
	}

	private Optional<PacManGameEvent> searchForFood() {
		Tile location = tileLocation();
		if (world.bonusFood().isPresent()) {
			BonusFood bonus = world.bonusFood().get();
			if (bonus.isPresent() && bonus.location().equals(location)) {
				foodWeight += Game.BIG_MEAL_WEIGHT;
				return Optional.of(new BonusFoundEvent(bonus));
			}
		}
		if (world.hasFood(Pellet.ENERGIZER, location)) {
			foodWeight += Game.BIG_MEAL_WEIGHT;
			return Optional.of(new FoodFoundEvent(location));
		}
		if (world.hasFood(Pellet.SNACK, location)) {
			foodWeight += Game.SNACK_WEIGHT;
			return Optional.of(new FoodFoundEvent(location));
		}
		return Optional.empty();
	}

	public void goToBed() {
		Bed bed = world.pacManBed();
		placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
		setMoveDir(bed.exitDir);
		setWishDir(bed.exitDir);
	}

	public boolean mustDigest() {
		return foodWeight > 0;
	}

	public long getPowerTicks() {
		return state(POWERFUL).getTicksRemaining();
	}

	public void wakeUp() {
		process(new PacManWakeUpEvent());
	}

	public void fallAsleep() {
		process(new PacManFallAsleepEvent());
	}

	public IPacManRenderer renderer() {
		return renderer;
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		renderer = theme.pacManRenderer(this);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (world.houses().anyMatch(house -> house.isDoor(neighbor))) {
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
		Tile tileAhead = world.tileToDir(tileLocation(), moveDir, numTiles);
		if (moveDir == UP && !settings.fixOverflowBug) {
			return world.tileToDir(tileAhead, LEFT, numTiles);
		}
		return tileAhead;
	}
}