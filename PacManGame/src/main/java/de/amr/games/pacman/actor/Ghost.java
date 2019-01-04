package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.DEAD;
import static de.amr.games.pacman.actor.GhostState.DYING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.LOCKED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.PacManGame.TS;

import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.StartChasingEvent;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.Behavior;
import de.amr.games.pacman.navigation.GhostBehavior;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends MazeEntity implements GhostBehavior {

	private final PacManGame game;
	private final String name;
	private final StateMachine<GhostState, GameEvent> fsm;
	private final Map<GhostState, Behavior<Ghost>> behaviorMap;
	private final Tile initialTile;
	private final Tile revivalTile;
	private final Tile scatteringTarget;
	private final int initialDir;
	public Supplier<GhostState> fnNextState; // chasing or scattering
	public BooleanSupplier fnCanLeaveHouse;

	public Ghost(PacManGame game, String name, GhostColor color, Tile initialTile, Tile revivalTile,
			Tile scatteringTarget, int initialDir) {
		this.game = game;
		this.name = name;
		this.initialTile = initialTile;
		this.revivalTile = revivalTile;
		this.scatteringTarget = scatteringTarget;
		this.initialDir = initialDir;
		behaviorMap = new EnumMap<>(GhostState.class);
		fsm = buildStateMachine();
		fsm.setIgnoreUnknownEvents(true);
		fsm.traceTo(LOGGER, app().clock::getFrequency);
		fnNextState = this::getState; // default
		fnCanLeaveHouse = () -> getState() != LOCKED || fsm.state().isTerminated();
		setSprites(color);
	}

	@Override
	public Ghost self() {
		return this;
	}

	public void initGhost() {
		placeAtTile(initialTile, TS / 2, 0);
		setMoveDir(initialDir);
		setNextDir(initialDir);
		sprites.select("s_color_" + initialDir);
		sprites.forEach(Sprite::resetAnimation);
	}

	private void reviveGhost() {
		placeAtTile(revivalTile, TS / 2, 0);
		if (this == getGame().getBlinky()) {
			setMoveDir(Top4.N);
			setNextDir(Top4.N);
		} else {
			setMoveDir(initialDir);
			setNextDir(initialDir);
		}
		sprites.select("s_color_" + getMoveDir());
		sprites.forEach(Sprite::resetAnimation);
	}

	// Accessors

	public PacManGame getGame() {
		return game;
	}

	@Override
	public Maze getMaze() {
		return game.getMaze();
	}

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

	public void setBehavior(GhostState state, Behavior<Ghost> behavior) {
		behaviorMap.put(state, behavior);
	}

	public Behavior<Ghost> getBehavior() {
		return behaviorMap.getOrDefault(getState(), keepDirection());
	}

	@Override
	public OptionalInt supplyIntendedDir() {
		return getBehavior().getRoute(this).getDir();
	}

	@Override
	public boolean canEnterTile(Tile tile) {
		if (getMaze().isWall(tile)) {
			return false;
		}
		if (getMaze().isDoor(tile)) {
			return getState() == DEAD || getState() != LOCKED && getMaze().inGhostHouse(getTile());
		}
		return true;
	}

	private boolean canLeaveGhostHouse() {
		return fnCanLeaveHouse.getAsBoolean();
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

	private StateMachine<GhostState, GameEvent> buildStateMachine() {
		/*@formatter:off*/
		return StateMachine.beginStateMachine(GhostState.class, GameEvent.class)
			 
			.description(String.format("[%s]", name))
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
						sprites.select(getGame().getPacMan().isLosingPower() ? "s_flashing" : "s_frightened");
					})
				
				.state(DYING)
					.timeoutAfter(getGame()::getGhostDyingTime)
					.onEntry(() -> {
						sprites.select("s_value" + getGame().getGhostsKilledByEnergizer()); 
						getGame().addGhostKilled();
					})
				
				.state(DEAD)
					.onEntry(() -> {
						getTheme().snd_ghost_dead().loop();
					})
					.onTick(() -> {	
						move();
						sprites.select("s_eyes_" + getMoveDir());
					})
					.onExit(() -> {
						if (getGame().getGhosts().filter(ghost -> ghost != this)
								.noneMatch(ghost -> ghost.getState() == DEAD)) {
							getTheme().snd_ghost_dead().stop();
						}
					})
					
			.transitions()

				.when(LOCKED).then(FRIGHTENED)
					.condition(() -> canLeaveGhostHouse() && getGame().getPacMan().hasPower())

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