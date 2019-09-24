# A comprehensible(?) Pac-Man implementation using explicit state-machines        

<img src="doc/intro.png"/>

## Pac-Man? Really? How uncool!

For the average school kid in 2018, a retro game like Pac-Man probably seems like the most boring and uncool thing you 
can deal with. Nevertheless, also a seemingly simple game like Pac-Man can be very instructive!

(My personal fascination for "Pac-Man" comes from the fact that the single computer game I played regularly was  ["Snack Attack"](https://www.youtube.com/watch?v=ivAZkuBbpsM), then running on my Apple II (compatible) computer in 1984, on a green monitor, no colors, but what a sound!).
  
## The challenge
Implementing Pac-Man is challenging not because of the core game functionality like implementing a game loop, updating and drawing entities, handling collisions etc. but for others reasons:

First, implementing a good representation of the maze and the correct movement of the game characters 
through the maze are not trivial. Pac-Man's movement direction is controlled by the keyboard and the intended move direction can be selected already before Pac-Man can actually turn to that direction. After having implemented this correctly, the next challenge is the logic and the control of the game itself. You have to sort out the different states of the game and the actors, you have to understand how the user interface should behave depending on the current state and which game "events" lead from one state to the other (state transitions).

Maybe you will start with a single ghost and implement its behavior: waiting (bouncing) in the ghost house, leaving the house to chase Pac-Man or scattering out to the ghosts corner. What should happen when Pac-Man and a ghost are colliding? 
Which part of your program should coordinate this? Should the code be distributed over the actors or should you have 
some kind of mediator, some central game control? Where should the game rules (points, lives, levels etc.) be implemented? 
Should this be placed in some kind of *model* (in the sense of the Model-View-Controller pattern)?

I looked into existing code, for example [here](https://github.com/leonardo-ono/Java2DPacmanGame) or [here](https://github.com/yichen0831/Pacman_libGdx) or [here](https://github.com/urossss/Pac-Man) which I find not bad at all. But I wanted something different, namely an implementation where you can directly see the underlying state machines.

## State machines

There are a number of tutorials about *(finite) state machines* and the different possibilities of implementing them: from basic switch-statements, function pointers (C, C++) to object-oriented "state pattern"-based implementations. There are also ready-to-use libraries like [Appcelerate](http://www.appccelerate.com/), [Stateless4j](https://github.com/oxo42/stateless4j) or [Squirrel](http://hekailiang.github.io/squirrel/). What should you do? 

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
type *Void* as event type. The states are identified by numbers:

```java
beginStateMachine()
	.description("[Intro]")
	.initialState(0)
	.states()

		.state(0)
			// Scroll logo into view
			.onEntry(() -> { show(logo); logo.startAnimation(); })
			.onExit(() -> logo.stopAnimation())

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
			.timeoutAfter(() -> app().clock.sec(6))
			.onEntry(() -> {
				show(ghostPoints, pressSpace, f11Hint, speedHint[0], speedHint[1], speedHint[2], visitGitHub);
				ghostPoints.startAnimation();
			})
			.onExit(() -> {
				ghostPoints.stopAnimation();
				hide(ghostPoints, pressSpace);
			})
			
		.state(42)
			
	.transitions()
		
		.when(0).then(1)
			.condition(() -> logo.isAnimationCompleted())
		
		.when(1).then(2)
			.condition(() -> chasePacMan.isAnimationCompleted() && chaseGhosts.isAnimationCompleted())
		
		.when(2).then(1)
			.onTimeout()
		
		.when(2).then(42)
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
				setViewController(getIntroView());
				theme.snd_insertCoin().play();
				theme.loadMusic();
			})

		.state(READY)
			.impl(new ReadyState())

		.state(PLAYING)
			.impl(new PlayingState())

		.state(CHANGING_LEVEL)
			.impl(new ChangingLevelState())
			.timeoutAfter(() -> app().clock.sec(3))

		.state(GHOST_DYING)
			.impl(new GhostDyingState())
			.timeoutAfter(game::getGhostDyingTime)

		.state(PACMAN_DYING)
			.impl(new PacManDyingState())

		.state(GAME_OVER)
			.impl(new GameOverState())
			.timeoutAfter(() -> app().clock.sec(30))

	.transitions()

		.when(INTRO).then(READY)
			.condition(() -> getIntroView().isComplete())
			.act(() -> setViewController(getPlayView()))

		.when(READY).then(PLAYING)
			.onTimeout()
			.act(() -> playingState().setInitialWaitTimer(app().clock.sec(1.7f)))

		.stay(PLAYING)
			.on(FoodFoundEvent.class)
			.act(playingState()::onFoodFound)

		.stay(PLAYING)
			.on(BonusFoundEvent.class)
			.act(playingState()::onBonusFound)

		.stay(PLAYING)
			.on(PacManGhostCollisionEvent.class)
			.act(playingState()::onPacManGhostCollision)

		.stay(PLAYING)
			.on(PacManGainsPowerEvent.class)
			.act(playingState()::onPacManGainsPower)

		.stay(PLAYING)
			.on(PacManGettingWeakerEvent.class)
			.act(playingState()::onPacManGettingWeaker)

		.stay(PLAYING)
			.on(PacManLostPowerEvent.class)
			.act(playingState()::onPacManLostPower)

		.when(PLAYING).then(GHOST_DYING)
			.on(GhostKilledEvent.class)
			.act(playingState()::onGhostKilled)

		.when(PLAYING).then(PACMAN_DYING)
			.on(PacManKilledEvent.class)
			.act(playingState()::onPacManKilled)

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

		.when(GHOST_DYING).then(PLAYING)
			.onTimeout()

		.when(PACMAN_DYING).then(GAME_OVER)
			.condition(() -> isPacManDead() && game.getLives() == 0)

		.when(PACMAN_DYING).then(PLAYING)
			.condition(() -> isPacManDead() && game.getLives() > 0)
			.act(() -> {
				resetPlayView();
				playingState().setInitialWaitTimer(app().clock.sec(1.7f));
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
beginStateMachine(PacManState.class, GameEvent.class)
		
	.description("[Pac-Man]")
	.initialState(HOME)

	.states()

		.state(HOME)
			.onEntry(this::initPacMan)
			.timeoutAfter(() -> app().clock.sec(1.5f))

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
			.act(fsm::resetTimer)

		.when(POWER).then(HUNGRY)
			.onTimeout()
			.act(() -> getEventManager().publish(new PacManLostPowerEvent()))

		.when(DYING).then(DEAD)
			.onTimeout()

.endStateMachine();
```

The **ghosts** are controlled using the following state machine:

```java
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
					getGame().getPacMan().isLosingPower() ? "s_flashing" : "s_frightened");
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
```

## Tracing

The processing of all used state machines can be traced. If a state machine processes an event and does not 
find a suitable state transition, a runtime exception is thrown by default. This is very useful for finding 
gaps in the state machine definition in the development stage. Afterwards, this behavior can be changed so
that only a message is logged for unhandled events. This avoids the need for specifying "empty" transitions
for any event that has no effect in the current state. The Ghost's state machine makes use of that feature.

Example trace:

```
C:\Users\armin\Desktop>java -jar pacman.jar
[2018-10-29 06:50:40:143] Clock frequency is 60 ticks/sec.
[2018-10-29 06:50:40:159] Application 'PacManApp' created.
[2018-10-29 06:50:41:617] Pac-Man sprites extracted.
[2018-10-29 06:50:41:663] Theme 'ClassicPacManTheme' created.
[2018-10-29 06:50:41:933] Entered window mode, resolution 448x576 (224x288 scaled by 2,00)
[2018-10-29 06:50:41:933] Application shell created.
[2018-10-29 06:50:42:234] Controller set: de.amr.games.pacman.controller.PacManGameController@6e50047c
[2018-10-29 06:50:42:234] [Game] entering initial state:
[2018-10-29 06:50:42:234] [Game] entering state 'INTRO'
[2018-10-29 06:50:42:281] [Intro] entering initial state:
[2018-10-29 06:50:42:281] [Intro] entering state '0'
[2018-10-29 06:50:42:720] Controller initialized.
[2018-10-29 06:50:42:720] Application initialized.
[2018-10-29 06:50:42:720] Loading music...
[2018-10-29 06:50:42:735] Clock started, running with 60 ticks/sec.
[2018-10-29 06:50:45:143] [Intro] changing from '0' to '1'
[2018-10-29 06:50:45:147] [Intro] exiting state '0'
[2018-10-29 06:50:45:147] [Intro] entering state '1'
[2018-10-29 06:50:45:576] Music loaded.
[2018-10-29 06:50:52:418] [Intro] changing from '1' to '2'
[2018-10-29 06:50:52:418] [Intro] exiting state '1'
[2018-10-29 06:50:52:422] [Intro] entering state '2' for 6,00 seconds (360 frames)
[2018-10-29 06:50:52:947] [Intro] changing from '2' to '42'
[2018-10-29 06:50:52:947] [Intro] exiting state '2'
[2018-10-29 06:50:52:952] [Intro] entering state '42'
[2018-10-29 06:50:52:965] [Game] changing from 'INTRO' to 'READY'
[2018-10-29 06:50:52:965] [Game] exiting state 'INTRO'
[2018-10-29 06:50:52:965] [Game] entering state 'READY' for 4,50 seconds (270 frames)
[2018-10-29 06:50:53:152] [Clyde] entering initial state:
[2018-10-29 06:50:53:152] [Clyde] entering state 'LOCKED' for 5,00 seconds (300 frames)
[2018-10-29 06:50:53:152] [Pinky] entering initial state:
[2018-10-29 06:50:53:152] [Pinky] entering state 'LOCKED' for 3,00 seconds (180 frames)
[2018-10-29 06:50:53:152] [Blinky] entering initial state:
[2018-10-29 06:50:53:152] [Blinky] entering state 'LOCKED' for 1,00 seconds (60 frames)
[2018-10-29 06:50:53:152] [Pac-Man] entering initial state:
[2018-10-29 06:50:53:152] [Pac-Man] entering state 'HOME' for 0,25 seconds (15 frames)
[2018-10-29 06:50:53:152] [Inky] entering initial state:
[2018-10-29 06:50:53:152] [Inky] entering state 'LOCKED' for 4,00 seconds (240 frames)
[2018-10-29 06:50:58:733] [Game] changing from 'READY' to 'PLAYING' on timeout
[2018-10-29 06:50:58:733] [Game] exiting state 'READY'
[2018-10-29 06:50:58:733] [Game] entering state 'PLAYING'
[2018-10-29 06:50:58:733] [GhostAttackTimer] entering initial state:
[2018-10-29 06:50:58:733] [GhostAttackTimer] entering state 'SCATTERING' for 7,00 seconds (420 frames)
[2018-10-29 06:50:58:733] Start scattering, round 0
[2018-10-29 06:50:58:751] [Game] stays 'PLAYING' on 'StartScatteringEvent'
[2018-10-29 06:50:58:752] [Blinky] stays 'LOCKED' on 'StartScatteringEvent'
[2018-10-29 06:50:58:755] [Pinky] stays 'LOCKED' on 'StartScatteringEvent'
[2018-10-29 06:50:58:755] [Inky] stays 'LOCKED' on 'StartScatteringEvent'
[2018-10-29 06:50:58:755] [Clyde] stays 'LOCKED' on 'StartScatteringEvent'
[2018-10-29 06:50:59:041] [Pac-Man] changing from 'HOME' to 'HUNGRY' on timeout
[2018-10-29 06:50:59:041] [Pac-Man] exiting state 'HOME'
[2018-10-29 06:50:59:042] [Pac-Man] entering state 'HUNGRY'
[2018-10-29 06:50:59:189] [PacMan] publishing event 'FoodFound(Pellet)'
...
[2018-10-29 09:56:52:872] [PacMan] publishing event 'PacManGhostCollisionEvent(Pinky)'
[2018-10-29 09:56:52:872] [Game] stays 'PLAYING' on 'PacManGhostCollisionEvent(Pinky)'
[2018-10-29 09:56:52:888] [Game] changing from 'PLAYING' to 'PACMAN_DYING' on 'PacManKilledEvent(Pinky)'
[2018-10-29 09:56:52:888] [Game] exiting state 'PLAYING'
[2018-10-29 09:56:52:888] PacMan killed by Pinky at (15,23)
[2018-10-29 09:56:52:888] [Pac-Man] changing from 'HUNGRY' to 'DYING' on 'PacManKilledEvent(Pinky)'
[2018-10-29 09:56:52:888] [Pac-Man] exiting state 'HUNGRY'
[2018-10-29 09:56:52:888] [Pac-Man] entering state 'DYING' for 3,00 seconds (180 frames)
[2018-10-29 09:56:52:888] [Game] entering state 'PACMAN_DYING'
[2018-10-29 09:56:56:047] [Pac-Man] changing from 'DYING' to 'DEAD' on timeout
[2018-10-29 09:56:56:047] [Pac-Man] exiting state 'DYING'
[2018-10-29 09:56:56:047] [Pac-Man] entering state 'DEAD'
[2018-10-29 09:56:56:062] [Game] changing from 'PACMAN_DYING' to 'PLAYING'
[2018-10-29 09:56:56:062] [Game] exiting state 'PACMAN_DYING'
[2018-10-29 09:56:56:062] [Pinky] entering initial state:
[2018-10-29 09:56:56:062] [Pinky] entering state 'LOCKED' for 3,00 seconds (180 frames)
[2018-10-29 09:56:56:062] [Blinky] entering initial state:
[2018-10-29 09:56:56:062] [Blinky] entering state 'LOCKED' for 1,00 seconds (60 frames)
[2018-10-29 09:56:56:062] [Inky] entering initial state:
[2018-10-29 09:56:56:062] [Inky] entering state 'LOCKED' for 4,00 seconds (240 frames)
[2018-10-29 09:56:56:062] [Clyde] entering initial state:
[2018-10-29 09:56:56:062] [Clyde] entering state 'LOCKED' for 5,00 seconds (300 frames)
[2018-10-29 09:56:56:062] [Pac-Man] entering initial state:
[2018-10-29 09:56:56:062] [Pac-Man] entering state 'HOME' for 0,25 seconds (15 frames)
[2018-10-29 09:56:56:062] [Game] entering state 'PLAYING'
[2018-10-29 09:56:56:062] [GhostAttackTimer] entering initial state:
[2018-10-29 09:56:56:062] [GhostAttackTimer] entering state 'SCATTERING' for 7,00 seconds (420 frames)
[2018-10-29 09:56:56:062] Start scattering, round 0
```

## Actor movement ("AI")

The game gets most of its entertainment factor from the diversity of attack behavior of the four ghosts. 
In this implementation, these differences in behavior are not realized by subclassing
but by assigning a different implementation of that behavior ("strategy pattern").

<img src="doc/pacman.png"/>

### Pac-Man

Pac-Man's movement is controlled by the keyboard:

```java
int[] STEERING = { VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT };

@Override
public OptionalInt supplyIntendedDir() {
	return NESW.dirs().filter(dir -> keyDown(STEERING[dir])).findFirst();
}
```

### Ghosts
The ghosts behave identically in all of their states except the *chasing* state:

```java
getGhosts().forEach(ghost -> {
	ghost.setBehavior(FRIGHTENED, ghost.flee(pacMan));
	ghost.setBehavior(SCATTERING, ghost.headFor(ghost::getScatteringTarget));
	ghost.setBehavior(DEAD, ghost.headFor(ghost::getRevivalTile));
	ghost.setBehavior(LOCKED, ghost.bounce());
});
```

The *chasing* behavior differs for each ghost as explained below. Using the common *headFor* behavior, the individual chase behaviors like *ambush*, *attackDirectly*, *attackWithPartner* can be implemented very easily.

### Blinky (the red ghost)

Blinky's chasing behavior is to directly attack Pac-Man.

```java
blinky.setBehavior(CHASING, blinky.attackDirectly(pacMan));
```

This is the implementation:

```java
default Behavior<Ghost> attackDirectly(PacMan pacMan) {
	return headFor(pacMan::getTile);
}
```

<img src="doc/blinky.png"/>

### Pinky

Pinky, the *ambusher*, heads for the position 4 tiles ahead of Pac-Man's current position (in the original game there is an overflow error leading to a slightly different behavior):

```java
pinky.setBehavior(CHASING, pinky.ambush(pacMan, 4));
```

```java
default Behavior<Ghost> ambush(PacMan pacMan, int numTilesAhead) {
	return headFor(() -> ahead(pacMan, numTilesAhead));
}
```

<img src="doc/pinky.png"/>

### Inky (the turquoise ghost)

Inky heads for a position that depends on blinky's current position and the position two tiles ahead of Pac-Man's current position:

Consider the vector `V` from Blinky's position `B` to the position `P` two tiles ahead of Pac-Man, so `V = (P - B)`. 
Add the doubled vector to Blinky's position: `B + 2 * (P - B) = 2 * P - B` to get Inky's target:

```java
inky.setBehavior(CHASING, inky.attackWith(blinky, pacMan));
```

```java
default Behavior<Ghost> attackWith(Ghost blinky, PacMan pacMan) {
	return headFor(() -> {
		Tile b = blinky.getTile(), p = ahead(pacMan, 2);
		return new Tile(2 * p.col - b.col, 2 * p.row - b.row);
	});
}
```

<img src="doc/inky.png"/>

### Clyde (the orange ghost)

Clyde attacks Pac-Man directly (like Blinky) if his straight line distance from Pac-Man is larger than than 8 tiles. 
If closer, he behaves as in scattering mode:

```java
clyde.setBehavior(CHASING, clyde.attackOrReject(pacMan, 8 * TS));
```

```java
default Behavior<Ghost> attackOrReject(PacMan pacMan, int distance) {
	return headFor(
			() -> euclideanDist(self().tf.getCenter(), pacMan.tf.getCenter()) >= distance 
			? pacMan.getTile()
			: self().getScatteringTarget());
}
```

<img src="doc/clyde.png"/>

The visualization of the attack behaviors can be toggled during the game by pressing the key 'r' ("show routes").

### Scattering

In *scattering* mode, each ghost tries to reach his "scattering target" which is a tile outside of the maze. Because ghosts
cannot reverse direction this results in a cyclic movement around the walls in the corresponding corner of the maze:

```java
ghost.setBehavior(SCATTERING, ghost.headFor(ghost::getScatteringTarget));
```

<img src="doc/scattering.png"/>


## Graph based path finding

For simulating the ghost behavior from the original Pac-Man game, no graph based path finding is needed but the *headFor* 
behavior can be used all over the place. To also give an example how graph based path finding can be used, 
the *frightened* behavior has been implemented differently from the original game: a frightened ghost choses a safe corner
by checking the path to each maze corner and selecting the path with the largest distance to Pac-Man's current position.
The distance of a path from Pac-Man's position is defined as the minimum distance of any tile on the path from Pac-Man's
position.

Shortest paths in the maze graph can be computed with the method *Maze.findPath(Tile source, Tile target)*. 
This method runs an [A* search](http://theory.stanford.edu/~amitp/GameProgramming/AStarComparison.html)
 on the underlying grid graph to compute the shortest path. The used
[graph library](https://github.com/armin-reichert/graph) provides also other search algorithms
like Dijkstra or "Hill Climbing".

```java
GraphSearch pathfinder = new AStarSearch(grid, (u, v) -> 1, grid::manhattan);
Path path = pathfinder.findPath(cell(source), cell(target));
```

## Additional features

- The following command-line arguments are available (`java -jar pacman.jar arguments...`)
  - Scaling, e.g.: `-scale 2.5`
  - Full-screen mode on start: `-fullScreenOnStart`
  - Full-screen resolution & depth, e.g.: `-fullScreenMode 800,600,32`
  - Window title e.g.: `-title "Pac-Man Game"`
- Speed can be changed during game ('1' = normal, '2' = fast, '3' = very fast)
- Display of actor states and timers can be switched on/off at runtime (key 's')
- Display of actor routes can be switched on/off at runtime (key 'r')
- Ghosts can be enabled/disabled during the game (keys 'b', 'p', 'i', 'c')
- Cheat key 'k' kills all ghosts
- Cheat key 'e' eats all normal pellets
- Alignment of actors on the grid can be visualized (key 'g')
- Game can be paused/resumed (CTRL+p)
- Game loop frequency and (full-)screen resolution can be changed (key F2)
- Key F11 toggles between window and full-screen exclusive mode

## References

This work would not have been possible without these invaluable sources of information:

- [GameInternals - Understanding Pac-Man Ghost Behavior](http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior)
- [Gamasutra - The Pac-Man Dossier](http://www.gamasutra.com/view/feature/3938/the_pacman_dossier.php)

## Summary

The goal of this project was to provide a [Pac-Man](https://en.wikipedia.org/wiki/List_of_Pac-Man_video_games) implementation such that the game's inner workings can be understood from the code. The implementation follows the MVC pattern and uses finite state machines for the control logic of the actors and the game. The state machines are implemented in a declarative way using the *builder* pattern. 

A simple home-grown library is used for the basic game infrastructure (active rendering, game loop, full-screen mode, 
keyboard and mouse handling etc.), but it should be not too difficult to implement these infrastructure parts from scratch or 
use some real game library instead.

It could be useful to further decouple UI, model and controller to enable an easy replacement of the complete UI 
or to implement the state machines using some other state machine library.

Comments are welcome!

*Armin Reichert, October 2018*
