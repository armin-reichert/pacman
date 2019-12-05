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
- Ghost attack timer ([GhostAttackTimer](PacManGame/src/main/java/de/amr/games/pacman/controller/GhostAttackTimer.java))
- Pac-Man controller ([Pac-Man](PacManGame/src/main/java/de/amr/games/pacman/actor/PacMan.java))
- Ghost controller ([Ghost](PacManGame/src/main/java/de/amr/games/pacman/actor/Ghost.java))

All these state machines are implemented in a declarative way (*builder pattern*). A single 
large Java expression defines the complete state graph together with node and edge annotations representing the actions,
 conditions, event conditions and timers. Lambda expressions (anonymous functions) and function references allow to embed code directly inside the state machine definition. If the state definition becomes more complex it is possible to implement it in a separate state class. Both variants are used here.

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
state machine. Further, the more complex states are implemented as subclasses of the generic `State` class. This
has the advantage that actions which are state-specific can be realized as methods of the state subclass.

```java
beginStateMachine()

	.description("[GameController]")
	.initialState(INTRO)

	.states()

		.state(INTRO)
			.onEntry(() -> {
				showUI(introView);
				introView.theme.snd_insertCoin().play();
				introView.theme.loadMusic();
			})

		.state(GETTING_READY)
			.timeoutAfter(() -> sec(5))
			.onEntry(() -> {
				game.reset();
				ensemble.theme.snd_clips_all().forEach(Sound::stop);
				ensemble.theme.snd_ready().play();
				ensemble.actors().forEach(Actor::activate);
				ensemble.actors().forEach(Actor::init);
				ensemble.clearBonus();
				playView.init();
				playView.showScores = true;
				playView.enableAnimations(false);
				playView.showInfoText("Ready!", Color.YELLOW);
			})

		.state(START_PLAYING)
			.timeoutAfter(() -> sec(1.7f))
			.onEntry(() -> {
				game.startLevel();
				ensemble.ghosts().forEach(ghost -> ghost.foodCount = 0);
				ghostAttackTimer.init();
				playView.clearInfoText();
				playView.enableAnimations(true);
				playView.startEnergizerBlinking();
				ensemble.theme.music_playing().volume(.90f);
				ensemble.theme.music_playing().loop();
			})
			.onTick(() -> {
				ensemble.activeGhosts().forEach(Ghost::update);
			})

		.state(PLAYING)
			.impl(playingState = new PlayingState())

		.state(CHANGING_LEVEL)
			.impl(new ChangingLevelState())

		.state(GHOST_DYING)
			.impl(new GhostDyingState())
			.timeoutAfter(Ghost::getDyingTime)

		.state(PACMAN_DYING)
			.onEntry(() -> {
				state().setTimerFunction(() -> game.lives > 1 ? sec(6) : sec(4));
				state().resetTimer();
				ensemble.theme.music_playing().stop();
				if (!app().settings.getAsBoolean("pacMan.immortable")) {
					game.lives -= 1;
				}
			})
			.onTick(() -> {
				if (state().getTicksConsumed() < sec(2)) {
					ensemble.pacMan.update();
				}
				if (state().getTicksConsumed() == sec(1)) {
					ensemble.activeGhosts().forEach(Ghost::hide);
				}
				if (game.lives > 0 && state().getTicksConsumed() == sec(4)) {
					ensemble.activeActors().forEach(Actor::init);
					ensemble.activeGhosts().forEach(Ghost::show);
					playView.init();
					ensemble.theme.music_playing().loop();
				}
			})

		.state(GAME_OVER)
			.timeoutAfter(() -> sec(60))
			.onEntry(() -> {
				LOGGER.info("Game is over");
				game.score.save();
				ensemble.activeGhosts().forEach(Ghost::show);
				ensemble.clearBonus();
				playView.enableAnimations(false);
				ensemble.theme.music_gameover().loop();
				playView.showInfoText("Game Over!", Color.RED);
			})
			.onExit(() -> {
				ensemble.theme.music_gameover().stop();
				playView.clearInfoText();
			})

	.transitions()

		.when(INTRO).then(GETTING_READY)
			.condition(() -> introView.isComplete() || app().settings.getAsBoolean("skipIntro"))
			.act(() -> showUI(playView))

		.when(GETTING_READY).then(START_PLAYING)
			.onTimeout()

		.when(START_PLAYING).then(PLAYING)
			.onTimeout()

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

		.when(GHOST_DYING).then(PLAYING)
			.onTimeout()

		.when(PACMAN_DYING).then(GAME_OVER)
			.onTimeout()
			.condition(() -> game.lives == 0)

		.when(PACMAN_DYING).then(PLAYING)
			.onTimeout()
			.condition(() -> game.lives > 0)

		.when(GAME_OVER).then(GETTING_READY)
			.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

		.when(GAME_OVER).then(INTRO)
			.onTimeout()

.endStateMachine();
```

