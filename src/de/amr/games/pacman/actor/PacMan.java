package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.PacMan.State.DYING;
import static de.amr.games.pacman.actor.PacMan.State.SAFE;
import static de.amr.games.pacman.actor.PacMan.State.STEROIDS;
import static de.amr.games.pacman.actor.PacMan.State.VULNERABLE;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;

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
import de.amr.games.pacman.view.PacManGameUI;
import de.amr.statemachine.StateMachine;

public class PacMan extends MazeMover<PacMan.State> {

	private final Game game;
	private final StateMachine<State, GameEvent> brain;
	private EventManager<GameEvent> events;
	private PacManWorld world;
	private int pauseTicks;

	public PacMan(Game game) {
		super(game.maze, game.maze.pacManHome, new EnumMap<>(State.class));
		this.game = game;
		brain = buildStateMachine();
		createSprites(2 * PacManGameUI.TS);
	}

	public void setEventManager(EventManager<GameEvent> events) {
		this.events = events;
	}

	public void setWorld(PacManWorld world) {
		this.world = world;
	}

	// Pac-Man look

	private Sprite s_walking_to[] = new Sprite[4];
	private Sprite s_dying;
	private Sprite s_full;
	private Sprite s_current;

	private void createSprites(int size) {
		TOPOLOGY.dirs().forEach(dir -> s_walking_to[dir] = PacManGameUI.SPRITES.pacManWalking(dir).scale(size));
		s_dying = PacManGameUI.SPRITES.pacManDying().scale(size);
		s_full = PacManGameUI.SPRITES.pacManFull().scale(size);
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(Stream.of(s_walking_to), Stream.of(s_dying, s_full)).flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		return s_current;
	}

	// Pac-Man behavior

	public enum State {
		SAFE, VULNERABLE, STEROIDS, DYING
	};

	@Override
	public void init() {
		brain.init();
	}

	@Override
	public StateMachine<State, GameEvent> getStateMachine() {
		return brain;
	}

	private void initPacMan() {
		placeAt(homeTile);
		setNextDir(Top4.E);
		setSpeed(game::getPacManSpeed);
		getSprites().forEach(Sprite::resetAnimation);
		s_current = s_full;
		pauseTicks = 0;
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
					.timeoutAfter(() -> game.sec(0.25f))

				.state(VULNERABLE)
					.onTick(this::inspectMaze)
					
				.state(STEROIDS)
						.onTick(() -> {	inspectMaze(); checkHealth(); })
						.timeoutAfter(game::getPacManSteroidTime)

				.state(DYING)
						.onEntry(() -> s_current = s_dying)
						.timeoutAfter(() -> game.sec(2))

			.transitions()

					.when(SAFE).then(VULNERABLE).onTimeout()
					
					.when(VULNERABLE).then(DYING).on(PacManKilledEvent.class)
	
					.when(VULNERABLE).then(STEROIDS).on(PacManGainsPowerEvent.class)
	
					.when(STEROIDS).on(PacManGainsPowerEvent.class).act(() -> brain.resetTimer())
	
					.when(STEROIDS).then(VULNERABLE).onTimeout().act(() -> events.publishEvent(new PacManLostPowerEvent()))
	
					.when(DYING).onTimeout().act(e -> events.publishEvent(new PacManDiedEvent()))

		.endStateMachine();
		/* @formatter:on */
	}

	// Pac-Man activities

	@Override
	public void move() {
		super.move();
		s_current = s_walking_to[getDir()];
	}

	private void checkHealth() {
		if (brain.stateTimeExpiredPct(50)) {
			// TODO this can occur multiple times or getting missed!
			events.publishEvent(new PacManGettingWeakerEvent());
		}
	}

	private void inspectMaze() {
		if (pauseTicks > 0) {
			--pauseTicks;
			return;
		}
		move();
		if (isOutsideMaze()) {
			return;
		}

		// Ghost collision?
		Optional<Ghost> collidingGhost = world.getActiveGhosts()
		/*@formatter:off*/
				.filter(ghost -> ghost.getTile().equals(getTile()))
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
		Optional<Bonus> activeBonus = world.getBonus()
		/*@formatter:off*/
				.filter(bonus -> !bonus.isHonored())
				.filter(bonus -> bonus.getTile().equals(getTile()));
		/*@formatter:on*/
		if (activeBonus.isPresent()) {
			Bonus bonus = activeBonus.get();
			events.publishEvent(new BonusFoundEvent(bonus.getSymbol(), bonus.getValue()));
			return;
		}

		// Food?
		Tile tile = getTile();
		char content = maze.getContent(tile);
		if (content == Content.PELLET || content == Content.ENERGIZER) {
			pauseTicks = (content == Content.PELLET ? 1 : 3);
			events.publishEvent(new FoodFoundEvent(tile, content));
		}
	}
}