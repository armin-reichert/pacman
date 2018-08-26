# A comprehensible Pac-Man implementation based on finite state machines          

(Work in progress)

<img src="doc/intro.png"/>

## Pac-Man? Really? How uncool!

For the average school kid in 2018, a retro game like Pac-Man probably seems like the most boring and uncool thing you can deal with. These cool kids implement their own 3D shooters with real and unreal engines in less than a week, don't they?

Well, some of them probably even can do that (I can't). But I think that for many young people with an interest in computer programming also a simple (?) old-school game like Pac-Man can be very instructive. And for me personally, it brings back  memories because the only computer game I played regularly was ["Snack Attack"](https://www.youtube.com/watch?v=ivAZkuBbpsM), then running on my Apple II compatible computer in 1984. On a green monitor, no colors. But what a sound!

Note: If you are the kind of kid that just wants to write quick and dirty code, this is not the right place for you. Also, if you are somebody who doesn't start with less than a game engine like Unity for implementing Pac-Man. I want to address people with an interest in writing code instead of using tools.
   
## The challenge
For a beginner in programming, a game like Pac-Man can be interesting for different reasons. First, it isn't as trivial as "Pong" for example. Programming "Pong" is surely a good start. Programming a game loop, updating and drawing paddles, ball movement, collisions with walls and paddles are good stuff for starters. The next step probably are "Breakout" variants with different kinds of targets, waves, levels, special balls and so on. 

Pac-Man offers new challenges. First, the representation of the maze and the correct movement of the characters in the maze is not trivial. The Pac-Man is moved through the maze using the keyboard and you can press the key for the intended direction before he reaches the position where he actually can change his direction. You can and should spend some time to get this (sufficiently) right.

Another task are the animations, timers and the rendering of Pac-Man and the four ghosts. You have to deal with sprites and different kinds of animations. It's a good exercise to implement all that from the ground up instead of just using predefined tools.

If you have mastered these basics and your actors can move correctly through the maze, you are challenged with making the "real" game. You have to think about what different phases (states) the game can have, how the actors and the user interface behave in these states and which "events" lead from one state to the other?

Maybe you should start with a single ghost and implement its behavior: waiting in the ghost house, jumping, leaving the house and chasing Pac-Man. Next, what should happen when Pac-Man and a ghost are colliding? Which part of your program should coordinate this? Should the code be distributed over the actors or should you have some kind of mediator, some central game control? Where should the game rules (points, lives, levels etc.) be implemented? Just where you need it or in a central place, a *model* in the sense of the Model-View-Controller pattern?

You may think that this is overengineering for such a simple program, but just have a look at some of the implementations you can find on the internet, read the code and judge if you really enjoy it. Can you learn how that game works from code where the game state is distributed in global variables, timers, flags and the game rules are distributed over the actors? 

Without thinking about all these issues and having an idea how to structure it, your code can quickly become a real mess. Probably you will get some parts running quickly but when it comes to the interaction between the different actors, the game state and the timing, you can easily lose ground. 

## Searching for help

And then you will look for help on the internet. You look at game "tutorials" on YouTube but they just tell you to put the right code in your entities' "update" method, add a few variables and flags here and there, write a few if- and switch-statements and all will be well and running. 

Or you will look into the code of others who have implemented Pac-Man, often with impressive results, for example see [here](https://github.com/leonardo-ono/Java2DPacmanGame) or [here](https://github.com/yichen0831/Pacman_libGdx). But when your look into the code, you are either overwhelmed or you understand only some parts but do not get the whole picture. Even if concepts like state machines are used in their code, they are not written in a way that helps you to understand the complete working of the game. From a practical point this is completely ok, often the underlying concepts somehow melt into the implementation and can only be recognized later if you have an idea what the implementor was trying to do. But often too, the concepts are only realized half-way or abandoned during the implementation to get the thing finally running.

And then you maybe become totally frustrated and lose interest. Or you give it a last try and search the internet again. And you will find articles about using state machines in games in general and also in the Pac-Man game. You will find introductory computer science courses on AI where Pac-Man is used as a test ground for implementing AI agents. This is certainly very interesting but it doesn't help you with your own Pac-Man implementation because these agents are programmed against some predefined Pac-Man framework and cannot be 1:1 used inside your own game. And it doesn't help you with the control of the overall game play too.

## State machines to the rescue

Maybe you will also find introductions and tutorials about *(finite) state machines* and the different possibilities of implementing them. From basic switch-statements to object-oriented "state pattern"-based implementations, function pointers in C, C++, ready-made libraries like [Appcelerate](http://www.appccelerate.com/), [Stateless4j](https://github.com/oxo42/stateless4j) or [Squirrel](http://hekailiang.github.io/squirrel/). Now you are totally confused about how to proceed but to make progress you have to take a decision.

The more low-level implementations of state machines (switches, function pointers) are the most performant ones but as long a you achieve the performance goals for your game (60 frames/updates per second), you can use whatever implementation you like. And if you want to make your code understandable to other people for learning purposes it is certainly not the best way to choose the low-level ones. 

I decided to build my own state machine implementation along the lines of Mealy machines with guarded conditions and transition actions. Of course, I could have used some existing framework, but eating your own dog food is more fun.

Whatever decision you take, the important thing IMHO is that you take a decision and stick to it, do not reinvent the state machine implementation for each use case from scratch! And now you can concentrate on the "real" stuff: 

Which entities in the Pac-Man game are candidates for getting controlled by state machines? You will be surprised how many parts of your program suddenly will look like state machines to you: 

Of course, Pac-Man and the four ghosts, but also the global game control, maybe also the screen selection logic or even simpler entities in your game. It is interesting to look at your program parts through the state machine glasses and find out where an explicit state machine becomes useful in contrast to just using variables, methods and control-flow statements in free-style.

I decided to implement the global game control as well as the Pac-Man and ghost control by state machines. Their control logic is sufficiently complex for being modelled/implemented explicitly. My implementation allows (similarly to e.g. Stateless4j) to define your state machines in a declarative way (*builder pattern*). Further, the overhead of embedding client code into the state machine definitions is reduced by the possibility to use lambda expressions (anonymous functions) or function/method references. This allows for a smooth integration of state machines in your program. You have the flexibility to write your code inline inside the state machine hooks (*onEntry, onExit, onTick, on(event), onTimeout*), or to delegate to separate classes/methods. You can either use the predefined state objects or define your own state objects in separate classes, with additional methods and variables. 

## State machines in practice

Sounds all well and nice, but how does that look in the real code? Here is the implementation of the global game control:

```java
StateMachine.define(PlayState.class, GameEvent.class)

	.description("[GameControl]")
	.initialState(INTRO)

	.states()

		.state(INTRO)
			.onEntry(() -> selectView(introView))

		.state(READY)
			.impl(new ReadyState())
			.timeoutAfter(() -> game.sec(3))

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

		.when(INTRO).then(READY)
			.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
			.act(() -> selectView(playView))

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

		.stay(CHANGING_LEVEL)
			.on(PacManGettingWeakerEvent.class)

		.stay(GHOST_DYING)
			.on(PacManGettingWeakerEvent.class)

		.when(GHOST_DYING).then(PLAYING)
			.onTimeout()

		.when(PACMAN_DYING).then(GAME_OVER)
			.condition(() -> actors.getPacMan().getState() == PacManState.DEAD && game.getLives() == 0)

		.when(PACMAN_DYING).then(PLAYING)
			.condition(() -> actors.getPacMan().getState() == PacManState.DEAD && game.getLives() > 0)
			.act(() -> actors.init())

		.when(GAME_OVER).then(READY)
			.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

.endStateMachine();
```

The states of this state machine are implemented as separate (inner) classes. However, this is not necessary in simpler cases and is the decision of the implementor.

Pac-Man's state machine is implemented as follows:

```java
StateMachine.define(PacManState.class, GameEvent.class)

	.description("[Pac-Man]")
	.initialState(HOME)

	.states()

		.state(HOME)
			.onEntry(this::initPacMan)

		.state(HUNGRY)
			.impl(new HungryState())

		.state(GREEDY)
			.impl(new GreedyState())
			.timeoutAfter(game::getPacManGreedyTime)

		.state(DYING)
			.onEntry(() -> sprite = s_dying)
			.timeoutAfter(() -> game.sec(2))

	.transitions()

		.when(HOME).then(HUNGRY)

		.when(HUNGRY).then(DYING)
			.on(PacManKilledEvent.class)

		.when(HUNGRY).then(GREEDY)
			.on(PacManGainsPowerEvent.class)

		.stay(GREEDY)
			.on(PacManGainsPowerEvent.class)
			.act(() -> controller.resetTimer())

		.when(GREEDY).then(HUNGRY)
			.onTimeout()
			.act(() -> events.publish(new PacManLostPowerEvent()))

		.stay(DYING)
			.onTimeout()
			.act(e -> events.publish(new PacManDiedEvent()))

.endStateMachine();
```

## Tracing

The processing of all used state machines can be traced to some logger. If a state machine processes an event and does not find a suitable state transition, a runtime exception is thrown. This is very useful for finding gaps in the state machine definitions because you will get a direct hint what is missing in your control logic. Without explicit state machines your program would probably just misbehave but give no information on the why and where.

Example trace:

```
[2018-08-17 06:18:21:240] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)'
[2018-08-17 06:18:21:411] [PacMan] publishing event 'FoodFound(Pellet)'
[2018-08-17 06:18:21:411] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)'
[2018-08-17 06:18:24:053] [PacMan] publishing event 'BonusFound(CHERRIES,100)'
[2018-08-17 06:18:24:053] [GameControl] stays 'PLAYING' on 'BonusFound(CHERRIES,100)'
[2018-08-17 06:18:24:053] PacMan found bonus CHERRIES of value 100
[2018-08-17 06:18:25:552] [PacMan] publishing event 'FoodFound(Pellet)'
[2018-08-17 06:18:25:552] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)'
[2018-08-17 06:18:25:752] [PacMan] publishing event 'FoodFound(Pellet)'
[2018-08-17 06:18:25:752] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)'
[2018-08-17 06:18:25:921] [PacMan] publishing event 'FoodFound(Pellet)'
[2018-08-17 06:18:25:921] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)'
[2018-08-17 06:18:26:103] [PacMan] publishing event 'FoodFound(Pellet)'
[2018-08-17 06:18:26:103] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)'
[2018-08-17 06:18:26:274] [PacMan] publishing event 'FoodFound(Pellet)'
[2018-08-17 06:18:26:274] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)'
[2018-08-17 06:18:26:434] [PacMan] publishing event 'FoodFound(Pellet)'
[2018-08-17 06:18:26:434] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)'
[2018-08-17 06:18:27:101] [PacMan] publishing event 'PacManGhostCollisionEvent(Blinky)'
[2018-08-17 06:18:27:101] [GameControl] stays 'PLAYING' on 'PacManGhostCollisionEvent(Blinky)'
[2018-08-17 06:18:27:118] [GameControl] changing from 'PLAYING' to 'PACMAN_DYING' on 'PacManKilledEvent(Blinky)'
[2018-08-17 06:18:27:118] [GameControl] exiting state 'PLAYING'
[2018-08-17 06:18:27:118] [Pac-Man] changing from 'HUNGRY' to 'DYING' on 'PacManKilledEvent(Blinky)'
[2018-08-17 06:18:27:118] [Pac-Man] exiting state 'HUNGRY'
[2018-08-17 06:18:27:118] [Pac-Man] entering state 'DYING'
[2018-08-17 06:18:27:118] PacMan killed by Blinky at (21,12)
[2018-08-17 06:18:27:118] [GameControl] entering state 'PACMAN_DYING'
[2018-08-17 06:18:29:145] [Pac-Man] stays 'DYING'
[2018-08-17 06:18:29:145] [PacMan] publishing event 'PacManDiedEvent'
[2018-08-17 06:18:29:145] [GameControl] changing from 'PACMAN_DYING' to 'PLAYING' on 'PacManDiedEvent'
[2018-08-17 06:18:29:145] [GameControl] exiting state 'PACMAN_DYING'
[2018-08-17 06:18:29:145] [Pac-Man] entering initial state 'HOME'
[2018-08-17 06:18:29:145] [Ghost Blinky] entering initial state 'HOME'
[2018-08-17 06:18:29:160] [GameControl] entering state 'PLAYING'
[2018-08-17 06:18:29:160] [Pac-Man] changing from 'HOME' to 'HUNGRY'
[2018-08-17 06:18:29:160] [Pac-Man] exiting state 'HOME'
[2018-08-17 06:18:29:160] [Pac-Man] entering state 'HUNGRY'
[2018-08-17 06:18:29:176] [Ghost Blinky] changing from 'HOME' to 'SAFE'
[2018-08-17 06:18:29:176] [Ghost Blinky] exiting state 'HOME'
[2018-08-17 06:18:29:176] [Ghost Blinky] entering state 'SAFE' for 2,00 seconds (120 frames)
[2018-08-17 06:18:31:190] [Ghost Blinky] changing from 'SAFE' to 'AGGRO'
[2018-08-17 06:18:31:190] [Ghost Blinky] exiting state 'SAFE'
[2018-08-17 06:18:31:190] [Ghost Blinky] entering state 'AGGRO'
[2018-08-17 06:18:34:865] [PacMan] publishing event 'PacManGhostCollisionEvent(Blinky)'
[2018-08-17 06:18:34:865] [GameControl] stays 'PLAYING' on 'PacManGhostCollisionEvent(Blinky)'
[2018-08-17 06:18:34:883] [GameControl] changing from 'PLAYING' to 'PACMAN_DYING' on 'PacManKilledEvent(Blinky)'
[2018-08-17 06:18:34:883] [GameControl] exiting state 'PLAYING'
[2018-08-17 06:18:34:883] [Pac-Man] changing from 'HUNGRY' to 'DYING' on 'PacManKilledEvent(Blinky)'
[2018-08-17 06:18:34:883] [Pac-Man] exiting state 'HUNGRY'
[2018-08-17 06:18:34:883] [Pac-Man] entering state 'DYING' for 2,00 seconds (120 frames)
[2018-08-17 06:18:34:883] PacMan killed by Blinky at (21,23)
[2018-08-17 06:18:34:883] [GameControl] entering state 'PACMAN_DYING'
[2018-08-17 06:18:36:912] [Pac-Man] stays 'DYING'
[2018-08-17 06:18:36:912] [PacMan] publishing event 'PacManDiedEvent'
[2018-08-17 06:18:36:912] [GameControl] changing from 'PACMAN_DYING' to 'PLAYING' on 'PacManDiedEvent'
[2018-08-17 06:18:36:912] [GameControl] exiting state 'PACMAN_DYING'
[2018-08-17 06:18:36:912] [Pac-Man] entering initial state 'HOME'
[2018-08-17 06:18:36:912] [Ghost Blinky] entering initial state 'HOME'
[2018-08-17 06:18:36:912] [GameControl] entering state 'PLAYING'
[2018-08-17 06:18:36:928] [Pac-Man] changing from 'HOME' to 'HUNGRY'
[2018-08-17 06:18:36:928] [Pac-Man] exiting state 'HOME'
[2018-08-17 06:18:36:928] [Pac-Man] entering state 'HUNGRY'
[2018-08-17 06:18:36:928] [Ghost Blinky] changing from 'HOME' to 'SAFE'
[2018-08-17 06:18:36:928] [Ghost Blinky] exiting state 'HOME'
[2018-08-17 06:18:36:928] [Ghost Blinky] entering state 'SAFE' for 2,00 seconds (120 frames)
[2018-08-17 06:18:38:973] [Ghost Blinky] changing from 'SAFE' to 'AGGRO'
[2018-08-17 06:18:38:973] [Ghost Blinky] exiting state 'SAFE'
[2018-08-17 06:18:38:973] [Ghost Blinky] entering state 'AGGRO'
[2018-08-17 06:18:41:393] Application window closing, app will exit...
[2018-08-17 06:18:41:401] Application terminated.
```

## Configurable navigation behavior (aka AI)

<img src="doc/pacman.png"/>

The navigation behavior of the actors is implemented modularly (*strategy pattern*) and can easily be configured.

### Pac-Man

Pac-Man is controlled by the keyboard:

```java
PacMan pacMan = new PacMan(game);
Navigation keySteering = followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
pacMan.setNavigation(PacManState.HUNGRY, keySteering);
pacMan.setNavigation(PacManState.GREEDY, keySteering);
```

### Ghosts
Configuration of ghost behavior:

```java
// common ghost behavior
Stream.of(blinky, pinky, inky, clyde).forEach(ghost -> {
	ghost.setNavigation(FRIGHTENED, flee(pacMan));
	ghost.setNavigation(SCATTERING, followTargetTile(() -> ghost.getScatteringTarget()));
	ghost.setNavigation(DEAD, followPath(ghost.getHome()));
	ghost.setNavigation(SAFE, bounce());
});

// individual ghost behavior
blinky.setNavigation(AGGRO, attackDirectly(pacMan));
pinky.setNavigation(AGGRO, ambush(pacMan));
inky.setNavigation(AGGRO, chaseLikeInky(blinky, pacMan));
clyde.setNavigation(AGGRO, chaseLikeClyde(clyde, pacMan));
clyde.fnCanLeaveHouse = () -> game.getLevel() > 1 || game.getFoodRemaining() < (66 * maze.getFoodTotal() / 100);
```

With the general *followTargetTile* behavior available, the individual behaviors *scatter*, *ambush*, *attackDirectly* etc. are trivial to implement:

### Blinky

Blinky's chase behavior is to directly attack Pac-Man:

```java
public static Navigation attackDirectly(MazeMover victim) {
	return followTargetTile(victim::getTile);
}
```

<img src="doc/blinky.png"/>

### Pinky

Pinky, the *ambusher*, targets the position 4 tiles ahead of Pac-Man (in the original game there is an overflow error that leads to a different behavior):

```java
public static Navigation ambush(MazeMover victim) {
	return followTargetTile(() -> victim.ahead(4));
}
```

<img src="doc/pinky.png"/>

### Inky

Inky's target tile is computed as follows:

Consider the vector `V` from Blinky's position `B` to the position `P` two tiles ahead of Pac-Man, so `V = (P - B)`. Add the doubled vector to Blinky's position: `B + 2 * (P - B) = 2 * P - B` to get Inky's target:

```java
public static Navigation chaseLikeInky(Ghost blinky, PacMan pacMan) {
	return followTargetTile(() -> {
		Tile b = blinky.getTile();
		Tile p = pacMan.ahead(2);
		Tile target = new Tile(2 * p.col - b.col, 2 * p.row - b.row);
		// TODO: correctly project target tile to border
		Maze maze = pacMan.getMaze();
		int row = Math.min(Math.max(0, target.row), maze.numRows() - 1);
		int col = Math.min(Math.max(0, target.col), maze.numCols() - 1);
		return new Tile(col, row);
	});
}
```

### Clyde

Clyde attacks Pac-Man directly (like Blinky) if his straight line distance from Pac-Man is more than 8 tiles. If closer, he goes into scattering mode:

```java
public static Navigation chaseLikeClyde(Ghost clyde, PacMan pacMan) {
	return followTargetTile(() -> dist(clyde.getCenter(), pacMan.getCenter()) >= 8 * Game.TS 
		? pacMan.getTile()
		: clyde.getScatteringTarget());
```

<img src="doc/clyde.png"/>

### Scattering

In *scatter* mode, each ghost tries to reach his scattering target tile outside of the maze which results in a cyclic movement around the block in that corner.

<img src="doc/scattering.png"/>

### Path finding

For simulating the ghost behavior from the original Pac-Man game, no graph based path finding is needed, the *followTargetTile* behavior is sufficient. To also give an example how graph based path finding can be used, the *flee* behavior has been implemented differently from the original game.

Shortest routes in the maze graph can be computed using the method *Maze.findPath(Tile source, Tile target)*. This method runs an A* or BFS algorithm on the underlying grid graph (A* sounds cooler than BFS :-). A* is rather useless here because the maze is represented by a (grid) graph where the distance between two vertices (neighbor tiles) is always equal. Thus the Dijkstra or A* path finding algorithms will just degenerate to BFS (correct me if I'm wrong). Of course you could represent the graph differently, for example with vertices only for crossings and weighted edges for passages. In that case, Dijkstra or A* would be useful.

## Additional features

- Display of actor states and timers can be switched on/off at runtime (key 's')
- Display of actor routes can be switched on/off at runtime (key 'r')
- Ghosts can enabled/disabled during the game (keys 'b', 'p', 'i', 'c')
- Cheat key 'k' kills all ghosts
- Cheat key 'e' eats all pellets
- Alignment of actors on the grid can be visualized (key 'g')
- Game can be paused (CTRL+p) and game loop frequency can be changed (F2 opens dialog)
- F11 toggles between window and full-screen exclusive mode (warning: may cause bluescreen with some graphic drivers!)

## References

This work would not have been possible without these invaluable sources of information:

- [GameInternals - Understanding Pac-Man Ghost Behavior](http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior)
- [Gamasutra - The Pac-Man Dossier](http://www.gamasutra.com/view/feature/3938/the_pacman_dossier.php)

## Summary

The goal of this project is to implement a Pac-Man game in a way that also beginners can more easily understand how the game is working. The implementation given tries to achieve this by following the MVC pattern and separating the control logic for the actors and the game play into explicit state machines. The state machines are defined in a declarative way using the builder pattern.

A very simple home-grown library is used for the basic game infrastructure (active rendering, game loop etc.) but it is not difficult to write these infrastructure parts from scratch or use some real game library instead. It would also be useful to even further decouple the UI from the model and controller to enable an easy replacement of the complete UI.

Comments are welcome.

*Armin Reichert, August 2018*