The **attack waves** (scattering, chasing) of the ghosts with their level-specific timing are also realized as a state machine:

```java
beginStateMachine()
	.description("[GhostAttackTimer]")
	.initialState(SCATTERING)
.states()
	.state(SCATTERING)
		.timeoutAfter(() -> game.scatterTicks(round))
		.onEntry(this::logStateEntry)
	.state(CHASING)
		.timeoutAfter(() -> game.chasingTicks(round))
		.onEntry(this::logStateEntry)
		.onExit(() -> ++round)
.transitions()
	.when(SCATTERING).then(CHASING).onTimeout()
	.when(CHASING).then(SCATTERING).onTimeout()
.endStateMachine();
```

**Pac-Man** himself is also controlled by a state machine, the main state *HUNGRY* is realized by a separate state subclass.

```java
beginStateMachine(PacManState.class, PacManGameEvent.class)

	.description("[Pac-Man]")
	.initialState(HOME)

	.states()

		.state(HOME)

		.state(HUNGRY)
			.impl(new HungryState())

		.state(DYING)
			.timeoutAfter(() -> sec(4f))
			.onEntry(() -> {
				sprites.select("full");
				ensemble.theme.snd_clips_all().forEach(Sound::stop);
			})
			.onTick(() -> {
				if (state().getTicksRemaining() == sec(2.5f)) {
					sprites.select("dying");
					ensemble.theme.snd_die().play();
					ensemble.activeGhosts().forEach(Ghost::hide);
				}
			})

	.transitions()

		.when(HOME).then(HUNGRY)

		.stay(HUNGRY)
			.on(PacManGainsPowerEvent.class)
			.act(() -> {
				state().setTimerFunction(() -> sec(game.level.pacManPowerSeconds));
				state().resetTimer();
				LOGGER.info(() -> String.format("Pac-Man got power for %d ticks (%d sec)", 
						state().getDuration(), state().getDuration() / 60));
				ensemble.theme.snd_waza().loop();
			})

		.when(HUNGRY).then(DYING)
			.on(PacManKilledEvent.class)

		.when(DYING).then(DEAD)
			.onTimeout()

.endStateMachine();
```

Finally, each **ghosts** is controlled by its own instance of the following state machine:

