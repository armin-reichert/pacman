package de.amr.games.pacman.view.intro;

import static de.amr.games.pacman.model.game.PacManGame.POINTS_GHOSTS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.BitSet;
import java.util.stream.Stream;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.game.Timing;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.theme.api.PacManRenderer;
import de.amr.games.pacman.theme.api.Theme;

/**
 * An animation showing Pac-Man and the four ghosts frightened and showing the points scored for the
 * ghosts.
 * 
 * @author Armin Reichert
 */
public class GhostPointsAnimation extends GameObject {

	private final PacMan pacMan;
	private final Ghost blinky, inky, pinky, clyde;
	private final BitSet killed = new BitSet(5);
	private Theme theme;
	private PacManRenderer pacManRenderer;

	private int ghostToKill;
	private long ghostTimer;
	private long energizerTimer;
	private boolean energizer;
	private int dx = 2 * Tile.SIZE + 3;

	public GhostPointsAnimation(Theme theme, World world) {
		tf.width = 6 * dx;
		tf.height = 2 * Tile.SIZE;
		pacMan = new PacMan(world, "Pac-Man");
		blinky = Ghost.shadowGhost(world, "Blinky", pacMan);
		inky = Ghost.bashfulGhost(world, "Inky", pacMan);
		pinky = Ghost.speedyGhost(world, "Pinky", pacMan);
		clyde = Ghost.pokeyGhost(world, "Clyde", pacMan);
		setTheme(theme);
	}

	public Stream<Guy<?>> guys() {
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
	public void draw(Graphics2D g) {
		pacManRenderer.render(g, pacMan);
		ghosts().forEach(ghost -> {
			theme.ghostRenderer().render(g, ghost);
		});
		g.translate(tf.x + dx, tf.y);
		renderPellet(g);
		g.translate(-(tf.x + dx), -tf.y);
	}

	public void renderPellet(Graphics2D g) {
		if (energizer) {
			g.setColor(Color.PINK);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.fillOval(0, 0, 8, 8);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			g.setColor(Color.PINK);
			g.setFont(new Font("Arial", Font.BOLD, 8));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString("50", 0, 7);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
	}

	@Override
	public void init() {
		ghostTimer = -1;
		killed.clear();
		ghostToKill = 0;
		energizer = true;
		guys().forEach(Lifecycle::init);
		pacMan.moveDir = Direction.RIGHT;
		pacMan.ai.setState(PacManState.AWAKE);
		ghosts().forEach(ghost -> {
			ghost.ai.setState(GhostState.FRIGHTENED);
			ghost.ai.state().removeTimer();
		});
		initPositions();
	}

	private void initPositions() {
		float x = tf.x;
		pacMan.tf.setPosition(x, tf.y);
		x += 2 * dx; // space for drawing pellet
		Ghost[] ghosts = ghosts().toArray(Ghost[]::new);
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i].tf.setPosition(x, tf.y);
			x += dx;
		}
	}

	private void resetGhostTimer() {
		ghostTimer = Timing.sec(1);
	}

	private void resetEnergizerTimer() {
		energizerTimer = Timing.sec(0.5f);
	}

	@Override
	public void start() {
		init();
		resetGhostTimer();
		resetEnergizerTimer();
	}

	@Override
	public void stop() {
		ghostTimer = -1;
	}

	@Override
	public boolean isComplete() {
		return false;
	}

	@Override
	public void update() {
		if (ghostTimer > 0) {
			ghostTimer -= 1;
			if (ghostTimer == 0) {
				if (ghostToKill == 4) {
					stop();
				} else {
					Ghost[] ghosts = ghosts().toArray(Ghost[]::new);
					theme.sounds().clipEatGhost().play();
					ghosts[ghostToKill].ai.setState(GhostState.DEAD);
					ghosts[ghostToKill].bounty = POINTS_GHOSTS[ghostToKill];
					killed.set(ghostToKill);
					ghostToKill = ghostToKill + 1;
					resetGhostTimer();
				}
			}
		}
		if (energizerTimer > 0) {
			energizerTimer -= 1;
		}
		if (energizerTimer == 0) {
			energizer = !energizer;
			resetEnergizerTimer();
		}
	}
}