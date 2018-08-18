package de.amr.games.pacman.actor.game;

import static de.amr.games.pacman.actor.game.GhostState.AFRAID;
import static de.amr.games.pacman.actor.game.GhostState.AGGRO;
import static de.amr.games.pacman.actor.game.GhostState.DEAD;
import static de.amr.games.pacman.actor.game.GhostState.DYING;
import static de.amr.games.pacman.actor.game.GhostState.HOME;
import static de.amr.games.pacman.actor.game.GhostState.SAFE;
import static de.amr.games.pacman.actor.game.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Game.TS;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.view.PacManGameUI.SPRITES;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.controller.event.game.GameEvent;
import de.amr.games.pacman.controller.event.game.GhostKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.Navigation;
import de.amr.games.pacman.navigation.impl.NavigationSystem;
import de.amr.games.pacman.view.PacManSprites.GhostColor;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends MazeMover {

	private final StateMachine<GhostState, GameEvent> controller;
	private final Map<GhostState, Navigation> navigationMap;
	private final Game game;
	private final GhostName name;
	private final PacMan pacMan;
	private final Tile home;
	private final int initialDir;

	public Ghost(GhostName name, PacMan pacMan, Game game, Tile home, int initialDir, GhostColor color) {
		this.name = name;
		this.pacMan = pacMan;
		this.game = game;
		this.home = home;
		this.initialDir = initialDir;
		controller = buildStateMachine();
		navigationMap = new EnumMap<>(GhostState.class);
		createSprites(color);
	}

	// Navigation

	public void setNavigation(GhostState state, Navigation navigation) {
		navigationMap.put(state, navigation);
	}

	public Navigation getNavigation() {
		return navigationMap.getOrDefault(getState(), NavigationSystem.forward());
	}

	@Override
	public int supplyIntendedDir() {
		return getNavigation().computeRoute(this).dir;
	}

	@Override
	public boolean canWalkThroughDoor(Tile door) {
		return getState() == GhostState.DEAD || getTile().row >= door.row;
	}

	// Accessors

	@Override
	public Maze getMaze() {
		return game.maze;
	}

	public GhostName getName() {
		return name;
	}

	public Tile getHome() {
		return home;
	}

	@Override
	public float getSpeed() {
		return game.getGhostSpeed(getState(), getTile());
	}

	// Sprites

	private Sprite sprite;
	private Sprite s_color[] = new Sprite[4];
	private Sprite s_eyes[] = new Sprite[4];
	private Sprite s_awed;
	private Sprite s_flashing;
	private Sprite s_numbers[] = new Sprite[4];

	private void createSprites(GhostColor color) {
		NESW.dirs().forEach(dir -> {
			s_color[dir] = SPRITES.ghostColored(color, dir);
			s_eyes[dir] = SPRITES.ghostEyes(dir);
		});
		for (int i = 0; i < 4; ++i) {
			s_numbers[i] = SPRITES.greenNumber(i);
		}
		s_awed = SPRITES.ghostAwed();
		s_flashing = SPRITES.ghostFlashing();
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(Stream.of(s_color), Stream.of(s_numbers), Stream.of(s_eyes), Stream.of(s_awed, s_flashing))
				.flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}

	// State machine

	private void initGhost() {
		placeAtTile(home, TS / 2, 0);
		setCurrentDir(initialDir);
		setNextDir(initialDir);
		getSprites().forEach(Sprite::resetAnimation);
		sprite = s_color[getCurrentDir()];
	}

	@Override
	public void init() {
		controller.init();
	}

	@Override
	public void update() {
		controller.update();
	}

	public GhostState getState() {
		return controller.currentState();
	}

	public StateObject<GhostState, GameEvent> currentStateObject() {
		return controller.currentStateObject();
	}

	public void processEvent(GameEvent e) {
		controller.process(e);
	}

	public void traceTo(Logger logger) {
		controller.traceTo(logger, game.fnTicksPerSec);
	}

	private StateMachine<GhostState, GameEvent> buildStateMachine() {
		return
		/*@formatter:off*/
		StateMachine.define(GhostState.class, GameEvent.class)
			 
			.description(String.format("[Ghost %s]", getName()))
			.initialState(HOME)
		
			.states()

					.state(HOME)
						.onEntry(this::initGhost)
					
					.state(AFRAID)
						.onEntry(() -> sprite = s_awed)
						.onTick(() -> move())
					
					.state(AGGRO)
						.onTick(() -> {	move();	sprite = s_color[getCurrentDir()]; })
					
					.state(DEAD)
						.onTick(() -> {	move();	sprite = s_eyes[getCurrentDir()]; })
					
					.state(DYING)
						.onEntry(() -> {
							sprite = s_numbers[game.getGhostsKilledByEnergizer()]; 
							game.addGhostKilled();
						})
						.timeoutAfter(game::getGhostDyingTime)
					
					.state(SAFE)
						.onTick(() -> {	move();	sprite = s_color[getCurrentDir()]; })
						.timeoutAfter(() -> game.sec(2))
					
					.state(SCATTERING) //TODO
				
			.transitions()

					.when(HOME).then(SAFE)

					.when(SAFE)
						.onTimeout().condition(() -> pacMan.getState() != PacManState.GREEDY)
						.then(AGGRO)
						
					.when(SAFE)
						.onTimeout().condition(() -> pacMan.getState() == PacManState.GREEDY)
						.then(AFRAID)
						
					.stay(SAFE).on(PacManGainsPowerEvent.class)
					.stay(SAFE).on(PacManGettingWeakerEvent.class)
					.stay(SAFE).on(PacManLostPowerEvent.class)
					.stay(SAFE).on(GhostKilledEvent.class)
						
					.when(AGGRO).on(PacManGainsPowerEvent.class).then(AFRAID)
					.when(AGGRO).on(GhostKilledEvent.class).then(DEAD) // used for cheating
						
					.stay(AFRAID).on(PacManGainsPowerEvent.class)
					.stay(AFRAID).on(PacManGettingWeakerEvent.class).act(e -> sprite = s_flashing)
					.when(AFRAID).on(PacManLostPowerEvent.class).then(AGGRO)
					.when(AFRAID).on(GhostKilledEvent.class).then(DYING)
						
					.when(DYING).then(DEAD).onTimeout()
						
					.when(DEAD).then(SAFE)
						.condition(() -> getTile().equals(home))
						.act(this::initGhost)
					
					.stay(DEAD).on(PacManGainsPowerEvent.class)
					.stay(DEAD).on(PacManGettingWeakerEvent.class)
					.stay(DEAD).on(PacManLostPowerEvent.class)
					.stay(DEAD).on(GhostKilledEvent.class) // happens only when cheating

		.endStateMachine();
		/*@formatter:on*/
	}
}