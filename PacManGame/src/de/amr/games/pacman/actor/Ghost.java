package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.DYING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.ActorBehavior;
import de.amr.games.pacman.navigation.GhostBehaviors;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends PacManGameActor implements GhostBehaviors {

	private final String name;
	private final StateMachine<GhostState, GameEvent> fsm;
	private final Map<GhostState, ActorBehavior<Ghost>> behaviorMap;
	private final Tile initialTile;
	private final Tile revivalTile;
	private final Tile scatteringTarget;
	private final int initialDir;
	public Supplier<GhostState> fnNextAttackState; // chasing or scattering
	public BooleanSupplier fnCanLeaveGhostHouse;

	public Ghost(PacManGame game, String name, GhostColor color, Tile initialTile, Tile revivalTile,
			Tile scatteringTarget, int initialDir) {
		super(game);
		this.name = name;
		setSprites(color);
		this.initialTile = initialTile;
		this.revivalTile = revivalTile;
		this.scatteringTarget = scatteringTarget;
		this.initialDir = initialDir;
		behaviorMap = new EnumMap<>(GhostState.class);
		fsm = buildStateMachine(name);
		fsm.traceTo(Application.LOGGER, app().clock::getFrequency);
		fnNextAttackState = () -> getState();
		fnCanLeaveGhostHouse = () -> fsm.state().getDuration() == State.ENDLESS || fsm.state().isTerminated();
	}

	public void initGhost() {
		placeAtTile(initialTile, getTileSize() / 2, 0);
		setCurrentDir(initialDir);
		setNextDir(initialDir);
		sprites.forEach(Sprite::resetAnimation);
		sprites.select("s_color_" + initialDir);
	}

	private void reviveGhost() {
		int dir = initialDir;
		if (dir == Top4.E || dir == Top4.W) {
			dir = Top4.N; // let Blinky look upwards when in ghost house
		}
		setCurrentDir(dir);
		setNextDir(dir);
		sprites.forEach(Sprite::resetAnimation);
		sprites.select("s_color_" + dir);
	}

	// Accessors

	public String getName() {
		return name;
	}

	public Tile getInitialTile() {
		return initialTile;
	}

	public Tile getRevivalTile() {
		return revivalTile;
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
		return getGame().getGhostSpeed(this);
	}

	public PacManTheme getTheme() {
		return app().settings.get("theme");
	}

	// Behavior

	public void setBehavior(GhostState state, ActorBehavior<Ghost> behavior) {
		behaviorMap.put(state, behavior);
	}

	public ActorBehavior<Ghost> getBehavior() {
		return behaviorMap.getOrDefault(getState(), keepDirection());
	}

	@Override
	public int supplyIntendedDir() {
		return getBehavior().getRoute(this).getDir();
	}

	@Override
	public boolean canTraverseDoor(Tile door) {
		if (getState() == GhostState.LOCKED) {
			return false;
		}
		if (getState() == GhostState.DEAD) {
			return true;
		}
		return inGhostHouse();
	}

	private boolean canLeaveGhostHouse() {
		return fnCanLeaveGhostHouse.getAsBoolean();
	}

	private boolean isPacManGreedy() {
		return getGame().getPacMan().getState() == PacManState.GREEDY;
	}

	// Sprites

	private void setSprites(GhostColor color) {
		NESW.dirs().forEach(dir -> {
			sprites.set("s_color_" + dir, getTheme().spr_ghostColored(color, dir));
			sprites.set("s_eyes_" + dir, getTheme().spr_ghostEyes(dir));
		});
		for (int i = 0; i < 4; ++i) {
			sprites.set("s_value" + i, getTheme().spr_greenNumber(i));
		}
		sprites.set("s_frightened", getTheme().spr_ghostFrightened());
		sprites.set("s_flashing", getTheme().spr_ghostFlashing());
	}

	// State machine

	@Override
	public void init() {
		initGhost();
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
			.initialState(LOCKED)
		
			.states()

				.state(LOCKED)
					.timeoutAfter(() -> getGame().getGhostSafeTime(this))
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
					.onEntry(() -> getTheme().snd_ghost_chase().loop())
					.onExit(() -> getTheme().snd_ghost_chase().stop())
					.onTick(() -> {	
						move();	
						sprites.select("s_color_" + getCurrentDir()); 
					})
				
				.state(FRIGHTENED)
					.onEntry(() -> {
						getBehavior().computePath(this); 
					})
					.onTick(() -> {
						move();
						sprites.select(inGhostHouse() ? "s_color_" + getCurrentDir() : 
							getGame().getPacMan().isGettingWeaker() ? "s_flashing" : "s_frightened");
					})
				
				.state(DYING)
					.timeoutAfter(getGame()::getGhostDyingTime)
					.onEntry(() -> {
						sprites.select("s_value" + getGame().getGhostsKilledByEnergizer()); 
						getGame().addGhostKilled();
					})
				
				.state(DEAD)
					.onEntry(() -> {
						getBehavior().computePath(this);
						getTheme().snd_ghost_dead().loop();
					})
					.onTick(() -> {	
						move();
						sprites.select("s_eyes_" + getCurrentDir());
					})
					.onExit(() -> {
						if (getGame().getActiveGhosts().filter(ghost -> ghost != this)
								.noneMatch(ghost -> ghost.getState() == DEAD)) {
							getTheme().snd_ghost_dead().stop();
						}
					})
					
			.transitions()

				.stay(LOCKED)
					.on(StartChasingEvent.class)
					.condition(() -> !canLeaveGhostHouse())
				
				.when(LOCKED).then(CHASING)
					.on(StartChasingEvent.class)
					.condition(() -> canLeaveGhostHouse())
				
				.stay(LOCKED)
					.on(StartScatteringEvent.class)
					.condition(() -> !canLeaveGhostHouse())
					
				.when(LOCKED).then(SCATTERING)
					.on(StartScatteringEvent.class)
					.condition(() -> canLeaveGhostHouse())
					
				.stay(LOCKED).on(PacManGainsPowerEvent.class)
				.stay(LOCKED).on(PacManGettingWeakerEvent.class)
				.stay(LOCKED).on(PacManLostPowerEvent.class)
				.stay(LOCKED).on(GhostKilledEvent.class)
				
				.when(LOCKED).then(FRIGHTENED)
					.condition(() -> canLeaveGhostHouse() && isPacManGreedy())

				.when(LOCKED).then(SCATTERING)
					.condition(() -> canLeaveGhostHouse() && getNextAttackState() == SCATTERING)
				
				.when(LOCKED).then(CHASING)
					.condition(() -> canLeaveGhostHouse() && getNextAttackState() == CHASING)
				
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
					
				.when(DEAD).then(LOCKED)
					.condition(() -> getTile().equals(getRevivalTile()))
					.act(this::reviveGhost)
				
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