package de.amr.games.pacman.controller.actor;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;
import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Game.sec;

import java.util.EnumMap;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.controller.actor.steering.ghost.SteeredGhost;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * A ghost.
 * 
 * <p>
 * Ghosts have a "brain" (finite-state machine) defining the ghost's behavior (steering, look).
 * 
 * @author Armin Reichert
 */
public class Ghost extends MovingActor<GhostState> implements SteeredGhost {

	/** Number of ghost house seat (0-3). */
	public int seat;

	/** State to enter after frightening ends. */
	public GhostState followState;

	private Steering prevSteering;

	public Ghost(Game game, String name) {
		super(game.maze, name);
		steerings = new EnumMap<>(GhostState.class);
		/*@formatter:off*/
		brain = StateMachine.beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(this::toString)
			.initialState(LOCKED)
		
			.states()
	
				.state(LOCKED)
					.onEntry(() -> {
						followState = LOCKED;
						visible = true;
						moveDir = wishDir = maze.ghostHomeDir[seat];
						tf.setPosition(maze.seatPosition(seat));
						enteredNewTile();
						sprites.forEach(Sprite::resetAnimation);
						showColored();
					})
					.onTick(() -> {
						move();
						// not sure if ghost locked inside house should look frightened
						if (game.pacMan.powerTicks > 0) {
							showFrightened();
						} else {
							showColored();
						}
					})
					
				.state(LEAVING_HOUSE)
					.onEntry(() -> {
						steering().init();
					})
					.onTick(() -> {
						move();
						showColored();
					})
					.onExit(() -> forceMoving(LEFT))
				
				.state(ENTERING_HOUSE)
					.onEntry(() -> {
						tf.setPosition(maze.seatPosition(0));
						moveDir = wishDir = DOWN;
						steering().init();
					})
					.onTick(() -> {
						move();
						showEyes();
					})
				
				.state(SCATTERING)
					.onTick(() -> {
						move();
						showColored();
						checkCollision(game.pacMan);
					})
			
				.state(CHASING)
					.onTick(() -> {
						move();
						showColored();
						checkCollision(game.pacMan);
					})
				
				.state(FRIGHTENED)
					.timeoutAfter(() -> sec(game.level.pacManPowerSeconds))
					.onTick((state, t, remaining) -> {
						move();
						// not sure about exact timing
						if (remaining < sec(2)) {
							showFlashing();
						} else  {
							showFrightened();
						}
						checkCollision(game.pacMan);
					})
				
				.state(DEAD)
					.timeoutAfter(sec(1)) // time while ghost is drawn as number of scored points
					.onEntry(() -> {
						int points = Game.POINTS_GHOST[game.level.ghostsKilledByEnergizer - 1];
						sprites.select("points-" + points);
					})
					.onTick((state, t, remaining) -> {
						if (remaining == 0) { // show as eyes returning to ghost home
							move();
							showEyes();
						}
					})
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> steering().isComplete() && followState == SCATTERING)
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> steering().isComplete() && followState == CHASING)
				
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> steering().isComplete())
				
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> forceTurningBack())
				
				.when(CHASING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(CHASING).then(SCATTERING)
					.condition(() -> followState == SCATTERING)
					.act(() -> forceTurningBack())
	
				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> forceTurningBack())
				
				.when(SCATTERING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING)
					.condition(() -> followState == CHASING)
					.act(() -> forceTurningBack())
					
				.stay(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> restartTimer(FRIGHTENED))
				
				.when(FRIGHTENED).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(FRIGHTENED).then(SCATTERING)
					.onTimeout()
					.condition(() -> followState == SCATTERING)
					
				.when(FRIGHTENED).then(CHASING)
					.onTimeout()
					.condition(() -> followState == CHASING)
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(() -> maze.atGhostHouseDoor(tile()))
					
		.endStateMachine();
		/*@formatter:on*/
		brain.setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		brain.getTracer().setLogger(PacManStateMachineLogging.LOGGER);
	}

	public void takeClothes(Theme theme, int color) {
		Direction.dirs().forEach(dir -> {
			sprites.set("color-" + dir, theme.spr_ghostColored(color, dir));
			sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir));
		});
		sprites.set("frightened", theme.spr_ghostFrightened());
		sprites.set("flashing", theme.spr_ghostFlashing());
		for (int points : Game.POINTS_GHOST) {
			sprites.set("points-" + points, theme.spr_number(points));
		}
	}

	public void showColored() {
		sprites.select("color-" + moveDir);
	}

	public void showFrightened() {
		sprites.select("frightened");
	}

	public void showEyes() {
		sprites.select("eyes-" + moveDir);
	}

	public void showFlashing() {
		sprites.select("flashing");
	}

	public void showPoints(int points) {
		sprites.select("points-" + points);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze.isDoor(neighbor)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		if (maze.isUpwardsBlocked(tile) && neighbor.equals(maze.neighbor(tile, UP))) {
			return !is(CHASING, SCATTERING);
		}
		return super.canMoveBetween(tile, neighbor);
	}

	public void move() {
		Steering currentSteering = steering();
		if (prevSteering != currentSteering) {
			PacManStateMachineLogging.loginfo("%s steering changed from %s to %s", this, Steering.name(prevSteering),
					Steering.name(currentSteering));
			currentSteering.init();
			currentSteering.force();
			prevSteering = currentSteering;
		}
		currentSteering.steer();
		movement.update();
	}

	private void checkCollision(PacMan pacMan) {
		if (isTeleporting() || pacMan.isTeleporting() || pacMan.is(PacManState.DEAD)) {
			return;
		}
		if (tile().equals(pacMan.tile())) {
			publish(new PacManGhostCollisionEvent(this));
		}
	}
}