package de.amr.games.pacman.controller.creatures.ghost;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.model.game.Game.sec;

import java.awt.Graphics2D;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.Creature;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.statemachine.core.StateMachine;

/**
 * A ghost.
 * 
 * <p>
 * Ghosts are creatures with additional behaviors like entering and leaving the ghost house or
 * jumping up and down at some position.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature<Ghost, GhostState> {

	private final GhostPersonality personality;
	private House house;
	private Bed bed;
	private Supplier<GhostState> fnSubsequentState;
	private GhostSanityControl sanity;
	private Steering<Ghost> previousSteering;
	private int bounty;
	private boolean flashing;
	private Supplier<Long> fnFlashTimeTicks = () -> 0L;
	private IRenderer renderer;

	public Ghost(World world, PacMan pacMan, String name, GhostPersonality personality) {
		super(GhostState.class, world, name);
		this.personality = personality;
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		/*@formatter:off*/
		beginStateMachine()
			 
			.description("Ghost " + name + " AI")
			.initialState(LOCKED)
		
			.states()
	
				.state(LOCKED)
					.onEntry(() -> {
						fnSubsequentState = () -> LOCKED;
						setVisible(true);
						flashing = false;
						bounty = 0;
						placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
						setMoveDir(bed.exitDir);
						setWishDir(bed.exitDir);
					})
					.onTick(this::move)
					
				.state(LEAVING_HOUSE)
					.onTick(this::move)
					.onExit(() -> forceMoving(Direction.LEFT))
				
				.state(ENTERING_HOUSE)
					.onEntry(() -> steering().init())
					.onTick(this::move)
				
				.state(SCATTERING)
					.onTick(() -> {
						maybeMeetPacMan(pacMan);
						move();
					})
			
				.state(CHASING)
					.onTick(() -> {
						maybeMeetPacMan(pacMan);
						move();
					})
				
				.state(FRIGHTENED)
					.onEntry(() -> steering().init())
					.onTick((state, consumed, remaining) -> {
						maybeMeetPacMan(pacMan);
						move();
						// one flashing animation takes 0.5 sec
						flashing = remaining < fnFlashTimeTicks.get() * 0.5f;
					})
				
				.state(DEAD)
					.onTick((s, consumed, remaining) -> {
						if (remaining == 0) {
							bounty = 0;
							move();
						}
					})
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> hasLeftGhostHouse() && getNextStateToEnter() == SCATTERING)
					.annotation("Outside house")
		
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> hasLeftGhostHouse() && getNextStateToEnter() == CHASING)
					.annotation("Outside house")
				
				.when(LEAVING_HOUSE).then(FRIGHTENED)
					.condition(() -> hasLeftGhostHouse() && getNextStateToEnter() == FRIGHTENED)
					.annotation("Outside house")
					
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> steering().isComplete())
					.annotation("Reached bed")
				
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> reverseDirection())
				
				.when(CHASING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(CHASING).then(SCATTERING)
					.condition(() -> getNextStateToEnter() == SCATTERING)
					.act(() -> reverseDirection())
					.annotation("Got scattering command")
					
				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> reverseDirection())
				
				.when(SCATTERING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING)
					.condition(() -> getNextStateToEnter() == CHASING)
					.act(() -> reverseDirection())
					.annotation("Got chasing command")
					
				.stay(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> resetTimer(FRIGHTENED))
				
				.when(FRIGHTENED).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(FRIGHTENED).then(SCATTERING)
					.onTimeout()
					.condition(() -> getNextStateToEnter() == SCATTERING)
					
				.when(FRIGHTENED).then(CHASING)
					.onTimeout()
					.condition(() -> getNextStateToEnter() == CHASING)
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(this::isAtHouseEntry)
					.annotation("Reached house entry")
					
		.endStateMachine();
		/*@formatter:on*/
	}

	@Override
	public Stream<StateMachine<?, ?>> machines() {
		return Stream.concat(super.machines(), Stream.of(sanity));
	}

	@Override
	public void init() {
		if (sanity != null) {
			sanity.init();
		}
		super.init();
	}

	@Override
	public World world() {
		return world;
	}

	public Bed bed() {
		return bed;
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		renderer = theme.ghostRenderer(this);
	}

	@Override
	public void draw(Graphics2D g) {
		renderer.render(g);
	}

	public IRenderer renderer() {
		return renderer;
	}

	public void assignBed(House house, int bedNumber) {
		this.house = house;
		this.bed = house.bed(bedNumber);
	}

	@Override
	public void getReadyToRumble(Game game) {
		if ("Blinky".equals(name)) {
			sanity = new GhostSanityControl(game, "Blinky", GhostSanity.INFECTABLE);
			sanity.init();
		}
		fnFlashTimeTicks = () -> game.level.numFlashes * sec(0.5f);
		setSpeed(() -> GameController.ghostSpeed(this, game.level));
		state(FRIGHTENED).setTimer(() -> sec(game.level.pacManPowerSeconds));
		state(DEAD).setTimer(sec(1));
		state(DEAD).entryAction = () -> bounty = game.killedGhostPoints();
	}

	private void maybeMeetPacMan(PacMan pacMan) {
		if (tileLocation().equals(pacMan.tileLocation()) && isVisible() && pacMan.isVisible()
				&& !pacMan.is(PacManState.DEAD)) {
			publish(new PacManGhostCollisionEvent(this));
		}
	}

	public void setNextStateToEnter(Supplier<GhostState> fnSubsequentState) {
		this.fnSubsequentState = fnSubsequentState;
	}

	public GhostState getNextStateToEnter() {
		return fnSubsequentState.get();
	}

	public GhostSanity getSanity() {
		return sanity != null ? sanity.getState() : GhostSanity.IMMUNE;
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

	private boolean hasLeftGhostHouse() {
		Tile location = tileLocation();
		return house.isEntry(location) && entity.tf.y == location.row * Tile.SIZE;
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (house.isDoor(neighbor)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		Optional<OneWayTile> maybeOneWay = world.oneWayTiles().filter(oneWay -> oneWay.tile.equals(neighbor)).findFirst();
		if (maybeOneWay.isPresent()) {
			OneWayTile oneWay = maybeOneWay.get();
			Direction toNeighbor = tile.dirTo(neighbor).get();
			if (toNeighbor.equals(oneWay.dir.opposite()) && is(CHASING, SCATTERING)) {
				return false;
			}
		}
		return super.canMoveBetween(tile, neighbor);
	}

	public void move() {
		Steering<Ghost> currentSteering = steering();
		if (previousSteering != currentSteering) {
			currentSteering.init();
			currentSteering.force();
			previousSteering = currentSteering;
		}
		currentSteering.steer(this);
		movement.update();
		if (sanity != null) {
			sanity.update();
		}
	}

	public boolean isAtHouseEntry() {
		return house.isEntry(tileLocation()) && (tileOffsetX() - Tile.SIZE / 2) <= 1;
	}

	public boolean isInsideHouse() {
		return house.isInsideOrDoor(tileLocation());
	}
}