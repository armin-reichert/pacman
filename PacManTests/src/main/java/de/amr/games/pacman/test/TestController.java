package de.amr.games.pacman.test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.theme.api.Theme;
import de.amr.games.pacman.theme.api.Themes;
import de.amr.games.pacman.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.theme.blocks.BlocksTheme;
import de.amr.games.pacman.theme.letters.LettersTheme;
import de.amr.games.pacman.view.play.ExtendedPlayView;

/**
 * Common controller for test applications.
 * 
 * @author Armin Reichert
 */
public class TestController implements VisualController {

	static {
		Themes.registerTheme(ArcadeTheme.THEME);
		Themes.registerTheme(BlocksTheme.THEME);
		Themes.registerTheme(LettersTheme.THEME);
	}

	protected final ArcadeWorld world;
	protected final Folks folks;
	protected final PacMan pacMan;
	protected final Ghost blinky, pinky, inky, clyde;
	protected final List<Theme> themes;
	protected final ExtendedPlayView view;
	protected int currentThemeIndex;

	public TestController() {
		world = new ArcadeWorld();
		world.tiles().forEach(world::removeFood);
		PacManGame.start(1, world.totalFoodCount());
		var house = world.house(0).orElseThrow();
		folks = new Folks(world, house);
		pacMan = folks.pacMan;
		blinky = folks.blinky;
		pinky = folks.pinky;
		inky = folks.inky;
		clyde = folks.clyde;
		themes = Themes.all();
		currentThemeIndex = themes.indexOf(ArcadeTheme.THEME);
		view = new ExtendedPlayView(theme(), folks, null, world);
		view.turnScoresOff();
	}

	@Override
	public void init() {
		folks.guys().forEach(Lifecycle::init);
		folks.guys().forEach(guy -> world.exclude(guy));
		view.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce("z")) {
			currentThemeIndex = (currentThemeIndex + 1) % themes.size();
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
		folks.guysInWorld().forEach(Lifecycle::update);
		view.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(view);
	}

	protected void include(Guy... guys) {
		Stream.of(guys).forEach(guy -> world.include(guy));
	}

	protected Theme theme() {
		return themes.get(currentThemeIndex);
	}
}