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

The processing of all used state machines (game control, Pac-Man, ghosts) can be traced separately. If a state machine processes an event and does not find a suitable state transition, a runtime exception is thrown. This helps in filling gaps in the state machine definitions.

Example trace:
```java
Aug 10, 2018 6:10:06 PM de.amr.games.pacman.controller.event.core.EventManager publishEvent
INFORMATION: [GameActorEvents] publishing event 'PacManGhostCollisionEvent(Blinky)'
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer firingTransition
INFORMATION: [GameControl] stays 'PLAYING' on 'PacManGhostCollisionEvent(Blinky)'
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer firingTransition
INFORMATION: [Ghost Pinky] changing from 'HOME' to 'SAFE'
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer exitingState
INFORMATION: [Ghost Pinky] exiting state 'HOME'
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer enteringState
INFORMATION: [Ghost Pinky] entering state 'SAFE'
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer firingTransition
INFORMATION: [GameControl] changing from 'PLAYING' to 'PACMAN_DYING' on 'PacManKilledEvent(Blinky)'
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer exitingState
INFORMATION: [GameControl] exiting state 'PLAYING'
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer firingTransition
INFORMATION: [Pac-Man] changing from 'VULNERABLE' to 'DYING' on 'PacManKilledEvent(Blinky)'
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer exitingState
INFORMATION: [Pac-Man] exiting state 'VULNERABLE'
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer enteringState
INFORMATION: [Pac-Man] entering state 'DYING'
Aug 10, 2018 6:10:06 PM de.amr.games.pacman.controller.GameController$PlayingState onPacManKilled
INFORMATION: PacMan killed by Blinky at (21,23)
Aug 10, 2018 6:10:06 PM de.amr.statemachine.StateMachineTracer enteringState
INFORMATION: [GameControl] entering state 'PACMAN_DYING'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer firingTransition
INFORMATION: [Pac-Man] stays 'DYING'
Aug 10, 2018 6:10:09 PM de.amr.games.pacman.controller.event.core.EventManager publishEvent
INFORMATION: [GameActorEvents] publishing event 'PacManDiedEvent'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer firingTransition
INFORMATION: [GameControl] changing from 'PACMAN_DYING' to 'PLAYING' on 'PacManDiedEvent'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer exitingState
INFORMATION: [GameControl] exiting state 'PACMAN_DYING'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer enteringInitialState
INFORMATION: [Pac-Man] entering initial state 'SAFE'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer enteringInitialState
INFORMATION: [Ghost Blinky] entering initial state 'HOME'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer enteringInitialState
INFORMATION: [Ghost Pinky] entering initial state 'HOME'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer enteringState
INFORMATION: [GameControl] entering state 'PLAYING'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer firingTransition
INFORMATION: [Ghost Blinky] changing from 'HOME' to 'SAFE'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer exitingState
INFORMATION: [Ghost Blinky] exiting state 'HOME'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer enteringState
INFORMATION: [Ghost Blinky] entering state 'SAFE' for 2,00 seconds (120 frames)
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer firingTransition
INFORMATION: [Ghost Pinky] changing from 'HOME' to 'SAFE'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer exitingState
INFORMATION: [Ghost Pinky] exiting state 'HOME'
Aug 10, 2018 6:10:09 PM de.amr.statemachine.StateMachineTracer enteringState
INFORMATION: [Ghost Pinky] entering state 'SAFE' for 2,00 seconds (120 frames)
Aug 10, 2018 6:10:09 PM de.amr.easy.game.Application pause
INFORMATION: Application paused.
```

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


