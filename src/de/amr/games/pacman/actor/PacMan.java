package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.PacManState.DEAD;
import static de.amr.games.pacman.actor.PacManState.DYING;
import static de.amr.games.pacman.actor.PacManState.GREEDY;
import static de.amr.games.pacman.actor.PacManState.HOME;
import static de.amr.games.pacman.actor.PacManState.HUNGRY;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.theme.PacManThemes.THEME;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.EventManager;
import de.amr.games.pacman.controller.StateMachineClient;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.Navigation;
import de.amr.games.pacman.navigation.NavigationSystem;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Actor implements StateMachineClient<PacManState, GameEvent>, NavigationSystem<PacMan> {

	private final StateMachine<PacManState, GameEvent> fsm;
	private final Map<PacManState, Navigation<PacMan>> navigationMap;
	private final EventManager<GameEvent> events;
	private boolean eventsEnabled;
	private int digestionTicks;
	private PacManWorld world;

	public PacMan(Game game) {
		super(game);
		events = new EventManager<>("[PacMan]");
		eventsEnabled = true;
		fsm = buildStateMachine();
		navigationMap = new EnumMap<>(PacManState.class);
		createSprites();
	}

	public void initPacMan() {
		digestionTicks = 0;
		placeAt(getHome(), getTileSize() / 2, 0);
		setNextDir(Top4.E);
		getSprites().forEach(Sprite::resetAnimation);
		setCurrentSprite("s_full");
	}

	public void setWorld(PacManWorld world) {
		this.world = world;
	}

	// Eventing

	public void subscribe(Consumer<GameEvent> subscriber) {
		events.subscribe(subscriber);
	}

	public EventManager<GameEvent> getEvents() {
		return events;
	}

	public void setEventsEnabled(boolean eventsEnabled) {
		this.eventsEnabled = eventsEnabled;
	}

	private void publishEvent(GameEvent event) {
		if (eventsEnabled) {
			events.publish(event);
		}
	}

	// Accessors

	public Tile getHome() {
		return getMaze().getPacManHome();
	}

	@Override
	public float getSpeed() {
		return game.getPacManSpeed(getState());
	}

	// Movement

	public void setMoveBehavior(PacManState state, Navigation<PacMan> navigation) {
		navigationMap.put(state, navigation);
	}

	public Navigation<PacMan> getMoveBehavior() {
		return navigationMap.getOrDefault(getState(), keepDirection());
	}

	@Override
	public int supplyIntendedDir() {
		return getMoveBehavior().computeRoute(this).getDir();
	}

	@Override
	public boolean canTraverseDoor(Tile door) {
		return false;
	}

	// Sprites

	private void createSprites() {
		NESW.dirs().forEach(dir -> setSprite("s_walking_" + dir, THEME.pacManWalking(dir)));
		setSprite("s_dying", THEME.pacManDying());
		setSprite("s_full", THEME.pacManFull());
		setCurrentSprite("s_full");
	}

	public void setFullSprite() {
		setCurrentSprite("s_full");
	}

	private void updateSprite() {
		setCurrentSprite("s_walking_" + getCurrentDir());
		currentSprite().enableAnimation(!isStuck());
	}

	// State machine

	@Override
	public void init() {
		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
	}

	@Override
	public StateMachine<PacManState, GameEvent> getStateMachine() {
		return fsm;
	}

	public void traceTo(Logger logger) {
		fsm.traceTo(logger, game.fnTicksPerSec);
	}

	private StateMachine<PacManState, GameEvent> buildStateMachine() {
		return
		/* @formatter:off */
		StateMachine.define(PacManState.class, GameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(HOME)

			.states()

				.state(HOME)
					.onEntry(this::initPacMan)
					.timeoutAfter(() -> game.sec(0.25f))
	
				.state(HUNGRY)
					.impl(new HungryState())
					
				.state(GREEDY)
					.impl(new GreedyState())
					.timeoutAfter(game::getPacManGreedyTime)
	
				.state(DYING)
					.onEntry(() -> setCurrentSprite("s_dying"))
					.timeoutAfter(() -> game.sec(2))

			.transitions()

				.when(HOME).then(HUNGRY).onTimeout()
				
				.when(HUNGRY).then(DYING)
					.on(PacManKilledEvent.class)
	
				.when(HUNGRY).then(GREEDY)
					.on(PacManGainsPowerEvent.class)
	
				.stay(GREEDY)
					.on(PacManGainsPowerEvent.class)
					.act(() -> fsm.resetTimer())
	
				.when(GREEDY).then(HUNGRY)
					.onTimeout()
					.act(() -> publishEvent(new PacManLostPowerEvent()))
	
				.when(DYING).then(DEAD)
					.onTimeout()

		.endStateMachine();
		/* @formatter:on */
	}

	private class HungryState extends StateObject<PacManState, GameEvent> {

		@Override
		public void onTick() {
			if (digestionTicks > 0) {
				--digestionTicks;
				return;
			}
			move();
			updateSprite();
			if (world != null && eventsEnabled) {
				inspectTile(world, getTile());
			}
		}

		protected void inspectTile(PacManWorld world, Tile tile) {
			// Ghost collision?
			/*@formatter:off*/
			Optional<Ghost> collidingGhost = world.getActiveGhosts()
				.filter(ghost -> ghost.getTile().equals(tile))
				.filter(ghost -> ghost.getState() != GhostState.DEAD)
				.filter(ghost -> ghost.getState() != GhostState.DYING)
				.filter(ghost -> ghost.getState() != GhostState.SAFE)
				.findFirst();
			/*@formatter:on*/
			if (collidingGhost.isPresent()) {
				publishEvent(new PacManGhostCollisionEvent(collidingGhost.get()));
				return;
			}

			// Unhonored bonus?
			/*@formatter:off*/
			Optional<Bonus> activeBonus = world.getBonus()
					.filter(bonus -> bonus.getTile().equals(tile))
					.filter(bonus -> !bonus.isHonored());
			/*@formatter:on*/
			if (activeBonus.isPresent()) {
				Bonus bonus = activeBonus.get();
				publishEvent(new BonusFoundEvent(bonus.getSymbol(), bonus.getValue()));
				return;
			}

			// Food?
			if (getMaze().isFood(tile)) {
				boolean energizer = getMaze().isEnergizer(tile);
				digestionTicks = game.getDigestionTicks(energizer);
				publishEvent(new FoodFoundEvent(tile, energizer));
			}
		}
	}

	private class GreedyState extends HungryState {

		@Override
		public void onTick() {
			super.onTick();
			if (getRemaining() == game.getPacManGettingWeakerRemainingTime()) {
				publishEvent(new PacManGettingWeakerEvent());
			}
		}
	}
}