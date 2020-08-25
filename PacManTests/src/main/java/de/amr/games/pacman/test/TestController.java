package de.amr.games.pacman.test;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.play.ExtendedPlayView;
import de.amr.games.pacman.view.theme.Themes;

/**
 * Common controller for test applications.
 * 
 * @author Armin Reichert
 */
public class TestController implements VisualController {

	protected final Game game;
	protected final ArcadeWorld world;
	protected final Folks folks;
	protected final PacMan pacMan;
	protected final Ghost blinky, pinky, inky, clyde;
	protected final Theme[] themes;
	protected final ExtendedPlayView view;

	protected int currentThemeIndex = 0;

	public TestController() {
		world = new ArcadeWorld();
		world.tiles().forEach(world::eatFood);
		game = new Game();
		game.nextLevel(world);
		folks = new Folks(world, world.house(0));
		folks.guys().forEach(guy -> guy.game = game);
		pacMan = folks.pacMan;
		blinky = folks.blinky;
		pinky = folks.pinky;
		inky = folks.inky;
		clyde = folks.clyde;
		themes = Themes.all().toArray(Theme[]::new);
		view = new ExtendedPlayView(theme(), folks, null, game, world);
		view.turnScoresOff();
	}

	@Override
	public void init() {
		folks.guys().forEach(Guy::init);
		folks.guys().forEach(guy -> world.exclude(guy));
		view.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce("z")) {
			currentThemeIndex = (currentThemeIndex + 1) % themes.length;
			view.setTheme(theme());
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
		folks.guysInWorld().forEach(Guy::update);
		view.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(view);
	}

	protected void include(Guy<?>... guys) {
		Stream.of(guys).forEach(guy -> world.include(guy));
	}

	protected Theme theme() {
		return themes[currentThemeIndex];
	}
}