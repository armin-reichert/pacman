package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.SmartGuy;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.view.api.Theme;

public class ChaseGhostsAnimation extends GameObject {

	private final ArcadeWorld world;
	private final Folks folks;
	private Theme theme;
	private int points;

	public ChaseGhostsAnimation(Theme theme, ArcadeWorld world) {
		this.world = world;
		folks = new Folks(world, world.house(0));
		setTheme(theme);
	}

	public Folks getFolks() {
		return folks;
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	@Override
	public void stop() {
		theme.sounds().clipCrunching().stop();
	}

	@Override
	public boolean isComplete() {
		return folks.guys().allMatch(creature -> creature.body.tf.x > world.width() * Tile.SIZE);
	}

	@Override
	public void init() {
		points = 200;
		folks.guys().forEach(SmartGuy::init);

		folks.pacMan.body.moveDir = Direction.RIGHT;
		folks.pacMan.body.tf.vx = 0.8f;
		folks.pacMan.ai.setState(PacManState.AWAKE);

		folks.ghosts().forEach(ghost -> {
			ghost.body.moveDir = Direction.RIGHT;
			ghost.body.tf.setVelocity(0.55f, 0);
			ghost.ai.setState(GhostState.FRIGHTENED);
			ghost.ai.state(GhostState.FRIGHTENED).removeTimer();
		});
		initPositions();
	}

	private void initPositions() {
		folks.pacMan.body.tf.setPosition(tf.x, tf.y);
		Ghost[] ghosts = folks.ghosts().toArray(Ghost[]::new);
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i].body.tf.setPosition(tf.x + 20 * i, tf.y);
		}
	}

	@Override
	public void update() {
		//@formatter:off
		folks.ghosts()
			.filter(ghost -> ghost.ai.getState() != GhostState.DEAD)
			.filter(ghost -> ghost.body.tile().equals(folks.pacMan.body.tile()))
			.forEach(ghost -> {
				ghost.ai.setState(GhostState.DEAD);
				ghost.bounty = points;
				points *= 2;
				theme.sounds().clipEatGhost().play();
			});
		//@formatter:on
		folks.guys().forEach(creature -> creature.body.tf.move());
	}

	@Override
	public void draw(Graphics2D g) {
		theme.pacManRenderer(folks.pacMan).render(g, folks.pacMan);
		folks.ghosts().forEach(ghost -> {
			theme.ghostRenderer(ghost).render(g, ghost);
		});
	}
}