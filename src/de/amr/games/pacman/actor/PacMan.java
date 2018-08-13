package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.PacMan.State.DYING;
import static de.amr.games.pacman.actor.PacMan.State.SAFE;
import static de.amr.games.pacman.actor.PacMan.State.STEROIDS;
import static de.amr.games.pacman.actor.PacMan.State.VULNERABLE;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.view.PacManGameUI.SPRITES;

import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
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
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 *
 */
public class PacMan extends ControlledMazeMover<PacMan.State, GameEvent> {

	private final Game game;
	private final StateMachine<State, GameEvent> controller;
	private EventManager<GameEvent> events;
	private PacManWorld world;
	private int digestionTicks;

	public PacMan(Game game) {
		super(game.maze, game.maze.pacManHome, new EnumMap<>(State.class));
		this.game = game;
		controller = buildStateMachine();
		createSprites();
	}

	public void setEventManager(EventManager<GameEvent> events) {
		this.events = events;
	}

	public void setWorld(PacManWorld world) {
		this.world = world;
	}

	@Override
	public void move() {
		super.move();
		sprite = s_walking_to[getDir()];
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

	// Others

	@Override
	public float getSpeed() {
		return game.getPacManSpeed(this);
	}

	private void initPacMan() {
		digestionTicks = 0;
		placeAt(homeTile);
		setNextDir(Top4.E);
		getSprites().forEach(Sprite::resetAnimation);
		sprite = s_full;
	}

	// State machine

	public enum State {
		SAFE, VULNERABLE, STEROIDS, DYING
	}

	@Override
	public StateMachine<State, GameEvent> getStateMachine() {
		return controller;
	}

	private StateMachine<State, GameEvent> buildStateMachine() {
		return
		/* @formatter:off */
		StateMachine.define(State.class, GameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(SAFE)

			.states()

				.state(SAFE)
					.onEntry(this::initPacMan)

				.state(VULNERABLE)
					.impl(new VulnerableState())
					
				.state(STEROIDS)
					.impl(new SteroidsState())
					.timeoutAfter(game::getPacManSteroidTime)

				.state(DYING)
					.onEntry(() -> sprite = s_dying)
					.timeoutAfter(() -> game.sec(2))

			.transitions()

					.when(SAFE).then(VULNERABLE)
					
					.when(VULNERABLE).then(DYING).on(PacManKilledEvent.class)
	
					.when(VULNERABLE).then(STEROIDS).on(PacManGainsPowerEvent.class)
	
					.when(STEROIDS).on(PacManGainsPowerEvent.class).act(() -> controller.resetTimer())
	
					.when(STEROIDS).then(VULNERABLE).onTimeout().act(() -> events.publishEvent(new PacManLostPowerEvent()))
	
					.when(DYING).onTimeout().act(e -> events.publishEvent(new PacManDiedEvent()))

		.endStateMachine();
		/* @formatter:on */
	}

	// Pac-Man states

	private class VulnerableState extends StateObject<State, GameEvent> {

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
			if (isOutsideMaze()) {
				return;
			}
			Tile tile = getTile();
			// Ghost collision?
			Optional<Ghost> collidingGhost = world.getActiveGhosts()
			/*@formatter:off*/
				.filter(ghost -> ghost.getTile().equals(tile))
				.filter(ghost -> ghost.getState() != Ghost.State.DEAD)
				.filter(ghost -> ghost.getState() != Ghost.State.DYING)
				.filter(ghost -> ghost.getState() != Ghost.State.SAFE)
				.findFirst();
			/*@formatter:on*/
			if (collidingGhost.isPresent()) {
				events.publishEvent(new PacManGhostCollisionEvent(collidingGhost.get()));
				return;
			}
			// Unhonored bonus?
			Optional<Bonus> activeBonus = world.getBonus().filter(bonus -> bonus.getTile().equals(tile))
					.filter(bonus -> !bonus.isHonored());
			if (activeBonus.isPresent()) {
				events.publishEvent(new BonusFoundEvent(activeBonus.get().getSymbol(), activeBonus.get().getValue()));
				return;
			}
			// Food?
			char food = maze.getContent(tile);
			if (food == Content.PELLET || food == Content.ENERGIZER) {
				digestionTicks = game.getDigestionTicks(food);
				events.publishEvent(new FoodFoundEvent(tile, food));
			}
		}
	}

	private class SteroidsState extends VulnerableState {

		@Override
		public void onTick() {
			super.onTick();
			if (getRemaining() == getDuration() / 2) {
				events.publishEvent(new PacManGettingWeakerEvent());
			}
		}
	}
}