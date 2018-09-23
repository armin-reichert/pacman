package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.DYING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.HOME;
import static de.amr.games.pacman.actor.GhostState.SAFE;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.ActorNavigation;
import de.amr.games.pacman.navigation.ActorNavigationSystem;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Actor implements ActorNavigationSystem<Ghost> {

	private final PacManGame game;
	private final String name;
	private final StateMachine<GhostState, GameEvent> fsm;
	private final Map<GhostState, ActorNavigation<Ghost>> navigationMap;
	private final PacMan pacMan;
	private final Tile home;
	private final Tile scatteringTarget;
	private final int initialDir;
	public Supplier<GhostState> fnNextAttackState; // chasing or scattering
	public BooleanSupplier fnCanLeaveHouse;

	public Ghost(String name, PacMan pacMan, PacManGame game, Tile home, Tile scatteringTarget, int initialDir,
			GhostColor color) {
		this.game = game;
		this.name = name;
		this.pacMan = pacMan;
		this.home = home;
		this.scatteringTarget = scatteringTarget;
		this.initialDir = initialDir;
		fsm = buildStateMachine(name);
		fnNextAttackState = () -> getState();
		fnCanLeaveHouse = () -> fsm.state().isTerminated();
		navigationMap = new EnumMap<>(GhostState.class);
		setSprites(color);
	}

	public void initGhost() {
		placeAtTile(home, getTileSize() / 2, 0);
		setCurrentDir(initialDir);
		setNextDir(initialDir);
		sprites.forEach(Sprite::resetAnimation);
		sprites.select("s_color_" + initialDir);
	}

	// Accessors

	public String getName() {
		return name;
	}
	
	@Override
	public Maze getMaze() {
		return game.getMaze();
	}

	public Tile getHomeTile() {
		return home;
	}

	public Tile getScatteringTarget() {
		return scatteringTarget;
	}

	public GhostState getNextAttackState() {
		GhostState nextAttackState = fnNextAttackState.get();
		return nextAttackState != null ? nextAttackState : getState();
	}

	@Override
	public float getSpeed() {
		return game.getGhostSpeed(getState(), getTile());
	}

	// Movement

	public void setMoveBehavior(GhostState state, ActorNavigation<Ghost> navigation) {
		navigationMap.put(state, navigation);
	}

	public ActorNavigation<Ghost> getMoveBehavior() {
		return navigationMap.getOrDefault(getState(), keepDirection());
	}

	@Override
	public int supplyIntendedDir() {
		return getMoveBehavior().getRoute(this).getDir();
	}

	@Override
	public boolean canTraverseDoor(Tile door) {
		if (getState() == GhostState.SAFE) {
			return false;
		}
		if (getState() == GhostState.DEAD) {
			return true;
		}
		return inGhostHouse();
	}

	private boolean canLeaveHouse() {
		return fnCanLeaveHouse.getAsBoolean();
	}

	private boolean isPacManGreedy() {
		return pacMan.getState() == PacManState.GREEDY;
	}

	// Sprites

	private void setSprites(GhostColor color) {
		NESW.dirs().forEach(dir -> {
			sprites.set("s_color_" + dir, PacManApp.theme.spr_ghostColored(color, dir));
			sprites.set("s_eyes_" + dir, PacManApp.theme.spr_ghostEyes(dir));
		});
		for (int i = 0; i < 4; ++i) {
			sprites.set("s_value" + i, PacManApp.theme.spr_greenNumber(i));
		}
		sprites.set("s_frightened", PacManApp.theme.spr_ghostFrightened());
		sprites.set("s_flashing", PacManApp.theme.spr_ghostFlashing());
	}

	// State machine

	@Override
	public void init() {
		super.init();
		fsm.traceTo(Application.LOGGER, Application.app().clock::getFrequency);
		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
	}

	public GhostState getState() {
		return fsm.getState();
	}

	public State<GhostState, GameEvent> getStateObject() {
		return fsm.state();
	}

	public void setState(GhostState state) {
		fsm.setState(state);
	}

	public void processEvent(GameEvent event) {
		fsm.process(event);
	}

	private StateMachine<GhostState, GameEvent> buildStateMachine(String ghostName) {
		/*@formatter:off*/
		return StateMachine.beginStateMachine(GhostState.class, GameEvent.class)
			 
			.description(String.format("[%s]", ghostName))
			.initialState(HOME)
		
			.states()

				.state(HOME)
					.onEntry(this::initGhost)
				
				.state(SAFE)
					.timeoutAfter(() -> app().clock.sec(2))
					.onTick(() -> {
						move();	
						sprites.select("s_color_" + getCurrentDir()); 
					})
				
				.state(SCATTERING)
					.onTick(() -> {
						move();	
						sprites.select("s_color_" + getCurrentDir()); 
					})
			
				.state(CHASING)
					.onTick(() -> {	
						move();	
						sprites.select("s_color_" + getCurrentDir()); 
					})
				
				.state(FRIGHTENED)
					.onEntry(() -> {
						sprites.select("s_frightened"); 
						getMoveBehavior().computePath(this); 
					})
					.onTick(this::move)
				
				.state(DYING)
					.timeoutAfter(game::getGhostDyingTime)
					.onEntry(() -> {
						sprites.select("s_value" + game.getGhostsKilledByEnergizer()); 
						game.addGhostKilled();
					})
				
				.state(DEAD)
					.onEntry(() -> getMoveBehavior().computePath(this))
					.onTick(() -> {	
						move();
						sprites.select("s_eyes_" + getCurrentDir());
					})
					
			.transitions()

				.when(HOME).then(SAFE)
				
				.stay(HOME)
					.on(StartScatteringEvent.class)
				
				.stay(HOME)
					.on(StartChasingEvent.class)

				.stay(SAFE)
					.on(StartChasingEvent.class)
					.condition(() -> !canLeaveHouse())
				
				.when(SAFE).then(CHASING)
					.on(StartChasingEvent.class)
					.condition(() -> canLeaveHouse())
				
				.stay(SAFE)
					.on(StartScatteringEvent.class)
					.condition(() -> !canLeaveHouse())
					
				.when(SAFE).then(SCATTERING)
					.on(StartScatteringEvent.class)
					.condition(() -> canLeaveHouse())
					
				.stay(SAFE).on(PacManGainsPowerEvent.class)
				.stay(SAFE).on(PacManGettingWeakerEvent.class)
				.stay(SAFE).on(PacManLostPowerEvent.class)
				.stay(SAFE).on(GhostKilledEvent.class)
				
				.when(SAFE).then(FRIGHTENED)
					.condition(() -> canLeaveHouse() && isPacManGreedy())

				.when(SAFE).then(SCATTERING)
					.condition(() -> canLeaveHouse() && getNextAttackState() == SCATTERING)
				
				.when(SAFE).then(CHASING)
					.condition(() -> canLeaveHouse() && getNextAttackState() == CHASING)
				
				.stay(CHASING).on(StartChasingEvent.class)
				.when(CHASING).then(FRIGHTENED).on(PacManGainsPowerEvent.class)
				.when(CHASING).then(DYING).on(GhostKilledEvent.class) // cheating-mode
				.when(CHASING).then(SCATTERING).on(StartScatteringEvent.class)

				.stay(SCATTERING).on(StartScatteringEvent.class)
				.stay(SCATTERING).on(PacManGettingWeakerEvent.class)
				.stay(SCATTERING).on(PacManLostPowerEvent.class)
				.when(SCATTERING).then(FRIGHTENED).on(PacManGainsPowerEvent.class)
				.when(SCATTERING).then(DYING).on(GhostKilledEvent.class) // cheating-mode
				.when(SCATTERING).then(CHASING).on(StartChasingEvent.class)
				
				.stay(FRIGHTENED).on(PacManGainsPowerEvent.class)
				.stay(FRIGHTENED).on(PacManGettingWeakerEvent.class).act(e -> sprites.select("s_flashing"))
				.stay(FRIGHTENED).on(StartScatteringEvent.class)
				.stay(FRIGHTENED).on(StartChasingEvent.class)
				
				.when(FRIGHTENED).then(CHASING).on(PacManLostPowerEvent.class)
				.when(FRIGHTENED).then(DYING).on(GhostKilledEvent.class)
					
				.when(DYING).then(DEAD).onTimeout()
				.stay(DYING).on(PacManGainsPowerEvent.class) // cheating-mode
				.stay(DYING).on(PacManGettingWeakerEvent.class) // cheating-mode
				.stay(DYING).on(PacManLostPowerEvent.class) // cheating-mode
				.stay(DYING).on(GhostKilledEvent.class) // cheating-mode
				.stay(DYING).on(StartScatteringEvent.class)
				.stay(DYING).on(StartChasingEvent.class)
					
				.when(DEAD).then(SAFE)
					.condition(() -> inGhostHouse())
					.act(this::initGhost)
				
				.stay(DEAD).on(PacManGainsPowerEvent.class)
				.stay(DEAD).on(PacManGettingWeakerEvent.class)
				.stay(DEAD).on(PacManLostPowerEvent.class)
				.stay(DEAD).on(GhostKilledEvent.class) // cheating-mode
				.stay(DEAD).on(StartScatteringEvent.class)
				.stay(DEAD).on(StartChasingEvent.class)

		.endStateMachine();
		/*@formatter:on*/
	}
}