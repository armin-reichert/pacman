package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.PacManState.DEAD;
import static de.amr.games.pacman.actor.PacManState.DYING;
import static de.amr.games.pacman.actor.PacManState.HOME;
import static de.amr.games.pacman.actor.PacManState.HUNGRY;
import static de.amr.games.pacman.actor.PacManState.POWER;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Logger;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * The one and only.
 * 
 * @author Armin Reichert
 */
public class PacMan extends MazeMover {

	private static final int[] STEERING = { VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT };

	private final StateMachine<PacManState, PacManGameEvent> fsm;
	private int eatTimer; // ticks since last pellet was eaten

	public PacMan(PacManGame game) {
		super(game, "Pac-Man");
		fsm = buildStateMachine();
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		setSprites();
	}

	private void initialize() {
		eatTimer = 0;
		placeAtTile(game.maze.getPacManHome(), TS / 2, 0);
		moveDir = nextDir = Top4.E;
		sprites.forEach(Sprite::resetAnimation);
		sprites.select("full");
	}

	@Override
	public float maxSpeed() {
		return game.getPacManSpeed();
	}

	public int getEatTimer() {
		return eatTimer;
	}

	public void resetEatTimer() {
		eatTimer = 0;
	}

	// Movement

	/**
	 * @return if a steering key is pressed, the corresponding direction, otherwise nothing
	 */
	@Override
	public OptionalInt getNextMoveDirection() {
		return NESW.dirs().filter(dir -> Keyboard.keyDown(STEERING[dir])).findFirst();
	}

	@Override
	public boolean canEnterTile(Tile tile) {
		if (game.maze.isDoor(tile)) {
			return false;
		}
		return super.canEnterTile(tile);
	}

	// Sprites

	private void setSprites() {
		NESW.dirs().forEach(dir -> sprites.set("walking_" + dir, game.theme.spr_pacManWalking(dir)));
		sprites.set("dying", game.theme.spr_pacManDying());
		sprites.set("full", game.theme.spr_pacManFull());
		sprites.select("full");
	}

	private void updateWalkingSprite() {
		sprites.select("walking_" + moveDir);
		sprites.current().ifPresent(sprite -> sprite.enableAnimation(!isStuck()));
	}

	// State machine

	public boolean isLosingPower() {
		if (!hasPower()) {
			return false;
		}
		int total = fsm.state().getDuration(), remaining = fsm.state().getTicksRemaining();
		return remaining <= total * 33 / 100;
	}

	private boolean startsLosingPower() {
		if (!hasPower()) {
			return false;
		}
		int total = fsm.state().getDuration(), remaining = fsm.state().getTicksRemaining();
		return remaining == total * 33 / 100;
	}

	public boolean hasPower() {
		return getState() == POWER;
	}

	public boolean isDead() {
		return getState() == DEAD;
	}

	public PacManState getState() {
		return fsm.getState();
	}

	public State<PacManState, PacManGameEvent> state() {
		return fsm.state();
	}

	public void processEvent(PacManGameEvent event) {
		fsm.process(event);
	}

	@Override
	public void init() {
		initialize();
		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
	}

	private StateMachine<PacManState, PacManGameEvent> buildStateMachine() {
		return StateMachine.
		/* @formatter:off */
		beginStateMachine(PacManState.class, PacManGameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(HOME)

			.states()

				.state(HOME)
					.timeoutAfter(() -> 0)
	
				.state(HUNGRY)
					.impl(new HungryState())
					
				.state(POWER)
					.impl(new PowerState())
					.timeoutAfter(game::getPacManPowerTime)
	
				.state(DYING)
					.impl(new DyingState())

			.transitions()

				.when(HOME).then(HUNGRY).onTimeout()
				
				.when(HUNGRY).then(DYING)
					.on(PacManKilledEvent.class)
	
				.when(HUNGRY).then(POWER)
					.on(PacManGainsPowerEvent.class)
	
				.stay(POWER)
					.on(PacManGainsPowerEvent.class)
					.act(() -> fsm.resetTimer())
	
				.when(POWER).then(HUNGRY)
					.onTimeout()
					.act(() -> publishEvent(new PacManLostPowerEvent()))
	
				.when(DYING).then(DEAD)
					.onTimeout()

		.endStateMachine();
		/* @formatter:on */
	}

	private class HungryState extends State<PacManState, PacManGameEvent> {

		private int digestionTicks;

		@Override
		public void onEntry() {
			digestionTicks = 0;
		}

		@Override
		public void onTick() {
			if (mustDigest()) {
				digest();
			}
			else {
				move();
				updateWalkingSprite();
				findSomethingInteresting().ifPresent(PacMan.this::publishEvent);
			}
		}

		private boolean mustDigest() {
			return digestionTicks > 0;
		}

		private void digest() {
			digestionTicks -= 1;
		}

		private Optional<PacManGameEvent> findSomethingInteresting() {
			Tile tile = currentTile();

			Optional<Ghost> collidingGhost = game.activeGhosts()
			/*@formatter:off*/
				.filter(ghost -> ghost.getState() != GhostState.DEAD)
				.filter(ghost -> ghost.getState() != GhostState.DYING)
				.filter(ghost -> ghost.getState() != GhostState.LOCKED)
				.filter(ghost -> ghost.currentTile().equals(tile))
				.findFirst();
			/*@formatter:on*/
			if (collidingGhost.isPresent()) {
				return Optional.of(new PacManGhostCollisionEvent(collidingGhost.get()));
			}

			if (tile == game.maze.getBonusTile()) {
				Optional<Bonus> activeBonus = game.getBonus().filter(bonus -> !bonus.consumed());
				if (activeBonus.isPresent()) {
					Bonus bonus = activeBonus.get();
					return Optional.of(new BonusFoundEvent(bonus.symbol(), bonus.value()));
				}
			}

			if (game.maze.containsFood(tile)) {
				eatTimer = 0;
				boolean energizer = game.maze.containsEnergizer(tile);
				digestionTicks = game.getDigestionTicks(energizer);
				return Optional.of(new FoodFoundEvent(tile, energizer));
			}
			eatTimer += 1;

			return Optional.empty();
		}
	}

	private class PowerState extends HungryState {

		@Override
		public void onEntry() {
			game.theme.snd_waza().loop();
		}

		@Override
		public void onExit() {
			game.theme.snd_waza().stop();
		}

		@Override
		public void onTick() {
			super.onTick();
			if (startsLosingPower()) {
				publishEvent(new PacManGettingWeakerEvent());
			}
		}
	}

	private class DyingState extends State<PacManState, PacManGameEvent> {

		private int paralyzedTicks; // time before dying animation starts

		{ // set duration of complete "Dying" state
			setTimerFunction(() -> app().clock.sec(3));
		}

		@Override
		public void onEntry() {
			paralyzedTicks = app().clock.sec(1);
			sprites.select("full");
			game.theme.snd_clips_all().forEach(Sound::stop);
		}

		@Override
		public void onTick() {
			if (paralyzedTicks > 0) {
				paralyzedTicks -= 1;
				if (paralyzedTicks == 0) {
					game.activeGhosts().forEach(ghost -> ghost.setVisible(false));
					sprites.select("dying");
					game.theme.snd_die().play();
				}
			}
		}
	}
}