package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.PacManApp.THEME;
import static de.amr.games.pacman.actor.PacManState.DEAD;
import static de.amr.games.pacman.actor.PacManState.DYING;
import static de.amr.games.pacman.actor.PacManState.GREEDY;
import static de.amr.games.pacman.actor.PacManState.HOME;
import static de.amr.games.pacman.actor.PacManState.HUNGRY;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.EventManager;
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
import de.amr.games.pacman.navigation.ActorNavigation;
import de.amr.games.pacman.navigation.ActorNavigationSystem;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends Actor implements ActorNavigationSystem<PacMan> {

	private final StateMachine<PacManState, GameEvent> fsm;
	private final Map<PacManState, ActorNavigation<PacMan>> navigationMap;
	private final EventManager<GameEvent> eventManager;
	private PacManWorld world;
	private int digestionTicks;

	public PacMan(Game game) {
		super(game);
		fsm = buildStateMachine();
		fsm.traceTo(LOGGER, app().clock::getFrequency);
		navigationMap = new EnumMap<>(PacManState.class);
		eventManager = new EventManager<>("[PacMan]");
		setSprites();
	}

	public void initPacMan() {
		digestionTicks = 0;
		placeAtTile(getHomeTile(), getTileSize() / 2, 0);
		setNextDir(Top4.E);
		getSprites().forEach(Sprite::resetAnimation);
		setSelectedSprite("s_full");
	}

	public void setWorld(PacManWorld world) {
		this.world = world;
	}

	// Accessors

	public EventManager<GameEvent> getEventManager() {
		return eventManager;
	}

	public Tile getHomeTile() {
		return getMaze().getPacManHome();
	}

	@Override
	public float getSpeed() {
		return game.getPacManSpeed(getState());
	}

	// Movement

	public void setMoveBehavior(PacManState state, ActorNavigation<PacMan> navigation) {
		navigationMap.put(state, navigation);
	}

	public ActorNavigation<PacMan> getMoveBehavior() {
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
	
	@Override
	public void move() {
		super.move();
		updateWalkingSprite();
	}

	// Sprites

	private void setSprites() {
		NESW.dirs().forEach(dir -> setSprite("s_walking_" + dir, THEME.spr_pacManWalking(dir)));
		setSprite("s_dying", THEME.spr_pacManDying());
		setSprite("s_full", THEME.spr_pacManFull());
		setSelectedSprite("s_full");
	}

	public void setFullSprite() {
		setSelectedSprite("s_full");
	}

	private void updateWalkingSprite() {
		setSelectedSprite("s_walking_" + getCurrentDir());
		getSelectedSprite().enableAnimation(!isStuck());
	}

	// State machine

	@Override
	public void init() {
		super.init();
		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
	}

	public PacManState getState() {
		return fsm.getState();
	}

	public State<PacManState, GameEvent> getStateObject() {
		return fsm.state();
	}

	public void processEvent(GameEvent event) {
		fsm.process(event);
	}

	private StateMachine<PacManState, GameEvent> buildStateMachine() {
		/* @formatter:off */
		return StateMachine.define(PacManState.class, GameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(HOME)

			.states()

				.state(HOME)
					.onEntry(this::initPacMan)
					.timeoutAfter(() -> app().clock.sec(0.25f))
	
				.state(HUNGRY)
					.impl(new HungryState())
					
				.state(GREEDY)
					.impl(new GreedyState())
					.timeoutAfter(game::getPacManGreedyTime)
	
				.state(DYING)
					.onEntry(() -> setSelectedSprite("s_dying"))
					.timeoutAfter(() -> app().clock.sec(2))

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
					.act(() -> getEventManager().publish(new PacManLostPowerEvent()))
	
				.when(DYING).then(DEAD)
					.onTimeout()

		.endStateMachine();
		/* @formatter:on */
	}

	private class HungryState extends State<PacManState, GameEvent> {

		@Override
		public void onTick() {
			if (digestionTicks > 0) {
				--digestionTicks;
				return;
			}
			move();
			if (world != null && getEventManager().isEnabled()) {
				inspectTile(world, getTile());
			}
		}

		protected void inspectTile(PacManWorld world, Tile tile) {
			// Ghost collision?
			/*@formatter:off*/
			Optional<Ghost> collidingGhost = world.getGhosts()
				.filter(ghost -> ghost.getTile().equals(tile))
				.filter(ghost -> ghost.getState() != GhostState.DEAD)
				.filter(ghost -> ghost.getState() != GhostState.DYING)
				.filter(ghost -> ghost.getState() != GhostState.SAFE)
				.findFirst();
			/*@formatter:on*/
			if (collidingGhost.isPresent()) {
				getEventManager().publish(new PacManGhostCollisionEvent(collidingGhost.get()));
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
				getEventManager().publish(new BonusFoundEvent(bonus.getSymbol(), bonus.getValue()));
				return;
			}

			// Food?
			if (getMaze().isFood(tile)) {
				boolean energizer = getMaze().isEnergizer(tile);
				digestionTicks = game.getDigestionTicks(energizer);
				getEventManager().publish(new FoodFoundEvent(tile, energizer));
			}
		}
	}

	private class GreedyState extends HungryState {

		@Override
		public void onTick() {
			super.onTick();
			if (getTicksRemaining() == game.getPacManGettingWeakerRemainingTime()) {
				getEventManager().publish(new PacManGettingWeakerEvent());
			}
		}
	}
}