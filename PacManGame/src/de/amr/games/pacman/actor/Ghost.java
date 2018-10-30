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
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
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
	public Supplier<GhostState> fnNextState; // chasing or scattering
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
		fsm.setIgnoreUnknownEvents(true);
		fsm.traceTo(Application.LOGGER, app().clock::getFrequency);
		fnNextState = this::getState; // default
		fnCanLeaveGhostHouse = () -> fsm.state().getDuration() == State.ENDLESS || fsm.state().isTerminated();
	}

	public void initGhost() {
		placeAtTile(initialTile, getTileSize() / 2, 0);
		setMoveDir(initialDir);
		setNextDir(initialDir);
		sprites.forEach(Sprite::resetAnimation);
		sprites.select("s_color_" + initialDir);
	}

	private void reviveGhost() {
		int dir = initialDir;
		if (this == getGame().getBlinky()) {
			dir = Top4.N; // let Blinky look upwards when in ghost house
		} else {
			placeAtTile(initialTile, getTileSize() / 2, 0);
		}
		setMoveDir(dir);
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

	public GhostState getNextState() {
		GhostState nextState = fnNextState.get();
		return nextState != null ? nextState : getState();
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
	public OptionalInt supplyIntendedDir() {
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
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(GhostState.class, GameEvent.class)
			 
			.description(String.format("[%s]", ghostName))
			.initialState(LOCKED)
		
			.states()

				.state(LOCKED)
					.timeoutAfter(() -> getGame().getGhostLockedTime(this))
					.onTick(() -> {
						move();	
						sprites.select("s_color_" + getMoveDir());
					})
				
				.state(SCATTERING)
					.onTick(() -> {
						move();	
						sprites.select("s_color_" + getMoveDir()); 
					})
			
				.state(CHASING)
					.onEntry(() -> getTheme().snd_ghost_chase().loop())
					.onExit(() -> getTheme().snd_ghost_chase().stop())
					.onTick(() -> {	
						move();	
						sprites.select("s_color_" + getMoveDir()); 
					})
				
				.state(FRIGHTENED)
					.onEntry(() -> {
						getBehavior().computePath(this); 
					})
					.onTick(() -> {
						move();
						sprites.select(inGhostHouse() ? "s_color_" + getMoveDir() : 
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
						sprites.select("s_eyes_" + getMoveDir());
					})
					.onExit(() -> {
						if (getGame().getActiveGhosts().filter(ghost -> ghost != this)
								.noneMatch(ghost -> ghost.getState() == DEAD)) {
							getTheme().snd_ghost_dead().stop();
						}
					})
					
			.transitions()

				.when(LOCKED).then(FRIGHTENED)
					.condition(() -> canLeaveGhostHouse() && isPacManGreedy())

				.when(LOCKED).then(SCATTERING)
					.condition(() -> canLeaveGhostHouse() && getNextState() == SCATTERING)
				
				.when(LOCKED).then(CHASING)
					.condition(() -> canLeaveGhostHouse() && getNextState() == CHASING)
				
				.when(CHASING).then(FRIGHTENED).on(PacManGainsPowerEvent.class)
				.when(CHASING).then(DYING).on(GhostKilledEvent.class)
				.when(CHASING).then(SCATTERING).on(StartScatteringEvent.class)

				.when(SCATTERING).then(FRIGHTENED).on(PacManGainsPowerEvent.class)
				.when(SCATTERING).then(DYING).on(GhostKilledEvent.class)
				.when(SCATTERING).then(CHASING).on(StartChasingEvent.class)
				
				.when(FRIGHTENED).then(CHASING).on(PacManLostPowerEvent.class)
					.condition(() -> getNextState() == CHASING)

				.when(FRIGHTENED).then(SCATTERING).on(PacManLostPowerEvent.class)
					.condition(() -> getNextState() == SCATTERING)
				
				.when(FRIGHTENED).then(DYING).on(GhostKilledEvent.class)
					
				.when(DYING).then(DEAD).onTimeout()
					
				.when(DEAD).then(LOCKED)
					.condition(() -> getTile().equals(getRevivalTile()))
					.act(this::reviveGhost)
				
		.endStateMachine();
		/*@formatter:on*/
	}
}