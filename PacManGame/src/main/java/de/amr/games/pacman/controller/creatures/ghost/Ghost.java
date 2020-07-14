package de.amr.games.pacman.controller.creatures.ghost;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.model.game.Game.sec;
import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.api.MobileCreature;
import de.amr.games.pacman.controller.creatures.Animal;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.world.arcade.ArcadeWorldFolks;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.core.Bed;
import de.amr.games.pacman.model.world.core.House;
import de.amr.games.pacman.model.world.core.OneWayTile;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.Theme;

/**
 * A ghost.
 * 
 * <p>
 * Ghosts are creatures with additional behaviors like entering and leaving the ghost house or
 * jumping up and down at some position.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Animal<GhostState> {

	public static final int RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

	private final Map<GhostState, Steering> steerings = new EnumMap<>(GhostState.class);
	private final ArcadeWorldFolks folks;
	private final int color;
	private Supplier<GhostState> fnSubsequentState;
	private GhostSanityControl sanityControl;
	private Steering previousSteering;
	private int bounty;
	private boolean flashing;
	private IntSupplier fnNumFlashes = () -> 0;
	private IRenderer renderer;

	public Ghost(ArcadeWorldFolks folks, String name, int color) {
		super(GhostState.class, name);
		this.folks = folks;
		this.color = color;
		/*@formatter:off*/
		beginStateMachine()
			 
			.description(this::toString)
			.initialState(LOCKED)
		
			.states()
	
				.state(LOCKED)
					.onEntry(() -> {
						fnSubsequentState = () -> LOCKED;
						entity.visible = true;
						flashing = false;
						bounty = 0;
						world.putIntoBed(this);
						enteredNewTile();
						if (sanityControl != null) {
							sanityControl.init();
						}
					})
					.onTick(this::move)
					
				.state(LEAVING_HOUSE)
					.onTick(this::move)
					.onExit(() -> forceMoving(Direction.LEFT))
				
				.state(ENTERING_HOUSE)
					.onEntry(() -> steering().init()) //TODO should not be necessary
					.onTick(this::move)
				
				.state(SCATTERING)
					.onTick(() -> {
						updateSanity();
						checkPacManCollision();
						move();
					})
			
				.state(CHASING)
					.onTick(() -> {
						updateSanity();
						checkPacManCollision();
						move();
					})
				
				.state(FRIGHTENED)
					.onTick((state, t, remaining) -> {
						checkPacManCollision();
						move();
						// one flashing animation takes 0.5 sec
						flashing = remaining < fnNumFlashes.getAsInt() *0.5f;
					})
				
				.state(DEAD)
					.onTick(this::move)
				
			.transitions()
			
				.when(LOCKED).then(LEAVING_HOUSE)
					.on(GhostUnlockedEvent.class)
			
				.when(LEAVING_HOUSE).then(SCATTERING)
					.condition(() -> hasLeftGhostHouse() && fnSubsequentState.get() == SCATTERING)
				
				.when(LEAVING_HOUSE).then(CHASING)
					.condition(() -> hasLeftGhostHouse() && fnSubsequentState.get() == CHASING)
				
				.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
					.condition(() -> steering().isComplete())
				
				.when(CHASING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> reverseDirection())
				
				.when(CHASING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(CHASING).then(SCATTERING)
					.condition(() -> fnSubsequentState.get() == SCATTERING)
					.act(() -> reverseDirection())
					
				.when(SCATTERING).then(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> reverseDirection())
				
				.when(SCATTERING).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(SCATTERING).then(CHASING)
					.condition(() -> fnSubsequentState.get() == CHASING)
					.act(() -> reverseDirection())
					
				.stay(FRIGHTENED)
					.on(PacManGainsPowerEvent.class)
					.act(() -> restartTimer(FRIGHTENED))
				
				.when(FRIGHTENED).then(DEAD)
					.on(GhostKilledEvent.class)
				
				.when(FRIGHTENED).then(SCATTERING)
					.onTimeout()
					.condition(() -> fnSubsequentState.get() == SCATTERING)
					
				.when(FRIGHTENED).then(CHASING)
					.onTimeout()
					.condition(() -> fnSubsequentState.get() == CHASING)
					
				.when(DEAD).then(ENTERING_HOUSE)
					.condition(() -> world.isJustBeforeDoor(location()))
					
		.endStateMachine();
		/*@formatter:on*/
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		PacManApp.fsm_register(this);
	}

	@Override
	protected Map<GhostState, Steering> steerings() {
		return steerings;
	}

	public ArcadeWorldFolks folks() {
		return folks;
	}

	@Override
	public void draw(Graphics2D g) {
		renderer.render(g);
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		renderer = theme.createGhostRenderer(this);
	}

	@Override
	public IRenderer renderer() {
		return renderer;
	}

	public void getReadyToRumble(Game game) {
		// frightened time is defined by the game level
		state(FRIGHTENED).setTimer(() -> sec(game.level.pacManPowerSeconds));
		this.fnNumFlashes = () -> sec(game.level.numFlashes * 0.5f);

		// when dead, the ghost first appears as a number (its value) for one second, then it
		// appears as eyes returning to the ghost house
		state(DEAD).setTimer(sec(1));
		state(DEAD).entryAction = () -> bounty = game.killedGhostPoints();
		state(DEAD).tickAction = (s, consumed, remaining) -> {
			if (remaining == 0) {
				bounty = 0;
				move();
			}
		};

		// Blinky can get insane ("cruise elroy")
		if (this == folks.blinky()) {
			sanityControl = new GhostSanityControl(game, "Blinky", GhostSanity.INFECTABLE);
			PacManApp.fsm_register(sanityControl);
		}
	}

	public void nextStateToEnter(Supplier<GhostState> fnSubsequentState) {
		this.fnSubsequentState = fnSubsequentState;
	}

	public GhostState getNextStateToEnter() {
		return fnSubsequentState.get();
	}

	public GhostSanity getSanity() {
		return sanityControl != null ? sanityControl.getState() : GhostSanity.IMMUNE;
	}

	private void updateSanity() {
		if (sanityControl != null) {
			sanityControl.update();
		}
	}

	public int getColor() {
		return color;
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

	/**
	 * Lets the ghost jump up and down on its bed.
	 */
	public void bouncingOnBed(Bed bed) {
		float dy = entity.tf.y + Tile.SIZE / 2 - bed.center.y;
		if (dy < -4) {
			setWishDir(DOWN);
		} else if (dy > 3) {
			setWishDir(UP);
		}
	}

	/**
	 * lets a ghost leave the given house
	 */
	public void leavingHouse(House house) {
		Tile exit = Tile.at(house.bed(0).col(), house.bed(0).row());
		int targetX = exit.centerX(), targetY = exit.y();
		if (entity.tf.y <= targetY) {
			entity.tf.y = targetY;
		} else if (Math.round(entity.tf.x) == targetX) {
			entity.tf.x = targetX;
			setWishDir(UP);
		} else if (entity.tf.x < targetX) {
			setWishDir(RIGHT);
		} else if (entity.tf.x > targetX) {
			setWishDir(LEFT);
		}
	}

	private boolean hasLeftGhostHouse() {
		return entity.tf.y == world.theHouse().bed(0).row() * Tile.SIZE;
	}

	/**
	 * Lets the actor avoid the attacker's path by walking to a "safe" maze corner.
	 * 
	 * @param attacker the attacking actor
	 * @return steering where actor flees to a "safe" maze corner
	 */
	public Steering fleeingToSafeCorner(MobileCreature attacker) {
		return new FleeingToSafeCorner(this, attacker, world.capeNW(), world.capeNE(), world.capeSW(), world.capeSE());
	}

	/**
	 * @return steering for bringing ghost back to ghost house entry
	 */
	public Steering returningToHouse(House house) {
		return headingFor(() -> Tile.at(house.bed(0).col(), house.bed(0).row()));
	}

	/**
	 * @return steering which lets ghost enter the house going to bed
	 */
	public Steering goingToBed(Bed bed) {
		return new EnteringGhostHouseAndGoingToBed(this, bed);
	}

	@Override
	public boolean canMoveBetween(Tile tile, Tile neighbor) {
		if (world.isDoor(neighbor)) {
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
		Steering currentSteering = steering();
		if (previousSteering != currentSteering) {
			currentSteering.init();
			currentSteering.force();
			previousSteering = currentSteering;
		}
		currentSteering.steer();
		movement.update();
	}

	public boolean isInsideHouse() {
		return world.insideHouseOrDoor(location());
	}

	private void checkPacManCollision() {
		PacMan pacMan = folks.pacMan();
		if (location().equals(pacMan.location()) && !isTeleporting() && !pacMan.isTeleporting()
				&& !pacMan.is(PacManState.DEAD)) {
			publish(new PacManGhostCollisionEvent(this));
		}
	}
}