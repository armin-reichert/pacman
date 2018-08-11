# Pac-Man State Machines (in progress)
A Pac-Man game implementation with an emphasis on state machines. Implements the global game control as well as the Pac-Man and ghost control using explicit state machines in a declarative way.

<img src="doc/pacman.png"/>

To illustrate, this is the game control state machine:

```java
		StateMachine.define(PlayState.class, GameEvent.class)
			
			.description("[GameControl]")
			.initialState(READY)
			
			.states()
			
				.state(READY)
					.impl(new ReadyState())
					.timeoutAfter(game::getReadyTime)
				
				.state(PLAYING)
					.impl(new PlayingState())
				
				.state(CHANGING_LEVEL)
					.impl(new ChangingLevelState())
					.timeoutAfter(game::getLevelChangingTime)
				
				.state(GHOST_DYING)
					.impl(new GhostDyingState())
					.timeoutAfter(game::getGhostDyingTime)
				
				.state(PACMAN_DYING)
					.impl(new PacManDyingState())
				
				.state(GAME_OVER)
					.impl(new GameOverState())
	
			.transitions()
				
				.when(READY).then(PLAYING).onTimeout()
					
				.stay(PLAYING)
					.on(FoodFoundEvent.class)
					.act(e -> playingState().onFoodFound(e))
					
				.stay(PLAYING)
					.on(BonusFoundEvent.class)
					.act(e -> playingState().onBonusFound(e))
					
				.stay(PLAYING)
					.on(PacManGhostCollisionEvent.class)
					.act(e -> playingState().onPacManGhostCollision(e))
					
				.stay(PLAYING)
					.on(PacManGainsPowerEvent.class)
					.act(e -> playingState().onPacManGainsPower(e))
					
				.stay(PLAYING)
					.on(PacManGettingWeakerEvent.class)
					.act(e -> playingState().onPacManGettingWeaker(e))
					
				.stay(PLAYING)
					.on(PacManLostPowerEvent.class)
					.act(e -> playingState().onPacManLostPower(e))
			
				.when(PLAYING).then(GHOST_DYING)
					.on(GhostKilledEvent.class)
					.act(e -> playingState().onGhostKilled(e))
					
				.when(PLAYING).then(PACMAN_DYING)
					.on(PacManKilledEvent.class)
					.act(e -> playingState().onPacManKilled(e))
					
				.when(PLAYING).then(CHANGING_LEVEL)
					.on(LevelCompletedEvent.class)
					
				.when(CHANGING_LEVEL).then(PLAYING)
					.onTimeout()
			
				.stay(GHOST_DYING)
					.on(PacManGettingWeakerEvent.class)
				
				.when(GHOST_DYING).then(PLAYING)
					.onTimeout()
					
				.when(PACMAN_DYING).then(GAME_OVER)
					.on(PacManDiedEvent.class)
					.condition(() -> game.livesRemaining == 0)
					
				.when(PACMAN_DYING).then(PLAYING)
					.on(PacManDiedEvent.class)
					.condition(() -> game.livesRemaining > 0)
					.act(() -> actors.init())
			
				.when(GAME_OVER).then(READY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
							
		.endStateMachine();

```

The states of this state machine are implemented as separate (inner) classes. However, this is not necessary in simpler cases and is the decision of the implementor.

Pac-Man's state machine looks like this:

