/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.ui.AppShell;
import de.amr.easy.game.ui.f2dialog.F2Dialog;
import de.amr.games.pacman.controller.game.ExtendedGameController;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.theme.api.Themes;
import de.amr.games.pacman.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.theme.blocks.BlocksTheme;
import de.amr.games.pacman.theme.letters.LettersTheme;
import de.amr.games.pacman.view.dashboard.fsm.FsmView;
import de.amr.games.pacman.view.dashboard.level.GameLevelView;
import de.amr.games.pacman.view.dashboard.states.GameStateView;
import de.amr.games.pacman.view.dashboard.theme.ThemeSelectionView;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;

/**
 * The Pac-Man game application with inspection views and lots of bells and whistles.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManAppEnhanced extends PacManApp {

	static {
		Themes.registerTheme(ArcadeTheme.THEME);
		Themes.registerTheme(BlocksTheme.THEME);
		Themes.registerTheme(LettersTheme.THEME);
	}

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
		Graphviz.useEngine(new GraphvizV8Engine());
		setIcon("/images/pacman-icon.png");
		setController(new ExtendedGameController(Themes.all()));
	}

	@Override
	public void configureF2Dialog(F2Dialog f2) {
		AppShell shell = shell().get();
		f2.setSize(700, shell.getHeight());
		f2.setRelativeLocation(shell.getWidth(), 0);

		ExtendedGameController gameController = (ExtendedGameController) getController();

		ThemeSelectionView themeSelectionView = new ThemeSelectionView();
		themeSelectionView.attachTo(gameController);
		f2.addCustomTab("Theme", themeSelectionView, () -> true);

		FsmView fsmView = new FsmView();
		f2.addCustomTab("State Machines", fsmView, () -> true);

		GameStateView gameStateView = new GameStateView();
		gameStateView.attachTo(gameController, gameController.folks);
		f2.addCustomTab("Game State", gameStateView, PacManGame::started);

		GameLevelView gameLevelView = new GameLevelView();
		gameLevelView.attachTo(gameController);
		f2.addCustomTab("Game Level", gameLevelView, PacManGame::started);
	}
}