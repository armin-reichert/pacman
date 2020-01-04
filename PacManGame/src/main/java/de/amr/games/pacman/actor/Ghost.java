package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Direction.UP;
import static de.amr.games.pacman.model.Game.POINTS_GHOST;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.model.Timing.speed;

import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.actor.core.AbstractMazeMover;
import de.amr.games.pacman.actor.core.Actor;
import de.amr.games.pacman.actor.steering.core.Steering;
import de.amr.games.pacman.actor.steering.ghost.SteerableGhost;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.client.FsmComponent;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends AbstractMazeMover implements SteerableGhost, Actor<GhostState> {

	public final SpriteMap sprites = new SpriteMap();
	public Direction eyes;
	private GhostState nextState;
	private final Cast cast;
	private final int seat;
	private final FsmComponent<GhostState, PacManGameEvent> brain;
	private final Map<GhostState, Steering> steerings = new EnumMap<>(GhostState.class);
	private final Steering defaultSteering = isHeadingFor(this::targetTile);

	public Ghost(String name, Cast cast, int seat) {
		super(name);
		this.cast = cast;
		this.seat = seat;
		brain = buildBrain();
		brain.fsm().setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		brain.fsm().setLogger(Game.FSM_LOGGER);
	}

	@Override
	public StateMachine<GhostState, PacManGameEvent> buildFsm() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(GhostState.class, PacManGameEvent.class)
			 
			.description(String.format("[%s]", name()))
			.initialState(LOCKED)
		
			.states()
	
				.state(LOCKED)
					.onEntry(() -> {
						visible = true;
						nextState = getState();
						placeHalfRightOf(myHomeTile());
						enteredNewTile();
						setMoveDir(eyes);
						setWishDir(eyes);
						sprites.select("color-" + moveDir());
						sprites.forEach(Sprite::resetAnimation);
					})
					.onTick(() -> makeStepAndDisplayAs("color-" + moveDir()))
					
				.state(LEAVING_HOUSE)
					.onTick(() -> makeStepAndDisplayAs("color-" + moveDir()))
				
				.state(ENTERING_HOUSE)
					.onTick(() -> makeStepAndDisplayAs("eyes-" + moveDir()))
				
				.state(SCATTERING)
					.onTick(() -> {
						makeStepAndDisplayAs("color-" + moveDir());
						checkPacManCollision();
					})
			
				.state(CHASING)
					.onEntry(() -> turnChasingGhostSoundOn())
					.onTick(() -> {
						makeStepAndDisplayAs("color-" + moveDir());
						checkPacManCollision();
					})
					.onExit(() -> turnChasingGhostSoundOff())
				
				.state(FRIGHTENED)
					.onTick(() -> {
						makeStepAndDisplayAs(cast.pacMan.isTired() ? "flashing" : "frightened");
						checkPacManCollision();
					})
				
				.state(DEAD)
					.timeoutAfter(sec(1)) // "dying" time
					.onEntry(() -> {
						int points = POINTS_GHOST[game().level().ghostsKilledByEnergizer - 1];
						sprites.select("number-" + points);
						setTargetTile(maze().ghostHouseSeats[0]);
						turnDeadGhostSoundOn();
					})
					.onTick(() -> {
						if (state().isTerminated()) { // "dead"
							makeStepAndDisplayAs("eyes-" + moveDir());
						}
					})
					.onExit(() -> {
						turnDeadGhostSoundOff();
					})
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> hasLeftTheHouse() && nextState == SCATTERING)
					.act(() -> {setWishDir(Direction.LEFT); steering().trigger(); step();})
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> hasLeftTheHouse() && nextState == CHASING)
					.act(() -> {setWishDir(Direction.LEFT); steering().trigger(); step();})
					
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> wishDir() == null)
				
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(this::turnAround)
				
				.when(CHASING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(CHASING).then(SCATTERING)
					.condition(() -> nextState == SCATTERING)
					.act(this::turnAround)
	
				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(this::turnAround)
				
				.when(SCATTERING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING)
					.condition(() -> nextState == CHASING)
					.act(this::turnAround)
				
				.when(FRIGHTENED).then(CHASING)
					.condition(() -> !cast.pacMan.isKicking() && nextState == CHASING)
	
				.when(FRIGHTENED).then(SCATTERING)
					.condition(() -> !cast.pacMan.isKicking() && nextState == SCATTERING)
				
				.when(FRIGHTENED).then(DEAD)
					.on(GhostKilledEvent.class)
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(() -> maze().inFrontOfGhostHouseDoor(tile()))
					.act(() -> {
						placeHalfRightOf(maze().ghostHouseSeats[0]);
						setWishDir(Direction.DOWN);
					})
				
		.endStateMachine();
		/*@formatter:on*/
	}

	private Tile myHomeTile() {
		return maze().ghostHouseSeats[seat];
	}

	@Override
	public Cast cast() {
		return cast;
	}

	public int seat() {
		return seat;
	}

	@Override
	public FsmComponent<GhostState, PacManGameEvent> fsmComponent() {
		return brain;
	}
	
	public void setNextState(GhostState nextState) {
		this.nextState = nextState;
	}
	
	public GhostState nextState() {
		return nextState;
	}

	@Override
	public void init() {
		super.init();
		brain.init();
	}

	@Override
	public void update() {
		GhostState stateBefore = getState();
		brain.update();
		if (getState() != stateBefore) {
			LOGGER.info(String.format("%s state changed from %s to %s", name(), stateBefore, getState()));
		}
	}

	@Override
	public void draw(Graphics2D g) {
		if (visible()) {
			sprites.current().ifPresent(sprite -> {
				float x = tf.getCenter().x - sprite.getWidth() / 2;
				float y = tf.getCenter().y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	public void during(GhostState state, Steering steering) {
		steerings.put(state, steering);
	}

	@Override
	public Steering steering() {
		return steeringForState(getState());
	}

	public Steering steeringForState(GhostState state) {
		return steerings.getOrDefault(state, defaultSteering);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (maze().isDoor(neighbor)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		if (maze().isNoUpIntersection(tile) && neighbor == maze().tileToDir(tile, UP)) {
			return !is(CHASING, SCATTERING);
		}
		return super.canMoveBetween(tile, neighbor);
	}

	@Override
	public float maxSpeed() {
		// TODO: Some values are still guessed
		boolean inTunnel = tile().isTunnel();
		boolean outsideHouse = !maze().inGhostHouse(tile());
		switch (getState()) {
		case LOCKED:
			return outsideHouse ? 0 : speed(game().level().ghostSpeed) / 2;
		case LEAVING_HOUSE:
			//$FALL-THROUGH$
		case ENTERING_HOUSE:
			return speed(game().level().ghostSpeed) / 2;
		case CHASING:
			//$FALL-THROUGH$
		case SCATTERING:
			return inTunnel ? speed(game().level().ghostTunnelSpeed) : speed(game().level().ghostSpeed);
		case FRIGHTENED:
			return inTunnel ? speed(game().level().ghostTunnelSpeed) : speed(game().level().ghostFrightenedSpeed);
		case DEAD:
			return 2 * speed(game().level().ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s for %s", getState(), name()));
		}
	}

	private Steering prevSteering;

	private String name(Steering steering) {
		return steering != null ? steering.getClass().getSimpleName() : "none";
	}

	private void makeStepAndDisplayAs(String spriteKey) {
		if (prevSteering != steering()) {
			LOGGER.info(String.format("%s steering (%s @ %s) changed from %s to %s", name(), getState(), tile(),
					name(prevSteering), name(steering())));
			steering().trigger();
		}
		steering().steer();
		step();
		sprites.select(spriteKey);
		prevSteering = steering();
	}

	private void checkPacManCollision() {
		if (!isTeleporting() && !cast.pacMan.isTeleporting() && cast.pacMan.is(PacManState.ALIVE)
				&& tile().equals(cast.pacMan.tile())) {
			publish(new PacManGhostCollisionEvent(this, tile()));
		}
	}

	private boolean hasLeftTheHouse() {
		Tile currentTile = tile();
		return !maze().partOfGhostHouse(currentTile) && tf.getPosition().roundedY() == currentTile.y();
	}

	// TODO move sound methods into some central handler

	public void turnChasingGhostSoundOn() {
		if (!cast.theme().snd_ghost_chase().isRunning()) {
			cast.theme().snd_ghost_chase().loop();
		}
	}

	public void turnChasingGhostSoundOff() {
		// if caller is the last chasing ghost, turn sound off
		if (cast.ghostsOnStage().filter(ghost -> this != ghost).noneMatch(ghost -> ghost.is(CHASING))) {
			cast.theme().snd_ghost_chase().stop();
		}
	}

	public void turnDeadGhostSoundOn() {
		if (!cast.theme().snd_ghost_dead().isRunning()) {
			cast.theme().snd_ghost_dead().loop();
		}
	}

	public void turnDeadGhostSoundOff() {
		// if caller is the last dead ghost, turn sound off
		if (cast.ghostsOnStage().filter(ghost -> this != ghost).noneMatch(ghost -> ghost.is(DEAD))) {
			cast.theme().snd_ghost_dead().stop();
		}
	}
}