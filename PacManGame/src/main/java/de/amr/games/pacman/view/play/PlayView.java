package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.render.IRenderer;
import de.amr.games.pacman.view.render.IWorldRenderer;
import de.amr.games.pacman.view.render.common.MessagesRenderer;
import de.amr.games.pacman.view.render.sprite.ScoreRenderer;

/**
 * Simple play view providing the core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class PlayView implements LivingView {

	public enum RenderingStyle {
		ARCADE, BLOCK
	}

	public static IWorldRenderer createWorldRenderer(RenderingStyle style, World world) {
		if (style == RenderingStyle.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.WorldRenderer(world);
		} else {
			return new de.amr.games.pacman.view.render.block.WorldRenderer(world);
		}
	}

	public static IRenderer createScoreRenderer(RenderingStyle style, World world, Game game) {
		if (style == RenderingStyle.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.ScoreRenderer(game);
		} else {
			return new de.amr.games.pacman.view.render.block.ScoreRenderer(game);
		}
	}

	public static IRenderer createLiveCounterRenderer(RenderingStyle style, World world, Game game) {
		if (style == RenderingStyle.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.LiveCounterRenderer(world, game);
		} else {
			return new de.amr.games.pacman.view.render.block.LiveCounterRenderer(world, game);
		}
	}

	public static IRenderer createLevelCounterRenderer(RenderingStyle style, World world, Game game) {
		if (style == RenderingStyle.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.LevelCounterRenderer(game);
		} else {
			return new de.amr.games.pacman.view.render.block.LevelCounterRenderer(game);
		}
	}

	public static IRenderer createPacManRenderer(RenderingStyle style, World world, PacMan pacMan) {
		if (style == RenderingStyle.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.PacManRenderer(world, pacMan);
		} else if (style == RenderingStyle.BLOCK) {
			return new de.amr.games.pacman.view.render.block.PacManRenderer(world, pacMan);
		}
		throw new IllegalArgumentException("Unknown style " + style);
	}

	public static IRenderer createGhostRenderer(RenderingStyle style, Ghost ghost) {
		if (style == RenderingStyle.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.GhostRenderer(ghost);
		} else if (style == RenderingStyle.BLOCK) {
			return new de.amr.games.pacman.view.render.block.GhostRenderer(ghost);
		}
		throw new IllegalArgumentException("Unknown style " + style);
	}

	protected World world;
	protected Game game;
	protected int width;
	protected int height;

	protected String[] messageTexts = new String[2];
	protected Color[] messageColors = new Color[2];

	public RenderingStyle style;

	protected IWorldRenderer worldRenderer;
	protected IRenderer scoreRenderer;
	protected IRenderer liveCounterRenderer;
	protected IRenderer levelCounterRenderer;
	protected MessagesRenderer messagesRenderer;
	protected IRenderer pacManRenderer;
	protected Map<Ghost, IRenderer> ghostRenderer = new HashMap<>();

	private boolean showingScores;

	public PlayView(World world, Game game, int width, int height) {
		this.world = world;
		this.game = game;
		this.width = width;
		this.height = height;
		showingScores = true;
		scoreRenderer = new ScoreRenderer(game);
		messagesRenderer = new MessagesRenderer();
		style = RenderingStyle.ARCADE;
		updateRenderers(world);
	}

	public void updateRenderers(World world) {
		worldRenderer = createWorldRenderer(style, world);
		liveCounterRenderer = createLiveCounterRenderer(style, world, game);
		levelCounterRenderer = createLevelCounterRenderer(style, world, game);
		scoreRenderer = createScoreRenderer(style, world, game);
		pacManRenderer = createPacManRenderer(style, world, world.population().pacMan());
		world.population().ghosts().forEach(ghost -> ghostRenderer.put(ghost, createGhostRenderer(style, ghost)));
	}

	@Override
	public void init() {
		clearMessages();
		world.setChangingLevel(false);
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		drawScores(g);
		drawLiveCounter(g);
		drawLevelCounter(g);
		drawWorld(g);
		drawMessages(g);
		drawActors(g);
	}

	public void showGameReady() {
		messageTexts[1] = "Ready!";
		messageColors[1] = Color.YELLOW;
	}

	public void showGameOver() {
		messageTexts[1] = "Game Over!";
		messageColors[1] = Color.RED;
	}

	public void showMessage(int oneOrTwo, String text, Color color) {
		messageTexts[oneOrTwo - 1] = text;
		messageColors[oneOrTwo - 1] = color;
	}

	public void clearMessages() {
		clearMessage(1);
		clearMessage(2);
	}

	public void clearMessage(int oneOrTwo) {
		messageTexts[oneOrTwo - 1] = null;
	}

	public void enableGhostAnimations(boolean enabled) {
		world.population().ghosts().map(ghostRenderer::get).forEach(renderer -> renderer.enableAnimation(enabled));
	}

	public void turnScoresOn() {
		this.showingScores = true;
	}

	public void turnScoresOff() {
		this.showingScores = false;
	}

	public boolean isShowingScores() {
		return showingScores;
	}

	protected void drawWorld(Graphics2D g) {
		worldRenderer.draw(g);
	}

	protected void drawMessages(Graphics2D g) {
		if (messageTexts[0] != null) {
			messagesRenderer.setRow(15);
			messagesRenderer.setTextColor(messageColors[0]);
			messagesRenderer.drawCentered(g, messageTexts[0], world.width());
		}
		if (messageTexts[1] != null) {
			messagesRenderer.setRow(21);
			messagesRenderer.setTextColor(messageColors[1]);
			messagesRenderer.drawCentered(g, messageTexts[1], world.width());
		}
	}

	protected void drawActors(Graphics2D g) {
		pacManRenderer.draw(g);
		// draw dead ghosts (as number or eyes) under living ghosts
		world.population().ghosts().filter(world::included).filter(ghost -> ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> ghostRenderer.get(ghost).draw(g));
		world.population().ghosts().filter(world::included).filter(ghost -> !ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> ghostRenderer.get(ghost).draw(g));
	}

	protected void drawScores(Graphics2D g) {
		if (showingScores) {
			scoreRenderer.draw(g);
		}
	}

	protected void drawLiveCounter(Graphics2D g) {
		liveCounterRenderer.draw(g);
	}

	protected void drawLevelCounter(Graphics2D g) {
		g.translate((world.width()) * Tile.SIZE, (world.height()) * Tile.SIZE);
		levelCounterRenderer.draw(g);
		g.translate(-(world.width()) * Tile.SIZE, -(world.height()) * Tile.SIZE);
	}
}