```java
		StateMachine.define(State.class, GameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(SAFE)

			.states()

				.state(SAFE)
					.onEntry(this::initPacMan)
					.timeoutAfter(() -> game.sec(0.25f))

				.state(VULNERABLE)
					.onTick(this::inspectMaze)
					
				.state(STEROIDS)
					.onTick(() -> {	inspectMaze(); checkHealth(); })
					.timeoutAfter(game::getPacManSteroidTime)

				.state(DYING)
					.onEntry(() -> s_current = s_dying)
					.timeoutAfter(() -> game.sec(2))

			.transitions()

					.when(SAFE).then(VULNERABLE).onTimeout()
					
					.when(VULNERABLE).then(DYING).on(PacManKilledEvent.class)
	
					.when(VULNERABLE).then(STEROIDS).on(PacManGainsPowerEvent.class)
	
					.when(STEROIDS).on(PacManGainsPowerEvent.class).act(() -> brain.resetTimer())
	
					.when(STEROIDS).then(VULNERABLE).onTimeout().act(() -> events.publishEvent(new PacManLostPowerEvent()))
	
					.when(DYING).onTimeout().act(e -> events.publishEvent(new PacManDiedEvent()))

		.endStateMachine();
```

The processing of all used state machines (game control, Pac-Man, ghosts) can be traced separately. If a state machine processes an event and does not find a suitable state transition, a runtime exception is thrown. This helps in filling gaps in the state machine definitions.

