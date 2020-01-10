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
import static de.amr.games.pacman.model.Direction.dirs;
import static de.amr.games.pacman.model.Game.POINTS_GHOST;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.model.Timing.speed;

import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
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
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.Theme;
import de.amr.statemachine.api.Fsm;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends AbstractMazeMover implements SteerableGhost, Actor<GhostState> {

	private final Cast cast;
	private final int seat;
	private final Direction eyes;
	private final SpriteMap sprites = new SpriteMap();
	private final Fsm<GhostState, PacManGameEvent> brain;
	private final Map<GhostState, Steering> steerings = new EnumMap<>(GhostState.class);
	private final Steering defaultSteering = isHeadingFor(this::targetTile);
	private GhostState afterFrightenedState;
	private Steering prevSteering;

	public Ghost(Cast cast, String name, int seat, Direction eyes) {
		super(name);
		this.cast = cast;
		this.seat = seat;
		this.eyes = eyes;
		brain = buildFsm();
		brain.setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		brain.setLogger(Game.FSM_LOGGER);
	}

	@Override
	public Fsm<GhostState, PacManGameEvent> fsm() {
		return brain;
	}

	@Override
	public Entity entity() {
		return this;
	}

	@Override
	public Theme theme() {
		return cast.theme();
	}

	private Game game() {
		return cast.game();
	}

	@Override
	public String toString() {
		return String.format("(%s, col:%d, row:%d, %s)", name(), tile().col, tile().row, getState());
	}

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
						afterFrightenedState = getState();
						placeHalfRightOf(maze().ghostHouseSeats[seat]);
						enteredNewTile();
						setMoveDir(eyes);
						setWishDir(eyes);
						sprites.select("color-" + moveDir());
						sprites.forEach(Sprite::resetAnimation);
					})
					.onTick(() -> {
						if (cast.pacMan.isKicking()) {
							step(cast.pacMan.isTired() ? "flashing" : "frightened");
						} else {
							step("color-" + moveDir());
						}
					})
					
				.state(LEAVING_HOUSE)
					.onEntry(() -> steering().init())
					.onTick(() -> {
						if (cast.pacMan.isKicking()) {
							step(cast.pacMan.isTired() ? "flashing" : "frightened");
						} else {
							step("color-" + moveDir());
						}
					})
				
				.state(ENTERING_HOUSE)
					.onEntry(() -> steering().init())
					.onTick(() -> step("eyes-" + moveDir()))
				
				.state(SCATTERING)
					.onTick(() -> {
						step("color-" + moveDir());
						checkPacManCollision();
					})
			
				.state(CHASING)
					.onTick(() -> {
						step("color-" + moveDir());
						checkPacManCollision();
					})
				
				.state(FRIGHTENED)
					.onTick(() -> {
						if (!cast.pacMan.isKicking()) {
							resumeState(afterFrightenedState);
							return;
						}
						step(cast.pacMan.isTired() ? "flashing" : "frightened");
						checkPacManCollision();
					})
				
				.state(DEAD)
					.timeoutAfter(sec(1)) // "dying" time
					.onEntry(() -> {
						int points = POINTS_GHOST[game().level().ghostsKilledByEnergizer - 1];
						sprites.select("number-" + points);
						setTargetTile(maze().ghostHouseSeats[0]);
					})
					.onTick(() -> {
						if (state().isTerminated()) { // "dead"
							step("eyes-" + moveDir());
						}
					})
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> steering().isComplete() && afterFrightenedState == SCATTERING)
					.act(() -> forceMove(Direction.LEFT))
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> steering().isComplete() && afterFrightenedState == CHASING)
					.act(() -> forceMove(Direction.LEFT))
					
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> steering().isComplete())
				
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> forceMove(moveDir().opposite()))
				
				.when(CHASING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(CHASING).then(SCATTERING)
					.condition(() -> afterFrightenedState == SCATTERING)
					.act(() -> forceMove(moveDir().opposite()))
	
				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> forceMove(moveDir().opposite()))
				
				.when(SCATTERING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING)
					.condition(() -> afterFrightenedState == CHASING)
					.act(() -> forceMove(moveDir().opposite()))
				
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

	@Override
	public Maze maze() {
		return cast.game().maze();
	}

	public int seat() {
		return seat;
	}

	public void setAfterFrightenedState(GhostState state) {
		this.afterFrightenedState = state;
	}

	public GhostState afterFrightenedState() {
		return afterFrightenedState;
	}

	@Override
	public void init() {
		super.init();
		brain.init();
	}

	@Override
	public void update() {
		brain.update();
	}

	@Override
	public void draw(Graphics2D g) {
		if (visible()) {
			sprites.current().ifPresent(sprite -> {
				Vector2f center = tf.getCenter();
				float x = center.x - sprite.getWidth() / 2;
				float y = center.y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	public void moveOneStep() {
		if (prevSteering != steering()) {
			steering().init();
			steering().force();
			LOGGER.info(String.format("%s: steering changed, was: %s now: %s", this, name(prevSteering), name(steering())));
		}
		steering().steer();
		movement.update();
		prevSteering = steering();
	}

	private void step(String spriteKey) {
		moveOneStep();
		sprites.select(spriteKey);
	}

	public void during(GhostState state, Steering steering) {
		steerings.put(state, steering);
	}

	public Steering steering(GhostState state) {
		return steerings.getOrDefault(state, defaultSteering);
	}

	@Override
	public Steering steering() {
		return steering(getState());
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
			return speed(game().level().ghostSpeed) / 2;
		case ENTERING_HOUSE:
			return speed(game().level().ghostSpeed);
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

	private String name(Steering steering) {
		return steering != null ? steering.getClass().getSimpleName() : "none";
	}

	private void checkPacManCollision() {
		if (!isTeleporting() && !cast.pacMan.isTeleporting() && cast.pacMan.is(PacManState.ALIVE)
				&& tile().equals(cast.pacMan.tile())) {
			publish(new PacManGhostCollisionEvent(this, tile()));
		}
	}

	public void dress(GhostColor color) {
		dirs().forEach(dir -> {
			sprites.set("color-" + dir, theme().spr_ghostColored(color, dir.ordinal()));
			sprites.set("eyes-" + dir, theme().spr_ghostEyes(dir.ordinal()));
		});
		for (int number : new int[] { 200, 400, 800, 1600 }) {
			sprites.set("number-" + number, theme().spr_number(number));
		}
		sprites.set("frightened", theme().spr_ghostFrightened());
		sprites.set("flashing", theme().spr_ghostFlashing());
	}

	public void enableAnimations(boolean b) {
		for (Sprite sprite : sprites) {
			sprite.enableAnimation(b);
		}
	}
}