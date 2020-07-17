package de.amr.games.pacman.controller.creatures.pacman;

import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.DEAD;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.RUNNING;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.SLEEPING;
import static de.amr.games.pacman.model.game.Game.DIGEST_ENERGIZER_TICKS;
import static de.amr.games.pacman.model.game.Game.DIGEST_PELLET_TICKS;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import java.util.Optional;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.creatures.Animal;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.BonusState;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.api.Theme;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Animal<PacManState> {

	private int power;
	private int digestion;
	private boolean collapsing;
	private IPacManRenderer renderer;

	public PacMan() {
		super(PacManState.class, "Pac-Man");
		/*@formatter:off*/
		beginStateMachine()

			.description(this::toString)
			.initialState(SLEEPING)

			.states()

				.state(SLEEPING)
					.onEntry(() -> {
						power = digestion = 0;
						entity.visible = true;
						world.putIntoBed(this, world.pacManBed());
					})

				.state(RUNNING)
					.onEntry(() -> {
						digestion = 0;
					})

					.onTick(() -> {
						if (power > 0) {
							if (--power == 0) {
								publish(new PacManLostPowerEvent());
								return;
							}
						}
						if (digestion > 0) {
							--digestion;
							return;
						}
						steering().steer();
						movement.update();
						if (!isTeleporting()) {
							findSomethingInteresting().ifPresent(this::publish);
						}
					})

				.state(DEAD)
					.onEntry(() -> {
						power = digestion = 0;
					})

			.transitions()

				.when(RUNNING).then(DEAD).on(PacManKilledEvent.class)

		.endStateMachine();
		/* @formatter:on */
		PacManApp.fsm_register(this);
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		doNotLogEventProcessingIf(e -> e instanceof FoodFoundEvent);
		doNotLogEventPublishingIf(e -> e instanceof FoodFoundEvent);
	}

	public void setCollapsing(boolean collapsing) {
		this.collapsing = collapsing;
	}

	public boolean isCollapsing() {
		return collapsing;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		renderer = theme.createPacManRenderer(this);
	}

	@Override
	public IPacManRenderer renderer() {
		return renderer;
	}

	public void startRunning() {
		setState(RUNNING);
	}

	public void fallAsleep() {
		Vector2f position = entity.tf.getPosition();
		setState(PacManState.SLEEPING);
		entity.tf.setPosition(position); // reset position
	}

	/**
	 * Defines the steering used in the {@link PacManState#RUNNING} state.
	 * 
	 * @param steering steering to use in every state
	 */
	public void behavior(Steering steering) {
		behavior(RUNNING, steering);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (world.isDoor(neighbor)) {
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
		Tile tileAhead = world.tileToDir(location(), moveDir, numTiles);
		if (moveDir == UP && !settings.fixOverflowBug) {
			return world.tileToDir(tileAhead, LEFT, numTiles);
		}
		return tileAhead;
	}

	private Optional<PacManGameEvent> findSomethingInteresting() {
		Tile tile = location();
		if (tile.equals(world.bonusTile())
				&& world.getBonus().filter(bonus -> bonus.state == BonusState.ACTIVE).isPresent()) {
			return Optional.of(new BonusFoundEvent());
		}
		if (world.containsFood(tile)) {
			digestion = world.containsEnergizer(tile) ? DIGEST_ENERGIZER_TICKS : DIGEST_PELLET_TICKS;
			return Optional.of(new FoodFoundEvent(tile));
		}
		return Optional.empty();
	}
}