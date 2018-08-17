package de.amr.games.pacman.actor.game;

import static de.amr.games.pacman.actor.game.PacManState.DYING;
import static de.amr.games.pacman.actor.game.PacManState.GREEDY;
import static de.amr.games.pacman.actor.game.PacManState.HOME;
import static de.amr.games.pacman.actor.game.PacManState.HUNGRY;
import static de.amr.games.pacman.model.Game.TS;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.view.PacManGameUI.SPRITES;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.controller.event.core.EventManager;
import de.amr.games.pacman.controller.event.game.BonusFoundEvent;
import de.amr.games.pacman.controller.event.game.FoodFoundEvent;
import de.amr.games.pacman.controller.event.game.GameEvent;
import de.amr.games.pacman.controller.event.game.PacManDiedEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.game.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.game.PacManKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Content;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.Navigation;
import de.amr.games.pacman.navigation.impl.NavigationSystem;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends MazeMover {

	private final Game game;
	private final StateMachine<PacManState, GameEvent> controller;
	private final Map<PacManState, Navigation> navigationMap;
	private final EventManager<GameEvent> events;
	private final PacManWorld world;
	private int digestionTicks;

	public PacMan(Game game, PacManWorld world) {
		this.world = world;
		this.game = game;
		events = new EventManager<>("[PacMan]");
		controller = buildStateMachine();
		navigationMap = new EnumMap<>(PacManState.class);
		createSprites();
	}

	public void subscribe(Consumer<GameEvent> subscriber) {
		events.subscribe(subscriber);
	}

	@Override
	public Maze getMaze() {
		return game.maze;
	}

	public Tile getHome() {
		return getMaze().pacManHome;
	}

	@Override
	public float getSpeed() {
		return game.getPacManSpeed(getState());
	}

	// Navigation and movement

	public void setNavigation(PacManState state, Navigation navigation) {
		navigationMap.put(state, navigation);
	}

	@Override
	public int supplyIntendedDir() {
		Navigation nav = navigationMap.getOrDefault(getState(), NavigationSystem.forward());
		return nav.computeRoute(this).dir;
	}

	@Override
	public void move() {
		super.move();
		sprite = s_walking_to[getCurrentDir()];
		sprite.enableAnimation(canMove(getCurrentDir()));
	}

	@Override
	public boolean canWalkThroughDoor(Tile door) {
		return false;
	}

	// Sprites

	private Sprite sprite;

	private Sprite s_walking_to[] = new Sprite[4];
	private Sprite s_dying;
	private Sprite s_full;

	private void createSprites() {
		NESW.dirs().forEach(dir -> s_walking_to[dir] = SPRITES.pacManWalking(dir));
		s_dying = SPRITES.pacManDying();
		s_full = SPRITES.pacManFull();
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(Stream.of(s_walking_to), Stream.of(s_dying, s_full)).flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}

	public void setFullSprite() {
		sprite = s_full;
	}

	// State machine

	private void initPacMan() {
		digestionTicks = 0;
		placeAtTile(getHome(), TS / 2, 0);
		setNextDir(Top4.E);
		getSprites().forEach(Sprite::resetAnimation);
		sprite = s_full;
	}

	@Override
	public void init() {
		controller.init();
	}

	@Override
	public void update() {
		controller.update();
	}

	public PacManState getState() {
		return controller.currentState();
	}

	public void processEvent(GameEvent e) {
		controller.process(e);
	}

	public StateObject<PacManState, GameEvent> currentStateObject() {
		return controller.currentStateObject();
	}

	public void traceTo(Logger logger) {
		controller.traceTo(logger, game.fnTicksPerSec);
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
	
				.state(HUNGRY)
					.impl(new HungryState())
					
				.state(GREEDY)
					.impl(new GreedyState())
					.timeoutAfter(game::getPacManGreedyTime)
	
				.state(DYING)
					.onEntry(() -> sprite = s_dying)
					.timeoutAfter(() -> game.sec(2))

			.transitions()

				.when(HOME).then(HUNGRY)
				
				.when(HUNGRY).then(DYING)
					.on(PacManKilledEvent.class)
	
				.when(HUNGRY).then(GREEDY)
					.on(PacManGainsPowerEvent.class)
	
				.stay(GREEDY)
					.on(PacManGainsPowerEvent.class)
					.act(() -> controller.resetTimer())
	
				.when(GREEDY).then(HUNGRY)
					.onTimeout()
					.act(() -> events.publish(new PacManLostPowerEvent()))
	
				.stay(DYING)
					.onTimeout()
					.act(e -> events.publish(new PacManDiedEvent()))

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
			inspectMaze();
		}

		protected void inspectMaze() {
			move();
			Tile tile = getTile();
			// Ghost collision?
			Optional<Ghost> collidingGhost = world.getActiveGhosts()
			/*@formatter:off*/
				.filter(ghost -> ghost.getTile().equals(tile))
				.filter(ghost -> ghost.getState() != GhostState.DEAD)
				.filter(ghost -> ghost.getState() != GhostState.DYING)
				.filter(ghost -> ghost.getState() != GhostState.SAFE)
				.findFirst();
			/*@formatter:on*/
			if (collidingGhost.isPresent()) {
				events.publish(new PacManGhostCollisionEvent(collidingGhost.get()));
				return;
			}
			if (getMaze().isTeleportSpace(tile)) {
				return;
			}
			// Unhonored bonus?
			Optional<Bonus> activeBonus = world.getBonus().filter(bonus -> bonus.getTile().equals(tile))
					.filter(bonus -> !bonus.isHonored());
			if (activeBonus.isPresent()) {
				events.publish(new BonusFoundEvent(activeBonus.get().getSymbol(), activeBonus.get().getValue()));
				return;
			}
			// Food?
			char food = getMaze().getContent(tile);
			if (food == Content.PELLET || food == Content.ENERGIZER) {
				digestionTicks = game.getDigestionTicks(food);
				events.publish(new FoodFoundEvent(tile, food));
			}
		}
	}

	private class GreedyState extends HungryState {

		@Override
		public void onTick() {
			super.onTick();
			if (getRemaining() == game.getPacManGettingWeakerRemainingTime()) {
				events.publish(new PacManGettingWeakerEvent());
			}
		}
	}
}