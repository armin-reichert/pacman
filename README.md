# Pac-Man (with an emphasis on state machines, in progress)

<img src="doc/pacman.png"/>

Pac-Man? Really? How uncool!

For the average school kid in 2018, a retro game like Pac-Man probably seems like the most boring and uncool thing you can deal with. These cool kids implement their own 3D shooters with real and unreal engines in less than a week, don't they?

Well, some of them probably even can do that (I can't). But I think that for many young people with an interest in computer programming also a simple (?) old-school game like Pac-Man can be very instructive. And for me personally, it brings back  memories because the only computer game I played regularly was ["Snack Attack"](https://www.youtube.com/watch?v=ivAZkuBbpsM), then running on my Apple II compatible computer in 1984. On a green monitor, no colors. But what a sound!

Note: If you are the kind of kid that just wants to program something, somehow, quick-and-dirty, this is not the right place for you. Also, if you are somebody who doesn't start with less than a game engine like Unity for implementing Pac-Man. I want to address people with an interest in writing code instead of using tools.
   
For a beginner in programming, a game like Pac-Man can be interesting for different reasons. First, it isn't as trivial as "Pong" for example. Programming "Pong" is surely a good start. Programming a game loop, updating and drawing paddles, ball movement, collisions with walls and paddles are good stuff for starters. The next step probably are "Breakout" variants with different kinds of targets, waves, levels, special balls and so on. 

Pac-Man offers new challenges. First, the representation of the maze and the correct movement of the characters in the maze is not trivial. The Pac-Man is moved through the maze using the keyboard and you can press the key for the intended direction before he reaches the position where he actually can change his direction. You can and should spend some time to get this (sufficiently) right.

Another task are the animations, timers and the rendering of Pac-Man and the four ghosts. You have to deal with sprites and different kinds of animations. It's a good exercise to implement all that from the ground up instead of just using predefined tools.

If you have mastered these basics and your actors can move correctly through the maze, you are challenged with making the "real" game. You have to think about what different phases (states) the game can have, how the actors and the user interface behave in these states and which "events" lead from one state to the other? 

Maybe you should start with a single ghost and implement its behavior: waiting in the ghost house, jumping, leaving the house and chasing Pac-Man. Next, what shoud happen when Pac-Man and the ghost are colliding? Which part of your program should coordinate this? Should the code be distributed in the actors or should you have some kind of mediator, some central game control? Where should the game rules (points, lives, levels etc.) be implemented? Just where you need it or may in a central place, some kind of model in the sense of the Model-View-Controller pattern?

You may think that this is overengineering for such a simple program, but just have a look at some of the implementations you can find on the internet, read the code and judge if you really enjoy it. Can you learn how that game works from code where the game state is distributed in global variables, timers, flags and the game rules are distributed over the actors? 

Without thinking about all these issues and having an idea how to structure it, your code can quickly become a real mess. Probably you will get some parts running quickly but when it comes to the interaction between the different actors, the game state and the timing, you can easily lose ground. 

And then you will look for help on the internet. You look at game "tutorials" on YouTube but they just tell you to put the right code in your entities' "update" method, add a few variables and flags here and there, write a few if- and switch-statements and all will be well and running. 

Or you will look into the code of others who have implemented Pac-Man, often with impressive results and little code. And you will look into their code, understand some parts but not get the whole picture because they have implemented certain concepts like state machines in their code but not in a way that helps you to understand the complete working of the game. From a practical point this is completely ok, often the underlying concepts somehow melt into the implementation and can only be recognized later if you have an idea what the implementor was trying to do. But often too, the concepts are only realized half-way or abandoned during the implementation to get the thing finally running.

And then you maybe become totally frustrated and lose interest. Or you give it a last try and search the internet again. And you will find articles about using state machines in game in general and also in the Pac-Man game. You will find introductory computer science courses on AI where Pac-Man is used as a test ground for implementing AI agents. This is certainly very interesting but it doesn't help you with your own Pac-Man implementation because these agents are programmed against some predefined Pac-Man framework and cannot be 1:1 used inside your own game. And it doesn't help you with the control of the overall game play too.

Or you will watch all these introductions and tutorials about state machines and the different ways of implementing them. From basic switch-statements to object-oriented "state pattern"-based implementations, function pointers in C, C++, ready-made libraries like [Appcelerate](http://www.appccelerate.com/) and code generating tools, in the end you have to decide for something.

The more low-level implementations of state machines (switches, function pointers) are the most performant ones but as long a you achieve the performance goals for your game (60 frames/updates per second), you can use whatever implementation you like. And if you want to make your code understandable to other people for learning purposes it is certainly not the best way to choose the low-level ones. 

For that reason I decided to start from the ground up (the theoretical base which are Mealy-machines) and build my own state machine implementation. Of course, I could have used something existing like [Stateless4j](https://github.com/oxo42/stateless4j).

Whatever decision you take, the important thing IMHO is that you take a decision and stick to it, do not reinvent the state machine implementation for each use case from scratch! And now you can concentrate on the "real" stuff: 

Which entities in the Pac-Man game are candidates for getting controlled by state machines? You will be surprised how many parts of your program suddenly will look like state machines to you: 

Of course, Pac-Man and the four ghosts, but also the global game control, maybe also the screen selection logic or even simpler entities in your game. It is interesting to look at your program parts through the state machine glasses and find out where an explicit state machine becomes useful in contrast to just using variables, methods and control-flow statements in free-style.

In the implementation here, I decided to implement the global game control as well as the Pac-Man and ghost control by state machines. Their control logic is sufficiently complex for being modelled/implemented explicitly. My state machine implementation allows (similarly to e.g. Stateless4j) to define your state machines inside in a declarative way. This is achieved by using the "builder pattern". Further, the overhead of embedding client code into the state machine definitions is reduced by the possibility to use lambda expressions (anonymous functions) or function/method references. This allows for a smooth integration of state machines in your program. You have the flexibility to write your code inline inside the state machine hooks (onEntry, onExit, onTick, on(event), onTimeout), or to delegate to separate classes/methods. You can either use the predefined state objects or define your own state objects in separate classes, with additional methods and variables. 

Sounds godd, but how does that look in the code? Here is the implementation of the global game control:

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

The states of this state machine are implemented as separate (inner) classes. However, this is not necessary in simpler cases and is the decision of the implementor. Isn't this clear and readable code?

Pac-Man's state machine:

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
					.act(() -> events.publishEvent(new PacManLostPowerEvent()))
	
				.stay(DYING)
					.onTimeout()
					.act(e -> events.publishEvent(new PacManDiedEvent()))

		.endStateMachine();
```

The processing of all used state machines can be traced to some logger. If a state machine processes an event and does not find a suitable state transition, a runtime exception is thrown. This is very useful for finding gaps in the state machine definitions because you will get a direct hint what is missing in your control logic. Without explicit state machines your program would probably just misbehave but give no information on the why and where.

Example trace:

```java
[2018-08-11 11:05:49:049] Application PacManApp created. 
[2018-08-11 11:05:49:237] Application shell created. 
[2018-08-11 11:05:49:315] Window-mode: 448x576 
[2018-08-11 11:05:49:315] Default view initialized. 
[2018-08-11 11:05:49:752] Pac-Man sprite images extracted 
[2018-08-11 11:05:49:924] Set controller to: de.amr.games.pacman.controller.GameController@6b32d068 
[2018-08-11 11:05:49:942] [Ghost Blinky] entering initial state 'HOME' 
[2018-08-11 11:05:49:942] [GameControl] entering initial state 'READY' 
[2018-08-11 11:05:49:942] [Pac-Man] entering initial state 'SAFE' 
[2018-08-11 11:05:49:942] [Ghost Blinky] entering initial state 'HOME' 
[2018-08-11 11:05:49:942] Initialized controller: de.amr.games.pacman.controller.GameController@6b32d068 
[2018-08-11 11:05:49:942] Application initialized. 
[2018-08-11 11:05:49:942] Pulse started. 
[2018-08-11 11:05:52:038] [GameControl] changing from 'READY' to 'PLAYING' 
[2018-08-11 11:05:52:038] [GameControl] exiting state 'READY' 
[2018-08-11 11:05:52:041] [GameControl] entering state 'PLAYING' 
[2018-08-11 11:05:52:056] [Ghost Blinky] changing from 'HOME' to 'SAFE' 
[2018-08-11 11:05:52:056] [Ghost Blinky] exiting state 'HOME' 
[2018-08-11 11:05:52:056] [Ghost Blinky] entering state 'SAFE' 
[2018-08-11 11:05:52:074] [Pac-Man] changing from 'SAFE' to 'VULNERABLE' 
[2018-08-11 11:05:52:074] [Pac-Man] exiting state 'SAFE' 
[2018-08-11 11:05:52:074] [Pac-Man] entering state 'VULNERABLE' 
[2018-08-11 11:05:52:318] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 11:05:52:318] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 11:05:52:506] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 11:05:52:506] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 11:05:52:681] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 11:05:52:681] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 11:05:52:853] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 11:05:52:853] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 11:05:53:039] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 11:05:53:039] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 11:05:53:216] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 11:05:53:216] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 11:05:53:404] [GameActorEvents] publishing event 'FoodFound(Pellet)' 
[2018-08-11 11:05:53:404] [GameControl] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2018-08-11 11:05:54:147] [Ghost Blinky] changing from 'SAFE' to 'AGGRO' 
[2018-08-11 11:05:54:147] [Ghost Blinky] exiting state 'SAFE' 
[2018-08-11 11:05:54:149] [Ghost Blinky] entering state 'AGGRO' 
[2018-08-11 11:05:57:611] [GameActorEvents] publishing event 'PacManGhostCollisionEvent(Blinky)' 
[2018-08-11 11:05:57:614] [GameControl] stays 'PLAYING' on 'PacManGhostCollisionEvent(Blinky)' 
[2018-08-11 11:05:57:630] [GameControl] changing from 'PLAYING' to 'PACMAN_DYING' on 'PacManKilledEvent(Blinky)' 
[2018-08-11 11:05:57:630] [GameControl] exiting state 'PLAYING' 
[2018-08-11 11:05:57:630] [Pac-Man] changing from 'VULNERABLE' to 'DYING' on 'PacManKilledEvent(Blinky)' 
[2018-08-11 11:05:57:630] [Pac-Man] exiting state 'VULNERABLE' 
[2018-08-11 11:05:57:630] [Pac-Man] entering state 'DYING' 
[2018-08-11 11:05:57:630] PacMan killed by Blinky at (21,23) 
[2018-08-11 11:05:57:630] [GameControl] entering state 'PACMAN_DYING' 
[2018-08-11 11:05:59:748] [Pac-Man] stays 'DYING' 
[2018-08-11 11:05:59:748] [GameActorEvents] publishing event 'PacManDiedEvent' 
[2018-08-11 11:05:59:750] [GameControl] changing from 'PACMAN_DYING' to 'PLAYING' on 'PacManDiedEvent' 
[2018-08-11 11:05:59:751] [GameControl] exiting state 'PACMAN_DYING' 
[2018-08-11 11:05:59:751] [Pac-Man] entering initial state 'SAFE' 
[2018-08-11 11:05:59:751] [Ghost Blinky] entering initial state 'HOME' 
[2018-08-11 11:05:59:751] [GameControl] entering state 'PLAYING' 
[2018-08-11 11:05:59:766] [Ghost Blinky] changing from 'HOME' to 'SAFE' 
[2018-08-11 11:05:59:766] [Ghost Blinky] exiting state 'HOME' 
[2018-08-11 11:05:59:766] [Ghost Blinky] entering state 'SAFE' for 2,00 seconds (120 frames) 
[2018-08-11 11:05:59:783] [Pac-Man] changing from 'SAFE' to 'VULNERABLE' 
[2018-08-11 11:05:59:783] [Pac-Man] exiting state 'SAFE' 
[2018-08-11 11:05:59:783] [Pac-Man] entering state 'VULNERABLE' 
[2018-08-11 11:06:01:826] [Ghost Blinky] changing from 'SAFE' to 'AGGRO' 
[2018-08-11 11:06:01:826] [Ghost Blinky] exiting state 'SAFE' 
[2018-08-11 11:06:01:827] [Ghost Blinky] entering state 'AGGRO' 
[2018-08-11 11:06:04:528] Application window closing, app will exit... 
[2018-08-11 11:06:04:544] Application terminated. 

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

