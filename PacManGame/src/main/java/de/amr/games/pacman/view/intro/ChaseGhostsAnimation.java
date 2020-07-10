package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.actor.ArcadeGameFolks;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.controller.sound.PacManSounds;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.Universe;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.Theme;

public class ChaseGhostsAnimation extends GameObject {

	private World world = Universe.arcadeWorld();
	private ArcadeGameFolks folks = new ArcadeGameFolks();
	private PacManSounds pacManSounds;
	private int points;

	public ChaseGhostsAnimation(Theme theme, PacManSounds pacManSounds) {
		this.pacManSounds = pacManSounds;
		setTheme(theme);
		folks.populate(world);
	}

	public void setTheme(Theme theme) {
		folks.all().forEach(c -> c.setTheme(theme));
	}

	@Override
	public void stop() {
		pacManSounds.snd_eatPill().stop();
	}

	@Override
	public boolean isComplete() {
		return folks.all().allMatch(creature -> creature.tf.x > world.width() * Tile.SIZE);
	}

	@Override
	public void init() {
		points = 200;
		folks.all().forEach(Creature::init);

		folks.pacMan().setSpeedLimit(() -> 3f);
		folks.pacMan().setMoveDir(Direction.RIGHT);
		folks.pacMan().tf.vx = 0.8f;
		folks.pacMan().setState(PacManState.RUNNING);

		folks.ghosts().forEach(ghost -> {
			ghost.setMoveDir(Direction.RIGHT);
			ghost.tf.setVelocity(0.55f, 0);
			ghost.setSpeedLimit(() -> 2f);
			ghost.setState(GhostState.FRIGHTENED);
			ghost.state(GhostState.FRIGHTENED).removeTimer();
		});
		initPositions();
	}

	private void initPositions() {
		folks.pacMan().tf.setPosition(tf.x, tf.y);
		Ghost[] ghosts = folks.ghosts().toArray(Ghost[]::new);
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i].tf.setPosition(tf.x + 20 * i, tf.y);
		}
	}

	@Override
	public void update() {
		//@formatter:off
		folks.ghosts()
			.filter(ghost -> ghost.getState() != GhostState.DEAD)
			.filter(ghost -> ghost.tile().equals(folks.pacMan().tile()))
			.forEach(ghost -> {
				ghost.setState(GhostState.DEAD);
				ghost.setBounty(points);
				points *= 2;
			});
		//@formatter:on
		folks.all().forEach(creature -> creature.tf.move());
	}

	@Override
	public void draw(Graphics2D g) {
		folks.all().forEach(c -> c.draw(g));
	}
}