```java
beginStateMachine(GhostState.class, PacManGameEvent.class)

	.description(String.format("[%s]", name))
	.initialState(LOCKED)

	.states()

		.state(LOCKED)
			.onTick(() -> walkAndDisplayAs("color-" + moveDir))
			.onExit(() -> {
				enteredNewTile = true;
				ensemble.pacMan.ticksSinceLastMeal = 0;
			})

		.state(LEAVING_HOUSE)
			.onEntry(() -> targetTile = maze.blinkyHome)
			.onTick(() -> walkAndDisplayAs("color-" + moveDir))
			.onExit(() -> moveDir = nextDir = Top4.W)

		.state(ENTERING_HOUSE)
			.onEntry(() -> targetTile = revivalTile)
			.onTick(() -> walkAndDisplayAs("eyes-" + moveDir))

		.state(SCATTERING)
			.onEntry(() -> targetTile = scatterTile)
			.onTick(() -> walkAndDisplayAs("color-" + moveDir))

		.state(CHASING)
			.onEntry(() -> ensemble.chasingSoundOn())
			.onTick(() -> {
				targetTile = fnChasingTarget.get();
				walkAndDisplayAs("color-" + moveDir);
			})
			.onExit(() -> ensemble.chasingSoundOff(this))

		.state(FRIGHTENED)
			.onTick(() -> walkAndDisplayAs(ensemble.pacMan.isLosingPower() ? "flashing" : "frightened"))

		.state(DYING)
			.timeoutAfter(Ghost::getDyingTime)
			.onEntry(() -> {
				sprites.select("value-" + game.numGhostsKilledByCurrentEnergizer);
			})

		.state(DEAD)
			.onEntry(() -> {
				targetTile = maze.blinkyHome;
				ensemble.deadSoundOn();
			})
			.onTick(() -> walkAndDisplayAs("eyes-" + moveDir))
			.onExit(() -> ensemble.deadSoundOff(this))

	.transitions()

		.when(LOCKED).then(LEAVING_HOUSE)
			.on(GhostUnlockedEvent.class)

		.when(LEAVING_HOUSE).then(SCATTERING)
			.condition(() -> leftHouse() && nextState() == SCATTERING)

		.when(LEAVING_HOUSE).then(CHASING)
			.condition(() -> leftHouse() && nextState() == CHASING)

		.when(ENTERING_HOUSE).then(LOCKED)
			.condition(() -> tile() == targetTile)

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
			.condition(() -> tile().equals(maze.blinkyHome))

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
[2019-12-05 05:49:43:838] Launching application 'PacManApp'  
[2019-12-05 05:49:44:226] Entered window mode, resolution 537x691 (224x288 scaled by 2,40) 
[2019-12-05 05:49:44:230] Application shell created. 
[2019-12-05 05:49:44:234] Clock frequency is 60 ticks/sec. 
[2019-12-05 05:49:44:674] Pac-Man sprites extracted. 
[2019-12-05 05:49:44:684] Theme 'ClassicPacManTheme' created. 
[2019-12-05 05:49:44:865] Controller set: de.amr.games.pacman.controller.PacManGameController@61112446 
[2019-12-05 05:49:45:137] Loading music... 
[2019-12-05 05:49:45:137] Controller initialized. 
[2019-12-05 05:49:45:149] Application state changes from 'NEW' to 'INITIALIZED' 
[2019-12-05 05:49:45:153] Application initialized. 
[2019-12-05 05:49:45:153] Clock started, running with 60 ticks/sec. 
[2019-12-05 05:49:45:153] Application state changes from 'INITIALIZED' to 'RUNNING' 
[2019-12-05 05:49:46:902] Music loaded. 
[2019-12-05 05:49:48:312] State machine logging is INFO 
[2019-12-05 05:49:53:970] [Intro] changing from 'CHASING_EACH_OTHER' to 'READY_TO_PLAY' 
[2019-12-05 05:49:53:970] [Intro] exiting state 'CHASING_EACH_OTHER' 
[2019-12-05 05:49:53:974] [Intro] entering state 'READY_TO_PLAY' for 6,00 seconds (360 frames) 
[2019-12-05 05:49:59:517] [GameController] changing from 'INTRO' to 'GETTING_READY' 
[2019-12-05 05:49:59:517] [GameController] exiting state 'INTRO' 
[2019-12-05 05:49:59:520] [GameController] entering state 'GETTING_READY' for 5,00 seconds (300 frames) 
[2019-12-05 05:49:59:648] [Pac-Man] entering initial state: 
[2019-12-05 05:49:59:648] [Pac-Man] entering state 'HOME' 
[2019-12-05 05:49:59:652] Pac-Man activated 
[2019-12-05 05:49:59:652] [Blinky] entering initial state: 
[2019-12-05 05:49:59:652] [Blinky] entering state 'LOCKED' 
[2019-12-05 05:49:59:652] Blinky activated 
[2019-12-05 05:49:59:652] [Pinky] entering initial state: 
[2019-12-05 05:49:59:652] [Pinky] entering state 'LOCKED' 
[2019-12-05 05:49:59:652] Pinky activated 
[2019-12-05 05:49:59:652] [Inky] entering initial state: 
[2019-12-05 05:49:59:652] [Inky] entering state 'LOCKED' 
[2019-12-05 05:49:59:652] Inky activated 
[2019-12-05 05:49:59:656] [Clyde] entering initial state: 
[2019-12-05 05:49:59:656] [Clyde] entering state 'LOCKED' 
[2019-12-05 05:49:59:656] Clyde activated 
[2019-12-05 05:49:59:656] [Pac-Man] entering initial state: 
[2019-12-05 05:49:59:656] [Pac-Man] entering state 'HOME' 
[2019-12-05 05:49:59:656] [Blinky] entering initial state: 
[2019-12-05 05:49:59:656] [Blinky] entering state 'LOCKED' 
[2019-12-05 05:49:59:656] [Pinky] entering initial state: 
[2019-12-05 05:49:59:656] [Pinky] entering state 'LOCKED' 
[2019-12-05 05:49:59:656] [Inky] entering initial state: 
[2019-12-05 05:49:59:656] [Inky] entering state 'LOCKED' 
[2019-12-05 05:49:59:656] [Clyde] entering initial state: 
[2019-12-05 05:49:59:656] [Clyde] entering state 'LOCKED' 
[2019-12-05 05:50:04:506] [GameController] changing from 'GETTING_READY' to 'START_PLAYING (timeout)' 
[2019-12-05 05:50:04:506] [GameController] exiting state 'GETTING_READY' 
[2019-12-05 05:50:04:509] [GameController] entering state 'START_PLAYING' for 1,70 seconds (102 frames) 
[2019-12-05 05:50:04:509] Start game level 1 
[2019-12-05 05:50:04:510] Initialize ghost attack timer 
[2019-12-05 05:50:04:510] [GhostAttackTimer] entering initial state: 
[2019-12-05 05:50:04:514] [GhostAttackTimer] entering state 'SCATTERING' for 7,00 seconds (420 frames) 
[2019-12-05 05:50:04:514] Start SCATTERING for 420 ticks (7,00 seconds) 
[2019-12-05 05:50:06:197] [GameController] changing from 'START_PLAYING' to 'PLAYING (timeout)' 
[2019-12-05 05:50:06:197] [GameController] exiting state 'START_PLAYING' 
[2019-12-05 05:50:06:198] [GameController] entering state 'PLAYING' 
[2019-12-05 05:50:06:214] [Pac-Man] changing from 'HOME' to 'HUNGRY' 
[2019-12-05 05:50:06:214] [Pac-Man] exiting state 'HOME' 
[2019-12-05 05:50:06:214] [Pac-Man] entering state 'HUNGRY' 
[2019-12-05 05:50:06:221] [Blinky] changing from 'LOCKED' to 'LEAVING_HOUSE' on 'GhostUnlockedEvent' 
[2019-12-05 05:50:06:221] [Blinky] exiting state 'LOCKED' 
[2019-12-05 05:50:06:221] [Blinky] entering state 'LEAVING_HOUSE' 
[2019-12-05 05:50:06:225] [Pinky] changing from 'LOCKED' to 'LEAVING_HOUSE' on 'GhostUnlockedEvent' 
[2019-12-05 05:50:06:225] [Pinky] exiting state 'LOCKED' 
[2019-12-05 05:50:06:225] [Pinky] entering state 'LEAVING_HOUSE' 
[2019-12-05 05:50:06:245] [Blinky] changing from 'LEAVING_HOUSE' to 'SCATTERING' 
[2019-12-05 05:50:06:245] [Blinky] exiting state 'LEAVING_HOUSE' 
[2019-12-05 05:50:06:249] [Blinky] entering state 'SCATTERING' 
[2019-12-05 05:50:06:292] [GameController] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2019-12-05 05:50:06:412] [GameController] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2019-12-05 05:50:06:530] [GameController] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2019-12-05 05:50:06:647] [GameController] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2019-12-05 05:50:06:767] [GameController] stays 'PLAYING' on 'FoodFound(Pellet)' 
[2019-12-05 05:50:06:767] [Pinky] changing from 'LEAVING_HOUSE' to 'SCATTERING' 
[2019-12-05 05:50:06:767] [Pinky] exiting state 'LEAVING_HOUSE' 
[2019-12-05 05:50:06:767] [Pinky] entering state 'SCATTERING' 
```

## Pac-Man steering

Pac-Man is steered by holding a key indicating its **intended** direction. As soon as Pac-Man reaches a tile where it can move towards this direction it changes its move direction accordingly. ("Cornering" is not yet implemented). In the code, this is implemented by setting the steering function as shown below. This makes it very easy to replace the manual steering by some sort of automatic steering ("AI"):

```java
pacMan = new PacMan(game);
pacMan.steering = steeredByKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT);

...

static Steering<PacMan> steeredByKeys(int... keys) {
	return pacMan -> NESW.dirs().filter(dir -> Keyboard.keyDown(keys[dir])).findAny().ifPresent(pacMan::setNextDir);
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
