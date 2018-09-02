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

If you have mastered these basics and your actors can move correctly through the maze, you are challenged with making the "real" game. You have to think about what different phases (states) the game can have, how the actors and the user interface behave in these states and which "events" lead from one state to the other (state transitions).

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

I decided to build my own [state machine implementation](https://github.com/armin-reichert/statemachine) along the lines of Mealy machines with guarded conditions and transition actions. Of course, I could have used some existing framework, but eating your own dog food is more fun.

Whatever decision you take, the important thing IMHO is that you take a decision and stick to it, do not reinvent the state machine implementation for each use case from scratch! And now you can concentrate on the "real" stuff: 

Which entities in the Pac-Man game are candidates for getting controlled by state machines? You will be surprised how many parts of your program suddenly will look like state machines to you: 

Of course, Pac-Man and the four ghosts, but also the global game control, maybe also the screen selection logic or even simpler entities in your game. It is interesting to look at your program parts through the state machine glasses and find out where an explicit state machine becomes useful in contrast to just using variables, methods and control-flow statements in free-style.

In the provided implementation, there are a number of explicit state machines:
- Global game controller ([GameController](src/de/amr/games/pacman/controller/GameController.java))
- Pac-Man controller ([Pac-Man](src/de/amr/games/pacman/actor/PacMan.java))
- Ghost controllers ([Ghost](src/de/amr/games/pacman/actor/Ghost.java))
- Intro view animation controller ([IntroView](src/de/amr/games/pacman/view/intro/IntroView.java))

The state machines are "implemented" in a declarative way (*builder pattern*). In essence, you write a single large expression representing the complete state graph together with node and edge annotations (actions, conditions, event conditions, timers).

Lambda expressions (anonymous functions) and function references allow to embed code directly inside the state machine definition. However, if the code becomes more complex it is of course possible to delegate to separate methods or classes. Both variants are used here.

## State machines in practice

Sounds all well and nice, but how does that look in the real code? 

The intro view shows some animations that have to be coordinated using timers and stop conditions. This
is an obvious candidate for using a state machine. This state machine has no events but only uses timers,
so we specify *Void* as event type. The states are identified by numbers:

```java
	StateMachine.define(Integer.class, Void.class)
		.description("IntroAnimation")
		.initialState(0)
		.states()

			.state(0)
				// Scroll logo into view
				.onEntry(() -> { show(logo); logo.start(); })
				.onExit(logo::stop)

			.state(1)
				// Show ghosts chasing Pac-Man and vice-versa
				.onEntry(() -> {
					show(chasePacMan, chaseGhosts);
					start(chasePacMan, chaseGhosts);
				})
				.onExit(() -> {
					stop(chasePacMan, chaseGhosts);
					chasePacMan.tf.centerX(width);
				})
				
			.state(2)
				// Show ghost points animation and blinking text
				.timeoutAfter(() -> CLOCK.sec(6))
				.onEntry(() -> {
					show(ghostPoints, pressSpace, link);
					ghostPoints.start();
				})
				.onExit(() -> {
					ghostPoints.stop();
					hide(ghostPoints, pressSpace);
				})
				
			.state(COMPLETE)
				
		.transitions()
			.when(0).then(1).condition(logo::isCompleted)
			.when(1).then(2).condition(() -> chasePacMan.isCompleted() && chaseGhosts.isCompleted())
			.when(2).then(1).onTimeout()
			.when(2).then(COMPLETE).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

	.endStateMachine();
```

A more complex state machine is used for defining the global game control. It processes game events which
are created during the game play, for example when Pac-Man finds food or meets ghosts. Also the different
game states like changing the level or the dying animations of Pac-Man and the ghosts are controlled by this
state machine. Further, the individual states are implemented by subclasses of the generic state class. This
has the advantage that actions which are state-specific can be realized as methods of the subclass.

```java
StateMachine.define(GameState.class, GameEvent.class)
	
	.description("[GameControl]")
	.initialState(INTRO)
	
	.states()
		
		.state(INTRO)
			.onEntry(() -> {
				setCurrentView(getIntroView(game));
				THEME.soundInsertCoin().play();
			})
			.onExit(() -> {
				THEME.allSounds().forEach(Sound::stop);
			})
		
		.state(READY)
			.impl(new ReadyState())
			.timeoutAfter(() -> CLOCK.sec(4.5f))
		
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
			.condition(() -> introView.isComplete())
			.act(() -> setCurrentView(getPlayView(game)))
		
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
			.act(() -> { playView.init(); actors.init(); })
	
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
			.timeoutAfter(() -> CLOCK.sec(2))

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
			.act(() -> publishEvent(new PacManLostPowerEvent()))

		.when(DYING).then(DEAD)
			.onTimeout()

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
[2018-08-17 06:18:31:190] [Ghost Blinky] changing from 'SAFE' to 'CHASING'
[2018-08-17 06:18:31:190] [Ghost Blinky] exiting state 'SAFE'
[2018-08-17 06:18:31:190] [Ghost Blinky] entering state 'CHASING'
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
[2018-08-17 06:18:38:973] [Ghost Blinky] changing from 'SAFE' to 'CHASING'
[2018-08-17 06:18:38:973] [Ghost Blinky] exiting state 'SAFE'
[2018-08-17 06:18:38:973] [Ghost Blinky] entering state 'CHASING'
[2018-08-17 06:18:41:393] Application window closing, app will exit...
[2018-08-17 06:18:41:401] Application terminated.
```


## Configurable navigation behavior (aka AI)

The game gets some of its entertainment factor from the diversity of the four ghosts. 
Especially, each of the ghosts has its own specific attack behavior. In this implementation, 
these differences in behavior are not realized by subclassing
but by configuration (This would theoretically allow to exchange behaviors at runtime). For each ghost
state there is a move behavior assigned that is used whenever the ghost is moving in that state.

<img src="doc/pacman.png"/>


### Pac-Man

Pac-Man is controlled by the keyboard:

```java
// Pac-Man behavior
Navigation<PacMan> followKeyboard = pacMan.followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
pacMan.setMoveBehavior(PacManState.HUNGRY, followKeyboard);
pacMan.setMoveBehavior(PacManState.GREEDY, followKeyboard);
```

### Ghosts
The ghosts behave identically in some of their states:

```java
// common ghost behavior
Stream.of(blinky, pinky, inky, clyde).forEach(ghost -> {
	ghost.setMoveBehavior(FRIGHTENED, ghost.flee(pacMan));
	ghost.setMoveBehavior(SCATTERING, ghost.headFor(ghost::getScatteringTarget));
	ghost.setMoveBehavior(DEAD, ghost.headFor(ghost::getHome));
	ghost.setMoveBehavior(SAFE, ghost.bounce());
});
```

The *chase* behavior is different for each ghost as explained below. Using the common *headFor* behavior, 
the implementation of the individual behaviors like *scatter*, *ambush*, *attackDirectly*, 
*attackWithPartner* etc. becomes trivial.

### Blinky

Blinky's chase behavior is to directly attack Pac-Man:

```java
public default Navigation<T> attackDirectly(Actor victim) {
	return headFor(victim::getTile);
}

blinky.setMoveBehavior(CHASING, blinky.attackDirectly(pacMan));
```

<img src="doc/blinky.png"/>

### Pinky

Pinky, the *ambusher*, targets the position 4 tiles ahead of Pac-Man (in the original game there is an overflow error that leads to a different behavior):

```java
public default Navigation<T> ambush(Actor victim, int n) {
	return headFor(() -> victim.ahead(n));
}

pinky.setMoveBehavior(CHASING, pinky.ambush(pacMan, 4));
```

<img src="doc/pinky.png"/>

### Inky

Inky's target tile is computed as follows:

Consider the vector `V` from Blinky's position `B` to the position `P` two tiles ahead of Pac-Man, so `V = (P - B)`. Add the doubled vector to Blinky's position: `B + 2 * (P - B) = 2 * P - B` to get Inky's target:

```java
public default Navigation<T> attackWithPartner(Ghost partner, PacMan pacMan) {
	return headFor(() -> {
		Tile partnerTile = partner.getTile();
		Tile pacManTile = pacMan.ahead(2);
		Tile target = new Tile(2 * pacManTile.col - partnerTile.col,
				2 * pacManTile.row - partnerTile.row);
		// TODO: correctly project target tile to border
		Maze maze = pacMan.getMaze();
		int row = Math.min(Math.max(0, target.row), maze.numRows() - 1);
		int col = Math.min(Math.max(0, target.col), maze.numCols() - 1);
		return new Tile(col, row);
	});
}

inky.setMoveBehavior(CHASING, inky.attackWithPartner(blinky, pacMan));
```

<img src="doc/inky.png"/>

### Clyde

Clyde attacks Pac-Man directly (like Blinky) if his straight line distance from Pac-Man is more than 8 tiles. If closer, he goes into scattering mode:

```java
public default Navigation<T> attackAndReject(Ghost attacker, PacMan pacMan, int distance) {
	return headFor(
			() -> dist(attacker.getCenter(), pacMan.getCenter()) >= distance ? pacMan.getTile()
					: attacker.getScatteringTarget());
}

clyde.setMoveBehavior(CHASING, clyde.attackAndReject(clyde, pacMan, 8 * Game.TS));
```

<img src="doc/clyde.png"/>

### Scattering

In *scatter* mode, each ghost tries to reach his scattering target tile outside of the maze which results in a cyclic movement around the block in that corner.

```java
ghost.setMoveBehavior(SCATTERING, ghost.headFor(ghost::getScatteringTarget));
```

<img src="doc/scattering.png"/>


## Graph based path finding

For simulating the ghost behavior from the original Pac-Man game, no graph based path finding is needed, the *headFor* behavior is sufficient. To also give an example how graph based path finding can be used, the *flee* behavior has been implemented differently from the original game.

Shortest routes in the maze graph can be computed using the method *Maze.findPath(Tile source, Tile target)*. This method randomly choses between the [A-Star](http://theory.stanford.edu/~amitp/GameProgramming/AStarComparison.html) and a simple Breadth-first-search and runs this algorithm on the underlying grid graph. A-Star sounds cooler than BFS but is in fact useless in this use-case because the maze is represented by a graph where the distance between two adjacent vertices (neighbor tiles) is always the same. Thus the A* or Dijkstra path finding algorithms will just degenerate to BFS (correct me if I'm wrong). Of course one could represent the graph differently, for example with vertices only for crossings and weighted edges for passages, then Dijkstra or A* would become useful.

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
