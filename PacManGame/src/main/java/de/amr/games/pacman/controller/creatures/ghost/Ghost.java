package de.amr.games.pacman.controller.creatures.ghost;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.game.Timing.sec;
import static de.amr.games.pacman.controller.game.Timing.speed;
import static de.amr.games.pacman.model.game.PacManGame.game;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.MovementType;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Guy<GhostState> {

	public static Ghost shadowGhost(TiledWorld world, String name, PacMan pacMan) {
		return new Ghost(world, name, GhostPersonality.SHADOW, pacMan);
	}

	public static Ghost speedyGhost(TiledWorld world, String name, PacMan pacMan) {
		return new Ghost(world, name, GhostPersonality.SPEEDY, pacMan);
	}

	public static Ghost bashfulGhost(TiledWorld world, String name, PacMan pacMan) {
		return new Ghost(world, name, GhostPersonality.BASHFUL, pacMan);
	}

	public static Ghost pokeyGhost(TiledWorld world, String name, PacMan pacMan) {
		return new Ghost(world, name, GhostPersonality.POKEY, pacMan);
	}

	public final StateMachine<GhostState, PacManGameEvent> ai;
	public final GhostMadness madness;
	public final GhostPersonality personality;
	public GhostState nextState;
	public House house;
	public Bed bed;
	public int bounty;
	public boolean recovering;

	private final Map<GhostState, Steering> behaviors;
	private Steering previousSteering;

	private Ghost(TiledWorld world, String name, GhostPersonality personality, PacMan pacMan) {
		super(world, name);
		this.personality = personality;
		behaviors = new EnumMap<>(GhostState.class);
		ai = new StateMachine<>(GhostState.class);
		buildGhostAI(pacMan);
		madness = personality == GhostPersonality.SHADOW ? new GhostMadness(this, pacMan) : null;
		tf.width = tf.height = Tile.SIZE;
	}

	private void buildGhostAI(PacMan pacMan) {
		/*@formatter:off*/
		ai.beginStateMachine()
			.description(name + " AI")
			.initialState(LOCKED)

			.states()

				.state(LOCKED)
					.onEntry(() -> {
						visible = true;
						recovering = false;
						bounty = 0;
						nextState = LOCKED;
						placeIntoBed();
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
						checkPacManCollision(pacMan);
						move();
					})
	
				.state(CHASING)
					.onTick(() -> {
						updateMentalHealth();
						checkPacManCollision(pacMan);
						move();
					})
	
				.state(FRIGHTENED)
					.timeoutAfter(this::getFrightenedTicks)
					.onTick((state, consumed, remaining) -> {
						updateMentalHealth();
						checkPacManCollision(pacMan);
						move();
						recovering = remaining < getFlashTimeTicks();
					})
	
				.state(DEAD)
					.timeoutAfter(sec(1))
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
					.condition(() -> justLeftHouse() && nextState == SCATTERING)
					.annotation("Outside house")
	
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> justLeftHouse() && nextState == CHASING)
					.annotation("Outside house")
	
				.when(LEAVING_HOUSE).then(FRIGHTENED)
					.condition(() -> justLeftHouse() && nextState == FRIGHTENED)
					.annotation("Outside house")
	
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> getSteering().isComplete())
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
		ai.setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
	}

	private void placeIntoBed() {
		if (bed != null) {
			placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
			moveDir = bed.exitDir;
			wishDir = bed.exitDir;
		}
	}

	@Override
	public void init() {
		previousSteering = null;
		movement.init();
		ai.init();
	}

	@Override
	public void update() {
		ai.update();
	}

	@Override
	public Steering getSteering() {
		Steering currentSteering = behaviors.getOrDefault(ai.getState(), Steering.STANDING_STILL);
		if (previousSteering != currentSteering) {
			currentSteering.init();
			currentSteering.force();
			previousSteering = currentSteering;
		}
		return currentSteering;
	}

	@Override
	public void setSteering(GhostState state, Steering steering) {
		behaviors.put(state, steering);
	}

	@Override
	public float getSpeed() {
		if (ai.getState() == null) {
			throw new IllegalStateException(String.format("Ghost %s is not initialized.", name));
		}
		if (!PacManGame.started()) {
			return 0;
		}
		Tile tile = tile();
		boolean tunnel = world.isTunnel(tile) || world.isPortal(tile);
		switch (ai.getState()) {
		case LOCKED:
			return speed(isInsideHouse() ? game.ghostSpeed / 2 : 0);
		case LEAVING_HOUSE:
			return speed(game.ghostSpeed / 2);
		case ENTERING_HOUSE:
			return speed(game.ghostSpeed);
		case CHASING:
		case SCATTERING:
			if (tunnel) {
				return speed(game.ghostTunnelSpeed);
			}
			GhostMentalState mentalState = getMentalState();
			if (mentalState == GhostMentalState.ELROY1) {
				return speed(game.elroy1Speed);
			}
			if (mentalState == GhostMentalState.ELROY2) {
				return speed(game.elroy2Speed);
			}
			return speed(game.ghostSpeed);
		case FRIGHTENED:
			return speed(tunnel ? game.ghostTunnelSpeed : game.ghostFrightenedSpeed);
		case DEAD:
			return speed(2 * game.ghostSpeed);
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
		bounty = PacManGame.started() ? game.ghostBounty() : 0;
	}

	private long getFrightenedTicks() {
		return PacManGame.started() ? sec(game.pacManPowerSeconds) : sec(5);
	}

	private long getFlashTimeTicks() {
		// assuming one flashing takes 0.5 seconds
		return PacManGame.started() ? game.numFlashes * sec(0.5f) : 0;
	}

	private void checkPacManCollision(PacMan pacMan) {
		if (!visible || !pacMan.visible) {
			return;
		}
		if (!tile().equals(pacMan.tile())) {
			return;
		}
		if (!ai.is(CHASING, SCATTERING, FRIGHTENED)) {
			return;
		}
		if (!pacMan.ai.is(PacManState.AWAKE, PacManState.POWERFUL)) {
			return;
		}
		if (movement.is(MovementType.INSIDE_PORTAL)) {
			return;
		}
		ai.publish(new PacManGhostCollisionEvent(this));
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (house.hasDoorAt(neighbor)) {
			return ai.is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		if (ai.is(CHASING, SCATTERING)) {
			OneWayTile oneWayNeighbor = world.oneWayTiles().filter(oneWay -> oneWay.tile.equals(neighbor)).findFirst()
					.orElse(null);
			if (oneWayNeighbor != null && tile.dirTo(neighbor).get().equals(oneWayNeighbor.dir.opposite())) {
				return false;
			}
		}
		return world.isAccessible(neighbor);
	}

	private void updateMentalHealth() {
		if (madness != null) {
			madness.update();
		}
	}

	public boolean justLeftHouse() {
		if (ai.is(LEAVING_HOUSE)) {
			Tile location = tile();
			return house.isEntry(location) && tf.y == location.row * Tile.SIZE;
		}
		return false;
	}

	public boolean isAtHouseEntry() {
		return house.isEntry(tile()) && (tileOffsetX() - Tile.SIZE / 2) <= 1;
	}

	public boolean isInsideHouse() {
		return house.includes(tile());
	}
}