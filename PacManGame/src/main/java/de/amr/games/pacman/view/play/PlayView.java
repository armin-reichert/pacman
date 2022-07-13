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
package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.theme.api.GameRenderer;
import de.amr.games.pacman.theme.api.PacManRenderer;
import de.amr.games.pacman.theme.api.Theme;
import de.amr.games.pacman.theme.api.WorldRenderer;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.common.MessagesView;

/**
 * Displays the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayView implements PacManGameView {

	public final TiledWorld world;
	public final Folks folks;
	public final SoundState soundState;
	public final MessagesView messagesView;

	protected Theme theme;
	protected WorldRenderer worldRenderer;
	protected GameRenderer pointsCounterRenderer;
	protected GameRenderer livesCounterRenderer;
	protected GameRenderer levelCounterRenderer;
	protected PacManRenderer pacManRenderer;

	public PlayView(Theme theme, Folks folks, TiledWorld world) {
		this.folks = folks;
		this.world = world;
		soundState = new SoundState();
		messagesView = new MessagesView(theme, world, 15, 21);
		// this is a hack to reset the collapsing animation of Pac-Man. Need clean solution.
		folks.pacMan.ai.addStateExitListener(PacManState.DEAD, state -> pacManRenderer.resetAnimations(folks.pacMan));
		setTheme(theme);
	}

	@Override
	public void init() {
		messagesView.clearMessages();
	}

	@Override
	public void update() {
		renderSound();
	}

	@Override
	public Theme getTheme() {
		return theme;
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		messagesView.setTheme(theme);
		updateRenderers();
	}

	private void updateRenderers() {
		worldRenderer = theme.worldRenderer();
		pointsCounterRenderer = theme.gameScoreRenderer();
		livesCounterRenderer = theme.livesCounterRenderer();
		levelCounterRenderer = theme.levelCounterRenderer();
		pacManRenderer = theme.pacManRenderer();
	}

	@Override
	public void draw(Graphics2D g) {
		drawWorld(g);
		drawMessages(g);
		drawFolks(g);
		drawPointsCounter(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
	}

	protected void drawWorld(Graphics2D g) {
		worldRenderer.render(g, world);
	}

	protected void drawPointsCounter(Graphics2D g) {
		pointsCounterRenderer.render(g, PacManGame.it());
	}

	protected void drawLivesCounter(Graphics2D g) {
		g.translate(Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		livesCounterRenderer.render(g, PacManGame.it());
		g.translate(-Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}

	protected void drawLevelCounter(Graphics2D g) {
		g.translate(world.width() * Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		levelCounterRenderer.render(g, PacManGame.it());
		g.translate(-world.width() * Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}

	protected void drawPacMan(Graphics2D g, PacMan pacMan) {
		pacManRenderer.render(g, pacMan);
	}

	protected void drawGhost(Graphics2D g, Ghost ghost) {
		theme.ghostRenderer().render(g, ghost);
	}

	protected void drawFolks(Graphics2D g) {
		folks.ghostsInWorld().filter(ghost -> ghost.ai.is(DEAD, ENTERING_HOUSE, FRIGHTENED))
				.forEach(ghost -> drawGhost(g, ghost));
		drawPacMan(g, folks.pacMan);
		folks.ghostsInWorld().filter(ghost -> !ghost.ai.is(DEAD, ENTERING_HOUSE, FRIGHTENED))
				.forEach(ghost -> drawGhost(g, ghost));
	}

	protected void drawMessages(Graphics2D g) {
		messagesView.draw(g);
	}

	private void renderSound() {
		// Pac-Man
		long starvingMillis = System.currentTimeMillis() - soundState.lastMealAt;
		if (starvingMillis > 300) {
			theme.sounds().clipCrunching().stop();
		} else if (!theme.sounds().clipCrunching().isRunning()) {
			theme.sounds().clipCrunching().loop();
		}
		if (!folks.pacMan.ai.is(PacManState.POWERFUL)) {
			theme.sounds().clipWaza().stop();
		} else if (!theme.sounds().clipWaza().isRunning()) {
			theme.sounds().clipWaza().loop();
		}
		if (soundState.pacManDied) {
			theme.sounds().clipPacManDies().play();
			soundState.pacManDied = false;
		}
		if (soundState.bonusEaten) {
			theme.sounds().clipEatFruit().play();
			soundState.bonusEaten = false;
		}
		if (soundState.gotExtraLife) {
			theme.sounds().clipExtraLife().play();
			soundState.gotExtraLife = false;
		}

		// Ghosts
		if (!soundState.chasingGhosts) {
			theme.sounds().clipGhostChase().stop();
		} else if (!theme.sounds().clipGhostChase().isRunning()) {
			theme.sounds().clipGhostChase().setVolume(0.5f);
			theme.sounds().clipGhostChase().loop();
		}
		if (!soundState.deadGhosts) {
			theme.sounds().clipGhostDead().stop();
		} else if (!theme.sounds().clipGhostDead().isRunning()) {
			theme.sounds().clipGhostDead().loop();
		}
		if (soundState.ghostEaten) {
			theme.sounds().clipEatGhost().play();
			soundState.ghostEaten = false;
		}
	}
}