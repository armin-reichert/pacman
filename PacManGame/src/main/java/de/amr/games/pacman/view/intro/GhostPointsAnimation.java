package de.amr.games.pacman.view.intro;

import static de.amr.games.pacman.model.game.Game.GHOST_BOUNTIES;
import static de.amr.games.pacman.model.game.Game.sec;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.BitSet;

import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.creatures.Creature;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.api.Themeable;

/**
 * An animation showing Pac-Man and the four ghosts frightened and showing the points scored for the
 * ghosts.
 * 
 * @author Armin Reichert
 */
public class GhostPointsAnimation extends GameObject implements Themeable {

	private final Folks folks;
	private final Ghost[] ghosts;
	private final BitSet killed = new BitSet(5);
	private Theme theme;
	private int ghostToKill;
	private long ghostTimer;
	private long energizerTimer;
	private boolean energizer;
	private int dx = 2 * Tile.SIZE + 3;

	public GhostPointsAnimation(Theme theme, World world) {
		tf.width = 6 * dx;
		tf.height = 2 * Tile.SIZE;
		folks = new Folks(world, world.house(0));
		ghosts = folks.ghosts().toArray(Ghost[]::new);
		setTheme(theme);
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		folks.all().forEach(creature -> creature.setTheme(theme));
	}

	@Override
	public Theme getTheme() {
		return theme;
	}

	@Override
	public void draw(Graphics2D g) {
		theme.pacManRenderer(folks.pacMan).render(g, folks.pacMan);
		folks.ghosts().forEach(ghost -> {
			theme.ghostRenderer(ghost).render(g, ghost);
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

		folks.all().forEach(Creature::init);

		folks.pacMan.setMoveDir(Direction.RIGHT);
		folks.pacMan.setState(PacManState.AWAKE);
		folks.pacMan.setSpeed(() -> 0f);

		folks.ghosts().forEach(ghost -> {
			ghost.setSpeed(() -> 0f);
			ghost.setState(GhostState.FRIGHTENED);
			ghost.state().removeTimer();
		});

		initPositions();
	}

	private void initPositions() {
		float x = tf.x;
		folks.pacMan.entity.tf.setPosition(x, tf.y);
		x += 2 * dx; // space for drawing pellet
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i].entity.tf.setPosition(x, tf.y);
			x += dx;
		}
	}

	private void resetGhostTimer() {
		ghostTimer = sec(1);
	}

	private void resetEnergizerTimer() {
		energizerTimer = sec(0.5f);
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
					theme.sounds().clipEatGhost().play();
					ghosts[ghostToKill].setState(GhostState.DEAD);
					ghosts[ghostToKill].setBounty(GHOST_BOUNTIES[ghostToKill]);
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