The move behaviours are implemented as reusable classes with a common interface. Behaviours which need to compute shortest routes in the maze can just call the method **Maze.findPath(Tile source, Tile target)**.

This method runs the A* path finding algorithm on the underlying grid graph (which is rather useless in this case but hey, it sounds cooler than BFS :-). You might ask why A* is "useless" here: it's because the Pac-Man maze here is represented a grid-like graph where the distance between vertices (tiles) is always the same. So a Dijkstra or even A* path finding algorithm will just degenerate to plain breadt-first search (correct my if I'm wrong). Of course you could represent the graph differently, for example by vertices only for crossings and weighted edges for passages. In that case, Dijkstra or A* would be more useful.

As an example, this is the *chase(victim)* code:

```java
/**
 * Chasing a refugee through the maze.
 */
class Chase implements Navigation {

	private final MazeMover<?> victim;

	public Chase(MazeMover<?> victim) {
		this.victim = victim;
	}

	@Override
	public MazeRoute computeRoute(MazeMover<?> chaser) {
		RouteData route = new RouteData();
		if (victim.isOutsideMaze()) {
			route.dir = chaser.getNextDir();
			return route;
		}
		route.path = chaser.maze.findPath(chaser.getTile(), victim.getTile());
		route.dir = chaser.maze.alongPath(route.path).orElse(chaser.getNextDir());
		return route;
	}
}
```

## Summary

I hope this project fulfils these goals:
- Provide a clean, readable implementation of a Pac-Man like game
- Motivate usage of explicit state machines in your code
- Provide a base for trying different state machine implementations
- Provide a template for your own Pac-Man game

It would certainly also be useful to further decouple the UI from the game model and controller to enable an easy replacement of the complete UI.

*Armin Reichert, August 2018*
