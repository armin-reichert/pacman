package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.creatures.Creature;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.view.api.Theme;

public class ChasePacManAnimation extends GameObject {

	enum PelletDisplay {
		SIMPLE, TEN, ENERGIZER, FIFTY
	}

	private final ArcadeWorld world;
	private final Folks folks;
	private Theme theme;
	private long pelletTimer;
	private PelletDisplay pelletDisplay;

	public ChasePacManAnimation(Theme theme, ArcadeWorld world) {
		this.world = world;
		folks = new Folks(world, world.house(0));
		setTheme(theme);
	}

	public Folks getFolks() {
		return folks;
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
		folks.all().forEach(guy -> guy.theme = theme);
	}

	@Override
	public void init() {
		pelletTimer = Game.sec(6 * 0.5f);
		pelletDisplay = PelletDisplay.SIMPLE;

		folks.all().forEach(Creature::init);

		folks.pacMan.entity.tf.vx = -0.55f;
		folks.pacMan.entity.moveDir = Direction.LEFT;
		folks.pacMan.ai.setState(PacManState.AWAKE);

		folks.ghosts().forEach(ghost -> {
			ghost.entity.moveDir = Direction.LEFT;
			ghost.entity.tf.setVelocity(-0.55f, 0);
			ghost.ai.setState(GhostState.CHASING);
			ghost.ai.state(GhostState.CHASING).removeTimer();
		});

		initPositions(world.width() * Tile.SIZE);
	}

	public void initPositions(int rightBorder) {
		int size = 2 * Tile.SIZE;
		int x = rightBorder;
		Ghost[] ghosts = folks.ghosts().toArray(Ghost[]::new);
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i].entity.tf.setPosition(x, tf.y);
			x -= size;
		}
		folks.pacMan.entity.tf.setPosition(x, tf.y);
	}

	@Override
	public void update() {
		folks.all().forEach(c -> c.entity.tf.move());
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
		theme.sounds().clipGhostChase().loop();
		if (!theme.sounds().clipCrunching().isRunning()) {
			theme.sounds().clipCrunching().loop();
		}
	}

	@Override
	public void stop() {
		theme.sounds().clipGhostChase().stop();
		theme.sounds().clipCrunching().stop();
		folks.all().forEach(creature -> creature.entity.tf.vx = 0);
	}

	@Override
	public boolean isComplete() {
		return folks.all().map(creature -> creature.entity.tf.x / Tile.SIZE).allMatch(x -> x > world.width() || x < -2);
	}

	@Override
	public void draw(Graphics2D g) {
		theme.pacManRenderer(folks.pacMan).render(g, folks.pacMan);
		folks.ghosts().forEach(ghost -> {
			theme.ghostRenderer(ghost).render(g, ghost);
		});
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int x = (int) folks.pacMan.entity.tf.x - Tile.SIZE;
		int y = (int) folks.pacMan.entity.tf.y;
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