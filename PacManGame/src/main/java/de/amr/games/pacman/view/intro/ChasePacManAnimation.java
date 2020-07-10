package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.actor.ArcadeWorldFolks;
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
import de.amr.games.pacman.view.core.Theme;

public class ChasePacManAnimation extends GameObject {

	enum PelletDisplay {
		SIMPLE, TEN, ENERGIZER, FIFTY
	}

	private World world = Universe.arcadeWorld();
	private ArcadeWorldFolks folks = new ArcadeWorldFolks(world);
	private PacManSounds sounds;
	private long pelletTimer;
	private PelletDisplay pelletDisplay;

	public ChasePacManAnimation(Theme theme, PacManSounds sounds) {
		this.sounds = sounds;
		setTheme(theme);
	}

	public void setTheme(Theme theme) {
		folks.all().forEach(c -> c.setTheme(theme));
	}

	public ArcadeWorldFolks folks() {
		return folks;
	}

	@Override
	public void init() {
		pelletTimer = Game.sec(6 * 0.5f);
		pelletDisplay = PelletDisplay.SIMPLE;

		folks.all().forEach(Creature::init);

		folks.pacMan().tf.vx = -0.55f;
		folks.pacMan().setMoveDir(Direction.LEFT);
		folks.pacMan().setSpeedLimit(() -> 3f);
		folks.pacMan().setState(PacManState.RUNNING);
		folks.pacMan().getRenderer().stopAnimationWhenStanding(false);

		folks.ghosts().forEach(ghost -> {
			ghost.tf.setVelocity(-0.55f, 0);
			ghost.setSpeedLimit(() -> 2f);
			ghost.setState(GhostState.CHASING);
			ghost.state(GhostState.CHASING).removeTimer();
			ghost.setMoveDir(Direction.LEFT);
		});

		initPositions(world.width() * Tile.SIZE);
	}

	public void initPositions(int rightBorder) {
		int size = 2 * Tile.SIZE;
		int x = rightBorder;
		Ghost[] ghosts = folks.ghosts().toArray(Ghost[]::new);
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i].tf.setPosition(x, tf.y);
			x -= size;
		}
		folks.pacMan().tf.setPosition(x, tf.y);
	}

	@Override
	public void update() {
		folks.all().forEach(creature -> creature.tf.move());
		if (pelletTimer > 0) {
			if (pelletTimer % Game.sec(0.5f) == 0)
				if (pelletDisplay == PelletDisplay.FIFTY) {
					pelletDisplay = PelletDisplay.SIMPLE;
				} else {
					pelletDisplay = PelletDisplay.values()[pelletDisplay.ordinal() + 1]; // succ
				}
			pelletTimer--;
		} else {
			pelletTimer = Game.sec(6 * 0.5f);
		}
	}

	@Override
	public void start() {
		init();
		sounds.snd_ghost_chase().loop();
	}

	@Override
	public void stop() {
		sounds.snd_ghost_chase().stop();
		folks.all().forEach(c -> c.tf.vx = 0);
	}

	@Override
	public boolean isComplete() {
		return folks.all().map(c -> c.tf.x / Tile.SIZE).allMatch(x -> x > world.width() || x < -2);
	}

	@Override
	public void draw(Graphics2D g) {
		folks.all().forEach(c -> c.draw(g));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int x = (int) folks.pacMan().tf.x - Tile.SIZE;
		int y = (int) folks.pacMan().tf.y;
		switch (pelletDisplay) {
		case SIMPLE:
			g.setColor(Color.PINK);
			g.fillRect(x, y + 4, 2, 2);
			break;
		case TEN:
			g.setFont(new Font("Arial", Font.BOLD, 8));
			g.setColor(Color.PINK);
			g.drawString("10", x - 6, y + 6);
			break;
		case ENERGIZER:
			g.setColor(Color.PINK);
			g.fillOval(x - 4, y, 8, 8);
			break;
		case FIFTY:
			g.setFont(new Font("Arial", Font.BOLD, 8));
			g.setColor(Color.PINK);
			g.drawString("50", x - 6, y + 6);
			break;
		default:
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}