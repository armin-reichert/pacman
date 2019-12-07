# A hopefully comprehensible Pac-Man implementation using finite-state machines        

[![Pac-Man](https://img.youtube.com/vi/NF8ynftis_U/0.jpg)](https://www.youtube.com/watch?v=NF8ynftis_U)

## Pac-Man? Really? How uncool!

My personal fascination for "Pac-Man" comes from the fact that the single computer game I played regularly was  ["Snack Attack"](https://www.youtube.com/watch?v=ivAZkuBbpsM), running on my Apple II+ clone in the mid-eighties, on a monochrome monitor, but what a sound came out of the crappy PC speaker! 

But also today, a seemingly simple game like Pac-Man can be very instructive from a programmer's point of view.
  
## The programming challenge
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

All state machines in this implementation are implemented in a declarative way (*builder pattern*). A single large Java expression defines the complete state graph together with node and edge annotations representing the actions, conditions, event conditions and timers. Lambda expressions (anonymous functions) and function references allow to embed code directly inside the state machine definition. If the state definition becomes more complex it is possible to implement it in a separate state class. Both variants are used here.

## State machines in action

Sounds all well and nice, but how does that look in the real code? 

The **start screen** ([IntroView](PacManGame/src/main/java/de/amr/games/pacman/view/intro/IntroView.java)) shows different animations that have to be coordinated using timers and stop conditions. This is an obvious candidate for using a state machine. The state machine only uses timers, so we can use *Void* as event type. The states are identified using an enumeration type.

A more complex state machine is used for implementing the **global game control** ([PacManGameController](PacManGame/src/main/java/de/amr/games/pacman/controller/PacManGameController.java)). It processes game events which
are created during the game play, for example when Pac-Man finds food or meets ghosts. Also the different
game states like changing the level or the dying animations of Pac-Man and the ghosts are controlled by this
state machine. Further, the more complex states are implemented as subclasses of the generic `State` class. This
has the advantage that actions which are state-specific can be realized as methods of the state subclass.

The **ghost motion waves** (scattering, chasing) with their level-specific timing are realized by the following state machine:

See [GhostMotionTimer](PacManGame/src/main/java/de/amr/games/pacman/controller/GhostMotionTimer.java)

```java
beginStateMachine()
	.description("[GhostMotionTimer]")
	.initialState(SCATTERING)
.states()
	.state(SCATTERING)
		.timeoutAfter(() -> game.level.scatterTicks(round))
		.onEntry(this::logStateEntry)
	.state(CHASING)
		.timeoutAfter(() -> game.level.chasingTicks(round))
		.onEntry(this::logStateEntry)
		.onExit(() -> ++round)
.transitions()
	.when(SCATTERING).then(CHASING).onTimeout()
	.when(CHASING).then(SCATTERING).onTimeout()
.endStateMachine();
```

The actors in this implementation are also controlled by finite-state machines:

**Pac-Man** ([Pac-Man](PacManGame/src/main/java/de/amr/games/pacman/actor/PacMan.java))

The **ghosts** ([Ghost](PacManGame/src/main/java/de/amr/games/pacman/actor/Ghost.java))

Even a simple entity like the **bonus symbol** which appears at dedicated scores uses a finite-state machine to implement its lifecycle:

```java
beginStateMachine(BonusState.class, PacManGameEvent.class)
		.description("[Bonus]")
		.initialState(ACTIVE)
		.states()
			.state(ACTIVE)
				.timeoutAfter(activeTime)
				.onEntry(() -> {
					sprites.set("symbol", theme.spr_bonusSymbol(symbol));
					sprites.select("symbol");
				})
			.state(CONSUMED)
				.timeoutAfter(consumedTime)
				.onEntry(() -> {
					sprites.set("number", theme.spr_pinkNumber(pointsIndex(value)));
					sprites.select("number");
				})
			.state(INACTIVE)
				.onEntry(cast::clearBonus)
		.transitions()
			.when(ACTIVE).then(CONSUMED).on(BonusFoundEvent.class)
			.when(ACTIVE).then(INACTIVE).onTimeout()
			.when(CONSUMED).then(INACTIVE).onTimeout()
.endStateMachine();
```

## Tracing

The processing of all used state machines can be traced. If a state machine processes an event and does not 
find a suitable state transition, a runtime exception is thrown by default. This is very useful for finding 
gaps in the state machine definition in the development stage. Afterwards, this behavior can be changed so
that only a message is logged for unhandled events. This avoids the need for specifying "empty" transitions
for any event that has no effect in the current state. The Ghost's state machine makes use of that feature.

## Pac-Man steering

Pac-Man is steered by holding a key indicating its **intended** direction. As soon as Pac-Man reaches a tile where it can move towards this direction it changes its move direction accordingly. ("Cornering" is not yet implemented). In the code, this is implemented by setting the steering function as shown below. This makes it very easy to replace the manual steering by some sort of automatic steering ("AI"):

```java
pacMan = new PacMan(game);
pacMan.steering = steeredByKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT);


Steering<PacMan> steeredByKeys(int... keys) {
	return pacMan -> NESW.dirs()
		.filter(dir -> Keyboard.keyDown(keys[dir]))
		.findAny()
		.ifPresent(pacMan::setNextDir);
}
```

## Ghost steering ("AI")

What makes the game so entertaining is the individual behavior of each ghost when chasing Pac-Man. The red ghost (Blinky) attacks Pac-Man directly, the pink ghost (Pinky) tries to ambush Pac-Man, the orange ghost (Clyde) either attacks directly or rejects, depending on its distance to Pac-Man, and finally the pink ghost (Pinky) uses Blinky's current position to get in Pac-Man's way. 

To realize these different ghost behaviors each ghost has a map of functions mapping each state (*scattering*, *chasing*, *frightened*, ...) to the corresponding behavior implementation. In terms of OO design patterns, one could call this a *strategy pattern*. 

<img src="doc/pacman.png"/>

The ghost behavior only differs for the *chasing* state namely in the logic for calculating the target tile. Beside the different target tiles, the ghost behavior is equal. Each ghost uses the same algorithm to calculate the next move direction to take for reaching the target tile as described in the references given at the end of this article.

The *frightened* behavior has two different implementations (just as a demonstration how the behavior can be exchanged during the game) and can be toggled for all ghosts at once by pressing the 'f'-key.

### Blinky (the red ghost)

Blinky's chasing behavior is to directly attack Pac-Man:

```java
blinky = new Ghost("Blinky", game);
blinky.initialDir = Top4.W;
blinky.initialTile = game.maze.blinkyHome;
blinky.scatterTile = game.maze.blinkyScatter;
blinky.revivalTile = game.maze.pinkyHome;
blinky.fnChasingTarget = pacMan::tile;
```

<img src="doc/blinky.png"/>

### Pinky

Pinky, the *ambusher*, heads for the position 4 tiles ahead of Pac-Man's current position. In the original game there is an overflow error leading to a different behavior: when Pac-Man looks upwards, the tile ahead of Pac-Man is falsely computed with an additional number of steps to the west. This behavior is active by default and can be toggled using the 'o'-key.

```java
pinky = new Ghost("Pinky", game);
pinky.initialDir = Top4.S;
pinky.initialTile = game.maze.pinkyHome;
pinky.scatterTile = game.maze.pinkyScatter;
pinky.revivalTile = game.maze.pinkyHome;
pinky.fnChasingTarget = () -> pacMan.tilesAhead(4);
```

<img src="doc/pinky.png"/>

### Inky (the cyan ghost)

Inky heads for a position that depends on Blinky's current position and the position two tiles ahead of Pac-Man:

Consider the vector `V` from Blinky's position `B` to the position `P` two tiles ahead of Pac-Man, so `V = (P - B)`. 
Add the doubled vector to Blinky's position: `B + 2 * (P - B) = 2 * P - B` to get Inky's target:

```java
inky = new Ghost("Inky", game);
inky.initialDir = Top4.N;
inky.initialTile = game.maze.inkyHome;
inky.scatterTile = game.maze.inkyScatter;
inky.revivalTile = game.maze.inkyHome;
inky.fnChasingTarget = () -> {
	Tile b = blinky.tile(), p = pacMan.tilesAhead(2);
	return game.maze.tileAt(2 * p.col - b.col, 2 * p.row - b.row);
};
```

<img src="doc/inky.png"/>

### Clyde (the orange ghost)

Clyde attacks Pac-Man directly (like Blinky) if his straight line distance from Pac-Man is more than 8 tiles. If closer, he behaves like in scattering mode.

```java
clyde = new Ghost("Clyde", game);
clyde.initialDir = Top4.N;
clyde.initialTile = game.maze.clydeHome;
clyde.scatterTile = game.maze.clydeScatter;
clyde.revivalTile = game.maze.clydeHome;
clyde.fnChasingTarget = () -> clyde.tileDistanceSq(pacMan) > 8 * 8 ? pacMan.tile() : game.maze.clydeScatter;
```

<img src="doc/clyde.png"/>

The visualization of the attack behaviors can be toggled during the game by pressing the 'r'-key ("show/hide routes").

### Scattering

In *scattering* mode, each ghost tries to reach his "scattering target" which is a tile outside of the maze. Because ghosts
cannot reverse direction this results in a cyclic movement around the walls in the corresponding corner of the maze.

<img src="doc/scattering.png"/>

In the *frightened* and *locked* mode, the ghoste have the same behavior:

```java
ghosts().forEach(ghost -> {
	ghost.setSteering(GhostState.FRIGHTENED, GhostSteerings.movingRandomly());
	ghost.setSteering(GhostState.LOCKED, GhostSteerings.jumpingUpAndDown());
});
```

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

- The following settings can be changed via command-line arguments (`java -jar pacman.jar arguments...`):
  - Scaling: e.g. `-scale 2.5`
  - Full-screen mode on start: `-fullScreenOnStart`
  - Full-screen resolution & depth: e.g. `-fullScreenMode 800,600,32`
  - Window title: e.g. `-title "Pac-Man Game"`
- General
  - CTRL-p pauses/resumes the game
  - F2 opens a dialog where the game loop frequency and (full-)screen resolution can be changed
  - F11 toggles between window and full-screen exclusive mode
- Game 
  - Speed can be changed during game 
    - Keys ('1' = normal, '2' = fast, '3' = very fast)
    - ALT-LEFT = slower, ALT-RIGHT = faster
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