Example trace:
```java
[2018-08-11 10:58:47:816] [INFORMATION] Application PacManApp created. 
[2018-08-11 10:58:48:004] [INFORMATION] Application shell created. 
[2018-08-11 10:58:48:082] [INFORMATION] Window-mode: 448x576 
[2018-08-11 10:58:48:082] [INFORMATION] Default view initialized. 
[2018-08-11 10:58:48:503] [INFORMATION] Pac-Man sprite images extracted 
[2018-08-11 10:58:48:660] [INFORMATION] Set controller to: de.amr.games.pacman.controller.GameController@6b32d068 
[2018-08-11 10:58:48:664] [INFORMATION] [Ghost Blinky] entering initial state 'HOME' 
[2018-08-11 10:58:48:665] [INFORMATION] [GameControl] entering initial state 'READY' 
[2018-08-11 10:58:48:665] [INFORMATION] [Pac-Man] entering initial state 'SAFE' 
[2018-08-11 10:58:48:665] [INFORMATION] [Ghost Blinky] entering initial state 'HOME' 
[2018-08-11 10:58:48:665] [INFORMATION] Initialized controller: de.amr.games.pacman.controller.GameController@6b32d068 
[2018-08-11 10:58:48:665] [INFORMATION] Application initialized. 
[2018-08-11 10:58:48:665] [INFORMATION] Pulse started. 
[2018-08-11 10:58:50:742] [INFORMATION] [GameControl] changing from 'READY' to 'PLAYING' 
[2018-08-11 10:58:50:742] [INFORMATION] [GameControl] exiting state 'READY' 
[2018-08-11 10:58:50:745] [INFORMATION] [GameControl] entering state 'PLAYING' 
[2018-08-11 10:58:50:759] [INFORMATION] [Ghost Blinky] changing from 'HOME' to 'SAFE' 
[2018-08-11 10:58:50:759] [INFORMATION] [Ghost Blinky] exiting state 'HOME' 
[2018-08-11 10:58:50:759] [INFORMATION] [Ghost Blinky] entering state 'SAFE' 
[2018-08-11 10:58:50:775] [INFORMATION] [Pac-Man] changing from 'SAFE' to 'VULNERABLE' 
[2018-08-11 10:58:50:775] [INFORMATION] [Pac-Man] exiting state 'SAFE' 
[2018-08-11 10:58:50:775] [INFORMATION] [Pac-Man] entering state 'VULNERABLE' 
[2018-08-11 10:58:51:013] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:013] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:202] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:202] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:372] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:372] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:544] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:544] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:732] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:732] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:906] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:51:906] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:095] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:095] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:198] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:198] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:374] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:374] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:561] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:561] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:665] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:665] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:836] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:836] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:52:836] [INFORMATION] [Ghost Blinky] changing from 'SAFE' to 'AGGRO' 
[2018-08-11 10:58:52:836] [INFORMATION] [Ghost Blinky] exiting state 'SAFE' 
[2018-08-11 10:58:52:836] [INFORMATION] [Ghost Blinky] entering state 'AGGRO' 
[2018-08-11 10:58:53:027] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:027] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:128] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:128] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:325] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:325] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:499] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:499] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:603] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:603] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:774] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Energizer)' 
[2018-08-11 10:58:53:774] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Energizer)' 
[2018-08-11 10:58:53:792] [INFORMATION] [GameControl] stays 'PLAYING' on 'PacManGainsPowerEvent' 
[2018-08-11 10:58:53:792] [INFORMATION] [Pac-Man] changing from 'VULNERABLE' to 'STEROIDS' on 'PacManGainsPowerEvent' 
[2018-08-11 10:58:53:792] [INFORMATION] [Pac-Man] exiting state 'VULNERABLE' 
[2018-08-11 10:58:53:792] [INFORMATION] [Pac-Man] entering state 'STEROIDS' 
[2018-08-11 10:58:53:792] [INFORMATION] [Ghost Blinky] changing from 'AGGRO' to 'AFRAID' on 'PacManGainsPowerEvent' 
[2018-08-11 10:58:53:792] [INFORMATION] [Ghost Blinky] exiting state 'AGGRO' 
[2018-08-11 10:58:53:792] [INFORMATION] [Ghost Blinky] entering state 'AFRAID' 
[2018-08-11 10:58:53:949] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:53:949] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:109] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:109] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:284] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:284] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:387] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:387] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:544] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:544] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:717] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:717] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:872] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:54:872] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:55:027] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:55:027] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:55:195] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:55:195] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:55:349] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:55:349] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:55:503] [INFORMATION] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 10:58:55:503] [INFORMATION] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 10:58:56:879] [INFORMATION] [GameActorEvents] publishing event 'PacManGettingWeakerEvent' 
[2018-08-11 10:58:56:879] [INFORMATION] [GameControl] stays 'PLAYING' on 'PacManGettingWeakerEvent' 
[2018-08-11 10:58:56:881] [INFORMATION] [Ghost Blinky] stays 'AFRAID' on 'PacManGettingWeakerEvent' 
[2018-08-11 10:58:59:924] [INFORMATION] [Pac-Man] changing from 'STEROIDS' to 'VULNERABLE' 
[2018-08-11 10:58:59:924] [INFORMATION] [Pac-Man] exiting state 'STEROIDS' 
[2018-08-11 10:58:59:926] [INFORMATION] [GameActorEvents] publishing event 'PacManLostPowerEvent' 
[2018-08-11 10:58:59:927] [INFORMATION] [GameControl] stays 'PLAYING' on 'PacManLostPowerEvent' 
[2018-08-11 10:58:59:927] [INFORMATION] [Ghost Blinky] changing from 'AFRAID' to 'AGGRO' on 'PacManLostPowerEvent' 
[2018-08-11 10:58:59:927] [INFORMATION] [Ghost Blinky] exiting state 'AFRAID' 
[2018-08-11 10:58:59:927] [INFORMATION] [Ghost Blinky] entering state 'AGGRO' 
[2018-08-11 10:58:59:927] [INFORMATION] [Pac-Man] entering state 'VULNERABLE' 
[2018-08-11 10:59:02:260] [INFORMATION] [GameActorEvents] publishing event 'PacManGhostCollisionEvent(Blinky)' 
[2018-08-11 10:59:02:260] [INFORMATION] [GameControl] stays 'PLAYING' on 'PacManGhostCollisionEvent(Blinky)' 
[2018-08-11 10:59:02:277] [INFORMATION] [GameControl] changing from 'PLAYING' to 'PACMAN_DYING' on 'PacManKilledEvent(Blinky)' 
[2018-08-11 10:59:02:277] [INFORMATION] [GameControl] exiting state 'PLAYING' 
[2018-08-11 10:59:02:277] [INFORMATION] [Pac-Man] changing from 'VULNERABLE' to 'DYING' on 'PacManKilledEvent(Blinky)' 
[2018-08-11 10:59:02:277] [INFORMATION] [Pac-Man] exiting state 'VULNERABLE' 
[2018-08-11 10:59:02:277] [INFORMATION] [Pac-Man] entering state 'DYING' 
[2018-08-11 10:59:02:277] [INFORMATION] PacMan killed by Blinky at (12,11) 
[2018-08-11 10:59:02:277] [INFORMATION] [GameControl] entering state 'PACMAN_DYING' 
[2018-08-11 10:59:04:317] [INFORMATION] [Pac-Man] stays 'DYING' 
[2018-08-11 10:59:04:317] [INFORMATION] [GameActorEvents] publishing event 'PacManDiedEvent' 
[2018-08-11 10:59:04:319] [INFORMATION] [GameControl] changing from 'PACMAN_DYING' to 'PLAYING' on 'PacManDiedEvent' 
[2018-08-11 10:59:04:320] [INFORMATION] [GameControl] exiting state 'PACMAN_DYING' 
[2018-08-11 10:59:04:321] [INFORMATION] [Pac-Man] entering initial state 'SAFE' 
[2018-08-11 10:59:04:321] [INFORMATION] [Ghost Blinky] entering initial state 'HOME' 
[2018-08-11 10:59:04:321] [INFORMATION] [GameControl] entering state 'PLAYING' 
[2018-08-11 10:59:04:334] [INFORMATION] [Ghost Blinky] changing from 'HOME' to 'SAFE' 
[2018-08-11 10:59:04:334] [INFORMATION] [Ghost Blinky] exiting state 'HOME' 
[2018-08-11 10:59:04:334] [INFORMATION] [Ghost Blinky] entering state 'SAFE' for 2,00 seconds (120 frames) 
[2018-08-11 10:59:04:351] [INFORMATION] [Pac-Man] changing from 'SAFE' to 'VULNERABLE' 
[2018-08-11 10:59:04:351] [INFORMATION] [Pac-Man] exiting state 'SAFE' 
[2018-08-11 10:59:04:351] [INFORMATION] [Pac-Man] entering state 'VULNERABLE' 
[2018-08-11 10:59:06:386] [INFORMATION] [Ghost Blinky] changing from 'SAFE' to 'AGGRO' 
[2018-08-11 10:59:06:386] [INFORMATION] [Ghost Blinky] exiting state 'SAFE' 
[2018-08-11 10:59:06:387] [INFORMATION] [Ghost Blinky] entering state 'AGGRO' ```

