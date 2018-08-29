package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.DYING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.HOME;
import static de.amr.games.pacman.actor.GhostState.SAFE;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.theme.PacManThemes.THEME;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.StateMachineClient;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.Navigation;
import de.amr.games.pacman.navigation.NavigationSystem;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Actor
		implements StateMachineClient<GhostState, GameEvent>, NavigationSystem<Ghost> {

	private final String name;
	private final Game game;
	private final StateMachine<GhostState, GameEvent> controller;
	private final Map<GhostState, Navigation<Ghost>> navigationMap;
	private final PacMan pacMan;
	private final Tile home;
	private final Tile scatteringTarget;
	private final int initialDir;

	BooleanSupplier fnCanLeaveHouse;

	public Ghost(String name, PacMan pacMan, Game game, Tile home, Tile scatteringTarget,
			int initialDir, GhostColor color) {
		this.name = name;
		this.pacMan = pacMan;
		this.game = game;
		this.home = home;
		this.scatteringTarget = scatteringTarget;
		this.initialDir = initialDir;
		fnCanLeaveHouse = () -> getStateObject().isTerminated();
		controller = buildStateMachine(name);
		navigationMap = new EnumMap<>(GhostState.class);
		createSprites(color);
	}

	public void initGhost() {
		placeAt(home, getTileSize() / 2, 0);
		setCurrentDir(initialDir);
		setNextDir(initialDir);
		getSprites().forEach(Sprite::resetAnimation);
		setCurrentSprite("s_color_" + initialDir);
	}

	// Accessors

	@Override
	public Transform getTransform() {
		return tf;
	}

	public String getName() {
		return name;
	}

	@Override
	public Maze getMaze() {
		return game.getMaze();
	}

	public Tile getHome() {
		return home;
	}

	public Tile getScatteringTarget() {
		return scatteringTarget;
	}

	@Override
	public float getSpeed() {
		return game.getGhostSpeed(getState(), getTile());
	}
	// Movement

	public void setMoveBehavior(GhostState state, Navigation<Ghost> navigation) {
		navigationMap.put(state, navigation);
	}

	public Navigation<Ghost> getMoveBehavior() {
		return navigationMap.getOrDefault(getState(), keepDirection());
	}

	@Override
	public int supplyIntendedDir() {
		return getMoveBehavior().computeRoute(this).getDir();
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

	// Sprites

	private void createSprites(GhostColor color) {
		NESW.dirs().forEach(dir -> {
			addSprite("s_color_" + dir, THEME.ghostColored(color, dir));
			addSprite("s_eyes_" + dir, THEME.ghostEyes(dir));
		});
		for (int i = 0; i < 4; ++i) {
			addSprite("s_numbers_" + i, THEME.greenNumber(i));
		}
		addSprite("s_frightened", THEME.ghostFrightened());
		addSprite("s_flashing", THEME.ghostFlashing());
		setCurrentSprite("s_color_" + getCurrentDir());
	}

	// State machine

	@Override
	public void init() {
		controller.init();
	}

	@Override
	public void update() {
		controller.update();
	}

	@Override
	public StateMachine<GhostState, GameEvent> getStateMachine() {
		return controller;
	}

	public void traceTo(Logger logger) {
		controller.traceTo(logger, game.fnTicksPerSec);
	}

	private StateMachine<GhostState, GameEvent> buildStateMachine(String ghostName) {
		return
		/*@formatter:off*/
		StateMachine.define(GhostState.class, GameEvent.class)
			 
			.description(String.format("[Ghost %s]", ghostName))
			.initialState(HOME)
		
			.states()

					.state(HOME)
						.onEntry(this::initGhost)
					
					.state(SAFE)
						.timeoutAfter(() -> game.sec(2))
						.onTick(() -> {
							if (!ghostName.equals("Blinky")) { //TODO better solution
								move();	
								setCurrentSprite("s_color_" + getCurrentDir()); 
							}
						})
					
					.state(CHASING)
						.onTick(() -> {	
							move();	
							setCurrentSprite("s_color_" + getCurrentDir()); 
						})
					
					.state(FRIGHTENED)
						.onEntry(() -> {
							setCurrentSprite("s_frightened"); 
							getMoveBehavior().computeStaticRoute(this); 
						})
						.onTick(() -> move())
					
					.state(DYING)
						.timeoutAfter(game::getGhostDyingTime)
						.onEntry(() -> {
							setCurrentSprite("s_numbers_" + game.getGhostsKilledByEnergizer()); 
							game.addGhostKilled();
						})
					
					.state(DEAD)
						.onEntry(() -> getMoveBehavior().computeStaticRoute(this))
						.onTick(() -> {	
							move();
							setCurrentSprite("s_eyes_" + getCurrentDir());
						})
					
					.state(SCATTERING)
						.onTick(() -> {
							move();	
							setCurrentSprite("s_color_" + getCurrentDir()); 
						})
				
			.transitions()

					.when(HOME).then(SAFE)

					.when(SAFE).then(CHASING)
						.condition(() -> fnCanLeaveHouse.getAsBoolean() && pacMan.getState() != PacManState.GREEDY)
						
					.when(SAFE).then(FRIGHTENED)
						.condition(() -> fnCanLeaveHouse.getAsBoolean() && pacMan.getState() == PacManState.GREEDY)

					.stay(SAFE).on(PacManGainsPowerEvent.class)
					.stay(SAFE).on(PacManGettingWeakerEvent.class)
					.stay(SAFE).on(PacManLostPowerEvent.class)
					.stay(SAFE).on(GhostKilledEvent.class)
						
					.when(CHASING).then(FRIGHTENED).on(PacManGainsPowerEvent.class)
					.when(CHASING).then(DEAD).on(GhostKilledEvent.class) // cheating-mode
						
					.stay(FRIGHTENED).on(PacManGainsPowerEvent.class)
					.stay(FRIGHTENED).on(PacManGettingWeakerEvent.class).act(e -> setCurrentSprite("s_flashing"))
					.when(FRIGHTENED).then(CHASING).on(PacManLostPowerEvent.class)
					.when(FRIGHTENED).then(DYING).on(GhostKilledEvent.class)
						
					.when(DYING).then(DEAD).onTimeout()
					.stay(DYING).on(PacManGainsPowerEvent.class) // cheating-mode
					.stay(DYING).on(PacManGettingWeakerEvent.class) // cheating-mode
						
					.when(DEAD).then(SAFE)
						//TODO: better solution
						.condition(() -> (ghostName.equals("Blinky") && getTile().equals(home)) || inGhostHouse())
						.act(this::initGhost)
					
					.stay(DEAD).on(PacManGainsPowerEvent.class)
					.stay(DEAD).on(PacManGettingWeakerEvent.class)
					.stay(DEAD).on(PacManLostPowerEvent.class)
					.stay(DEAD).on(GhostKilledEvent.class) // cheating-mode

		.endStateMachine();
		/*@formatter:on*/
	}
}