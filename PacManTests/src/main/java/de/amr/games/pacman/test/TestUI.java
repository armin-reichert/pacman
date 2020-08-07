package de.amr.games.pacman.test;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.controller.creatures.Creature;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.play.EnhancedPlayView;
import de.amr.games.pacman.view.theme.Themes;
import de.amr.games.pacman.view.theme.arcade.ArcadeSounds;

public class TestUI implements Lifecycle, VisualController {

	protected final ArcadeWorld world;
	protected final Folks folks;
	protected final PacMan pacMan;
	protected final Ghost blinky, pinky, inky, clyde;
	protected final ArcadeSounds sounds;
	protected EnhancedPlayView view;
	protected Theme[] themes = Themes.all().toArray(Theme[]::new);
	protected int currentThemeIndex = 0;
	protected final Game game;

	@Override
	public Optional<View> currentView() {
		return Optional.of(view);
	}

	protected Stream<Ghost> ghostsOnStage() {
		return folks.ghosts().filter(world::contains);
	}

	protected void include(Creature<?, ?>... creatures) {
		Stream.of(creatures).forEach(world::include);
	}

	protected Theme theme() {
		return themes[currentThemeIndex];
	}

	public TestUI() {
		world = new ArcadeWorld();
		world.clearFood();
		game = new Game(1, world.totalFoodCount());
		folks = new Folks(world, world.house(0));
		folks.all().forEach(guy -> guy.getReadyToRumble(game));
		pacMan = folks.pacMan;
		blinky = folks.blinky;
		pinky = folks.pinky;
		inky = folks.inky;
		clyde = folks.clyde;
		sounds = new ArcadeSounds();
		view = new EnhancedPlayView(world, theme(), folks, game, null, null);
		view.turnScoresOff();
		view.init();
	}

	@Override
	public void init() {
		folks.all().forEach(Creature::init);
		folks.all().forEach(guy -> world.exclude(guy));
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
		folks.all().filter(world::contains).forEach(Creature::update);
		view.update();
	}
}