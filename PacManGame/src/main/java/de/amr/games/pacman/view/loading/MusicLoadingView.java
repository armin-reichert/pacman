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
package de.amr.games.pacman.view.loading;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Tile;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.theme.api.MessagesRenderer;
import de.amr.games.pacman.theme.api.PacManRenderer;
import de.amr.games.pacman.theme.api.Theme;
import de.amr.games.pacman.view.api.PacManGameView;

/**
 * View displayed while the music files are loaded.
 * 
 * @author Armin Reichert
 */
public class MusicLoadingView implements PacManGameView {

	private final ArcadeWorld world = new ArcadeWorld();
	private final Folks folks = new Folks(world, world.house(0).get());
	private final List<Ghost> ghosts = folks.ghosts().collect(Collectors.toList());
	private Theme theme;
	private PacManRenderer pacManRenderer;
	private MessagesRenderer messagesRenderer;

	private final int width;
	private final int height;
	private int alpha;
	private int alphaInc;
	private int ghostCount;
	private int ghostInc;
	private Random rnd = new Random();

	public MusicLoadingView(Theme theme) {
		this.width = PacManApp.appSettings.width;
		this.height = PacManApp.appSettings.height;
		setTheme(theme);
		init();
	}

	@Override
	public Theme getTheme() {
		return theme;
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		pacManRenderer = theme.pacManRenderer();
		messagesRenderer = theme.messagesRenderer();
	}

	@Override
	public void init() {
		ghostCount = 0;
		ghostInc = 1;
		ghosts.forEach(ghost -> ghost.moveDir = Direction.random());
		folks.pacMan.init();
		folks.pacMan.wakeUp();
		theme.sounds().loadMusic();
	}

	@Override
	public void update() {
		float x = folks.pacMan.tf.getCenter().x;
		if (x > 0.9f * width || x < 0.1 * width) {
			folks.pacMan.moveDir = folks.pacMan.moveDir.opposite();
			ghostCount += ghostInc;
			if (ghostCount == 9 || ghostCount == 0) {
				ghostInc = -ghostInc;
			}
		}
		folks.pacMan.tf.setVelocity(Vector2f.smul(2.5f, folks.pacMan.moveDir.vector()));
		folks.pacMan.tf.move();

		alpha += alphaInc;
		if (alpha >= 160) {
			alphaInc = -2;
			alpha = 160;
		} else if (alpha <= 0) {
			alphaInc = 2;
			alpha = 0;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(new Color(0, 23, 61));
		g.fillRect(0, 0, width, height);
		messagesRenderer.setRow(18);
		messagesRenderer.setTextColor(new Color(255, 0, 0, alpha));
		messagesRenderer.draw(g, PacManGameView.texts.getString("loading_music"), width);
		pacManRenderer.render(g, folks.pacMan);
		float x = width / 2 - (ghostCount / 2) * 20 - Tile.TS / 2;
		float y = folks.pacMan.tf.y + 20;
		for (int i = 0; i < ghostCount; ++i) {
			Ghost ghost = ghosts.get(rnd.nextInt(4));
			ghost.tf.x = x;
			ghost.tf.y = y;
			theme.ghostRenderer().render(g, ghost);
			x += 20;
		}
	}
}