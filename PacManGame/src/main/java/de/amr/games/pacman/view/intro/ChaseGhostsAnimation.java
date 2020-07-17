package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.creatures.api.Creature;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.sound.PacManSounds;
import de.amr.games.pacman.controller.world.arcade.ArcadeWorld;
import de.amr.games.pacman.controller.world.arcade.ArcadeWorldFolks;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.view.theme.api.Theme;

public class ChaseGhostsAnimation extends GameObject {

	private ArcadeWorld world = new ArcadeWorld();
	private ArcadeWorldFolks folks = new ArcadeWorldFolks(world);
	private PacManSounds pacManSounds;
	private int points;

	public ChaseGhostsAnimation(Theme theme, PacManSounds pacManSounds) {
		this.pacManSounds = pacManSounds;
		setTheme(theme);
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
		return folks.all().allMatch(creature -> creature.entity().tf.x > world.width() * Tile.SIZE);
	}

	@Override
	public void init() {
		points = 200;
		folks.all().forEach(Creature::init);

		folks.pacMan().setMoveDir(Direction.RIGHT);
		folks.pacMan().entity.tf.vx = 0.8f;
		folks.pacMan().setState(PacManState.RUNNING);

		folks.ghosts().forEach(ghost -> {
			ghost.setMoveDir(Direction.RIGHT);
			ghost.entity.tf.setVelocity(0.55f, 0);
			ghost.setState(GhostState.FRIGHTENED);
			ghost.state(GhostState.FRIGHTENED).removeTimer();
		});
		initPositions();
	}

	private void initPositions() {
		folks.pacMan().entity.tf.setPosition(tf.x, tf.y);
		Ghost[] ghosts = folks.ghosts().toArray(Ghost[]::new);
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i].entity.tf.setPosition(tf.x + 20 * i, tf.y);
		}
	}

	@Override
	public void update() {
		//@formatter:off
		folks.ghosts()
			.filter(ghost -> ghost.getState() != GhostState.DEAD)
			.filter(ghost -> ghost.location().equals(folks.pacMan().location()))
			.forEach(ghost -> {
				ghost.setState(GhostState.DEAD);
				ghost.setBounty(points);
				points *= 2;
			});
		//@formatter:on
		folks.all().forEach(creature -> creature.entity().tf.move());
	}

	@Override
	public void draw(Graphics2D g) {
		folks.all().map(Creature::renderer).forEach(r -> r.render(g));
	}
}