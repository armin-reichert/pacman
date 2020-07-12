package de.amr.games.pacman.test;

import static de.amr.games.pacman.controller.game.SpeedLimits.pacManSpeedLimit;
import static de.amr.games.pacman.controller.game.SpeedLimits.speedLimit;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.api.Creature;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.sound.PacManSounds;
import de.amr.games.pacman.controller.world.arcade.ArcadeWorld;
import de.amr.games.pacman.controller.world.arcade.ArcadeWorldFolks;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.Themes;
import de.amr.games.pacman.view.theme.api.Theme;

public class TestUI implements Lifecycle, VisualController {

	protected final ArcadeWorld world;
	protected final ArcadeWorldFolks folks;
	protected final PacMan pacMan;
	protected final Ghost blinky, pinky, inky, clyde;
	protected final PacManSounds soundManager;
	protected PlayView view;
	protected Theme[] themes = { Themes.ARCADE_THEME, Themes.BLOCKS_THEME, Themes.LETTERS_THEME };
	protected int currentThemeIndex = 0;
	protected final Game game;

	@Override
	public Optional<View> currentView() {
		return Optional.of(view);
	}

	protected Stream<Ghost> ghostsOnStage() {
		return folks.ghosts().filter(world::contains);
	}

	protected void include(Creature... creatures) {
		Stream.of(creatures).forEach(world::bringIn);
	}

	public TestUI() {
		world = new ArcadeWorld();
		world.clearFood();
		folks = new ArcadeWorldFolks(world);

		pacMan = folks.pacMan();
		blinky = folks.blinky();
		pinky = folks.pinky();
		inky = folks.inky();
		clyde = folks.clyde();

		soundManager = new PacManSounds(world, folks);

		game = new Game(1, world.totalFoodCount());
		folks.ghosts().forEach(ghost -> ghost.getReadyToRumble(game));

		pacMan.setSpeedLimit(() -> pacManSpeedLimit(pacMan, game));
		folks.ghosts().forEach(ghost -> ghost.setSpeedLimit(() -> speedLimit(ghost, game)));

		view = new PlayView(world, themes[currentThemeIndex], folks, game, null, null);
		view.turnScoresOff();
		view.init();
	}

	@Override
	public void init() {
		folks.all().forEach(Creature::init);
		folks.all().forEach(creature -> world.takeOut(creature));
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce("z")) {
			currentThemeIndex = (currentThemeIndex + 1) % themes.length;
			view.setTheme(themes[currentThemeIndex]);
		}
		if (Keyboard.keyPressedOnce("g")) {
			if (view.isShowingGrid()) {
				view.turnGridOff();
			} else {
				view.turnGridOn();
			}
		}
		if (Keyboard.keyPressedOnce("r")) {
			if (view.isShowingRoutes()) {
				view.turnRoutesOff();
			} else {
				view.turnRoutesOn();
			}
		}
		if (Keyboard.keyPressedOnce("s")) {
			if (view.isShowingStates()) {
				view.turnStatesOff();
			} else {
				view.turnStatesOn();
			}
		}
		folks.all().filter(world::contains).forEach(Creature::update);
		view.update();
	}
}