Other features:
- Entity states and timers can be shown at runtime
- Entity routes can be shown at runtime
- Configurable entity navigation behaviour

Example:

Blinky's navigation behaviour is defined as follows:
```java
Ghost ghost = new Ghost(Ghosts.Blinky, pacMan, game, game.maze.blinkyHome, Top4.E, RED_GHOST);
ghost.setNavigation(Ghost.State.AGGRO, chase(pacMan));
ghost.setNavigation(Ghost.State.AFRAID, flee(pacMan));
ghost.setNavigation(Ghost.State.DEAD, goHome());
ghost.setNavigation(Ghost.State.SAFE, bounce());
```

The individual behaviours are implemented as simple classes implementing a common interface. Behaviours which need to compute routes in the maze can just call the method **Maze.findPath(Tile source, Tile target)** which runs the A* path finder on the underlying grid graph (which is completely useless for this kind of graph but sounds better than BFS :-).

```java
public List<Tile> findPath(Tile source, Tile target) {
	if (isValidTile(source) && isValidTile(target)) {
		GraphTraversal pathfinder = new AStarTraversal<>(graph, edge -> 1, graph::manhattan);
//		GraphTraversal pathfinder = new BreadthFirstTraversal<>(graph);
		pathfinder.traverseGraph(cell(source), cell(target));
		return pathfinder.path(cell(target)).stream().map(this::tile).collect(Collectors.toList());
	}
	return Collections.emptyList();
}
```
