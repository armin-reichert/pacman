package de.amr.games.pacman.controller.creatures.ghost;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;

import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.game.Timing;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.game.GameLevel;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Guy<GhostState> {

	public GhostPersonality personality;
	public PacMan pacMan;
	public House house;
	public Bed bed;
	public GhostState nextState;
	public GhostMadness madness;
	public int bounty;
	public boolean recovering;

	public Ghost(World world, String name, GhostPersonality personality) {
		super(world, name, new EnumMap<>(GhostState.class));
		this.personality = personality;
		if (personality == GhostPersonality.SHADOW) {
			madness = new GhostMadness(this);
		}
	}

	@Override
	protected StateMachine<GhostState, PacManGameEvent> buildAI() {
		/*@formatter:off*/
		StateMachine<GhostState, PacManGameEvent> fsm = StateMachine
			.beginStateMachine(GhostState.class, PacManGameEvent.class, TransitionMatchStrategy.BY_CLASS)
				.description(name + " AI")
				.initialState(LOCKED)

			.states()

				.state(LOCKED)
					.onEntry(() -> {
						recovering = false;
						bounty = 0;
						nextState = LOCKED;
						enabled = true;
						placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
						visible = true;
						moveDir = bed.exitDir;
						wishDir = bed.exitDir;
					})
					.onTick(this::move)
	
				.state(LEAVING_HOUSE)
					.onTick(this::move)
					.onExit(() -> forceMoving(Direction.LEFT))
	
				.state(ENTERING_HOUSE)
					.onTick(this::move)
	
				.state(SCATTERING)
					.onTick(() -> {
						updateMentalHealth();
						checkPacManCollision();
						move();
					})
	
				.state(CHASING)
					.onTick(() -> {
						updateMentalHealth();
						checkPacManCollision();
						move();
					})
	
				.state(FRIGHTENED)
					.timeoutAfter(this::getFrightenedTicks)
					.onTick((state, consumed, remaining) -> {
						updateMentalHealth();
						checkPacManCollision();
						move();
						recovering = remaining < getFlashTimeTicks() * 0.5f; // one flashing takes 0.5 sec
					})
	
				.state(DEAD)
					.timeoutAfter(Timing.sec(1))
					.onEntry(this::computeBounty)
					.onTick((s, consumed, remaining) -> {
						if (remaining == 0) {
							bounty = 0;
							move();
						}
					})

			.transitions()

				.when(LOCKED).then(LEAVING_HOUSE).on(GhostUnlockedEvent.class)
	
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> hasLeftHouse() && nextState == SCATTERING)
					.annotation("Outside house")
	
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> hasLeftHouse() && nextState == CHASING)
					.annotation("Outside house")
	
				.when(LEAVING_HOUSE).then(FRIGHTENED)
					.condition(() -> hasLeftHouse() && nextState == FRIGHTENED)
					.annotation("Outside house")
	
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> steering().isComplete())
					.annotation("Reached bed")
	
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(this::reverseDirection)
	
				.when(CHASING).then(DEAD)
					.on(GhostKilledEvent.class)
	
				.when(CHASING).then(SCATTERING)
					.condition(() -> nextState == SCATTERING)
					.act(this::reverseDirection)
					.annotation("Got scattering command")
	
				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(this::reverseDirection)
	
				.when(SCATTERING).then(DEAD)
					.on(GhostKilledEvent.class)
	
				.when(SCATTERING).then(CHASING)
					.condition(() -> nextState == CHASING)
					.act(this::reverseDirection)
					.annotation("Got chasing command")
	
				.stay(FRIGHTENED).on(PacManGainsPowerEvent.class)
					.act(() -> ai.resetTimer(FRIGHTENED))
	
				.when(FRIGHTENED).then(DEAD)
					.on(GhostKilledEvent.class)
	
				.when(FRIGHTENED).then(SCATTERING)
					.onTimeout()
					.condition(() -> nextState == SCATTERING)
	
				.when(FRIGHTENED).then(CHASING)
					.onTimeout()
					.condition(() -> nextState == CHASING)
	
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(this::isAtHouseEntry)
					.annotation("Reached house entry")

			.endStateMachine();
		/*@formatter:on*/
		fsm.setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		return fsm;
	}

	@Override
	public void update() {
		ai.update();
	}

	@Override
	public float getSpeed() {
		if (ai.getState() == null) {
			throw new IllegalStateException(String.format("Ghost %s is not initialized.", name));
		}
		if (game == null) {
			return 0;
		}
		GameLevel level = game.level;
		boolean tunnel = world.isTunnel(tile());
		switch (ai.getState()) {
		case LOCKED:
			return Timing.speed(isInsideHouse() ? level.ghostSpeed / 2 : 0);
		case LEAVING_HOUSE:
			return Timing.speed(level.ghostSpeed / 2);
		case ENTERING_HOUSE:
			return Timing.speed(level.ghostSpeed);
		case CHASING:
		case SCATTERING:
			if (tunnel) {
				return Timing.speed(level.ghostTunnelSpeed);
			}
			GhostMentalState mentalState = getMentalState();
			if (mentalState == GhostMentalState.ELROY1) {
				return Timing.speed(level.elroy1Speed);
			}
			if (mentalState == GhostMentalState.ELROY2) {
				return Timing.speed(level.elroy2Speed);
			}
			return Timing.speed(level.ghostSpeed);
		case FRIGHTENED:
			return Timing.speed(tunnel ? level.ghostTunnelSpeed : level.ghostFrightenedSpeed);
		case DEAD:
			return Timing.speed(2 * level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s", ai.getState()));
		}
	}

	@Override
	public Stream<StateMachine<?, ?>> machines() {
		return madness != null ? Stream.of(ai, movement, madness) : Stream.of(ai, movement);
	}

	public GhostMentalState getMentalState() {
		return madness != null ? madness.getState() : GhostMentalState.HEALTHY;
	}

	private void computeBounty() {
		bounty = game != null ? game.level.killedGhostPoints() : 0;
	}

	private long getFrightenedTicks() {
		return game != null ? Timing.sec(game.level.pacManPowerSeconds) : Timing.sec(5);
	}

	private long getFlashTimeTicks() {
		return game != null ? game.level.numFlashes * Timing.sec(0.5f) : 0;
	}

	private void checkPacManCollision() {
		if (visible && pacMan.visible && tile().equals(pacMan.tile())
				&& !pacMan.ai.is(PacManState.DEAD, PacManState.COLLAPSING)) {
			ai.publish(new PacManGhostCollisionEvent(this));
		}
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (house.hasDoorAt(neighbor)) {
			return ai.is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		if (ai.is(CHASING, SCATTERING)) {
			Optional<OneWayTile> oneWay = world.oneWayTiles().filter(oneWayTile -> oneWayTile.tile.equals(neighbor))
					.findFirst();
			if (oneWay.isPresent() && tile.dirTo(neighbor).get().equals(oneWay.get().dir.opposite())) {
				return false;
			}
		}
		return world.isAccessible(neighbor);
	}

	public void move() {
		Steering currentSteering = steering();
		if (!world.isTunnel(tile())) {
			currentSteering.steer(this);
		}
		movement.update();
		enabled = tf.vx != 0 || tf.vy != 0;
	}

	private void updateMentalHealth() {
		if (madness != null) {
			madness.update();
		}
	}

	public boolean hasLeftHouse() {
		Tile location = tile();
		return ai.is(LEAVING_HOUSE) && house.isEntry(location) && tf.y == location.row * Tile.SIZE;
	}

	public boolean isAtHouseEntry() {
		return house.isEntry(tile()) && (tileOffsetX() - Tile.SIZE / 2) <= 1;
	}

	public boolean isInsideHouse() {
		return house.isInsideOrDoor(tile());
	}
}