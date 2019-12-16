package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.DYING;
import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.actor.behavior.Steerings.headingForTargetTile;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.model.Timing.speed;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.AbstractMazeMover;
import de.amr.games.pacman.actor.fsm.FsmComponent;
import de.amr.games.pacman.actor.fsm.FsmContainer;
import de.amr.games.pacman.actor.fsm.FsmControlled;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateMachine.MissingTransitionBehavior;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends AbstractMazeMover implements FsmContainer<GhostState> {

	private final Map<GhostState, Steering<Ghost>> steeringByState = new EnumMap<>(GhostState.class);
	private final Steering<Ghost> defaultSteering = headingForTargetTile();

	public final PacManGameCast cast;
	public final PacManGame game;
	public final FsmComponent<GhostState> fsmComponent;
	public Direction initialDir;
	public int ghostHousePlace;
	public Tile scatterTile;
	public GhostState nextState;
	public Supplier<Tile> fnChasingTarget;

	public int dotCounter; // used by logic when ghost can leave house

	public Ghost(String name, PacManGameCast cast) {
		this.cast = cast;
		this.game = cast.game;
		fsmComponent = buildFsmComponent(name);
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
	}

	private FsmComponent<GhostState> buildFsmComponent(String name) {
		StateMachine<GhostState, PacManGameEvent> fsm = buildStateMachine(name);
		fsm.setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		fsm.traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
		return new FsmComponent<>(name, fsm);
	}

	private StateMachine<GhostState, PacManGameEvent> buildStateMachine(String name) {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(String.format("[%s]", name))
			.initialState(LOCKED)
		
			.states()
	
				.state(LOCKED)
					.onTick(() -> walkAndDisplayAs("color-" + moveDir()))
					.onExit(() -> {
						enteredNewTile = true;
						cast.pacMan.ticksSinceLastMeal = 0;
					})
					
				.state(LEAVING_HOUSE)
					.onTick(() -> walkAndDisplayAs("color-" + moveDir()))
					.onExit(() -> setNextDir(LEFT))
				
				.state(ENTERING_HOUSE)
					.onTick(() -> walkAndDisplayAs("eyes-" + moveDir()))
				
				.state(SCATTERING)
					.onEntry(() -> setTargetTile(scatterTile))
					.onTick(() -> walkAndDisplayAs("color-" + moveDir()))
			
				.state(CHASING)
					.onEntry(() -> turnChasingGhostSoundOn())
					.onTick(() -> walkAndDisplayAs("color-" + moveDir()))
					.onExit(() -> turnChasingGhostSoundOff())
				
				.state(FRIGHTENED)
					.onTick(() -> walkAndDisplayAs(cast.pacMan.isLosingPower() ? "flashing" : "frightened"))
				
				.state(DYING)
					.timeoutAfter(Ghost::getDyingTime)
					.onEntry(() -> sprites.select("value-" + game.level.ghostsKilledByEnergizer))
				
				.state(DEAD)
					.onEntry(() -> {
						setTargetTile(maze().ghostHome[0]);
						turnDeadGhostSoundOn();
					})
					.onTick(() -> walkAndDisplayAs("eyes-" + moveDir()))
					.onExit(() -> turnDeadGhostSoundOff())
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> leftHouse() && nextState == SCATTERING)
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> leftHouse() && nextState == CHASING)
					
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> nextDir() == null)
				
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(this::turnAround)
				
				.when(CHASING).then(DYING)
					.on(GhostKilledEvent.class)
				
				.when(CHASING).then(SCATTERING)
					.on(StartScatteringEvent.class)
					.act(this::turnAround)
	
				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(this::turnAround)
				
				.when(SCATTERING).then(DYING)
					.on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING)
					.on(StartChasingEvent.class)
					.act(this::turnAround)
				
				.when(FRIGHTENED).then(CHASING)
					.on(PacManLostPowerEvent.class)
					.condition(() -> nextState == CHASING)
	
				.when(FRIGHTENED).then(SCATTERING)
					.on(PacManLostPowerEvent.class)
					.condition(() -> nextState == SCATTERING)
				
				.when(FRIGHTENED).then(DYING)
					.on(GhostKilledEvent.class)
					
				.when(DYING).then(DEAD)
					.onTimeout()
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(() -> tile().equals(maze().ghostHome[0]))
					.act(() -> placeAtTile(maze().ghostHome[0], Tile.SIZE / 2, 0))
				
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public Maze maze() {
		return cast.game.maze;
	}

	@Override
	public FsmControlled<GhostState> fsmComponent() {
		return fsmComponent;
	}

	@Override
	public void init() {
		super.init();
		fsmComponent.init();
		visible = true;
		setMoveDir(initialDir);
		setNextDir(initialDir);
		enteredNewTile = true;
		placeAtTile(maze().ghostHome[ghostHousePlace], Tile.SIZE / 2, 0);
		sprites.select("color-" + initialDir);
		sprites.forEach(Sprite::resetAnimation);
		nextState = fsmComponent.getState();
	}

	@Override
	public void update() {
		super.update();
		fsmComponent.update();
	}

	public void setSteering(GhostState state, Steering<Ghost> steering) {
		steeringByState.put(state, steering);
		steering.triggerSteering(this);
	}

	public Steering<Ghost> getSteering() {
		return steeringByState.getOrDefault(fsmComponent.getState(), defaultSteering);
	}

	@Override
	public void steer() {
		getSteering().steer(this);
	}

	@Override
	public Tile targetTile() {
		return is(CHASING) ? fnChasingTarget.get() : super.targetTile();
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (neighbor.isDoor()) {
			return is(ENTERING_HOUSE) || is(LEAVING_HOUSE);
		}
		if (maze().isNoUpIntersection(tile) && neighbor == maze().tileToDir(tile, UP)) {
			return !is(CHASING) && !is(SCATTERING);
		}
		return super.canMoveBetween(tile, neighbor);
	}

	@Override
	/* TODO: Some values are still guessed */
	public float maxSpeed() {
		boolean inTunnel = tile().isTunnel();
		boolean outsideHouse = !maze().inGhostHouse(tile());
		switch (getState()) {
		case LOCKED:
			return outsideHouse ? 0 : speed(game.level.ghostSpeed) / 2;
		case LEAVING_HOUSE:
			//$FALL-THROUGH$
		case ENTERING_HOUSE:
			return speed(game.level.ghostSpeed) / 2;
		case CHASING:
			//$FALL-THROUGH$
		case SCATTERING:
			return inTunnel ? speed(game.level.ghostTunnelSpeed) : speed(game.level.ghostSpeed);
		case FRIGHTENED:
			return inTunnel ? speed(game.level.ghostTunnelSpeed) : speed(game.level.ghostFrightenedSpeed);
		case DYING:
			return 0;
		case DEAD:
			return 2 * speed(game.level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s for %s", getState(), fsmComponent.name));
		}
	}

	@Override
	protected boolean snapToGrid() {
		// when entering or leaving the ghost house, pixel-exact movement is needed
		return !is(ENTERING_HOUSE) && !is(LEAVING_HOUSE);
	}

	private void walkAndDisplayAs(String spriteKey) {
		steer();
		if (nextDir() != null) {
			walkMaze();
		}
		sprites.select(spriteKey);
	}

	private boolean leftHouse() {
		Tile currentTile = tile();
		return !maze().partOfGhostHouse(currentTile) && tf.getY() - currentTile.row * Tile.SIZE == 0;
	}

	public static int getDyingTime() {
		return sec(1);
	}

	public void turnChasingGhostSoundOn() {
		if (!cast.theme.snd_ghost_chase().isRunning()) {
			cast.theme.snd_ghost_chase().loop();
		}
	}

	public void turnChasingGhostSoundOff() {
		// if caller is the last chasing ghost, turn sound off
		if (cast.ghostsOnStage().filter(ghost -> this != ghost).noneMatch(ghost -> ghost.is(CHASING))) {
			cast.theme.snd_ghost_chase().stop();
		}
	}

	public void turnDeadGhostSoundOn() {
		if (!cast.theme.snd_ghost_dead().isRunning()) {
			cast.theme.snd_ghost_dead().loop();
		}
	}

	public void turnDeadGhostSoundOff() {
		// if caller is the last dead ghost, turn sound off
		if (cast.ghostsOnStage().filter(ghost -> this != ghost).noneMatch(ghost -> ghost.is(DEAD))) {
			cast.theme.snd_ghost_dead().stop();
		}
	}
}