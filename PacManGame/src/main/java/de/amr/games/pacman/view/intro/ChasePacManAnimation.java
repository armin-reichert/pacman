package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.Map;

import de.amr.easy.game.entity.GameObject;
import de.amr.games.pacman.controller.actor.ArcadeGameFolks;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.controller.sound.PacManSoundManager;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.Universe;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.arcade.PacManRenderer;

public class ChasePacManAnimation extends GameObject {

	enum PelletDisplay {
		SIMPLE, TEN, ENERGIZER, FIFTY
	}

	private World world = Universe.arcadeWorld();
	private ArcadeGameFolks folks = new ArcadeGameFolks();
	private PacManSoundManager pacManSounds;
	private Map<Creature<?>, IRenderer> renderers = new LinkedHashMap<>();
	private long pelletTimer;
	private PelletDisplay pelletDisplay;

	public ChasePacManAnimation(Theme theme, PacManSoundManager pacManSounds) {
		this.pacManSounds = pacManSounds;
		folks.populate(world);
		folks.ghosts().forEach(ghost -> renderers.put(ghost, theme.createGhostRenderer(ghost)));
		renderers.put(folks.pacMan(), theme.createPacManRenderer(world));
	}
	
	public ArcadeGameFolks folks() {
		return folks;
	}

	@Override
	public void init() {
		pelletTimer = Game.sec(6 * 0.5f);
		pelletDisplay = PelletDisplay.SIMPLE;

		// TODO game should not be needed
		Game game = new Game(1, 244);
		folks.takePartIn(game);
		folks.all().forEach(Creature::init);

		folks.pacMan().tf.vx = -0.55f;
		folks.pacMan().setMoveDir(Direction.LEFT);
		folks.pacMan().setSpeedLimit(() -> 3f);
		folks.pacMan().setState(PacManState.RUNNING);
		PacManRenderer r = (PacManRenderer) renderers.get(folks.pacMan());
		r.setAnimationStoppedWhenStanding(false);
		
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
		pacManSounds.snd_ghost_chase().loop();
	}

	@Override
	public void stop() {
		pacManSounds.snd_ghost_chase().stop();
		folks.all().forEach(c -> c.tf.vx = 0);
	}

	@Override
	public boolean isComplete() {
		return folks.all().map(c -> c.tf.x / Tile.SIZE).allMatch(x -> x > world.width() || x < -2);
	}

	@Override
	public void draw(Graphics2D g) {
		renderers.forEach((c, r) -> r.render(g));

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
	}
}