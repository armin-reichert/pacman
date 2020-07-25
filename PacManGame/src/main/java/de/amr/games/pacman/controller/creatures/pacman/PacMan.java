package de.amr.games.pacman.controller.creatures.pacman;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.AWAKE;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.COLLAPSING;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.DEAD;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.POWERFUL;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.SLEEPING;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.TIRED;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.creatures.api.Creature;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.PacManWakeUpEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.Bonus;
import de.amr.games.pacman.model.world.components.BonusState;
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
			.initialState(TIRED)

			.states()
			
				.state(TIRED)
					.onEntry(() -> {
						gotoBed();
						setVisible(true);
						foodWeight = 0;
					})

				.state(SLEEPING)

				.state(AWAKE)
					.onTick(this::wander)
					
				.state(POWERFUL)
					.onTick(this::wander)
					
				.state(DEAD)
					.timeoutAfter(sec(2f))

				.state(COLLAPSING)

			.transitions()

				.when(TIRED).then(SLEEPING)
					.annotation("What else without Ms. Pac-Man?")

				.when(TIRED).then(AWAKE).on(PacManWakeUpEvent.class)
				
				.when(SLEEPING).then(AWAKE).on(PacManWakeUpEvent.class)
				
				.when(AWAKE).then(POWERFUL).on(PacManGainsPowerEvent.class)
					.act(e -> {
						PacManGainsPowerEvent powerEvent = (PacManGainsPowerEvent) e;
						state(POWERFUL).setTimer(powerEvent.duration);
					})

				.when(AWAKE).then(SLEEPING).on(PacManFallAsleepEvent.class)
				
				.when(AWAKE).then(DEAD).on(PacManKilledEvent.class)
				
				.stay(POWERFUL).on(PacManGainsPowerEvent.class)
					.act(e -> {
						PacManGainsPowerEvent powerEvent = (PacManGainsPowerEvent) e;
						state(POWERFUL).setTimer(powerEvent.duration);
					})
				
				.when(POWERFUL).then(DEAD).on(PacManKilledEvent.class)
				
				.when(POWERFUL).then(SLEEPING).on(PacManFallAsleepEvent.class)
				
				.when(POWERFUL).then(AWAKE)
					.onTimeout()
					.annotation("Lost power")
					.act(() -> publish(new PacManLostPowerEvent()))
					
				.when(DEAD).then(COLLAPSING)
					.onTimeout()	

		.endStateMachine();
		/* @formatter:on */
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent);
		doNotLogEventPublishingIf(e -> e instanceof FoodFoundEvent);
	}

	private void wander() {
		steering().steer(this);
		movement.update();
		if (isTeleporting()) {
			return;
		}
		findSomethingInteresting().ifPresent(this::publish);
	}

	private Optional<PacManGameEvent> findSomethingInteresting() {
		if (enteredNewTile()) {
			foodWeight = Math.max(0, foodWeight - 1);
		}
		Tile pacManLocation = tileLocation();
		Optional<Bonus> maybeBonus = world.getBonus().filter(bonus -> bonus.state == BonusState.ACTIVE);
		if (maybeBonus.isPresent()) {
			Bonus bonus = maybeBonus.get();
			if (pacManLocation.equals(bonus.location)) {
				foodWeight += Game.DIGEST_BIG_MEAL_TICKS;
				return Optional.of(new BonusFoundEvent(bonus));
			}
		}
		if (world.containsFood(pacManLocation)) {
			foodWeight += world.containsEnergizer(pacManLocation) ? Game.DIGEST_BIG_MEAL_TICKS : Game.DIGEST_SNACK_TICKS;
			return Optional.of(new FoodFoundEvent(pacManLocation));
		}
		return Optional.empty();
	}

	private void gotoBed() {
		Bed bed = world.pacManBed();
		placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
		setMoveDir(bed.exitDir);
		setWishDir(bed.exitDir);
	}

	public void wakeUp() {
		process(new PacManWakeUpEvent());
	}

	public void fallAsleep() {
		process(new PacManFallAsleepEvent());
	}

	public int getPower() {
		return state(POWERFUL).getTicksRemaining();
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		renderer = theme.createPacManRenderer(this);
	}

	@Override
	public void draw(Graphics2D g) {
		renderer.render(g);
	}

	public IPacManRenderer renderer() {
		return renderer;
	}

	public boolean mustDigest() {
		return foodWeight > 0;
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
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (world.isDoorAt(neighbor)) {
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
		Tile tileAhead = world.tileToDir(tileLocation(), moveDir(), numTiles);
		if (moveDir() == UP && !settings.fixOverflowBug) {
			return world.tileToDir(tileAhead, LEFT, numTiles);
		}
		return tileAhead;
	}
}