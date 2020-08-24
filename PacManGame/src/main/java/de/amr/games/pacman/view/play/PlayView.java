package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.common.MessagesView;

/**
 * View where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayView implements PacManGameView {

	public final World world;
	public final Game game;
	public final Folks folks;
	public final SoundState sound;
	public final MessagesView messages;

	protected Theme theme;

	public PlayView(Theme theme, Folks folks, Game game, World world) {
		this.theme = theme;
		this.folks = folks;
		this.game = game;
		this.world = world;
		sound = new SoundState();
		messages = new MessagesView(theme, world, 15, 21);
		// this is a hack to reset the collapsing animation of Pac-Man. Need clean solution.
		folks.pacMan.ai.addStateExitListener(PacManState.DEAD, state -> {
			theme.pacManRenderer(folks.pacMan).resetAnimations(folks.pacMan);
		});
	}

	@Override
	public void init() {
		messages.clearMessages();
	}

	@Override
	public void update() {
		renderSound();
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		messages.setTheme(theme);
	}

	@Override
	public Theme getTheme() {
		return theme;
	}

	@Override
	public void draw(Graphics2D g) {
		drawWorld(g);
		drawMessages(g);
		drawActors(g);
		drawScores(g);
		drawLiveCounter(g);
		drawLevelCounter(g);
	}

	protected void drawWorld(Graphics2D g) {
		theme.worldRenderer(world).render(g, world);
	}

	protected void drawScores(Graphics2D g) {
		theme.pointsCounterRenderer().render(g, game);
	}

	protected void drawPacMan(Graphics2D g, PacMan pacMan) {
		theme.pacManRenderer(pacMan).render(g, pacMan);
	}

	protected void drawGhost(Graphics2D g, Ghost ghost) {
		theme.ghostRenderer(ghost).render(g, ghost);
	}

	protected void drawActors(Graphics2D g) {
		folks.ghostsInWorld().filter(ghost -> ghost.ai.is(DEAD, ENTERING_HOUSE)).forEach(ghost -> drawGhost(g, ghost));
		drawPacMan(g, folks.pacMan);
		folks.ghostsInWorld().filter(ghost -> !ghost.ai.is(DEAD, ENTERING_HOUSE)).forEach(ghost -> drawGhost(g, ghost));
	}

	protected void drawLiveCounter(Graphics2D g) {
		g.translate(Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		theme.livesCounterRenderer().render(g, game);
		g.translate(-Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}

	protected void drawLevelCounter(Graphics2D g) {
		g.translate(world.width() * Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		theme.levelCounterRenderer().render(g, game);
		g.translate(-world.width() * Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}

	protected void drawMessages(Graphics2D g) {
		messages.draw(g);
	}

	private void renderSound() {
		// Pac-Man
		long starvingMillis = System.currentTimeMillis() - sound.lastMealAt;
		if (starvingMillis > 300) {
			theme.sounds().clipCrunching().stop();
		} else if (!theme.sounds().clipCrunching().isRunning()) {
			theme.sounds().clipCrunching().loop();
		}
		if (!folks.pacMan.ai.is(PacManState.POWERFUL)) {
			theme.sounds().clipWaza().stop();
		} else if (!theme.sounds().clipWaza().isRunning()) {
			theme.sounds().clipWaza().loop();
		}
		if (sound.pacManDied) {
			theme.sounds().clipPacManDies().play();
			sound.pacManDied = false;
		}
		if (sound.bonusEaten) {
			theme.sounds().clipEatFruit().play();
			sound.bonusEaten = false;
		}
		if (sound.gotExtraLife) {
			theme.sounds().clipExtraLife().play();
			sound.gotExtraLife = false;
		}

		// Ghosts
		if (!sound.chasingGhosts) {
			theme.sounds().clipGhostChase().stop();
		} else if (!theme.sounds().clipGhostChase().isRunning()) {
			theme.sounds().clipGhostChase().setVolume(0.5f);
			theme.sounds().clipGhostChase().loop();
		}
		if (!sound.deadGhosts) {
			theme.sounds().clipGhostDead().stop();
		} else if (!theme.sounds().clipGhostDead().isRunning()) {
			theme.sounds().clipGhostDead().loop();
		}
		if (sound.ghostEaten) {
			theme.sounds().clipEatGhost().play();
			sound.ghostEaten = false;
		}
	}
}