package de.amr.games.pacman.view.intro;

import static de.amr.games.pacman.model.game.Game.POINTS_GHOST;
import static de.amr.games.pacman.model.game.Game.sec;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.actor.ArcadeGameFolks;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.controller.sound.PacManSounds;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.Universe;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.IPacManRenderer;
import de.amr.games.pacman.view.core.IRenderer;
import de.amr.games.pacman.view.theme.Theme;

/**
 * An animation showing Pac-Man and the four ghosts frightened and showing the points scored for the
 * ghosts.
 * 
 * @author Armin Reichert
 */
public class GhostPointsAnimation extends GameObject {

	private final World world = Universe.arcadeWorld();
	private final ArcadeGameFolks folks = new ArcadeGameFolks();
	private final Ghost[] ghosts = folks.ghosts().toArray(Ghost[]::new);
	private final PacManSounds sounds;

	private Map<Creature<?>, IRenderer> renderers = new HashMap<>();

	private final BitSet killed = new BitSet(5);
	private int ghostToKill;
	private int ghostTimer;
	private int energizerTimer;
	private boolean energizer;

	public GhostPointsAnimation(Theme theme, PacManSounds sounds) {
		this.sounds = sounds;
		folks.populate(world);
		renderers.put(folks.pacMan(), theme.createPacManRenderer(folks.pacMan()));
		folks.ghosts().forEach(ghost -> renderers.put(ghost, theme.createGhostRenderer(ghost)));
		tf.width = 90;
		tf.height = 18;
	}

	@Override
	public void draw(Graphics2D g) {
		renderers.values().forEach(r -> r.render(g));
		int dx = 2 * Tile.SIZE + 2;
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

		// TODO game should not be needed
		Game game = new Game(1, 244);
		folks.takePartIn(game);
		folks.all().forEach(Creature::init);

		folks.pacMan().setMoveDir(Direction.RIGHT);
		folks.pacMan().setState(PacManState.RUNNING);
		folks.pacMan().setSpeedLimit(() -> 0f);
		IPacManRenderer r = (IPacManRenderer) renderers.get(folks.pacMan());
		r.stopAnimationWhenStanding(false);

		folks.ghosts().forEach(ghost -> {
			ghost.setSpeedLimit(() -> 0f);
			ghost.setState(GhostState.FRIGHTENED);
			ghost.state().removeTimer();
		});

		initPositions();
	}

	private void initPositions() {
		int dx = 2 * Tile.SIZE + 2;
		float x = tf.x;
		folks.pacMan().tf.setPosition(x, tf.y);
		x += 2 * dx; // space for drawing pellet
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i].tf.setPosition(x, tf.y);
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
					sounds.snd_eatGhost().play();
					ghosts[ghostToKill].setState(GhostState.DEAD);
					ghosts[ghostToKill].points = POINTS_GHOST[ghostToKill];
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