package de.amr.games.pacman.controller.creatures.ghost;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.game.GameController.speed;
import static de.amr.games.pacman.model.game.Game.sec;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.Creature;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.game.GameLevel;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature<GhostState> {

	private GhostPersonality personality;
	private PacMan pacMan;
	private House house;
	private Bed bed;
	private GhostState nextState;
	private GhostMadnessController madnessController;
	private Steering previousSteering;
	private int bounty;
	private boolean flashing;

	public Ghost(String name, GhostPersonality personality, World world) {
		super(GhostState.class, name, world);
		this.personality = personality;
		if (personality == GhostPersonality.SHADOW) {
			madnessController = new GhostMadnessController(this);
		}
	}

	@Override
	protected StateMachine<GhostState, PacManGameEvent> buildAI() {

		StateMachine<GhostState, PacManGameEvent> fsm = StateMachine
				.beginStateMachine(GhostState.class, PacManGameEvent.class, TransitionMatchStrategy.BY_CLASS)

				.description("Ghost " + name + " AI").initialState(LOCKED)

				.states()

				.state(LOCKED).onEntry(() -> {
					flashing = false;
					bounty = 0;
					nextState = LOCKED;
					setEnabled(true);
					placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
					entity.visible = true;
					entity.moveDir = bed.exitDir;
					entity.wishDir = bed.exitDir;
				}).onTick(this::move)

				.state(LEAVING_HOUSE).onTick(this::move).onExit(() -> forceMoving(Direction.LEFT))

				.state(ENTERING_HOUSE).onEntry(() -> steering().init()).onTick(this::move)

				.state(SCATTERING).onTick(() -> {
					maybeMeetPacMan(pacMan);
					move();
				})

				.state(CHASING).onTick(() -> {
					maybeMeetPacMan(pacMan);
					move();
				})

				.state(FRIGHTENED).timeoutAfter(this::getFrightenedTicks).onEntry(() -> steering().init())
				.onTick((state, consumed, remaining) -> {
					maybeMeetPacMan(pacMan);
					move();
					flashing = remaining < getFlashTimeTicks() * 0.5f; // one flashing takes 0.5 sec
				})

				.state(DEAD).timeoutAfter(sec(1)).onEntry(this::computeBounty).onTick((s, consumed, remaining) -> {
					if (remaining == 0) {
						bounty = 0;
						move();
					}
				})

				.transitions()

				.when(LOCKED).then(LEAVING_HOUSE).on(GhostUnlockedEvent.class)

				.when(LEAVING_HOUSE).then(SCATTERING).condition(() -> justLeftGhostHouse() && nextState == SCATTERING)
				.annotation("Outside house")

				.when(LEAVING_HOUSE).then(CHASING).condition(() -> justLeftGhostHouse() && nextState == CHASING)
				.annotation("Outside house")

				.when(LEAVING_HOUSE).then(FRIGHTENED).condition(() -> justLeftGhostHouse() && nextState == FRIGHTENED)
				.annotation("Outside house")

				.when(ENTERING_HOUSE).then(LEAVING_HOUSE).condition(() -> steering().isComplete()).annotation("Reached bed")

				.when(CHASING).then(FRIGHTENED).on(PacManGainsPowerEvent.class).act(() -> reverseDirection())

				.when(CHASING).then(DEAD).on(GhostKilledEvent.class)

				.when(CHASING).then(SCATTERING).condition(() -> nextState == SCATTERING).act(() -> reverseDirection())
				.annotation("Got scattering command")

				.when(SCATTERING).then(FRIGHTENED).on(PacManGainsPowerEvent.class).act(() -> reverseDirection())

				.when(SCATTERING).then(DEAD).on(GhostKilledEvent.class)

				.when(SCATTERING).then(CHASING).condition(() -> nextState == CHASING).act(() -> reverseDirection())
				.annotation("Got chasing command")

				.stay(FRIGHTENED).on(PacManGainsPowerEvent.class).act(() -> ai.resetTimer(FRIGHTENED))

				.when(FRIGHTENED).then(DEAD).on(GhostKilledEvent.class)

				.when(FRIGHTENED).then(SCATTERING).onTimeout().condition(() -> nextState == SCATTERING)

				.when(FRIGHTENED).then(CHASING).onTimeout().condition(() -> nextState == CHASING)

				.when(DEAD).then(ENTERING_HOUSE).condition(this::isAtHouseEntry).annotation("Reached house entry")

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
		if (game == null) {
			return 0;
		}
		GameLevel level = game.level;
		if (ai.getState() == null) {
			throw new IllegalStateException(String.format("Ghost %s is not initialized.", name));
		}
		boolean tunnel = entity.world.isTunnel(entity.tileLocation());
		switch (ai.getState()) {
		case LOCKED:
			return speed(isInsideHouse() ? level.ghostSpeed / 2 : 0);
		case LEAVING_HOUSE:
			return speed(level.ghostSpeed / 2);
		case ENTERING_HOUSE:
			return speed(level.ghostSpeed);
		case CHASING:
		case SCATTERING:
			if (tunnel) {
				return speed(level.ghostTunnelSpeed);
			}
			GhostMentalState mentalState = getMentalState();
			if (mentalState == GhostMentalState.ELROY1) {
				return speed(level.elroy1Speed);
			}
			if (mentalState == GhostMentalState.ELROY2) {
				return speed(level.elroy2Speed);
			}
			return speed(level.ghostSpeed);
		case FRIGHTENED:
			return speed(tunnel ? level.ghostTunnelSpeed : level.ghostFrightenedSpeed);
		case DEAD:
			return speed(2 * level.ghostSpeed);
		default:
			throw new IllegalStateException(String.format("Illegal ghost state %s", ai.getState()));
		}
	}

	@Override
	public Stream<StateMachine<?, ?>> machines() {
		return Stream.concat(super.machines(), Stream.of(madnessController));
	}

	public PacMan getPacMan() {
		return pacMan;
	}

	public void setPacMan(PacMan pacMan) {
		this.pacMan = pacMan;
	}

	public Bed bed() {
		return bed;
	}

	public void assignBed(House house, int bedNumber) {
		this.house = house;
		this.bed = house.bed(bedNumber);
	}

	public GhostMentalState getMentalState() {
		return Optional.ofNullable(madnessController).map(GhostMadnessController::getState)
				.orElse(GhostMentalState.HEALTHY);
	}

	public GhostMadnessController getMadnessController() {
		return madnessController;
	}

	@Override
	public void setGame(Game game) {
		this.game = game;
		if (madnessController != null) {
			madnessController.setGame(game);
		}
		init();
	}

	private void computeBounty() {
		bounty = game != null ? game.killedGhostPoints() : 0;
	}

	private long getFrightenedTicks() {
		return game != null ? sec(game.level.pacManPowerSeconds) : sec(5);
	}

	private long getFlashTimeTicks() {
		return game != null ? game.level.numFlashes * sec(0.5f) : 0;
	}

	private void maybeMeetPacMan(PacMan pacMan) {
		if (entity.tileLocation().equals(pacMan.entity.tileLocation()) && entity.visible && pacMan.entity.visible
				&& !pacMan.ai.is(PacManState.DEAD)) {
			ai.publish(new PacManGhostCollisionEvent(this));
		}
	}

	public void setNextState(GhostState state) {
		nextState = state;
	}

	public GhostState getNextState() {
		return nextState;
	}

	public GhostPersonality getPersonality() {
		return personality;
	}

	public int getBounty() {
		return bounty;
	}

	public void setBounty(int bounty) {
		this.bounty = bounty;
	}

	public boolean isFlashing() {
		return flashing;
	}

	public boolean justLeftGhostHouse() {
		Tile location = entity.tileLocation();
		return ai.getState() == LEAVING_HOUSE && house.isEntry(location) && entity.tf.y == location.row * Tile.SIZE;
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (house.isDoor(neighbor)) {
			return ai.is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		Optional<OneWayTile> maybeOneWay = entity.world.oneWayTiles().filter(oneWay -> oneWay.tile.equals(neighbor))
				.findFirst();
		if (maybeOneWay.isPresent()) {
			OneWayTile oneWay = maybeOneWay.get();
			Direction toNeighbor = tile.dirTo(neighbor).get();
			if (toNeighbor.equals(oneWay.dir.opposite()) && ai.is(CHASING, SCATTERING)) {
				return false;
			}
		}
		return super.canMoveBetween(tile, neighbor);
	}

	public void move() {
		Steering currentSteering = steering();
		if (previousSteering != currentSteering) {
			currentSteering.init();
			currentSteering.force();
			previousSteering = currentSteering;
		}
		// TODO check why ghosts get lost in tunnel/portal
		if (!entity.world.isTunnel(entity.tileLocation())) {
			currentSteering.steer(entity);
		}
		movement.update();
		if (madnessController != null) {
			madnessController.update();
		}
		setEnabled(entity.tf.vx != 0 || entity.tf.vy != 0);
	}

	public boolean isAtHouseEntry() {
		return house.isEntry(entity.tileLocation()) && (entity.tileOffsetX() - Tile.SIZE / 2) <= 1;
	}

	public boolean isInsideHouse() {
		return house.isInsideOrDoor(entity.tileLocation());
	}
}