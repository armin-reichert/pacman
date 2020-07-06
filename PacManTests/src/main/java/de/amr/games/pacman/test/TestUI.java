package de.amr.games.pacman.test;

import static de.amr.games.pacman.controller.SpeedLimits.ghostSpeedLimit;
import static de.amr.games.pacman.controller.SpeedLimits.pacManSpeedLimit;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.ArcadeGameFolks;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.sound.PacManSoundManager;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.Universe;
import de.amr.games.pacman.model.world.api.Population;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.Themes;

public class TestUI implements Lifecycle, VisualController {

	protected final World world;
	protected final Game game;
	protected final Population people;
	protected final PacMan pacMan;
	protected final Ghost blinky, pinky, inky, clyde;
	protected final PacManSoundManager soundManager;
	protected PlayView view;
	protected Theme[] themes = { Themes.ARCADE_THEME, Themes.BLOCKS_THEME, Themes.ASCII_THEME };
	protected int currentThemeIndex = 0;

	@Override
	public Optional<View> currentView() {
		return Optional.of(view);
	}

	protected Stream<Ghost> ghostsOnStage() {
		return world.population().ghosts().filter(world::included);
	}

	protected void include(Creature<?>... creatures) {
		Stream.of(creatures).forEach(world::include);
	}

	public TestUI() {
		world = Universe.arcadeWorld();
		world.clearFood();
		people = new ArcadeGameFolks();
		people.populate(world);

		pacMan = people.pacMan();
		blinky = people.blinky();
		pinky = people.pinky();
		inky = people.inky();
		clyde = people.clyde();

		soundManager = new PacManSoundManager(world);

		game = new Game(1, world.totalFoodCount());
		people.play(game);

		pacMan.setSpeedLimit(() -> pacManSpeedLimit(pacMan, game));
		world.population().ghosts().forEach(ghost -> ghost.setSpeedLimit(() -> ghostSpeedLimit(ghost, game)));

		view = new PlayView(world, game, null, null);
		view.setTheme(themes[currentThemeIndex]);
		view.turnScoresOff();
		view.init();
	}

	@Override
	public void init() {
		world.population().creatures().forEach(Creature::init);
		world.population().creatures().forEach(creature -> world.exclude(creature));
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
		world.population().creatures().filter(world::included).forEach(Creature::update);
	}
}