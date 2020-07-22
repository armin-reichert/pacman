package de.amr.games.pacman;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.ui.AppShell;
import de.amr.easy.game.ui.f2dialog.F2Dialog;
import de.amr.games.pacman.controller.StateMachineRegistry;
import de.amr.games.pacman.controller.game.EnhancedGameController;
import de.amr.games.pacman.view.dashboard.fsm.FsmView;
import de.amr.games.pacman.view.dashboard.level.GameLevelView;
import de.amr.games.pacman.view.dashboard.states.GameStateView;
import de.amr.games.pacman.view.dashboard.theme.ThemeSelectionView;

public class PacManAppEnhanced extends PacManApp {

	public static void main(String[] args) {
		StateMachineRegistry.IT.setLogging(false);
		launch(PacManAppEnhanced.class, settings, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Pac-Man Enhanced";
	}

	@Override
	public void configureF2Dialog(F2Dialog f2) {
		ThemeSelectionView themeSelectionView = new ThemeSelectionView();
		GameStateView gameStateView = new GameStateView();
		GameLevelView gameLevelView = new GameLevelView();
		FsmView fsmView = new FsmView();
		EnhancedGameController gameController = (EnhancedGameController) getController();
		gameStateView.attachTo(gameController, gameController.folks());
		gameLevelView.attachTo(gameController);
		themeSelectionView.attachTo(gameController);
		f2.addCustomTab("Theme", themeSelectionView, () -> true);
		f2.addCustomTab("State Machines", fsmView, () -> true);
		f2.addCustomTab("Game State", gameStateView, () -> gameController.game().isPresent());
		f2.addCustomTab("Game Level", gameLevelView, () -> gameController.game().isPresent());
		AppShell shell = shell().get();
		f2.setSize(700, shell.getHeight());
		f2.setRelativeLocation(shell.getWidth() + 5, 0);
	}

	@Override
	public void init() {
		loginfo("Finite-state machine logging is " + StateMachineRegistry.IT.isLoggingEnabled());
		setIcon("/images/pacman-icon.png");
		setController(new EnhancedGameController());
	}
}