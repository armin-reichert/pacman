# A hopefully comprehensible Pac-Man implementation using finite-state machines        



[![Pac-Man](https://img.youtube.com/vi/NF8ynftis_U/0.jpg)](https://www.youtube.com/watch?v=NF8ynftis_U)

## Pac-Man? Really? How uncool!

For the average school kid of today, a retro game like Pac-Man probably feels like the most boring and uncool thing you 
can deal with. Nevertheless, also a seemingly simple game like Pac-Man can be very instructive!

My personal fascination for "Pac-Man" comes from the fact that the single computer game I played regularly was  ["Snack Attack"](https://www.youtube.com/watch?v=ivAZkuBbpsM), running on my Apple II+ clone in 1984, no color monitor, but what a sound!.
  
## The challenge
Implementing Pac-Man is challenging not because of the core game functionality like implementing a game loop, updating and drawing entities, handling collisions etc. but for others reasons:

First, implementing a good representation of the maze and the correct movement of the game characters 
through the maze are not trivial. Pac-Man's movement direction is controlled by the keyboard and the intended move direction can be selected already before Pac-Man actually can turn to that direction. 

After having implemented this correctly, the next challenge is the logic and the control of the game itself. You have to sort out the different states of the game and the actors, you have to understand how the user interface should behave depending on the current state and which game "events" lead from one state to the other (state transitions).

Maybe you will start with a single ghost and implement its behavior: waiting (bouncing) in the ghost house, leaving the house to chase Pac-Man or scattering out to the ghost's maze corner. What should happen when Pac-Man and a ghost are colliding? 
Which part of your program should coordinate this? Should the code be distributed over the actors or should you have 
some kind of mediator, some central game control? Where should the game rules (points, lives, levels etc.) be implemented? 
Should this be placed in some kind of *model* (in the sense of the Model-View-Controller pattern)?

I looked into existing code, for example [here](https://github.com/leonardo-ono/Java2DPacmanGame) or [here](https://github.com/yichen0831/Pacman_libGdx) or [here](https://github.com/urossss/Pac-Man) which I find not bad at all. But I wanted something different, namely an implementation where you can directly see the underlying state machines.

## State machines

There are many possibilities of implementing *finite state machines* in software: from basic switch-statements, function pointers (C, C++) to object-oriented "state pattern"-based implementations. There are also ready-to-use libraries like [Appcelerate](http://www.appccelerate.com/), [Stateless4j](https://github.com/oxo42/stateless4j) or [Squirrel](http://hekailiang.github.io/squirrel/). What should you do? 

The low-level implementations using switch-statements or function pointers (if your programming language supports this) are the most performant ones but as long a you achieve the performance goals for your game (60 frames/updates per second) you can use whatever you like. Of course, using  a higher-level implementation should make your code more readable and easier to maintain.

I decided to write my own [state machine implementation](https://github.com/armin-reichert/statemachine), which was a good exercise and really fun because of the availability of lambda expressions and method references.

After you have decided which implementation you want to use for your state machines you can finally focus on the game itself.

Which entities in the Pac-Man game are candidates for getting controlled by state machines?

Of course, Pac-Man and the four ghosts, but also the global game control, maybe also the screen selection logic or even simpler entities in your game. It is interesting to look at your program parts through the state machine glasses and find out where an explicit state machine becomes useful.

In the provided implementation, there are a number of explicit state machines:
- Intro screen controller ([IntroView](PacManGame/src/main/java/de/amr/games/pacman/view/intro/IntroView.java))
- Global game controller ([PacManGameController](PacManGame/src/main/java/de/amr/games/pacman/controller/PacManGameController.java))
- Ghost attack controller ([GhostAttackController](PacManGame/src/main/java/de/amr/games/pacman/controller/GhostAttackController.java))
- Pac-Man controller ([Pac-Man](PacManGame/src/main/java/de/amr/games/pacman/actor/PacMan.java))
- Ghost controller ([Ghost](PacManGame/src/main/java/de/amr/games/pacman/actor/Ghost.java))

All these state machines are "implemented" in a declarative way (*builder pattern*). In essence, you write a single 
large Java expression representing the complete state graph together with node and edge annotations representing actions,
 conditions, event conditions and timers.

Lambda expressions (anonymous functions) and function references allow to embed code directly inside the state machine 
definition. However, if the code becomes more complex it is of course possible to delegate to separate methods or 
classes. Both variants are used here.

## State machines in action

Sounds all well and nice, but how does that look in the real code? 

The **intro screen** shows different animations that have to be coordinated using timers and stop conditions. This
is an obvious candidate for using a state machine. The state machine only uses timers, so we can specify
type *Void* as event type. The states are identified by an enumeration type:

```java
beginStateMachine()
	.description("[Intro]")
	.initialState(LOGO_SCROLLING_IN)
	.states()

		.state(LOGO_SCROLLING_IN)
			.onEntry(() -> { show(logo); logo.startAnimation(); })
			.onExit(() -> logo.stopAnimation())

		.state(CHASING_EACH_OTHER)
			// Show ghosts chasing Pac-Man and vice-versa
			.onEntry(() -> {
				show(chasePacMan, chaseGhosts);
				start(chasePacMan, chaseGhosts);
			})
			.onExit(() -> {
				stop(chasePacMan, chaseGhosts);
				chasePacMan.tf.centerX(width);
			})

		.state(READY_TO_PLAY)
			// Show ghost points animation and blinking text
			.timeoutAfter(() -> app().clock.sec(6))
			.onEntry(() -> {
				show(ghostPoints, pressSpace, f11Hint, speedHint[0], speedHint[1], speedHint[2], visitGitHub);
				ghostPoints.startAnimation();
			})
			.onExit(() -> {
				ghostPoints.stopAnimation();
				hide(ghostPoints, pressSpace);
			})

		.state(LEAVING_INTRO)

	.transitions()

		.when(LOGO_SCROLLING_IN).then(CHASING_EACH_OTHER)
			.condition(() -> logo.isAnimationCompleted())

		.when(CHASING_EACH_OTHER).then(READY_TO_PLAY)
			.condition(() -> chasePacMan.isAnimationCompleted() && chaseGhosts.isAnimationCompleted())

		.when(READY_TO_PLAY).then(CHASING_EACH_OTHER)
			.onTimeout()

		.when(READY_TO_PLAY).then(LEAVING_INTRO)
			.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

.endStateMachine();
```

A more complex state machine is used for defining the **global game control**. It processes game events which
are created during the game play, for example when Pac-Man finds food or meets ghosts. Also the different
game states like changing the level or the dying animations of Pac-Man and the ghosts are controlled by this
state machine. Further, the individual states are implemented by subclasses of the generic state class. This
has the advantage that actions which are state-specific can be realized as methods of the subclass.

```java
beginStateMachine()

	.description("[GameController]")
	.initialState(INTRO)

	.states()

		.state(INTRO)
			.onEntry(() -> {
				showIntroView();
				theme.snd_insertCoin().play();
				theme.loadMusic();
			})

		.state(READY)
			.impl(new ReadyState())

		.state(PLAYING)
			.impl(playingState = new PlayingState())

		.state(CHANGING_LEVEL)
			.impl(new ChangingLevelState())

		.state(GHOST_DYING)
			.impl(new GhostDyingState())
			.timeoutAfter(game::getGhostDyingTime)

		.state(PACMAN_DYING)
			.impl(new PacManDyingState())

		.state(GAME_OVER)
			.impl(new GameOverState())
			.timeoutAfter(() -> app().clock.sec(60))

	.transitions()

		.when(INTRO).then(READY)
			.condition(() -> introView.isComplete())
			.act(() -> showPlayView())

		.when(READY).then(PLAYING)
			.onTimeout()
			.act(() -> playingState.setInitialWaitTimer(app().clock.sec(1.7f)))

		.stay(PLAYING)
			.on(FoodFoundEvent.class)
			.act(playingState::onFoodFound)

		.stay(PLAYING)
			.on(BonusFoundEvent.class)
			.act(playingState::onBonusFound)

		.stay(PLAYING)
			.on(PacManGhostCollisionEvent.class)
			.act(playingState::onPacManGhostCollision)

		.stay(PLAYING)
			.on(PacManGainsPowerEvent.class)
			.act(playingState::onPacManGainsPower)

		.stay(PLAYING)
			.on(PacManGettingWeakerEvent.class)
			.act(playingState::onPacManGettingWeaker)

		.stay(PLAYING)
			.on(PacManLostPowerEvent.class)
			.act(playingState::onPacManLostPower)

		.when(PLAYING).then(GHOST_DYING)
			.on(GhostKilledEvent.class)
			.act(playingState::onGhostKilled)

		.when(PLAYING).then(PACMAN_DYING)
			.on(PacManKilledEvent.class)
			.act(playingState::onPacManKilled)

		.when(PLAYING).then(CHANGING_LEVEL)
			.on(LevelCompletedEvent.class)

		.when(CHANGING_LEVEL).then(PLAYING)
			.onTimeout()

		.stay(CHANGING_LEVEL)
			.on(PacManGettingWeakerEvent.class)

		.stay(CHANGING_LEVEL)
			.on(PacManLostPowerEvent.class)

		.stay(GHOST_DYING)
			.on(PacManGettingWeakerEvent.class)

		.stay(GHOST_DYING)
			.on(PacManLostPowerEvent.class)

		.when(GHOST_DYING).then(PLAYING)
			.onTimeout()

		.when(PACMAN_DYING).then(GAME_OVER)
			.condition(() -> game.pacMan.isDead() && game.getLives() == 0)

		.when(PACMAN_DYING).then(PLAYING)
			.condition(() -> game.pacMan.isDead() && game.getLives() > 0)
			.act(() -> {
				game.activeActors().forEach(MazeMover::init);
				playView.init();
				playingState.setInitialWaitTimer(app().clock.sec(1.7f));
			})

		.when(GAME_OVER).then(READY)
			.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

		.when(GAME_OVER).then(INTRO)
			.onTimeout()

.endStateMachine();
```

The states of the game controller are implemented as inner classes inheriting from the generic state class. This offers
the possibility to add fields and methods to the state class, in simpler use cases, the base class is sufficient.

The **ghost attack waves** (scattering, chasing) and their timing are implemented by the following state machine:

```java
public class GhostAttackController extends StateMachine<GhostState, Void> {
...
	public GhostAttackController(PacManGame game) {
		super(GhostState.class);
		this.game = game;
		/*@formatter:off*/
		beginStateMachine()
			.description("[GhostAttackTimer]")
			.initialState(SCATTERING)
		.states()
			.state(SCATTERING)
				.timeoutAfter(this::getScatteringDuration)
			.state(CHASING)
				.timeoutAfter(this::getChasingDuration)
				.onExit(this::nextRound)
		.transitions()
			.when(SCATTERING).then(CHASING).onTimeout()
			.when(CHASING).then(SCATTERING).onTimeout()
		.endStateMachine();
		/*@formatter:on*/
	}
}
```

**Pac-Man** is controlled by the following state machine:

```java
beginStateMachine(PacManState.class, PacManGameEvent.class)

	.description("[Pac-Man]")
	.initialState(HOME)

	.states()

		.state(HOME)
			.timeoutAfter(() -> 0)

		.state(HUNGRY)
			.impl(new HungryState())

		.state(POWER)
			.impl(new PowerState())
			.timeoutAfter(game::getPacManPowerTime)

		.state(DYING)
			.impl(new DyingState())

	.transitions()

		.when(HOME).then(HUNGRY).onTimeout()

		.when(HUNGRY).then(DYING)
			.on(PacManKilledEvent.class)

		.when(HUNGRY).then(POWER)
			.on(PacManGainsPowerEvent.class)

		.stay(POWER)
			.on(PacManGainsPowerEvent.class)
			.act(() -> fsm.resetTimer())

		.when(POWER).then(HUNGRY)
			.onTimeout()
			.act(() -> publishEvent(new PacManLostPowerEvent()))

		.when(DYING).then(DEAD)
			.onTimeout()

.endStateMachine();
```

The **ghosts** are controlled using the following state machine:

```java
beginStateMachine(GhostState.class, PacManGameEvent.class)

	.description(String.format("[%s]", name))
	.initialState(LOCKED)

	.states()

		.state(LOCKED)
			.onTick(() -> walkAndAppearAs("color-" + moveDir))
			.onExit(() -> {
				enteredNewTile = true;
				game.pacMan.ticksSinceLastMeal = 0;
			})

		.state(LEAVING_HOUSE)
			.onEntry(() -> targetTile = maze.blinkyHome)
			.onTick(() -> walkAndAppearAs("color-" + moveDir))
			.onExit(() -> moveDir = nextDir = Top4.W)

		.state(ENTERING_HOUSE)
			.onEntry(() -> targetTile = revivalTile)
			.onTick(() -> walkAndAppearAs("eyes-" + moveDir))

		.state(SCATTERING)
			.onEntry(() -> targetTile = scatterTile)
			.onTick(() -> walkAndAppearAs("color-" + moveDir))

		.state(CHASING)
			.onEntry(() -> chasingSoundOn())
			.onTick(() -> {
				targetTile = fnChasingTarget.get();
				walkAndAppearAs("color-" + moveDir);
			})
			.onExit(this::chasingSoundOff)

		.state(FRIGHTENED)
			.onTick(() -> walkAndAppearAs(game.pacMan.isLosingPower()	? "flashing" : "frightened"))

		.state(DYING)
			.timeoutAfter(Ghost::getDyingTime)
			.onEntry(() -> sprites.select("value-" + game.numGhostsKilledByCurrentEnergizer()))
			.onExit(game::addGhostKilled)

		.state(DEAD)
			.onEntry(() -> {
				targetTile = maze.blinkyHome;
				deadSoundOn();
			})
			.onTick(() -> walkAndAppearAs("eyes-" + moveDir))
			.onExit(this::deadSoundOff)

	.transitions()

		.when(LOCKED).then(LEAVING_HOUSE)
			.condition(this::unlocked)

		.when(LEAVING_HOUSE).then(FRIGHTENED)
			.condition(() -> leftHouse() && game.pacMan.hasPower())

		.when(LEAVING_HOUSE).then(SCATTERING)
			.condition(() -> leftHouse() && nextState() == SCATTERING)

		.when(LEAVING_HOUSE).then(CHASING)
			.condition(() -> leftHouse() && nextState() == CHASING)

		.when(ENTERING_HOUSE).then(LOCKED)
			.condition(() -> currentTile() == targetTile)

		.when(CHASING).then(FRIGHTENED)
			.on(PacManGainsPowerEvent.class)
			.act(this::turnBack)

		.when(CHASING).then(DYING)
			.on(GhostKilledEvent.class)

		.when(CHASING).then(SCATTERING)
			.on(StartScatteringEvent.class)
			.act(this::turnBack)

		.when(SCATTERING).then(FRIGHTENED)
			.on(PacManGainsPowerEvent.class)
			.act(this::turnBack)

		.when(SCATTERING).then(DYING)
			.on(GhostKilledEvent.class)

		.when(SCATTERING).then(CHASING)
			.on(StartChasingEvent.class)
			.act(this::turnBack)

		.when(FRIGHTENED).then(CHASING)
			.on(PacManLostPowerEvent.class)
			.condition(() -> nextState() == CHASING)

		.when(FRIGHTENED).then(SCATTERING)
			.on(PacManLostPowerEvent.class)
			.condition(() -> nextState() == SCATTERING)

		.when(FRIGHTENED).then(DYING)
			.on(GhostKilledEvent.class)

		.when(DYING).then(DEAD)
			.onTimeout()

		.when(DEAD).then(ENTERING_HOUSE)
			.condition(() -> currentTile().equals(maze.blinkyHome))

.endStateMachine();
```

## Tracing

The processing of all used state machines can be traced. If a state machine processes an event and does not 
find a suitable state transition, a runtime exception is thrown by default. This is very useful for finding 
gaps in the state machine definition in the development stage. Afterwards, this behavior can be changed so
that only a message is logged for unhandled events. This avoids the need for specifying "empty" transitions
for any event that has no effect in the current state. The Ghost's state machine makes use of that feature.

Example trace:

```
[2019-11-15 06:01:06:863] [GhostAttackTimer] entering initial state: 
[2019-11-15 06:01:06:863] [GhostAttackTimer] entering state 'SCATTERING' for 7,00 seconds (420 frames) 
[2019-11-15 06:01:06:863] [Blinky] in state LOCKED could not handle 'StartScatteringEvent' 
[2019-11-15 06:01:06:863] [Pinky] in state LOCKED could not handle 'StartScatteringEvent' 
[2019-11-15 06:01:06:863] [Inky] in state LOCKED could not handle 'StartScatteringEvent' 
[2019-11-15 06:01:06:863] [Clyde] in state LOCKED could not handle 'StartScatteringEvent' 
[2019-11-15 06:01:08:519] [Blinky] changing from 'LOCKED' to 'SCATTERING' 
[2019-11-15 06:01:08:519] [Blinky] exiting state 'LOCKED' 
[2019-11-15 06:01:08:519] [Blinky] entering state 'SCATTERING' 
[2019-11-15 06:01:08:519] [Pinky] changing from 'LOCKED' to 'SCATTERING' 
[2019-11-15 06:01:08:519] [Pinky] exiting state 'LOCKED' 
[2019-11-15 06:01:08:519] [Pinky] entering state 'SCATTERING' 
[2019-11-15 06:01:08:519] [Inky] changing from 'LOCKED' to 'SCATTERING' 
[2019-11-15 06:01:08:519] [Inky] exiting state 'LOCKED' 
[2019-11-15 06:01:08:519] [Inky] entering state 'SCATTERING' 
[2019-11-15 06:01:08:519] [Clyde] changing from 'LOCKED' to 'SCATTERING' 
[2019-11-15 06:01:08:519] [Clyde] exiting state 'LOCKED' 
[2019-11-15 06:01:08:519] [Clyde] entering state 'SCATTERING' 
[2019-11-15 06:01:08:519] [Pac-Man] changing from 'HOME' to 'HUNGRY (timeout)' 
[2019-11-15 06:01:08:520] [Pac-Man] exiting state 'HOME' 
[2019-11-15 06:01:08:520] [Pac-Man] entering state 'HUNGRY' 
[2019-11-15 06:01:11:057] Pac-Man reports 'PacManGhostCollisionEvent(Inky)' 
[2019-11-15 06:01:11:057] [GameController] stays 'PLAYING' on 'PacManGhostCollisionEvent(Inky)' 
[2019-11-15 06:01:11:074] [GameController] changing from 'PLAYING' to 'PACMAN_DYING' on 'PacManKilledEvent(Inky)' 
[2019-11-15 06:01:11:074] [GameController] exiting state 'PLAYING' 
[2019-11-15 06:01:11:074] PacMan killed by Inky at (21,26,'%') 
[2019-11-15 06:01:11:074] [Pac-Man] changing from 'HUNGRY' to 'DYING' on 'PacManKilledEvent(Inky)' 
[2019-11-15 06:01:11:074] [Pac-Man] exiting state 'HUNGRY' 
[2019-11-15 06:01:11:074] [Pac-Man] entering state 'DYING' for 3,00 seconds (180 frames) 
[2019-11-15 06:01:11:075] [GameController] entering state 'PACMAN_DYING' 
[2019-11-15 06:01:14:871] [Pac-Man] changing from 'DYING' to 'DEAD (timeout)' 
[2019-11-15 06:01:14:871] [Pac-Man] exiting state 'DYING' 
[2019-11-15 06:01:14:871] [Pac-Man] entering state 'DEAD' 
[2019-11-15 06:01:14:888] [GameController] changing from 'PACMAN_DYING' to 'GAME_OVER' 
[2019-11-15 06:01:14:888] [GameController] exiting state 'PACMAN_DYING' 
[2019-11-15 06:01:14:888] [GameController] entering state 'GAME_OVER' for 60,00 seconds (3600 frames) 
```

## Pac-Man movement

Pac-Man's movement by default is controlled by holding a key indicating its intended direction. As soon as Pac-Man reaches a tile where it can move towards this direction it changes its current direction accordingly. "Cornering" is not implemented.

```java
default Steering steeredByKeys(int... keys) {
	return pacMan -> NESW.dirs().filter(dir -> Keyboard.keyDown(keys[dir])).findAny()
			.ifPresent(pacMan::setNextDir);
}

steering = steeredByKeys(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
```

## Defining the ghost behavior ("AI")

The game gets its entertainment factor from the individual *attack behavior* of the ghosts which gives each ghost his unique personality. Instead of creating a separate subclass for each ghost type, each ghost has a map from its states (*locked*, *chasing*, *frightened*, ...) to the corresponding behavior implementation, the current behavior is determined by the current state of the ghost. Different ghost types have different implementations of their *chasing* behavior (*strategy pattern*).

<img src="doc/pacman.png"/>

The ghost behavior only differs for the *chasing* state. The *frightened* behavior has two different implementations and can be toggled for all ghosts at once by pressing the 'f'-key.

### Blinky (the red ghost)

Blinky's chasing behavior is to directly attack Pac-Man:

```java
blinky.fnChasingTarget = pacMan::currentTile;
```

<img src="doc/blinky.png"/>

### Pinky

Pinky, the *ambusher*, heads for the position 4 tiles ahead of Pac-Man's current position. In the original game there is an overflow error leading to a different behavior: when Pac-Man looks upwards, the tile ahead of Pac-Man is falsely computed with an additional number of steps to the west. This behavior is active by default and can be toggled on/off using the 'o'-key.

```java
pinky.fnChasingTarget = () -> pacMan.tilesAhead(4);
```

<img src="doc/pinky.png"/>

### Inky (the cyan ghost)

Inky heads for a position that depends on Blinky's current position and the position two tiles ahead of Pac-Man:

Consider the vector `V` from Blinky's position `B` to the position `P` two tiles ahead of Pac-Man, so `V = (P - B)`. 
Add the doubled vector to Blinky's position: `B + 2 * (P - B) = 2 * P - B` to get Inky's target:

```java
inky.fnChasingTarget = () -> {
	Tile b = blinky.currentTile(), p = pacMan.tilesAhead(2);
	return maze.tileAt(2 * p.col - b.col, 2 * p.row - b.row);
};
```

<img src="doc/inky.png"/>

### Clyde (the orange ghost)

Clyde attacks Pac-Man directly (like Blinky) if his straight line distance from Pac-Man is more than 8 tiles. If closer, he behaves like in scattering mode.

```java
clyde.fnChasingTarget = () -> Vector2f.euclideanDist(clyde.tf.getCenter(), pacMan.tf.getCenter()) > 8
		? pacMan.currentTile()
		: maze.clydeScatter;
```

<img src="doc/clyde.png"/>

The visualization of the attack behaviors can be toggled during the game by pressing the 'r'-key ("show/hide routes").

### Scattering

In *scattering* mode, each ghost tries to reach his "scattering target" which is a tile outside of the maze. Because ghosts
cannot reverse direction this results in a cyclic movement around the walls in the corresponding corner of the maze.

<img src="doc/scattering.png"/>

## Graph-based pathfinding

The original Pac-Man game did not use any graph-based pathfinding. To give an example how graph-based pathfinding could be useful, there is an additional implementation of the *frightened* behavior: when Pac-Man eats a power-pill each frightened ghost choses the "safest" corner to flee to. It computes the shortest path to each corner and selects the one with the largest distance to Pac-Man's current position. Here, the distance of a path from Pac-Man's position is defined as the minimum distance of any tile on the path from Pac-Man's position.

Shortest paths in the maze (grid graph) can be computed using *Maze.findPath(Tile source, Tile target)*. This method runs an [A* search](http://theory.stanford.edu/~amitp/GameProgramming/AStarComparison.html) on the underlying grid graph to compute the shortest path. The used [graph library](https://github.com/armin-reichert/graph) provides a whole number of search algorithms
like BFS, Dijkstra etc. The code to compute a shortest path between two tiles using the A* algorithm with Manhattan distance heuristics looks like this:

```java
GraphSearch pathfinder = new AStarSearch(grid, (u, v) -> 1, grid::manhattan);
Path path = pathfinder.findPath(vertex(source), vertex(target));
```
However, for a maze of such a small size the used algorithm doesn't matter much, a simple breadth-first search would also do the job.

## Additional features

- The following command-line arguments are available (`java -jar pacman.jar arguments...`)
  - Scaling, e.g.: `-scale 2.5`
  - Full-screen mode on start: `-fullScreenOnStart`
  - Full-screen resolution & depth, e.g.: `-fullScreenMode 800,600,32`
  - Window title e.g.: `-title "Pac-Man Game"`
- General
  - CTRL-p pauses/resumes the game
  - F2 opens a dialog where the game loop frequency and (full-)screen resolution can be changed
  - F11 toggles between window and full-screen exclusive mode
- Game 
  - Speed can be changed during game ('1' = normal, '2' = fast, '3' = very fast)
  - 'b', 'p', 'i', 'c' toggles the presence of the 4 ghosts in the game
  - 'f' toggles the ghost's *frightened* behavior between "random" (original) and "select safe corner"
  - 's' toggles the display of actor states and timers
  - 'r' toggles the display of actor routes and target tiles
  - 'g' toggles the display of the grid and the alignment of the actors on the grid
  - 'o' toggles the simulation of the overflow bug from the original game which occurs when Pac-Man is looking upwards
- Cheats
  - ALT-'k' kills all ghosts
  - ALT-'e' eats all normal pellets
  - ALT-'+' switches to the next level
  - ALT-'i' makes Pac-Man immortable
- Logging/tracing
  - Tracing of state machines can be switched on/off (key 'l')

## References

This work would not have been possible without these invaluable sources of information:

- [GameInternals - Understanding Pac-Man Ghost Behavior](http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior)
- [Gamasutra - The Pac-Man Dossier](http://www.gamasutra.com/view/feature/3938/the_pacman_dossier.php)

Just recently I found this excellent video on YouTube:

- [Pac-Man ghost AI explained](https://www.youtube.com/watch?v=ataGotQ7ir8)

## Summary

The goal of this project is to provide a [Pac-Man](https://en.wikipedia.org/wiki/List_of_Pac-Man_video_games) implementation in which the game's inner workings can be understood from the code more easily. The implementation follows the MVC pattern and uses *finite state machines* for the control logic of the actors and the game. The state machines are implemented in a declarative way using the *builder* pattern. 

A home-grown library is used for the basic game infrastructure (active rendering, game loop, full-screen mode, 
keyboard and mouse handling etc.), but it should be not too difficult to implement these parts from scratch or 
use some real game library instead.

It could be useful to further decouple UI, model and controller to enable an easy replacement of the complete UI 
or to implement the state machines using some other state machine library. 

Comments are welcome.

*Armin Reichert, November 2019*
