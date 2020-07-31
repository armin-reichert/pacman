package de.amr.games.pacman;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.ui.AppShell;
import de.amr.easy.game.ui.f2dialog.F2Dialog;
import de.amr.games.pacman.controller.game.EnhancedGameController;
import de.amr.games.pacman.view.dashboard.fsm.FsmView;
import de.amr.games.pacman.view.dashboard.level.GameLevelView;
import de.amr.games.pacman.view.dashboard.states.GameStateView;
import de.amr.games.pacman.view.dashboard.theme.ThemeSelectionView;
import de.amr.games.pacman.view.theme.Themes;
import de.amr.games.pacman.view.theme.api.Theme;

/**
 * The Pac-Man game application with inspection views and lots of bells and whistles.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManAppEnhanced extends PacManApp {

	public static void main(String[] args) {
		launch(PacManAppEnhanced.class, settings, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Pac-Man Enhanced";
	}

	@Override
	public void init() {
		setIcon("/images/pacman-icon.png");
		setController(new EnhancedGameController(Themes.all().toArray(Theme[]::new)));
	}

	@Override
	public void configureF2Dialog(F2Dialog f2) {
		AppShell shell = shell().get();
		f2.setSize(700, shell.getHeight());
		f2.setRelativeLocation(shell.getWidth(), 0);

		EnhancedGameController gameController = (EnhancedGameController) getController();

		ThemeSelectionView themeSelectionView = new ThemeSelectionView();
		themeSelectionView.attachTo(gameController);
		f2.addCustomTab("Theme", themeSelectionView, () -> true);

		FsmView fsmView = new FsmView();
		f2.addCustomTab("State Machines", fsmView, () -> true);

		GameStateView gameStateView = new GameStateView();
		gameStateView.attachTo(gameController, gameController.folks());
		f2.addCustomTab("Game State", gameStateView, () -> gameController.game().isPresent());

		GameLevelView gameLevelView = new GameLevelView();
		gameLevelView.attachTo(gameController);
		f2.addCustomTab("Game Level", gameLevelView, () -> gameController.game().isPresent());
	}
}