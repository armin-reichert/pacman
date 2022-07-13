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
package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.theme.api.PacManRenderer;
import de.amr.games.pacman.theme.api.Theme;

/**
 * Pac-Man chasing ghosts.
 * 
 * @author Armin Reichert
 */
public class ChaseGhostsAnimation extends GameObject {

	private final TiledWorld world;
	private final PacMan pacMan;
	private final Ghost blinky, inky, pinky, clyde;
	private Theme theme;
	private PacManRenderer pacManRenderer;
	private int points;

	public ChaseGhostsAnimation(Theme theme, TiledWorld world) {
		this.world = world;
		pacMan = new PacMan(world, "Pac-Man");
		blinky = Ghost.shadowGhost(world, "Blinky", pacMan);
		inky = Ghost.bashfulGhost(world, "Inky", pacMan);
		pinky = Ghost.speedyGhost(world, "Pinky", pacMan);
		clyde = Ghost.pokeyGhost(world, "Clyde", pacMan);
		setTheme(theme);
	}

	public Stream<Guy> guys() {
		return Stream.of(pacMan, blinky, inky, pinky, clyde);
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(blinky, inky, pinky, clyde);
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
		pacManRenderer = theme.pacManRenderer();
	}

	@Override
	public void stop() {
		theme.sounds().clipCrunching().stop();
	}

	@Override
	public boolean isComplete() {
		return guys().allMatch(guy -> guy.tf.x > world.width() * Tile.SIZE);
	}

	@Override
	public void init() {
		points = 200;
		guys().forEach(Lifecycle::init);

		pacMan.tf.setPosition(tf.x, tf.y);
		pacMan.moveDir = Direction.RIGHT;
		pacMan.tf.vx = 0.8f;
		pacMan.ai.setState(PacManState.AWAKE);

		ghosts().forEach(ghost -> {
			ghost.moveDir = Direction.RIGHT;
			ghost.tf.setVelocity(0.55f, 0);
			ghost.ai.setState(GhostState.FRIGHTENED);
			ghost.ai.state(GhostState.FRIGHTENED).removeTimer();
		});
		Ghost[] ghosts = ghosts().toArray(Ghost[]::new);
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i].tf.setPosition(tf.x + 20 * i, tf.y);
		}
	}

	@Override
	public void update() {
		//@formatter:off
		ghosts()
			.filter(ghost -> ghost.ai.getState() != GhostState.DEAD)
			.filter(ghost -> ghost.tile().equals(pacMan.tile()))
			.forEach(ghost -> {
				ghost.ai.setState(GhostState.DEAD);
				ghost.bounty = points;
				points *= 2;
				theme.sounds().clipEatGhost().play();
			});
		//@formatter:on
		guys().forEach(guy -> guy.tf.move());
	}

	@Override
	public void draw(Graphics2D g) {
		pacManRenderer.render(g, pacMan);
		ghosts().forEach(ghost -> theme.ghostRenderer().render(g, ghost));
	}
}