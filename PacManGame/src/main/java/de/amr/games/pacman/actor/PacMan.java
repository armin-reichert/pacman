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

	private static final int WEAK_AFTER = 66; /* percentage of power time */
	private static final int[] STEERING = { VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT };

	private final StateMachine<PacManState, PacManGameEvent> fsm;
	private int eatTimer; // ticks since last pellet was eaten

	public PacMan(PacManGame game) {
		super(game, "Pac-Man");
		fsm = buildStateMachine();
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		setSprites();
	}

	public void initPacMan() {
		eatTimer = 0;
		placeAtTile(game.maze.getPacManHome(), TS / 2, 0);
		setNextDir(Top4.E);
		sprites.forEach(Sprite::resetAnimation);
		sprites.select("s_full");
	}

	// Accessors

	public PacManGame getGame() {
		return game;
	}

	@Override
	public float getSpeed() {
		return game.getPacManSpeed(this);
	}

	public boolean isPowerEnding() {
		return hasPower() && getStateObject().getDuration()
				- getStateObject().getTicksRemaining() >= getStateObject().getDuration() * WEAK_AFTER / 100;
	}

	public boolean hasPower() {
		return getState() == POWER;
	}

	public boolean isDead() {
		return getState() == DEAD;
	}

	public int getEatTimer() {
		return eatTimer;
	}

	public void resetEatTimer() {
		eatTimer = 0;
	}

	// Movement

	@Override
	public OptionalInt supplyIntendedDir() {
		return NESW.dirs().filter(dir -> Keyboard.keyDown(STEERING[dir])).findFirst();
	}

	@Override
	public void move() {
		super.move();
		sprites.select("s_walking_" + getMoveDir());
		sprites.current().get().enableAnimation(!isStuck());
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
		NESW.dirs().forEach(dir -> sprites.set("s_walking_" + dir, game.theme.spr_pacManWalking(dir)));
		sprites.set("s_dying", game.theme.spr_pacManDying());
		sprites.set("s_full", game.theme.spr_pacManFull());
		sprites.select("s_full");
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

	public PacManState getState() {
		return fsm.getState();
	}

	public State<PacManState, PacManGameEvent> getStateObject() {
		return fsm.state();
	}

	public void processEvent(PacManGameEvent event) {
		fsm.process(event);
	}

	private StateMachine<PacManState, PacManGameEvent> buildStateMachine() {
		return StateMachine.
		/* @formatter:off */
		beginStateMachine(PacManState.class, PacManGameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(HOME)

			.states()

				.state(HOME)
					.onEntry(this::initPacMan)
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
				inspectWorld();
			}
		}

		private boolean mustDigest() {
			return digestionTicks > 0;
		}

		private void digest() {
			digestionTicks -= 1;
		}

		protected void inspectWorld() {
			if (!eventsEnabled) {
				return;
			}
			Tile tile = tilePosition();

			/*@formatter:off*/
			Optional<Ghost> collidingGhost = game.activeGhosts()
				.filter(ghost -> ghost.getState() != GhostState.DEAD)
				.filter(ghost -> ghost.getState() != GhostState.DYING)
				.filter(ghost -> ghost.getState() != GhostState.LOCKED)
				.filter(ghost -> ghost.tilePosition().equals(tile))
				.findFirst();
			/*@formatter:on*/
			if (collidingGhost.isPresent()) {
				publishEvent(new PacManGhostCollisionEvent(collidingGhost.get()));
				return;
			}

			if (tile == game.maze.getBonusTile()) {
				Optional<Bonus> activeBonus = game.getBonus().filter(bonus -> !bonus.consumed());
				if (activeBonus.isPresent()) {
					Bonus bonus = activeBonus.get();
					publishEvent(new BonusFoundEvent(bonus.symbol(), bonus.value()));
					return;
				}
			}

			if (game.maze.containsFood(tile)) {
				eatTimer = 0;
				boolean energizer = game.maze.containsEnergizer(tile);
				digestionTicks = game.getDigestionTicks(energizer);
				publishEvent(new FoodFoundEvent(tile, energizer));
			}
			else {
				eatTimer += 1;
			}
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
			if (getDuration() - getTicksRemaining() == getDuration() * WEAK_AFTER / 100) {
				publishEvent(new PacManGettingWeakerEvent());
			}
		}
	}

	private class DyingState extends State<PacManState, PacManGameEvent> {

		private int paralyzedTimer; // time before dying animation starts

		{
			// duration of complete dying state
			setTimer(() -> app().clock.sec(3));
		}

		@Override
		public void onEntry() {
			paralyzedTimer = app().clock.sec(1);
			sprites.select("s_full");
			game.theme.snd_clips_all().forEach(Sound::stop);
		}

		@Override
		public void onTick() {
			if (paralyzedTimer > 0) {
				paralyzedTimer -= 1;
				if (paralyzedTimer == 0) {
					getGame().activeGhosts().forEach(ghost -> ghost.setVisible(false));
					sprites.select("s_dying");
					game.theme.snd_die().play();
				}
			}
		}
	}

}