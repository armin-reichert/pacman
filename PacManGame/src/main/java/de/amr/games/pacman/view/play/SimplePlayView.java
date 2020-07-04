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
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.render.api.IRenderer;
import de.amr.games.pacman.view.render.api.IWorldRenderer;
import de.amr.games.pacman.view.render.sprite.ScoreRenderer;
import de.amr.games.pacman.view.render.sprite.TextRenderer;
import de.amr.games.pacman.view.theme.Theme;

/**
 * Simple play view providing the core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView implements LivingView {

	public enum RenderingStyle {
		ARCADE, BLOCK
	}

	public static IWorldRenderer createWorldRenderer(RenderingStyle style, World world, Theme theme) {
		if (style == RenderingStyle.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.WorldRenderer(world, theme);
		} else {
			return new de.amr.games.pacman.view.render.block.WorldRenderer(world, theme);
		}
	}

	public static IRenderer createPacManRenderer(RenderingStyle style, PacMan pacMan, Theme theme) {
		if (style == RenderingStyle.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.PacManRenderer(pacMan, theme);
		} else if (style == RenderingStyle.BLOCK) {
			return new de.amr.games.pacman.view.render.block.PacManRenderer(pacMan, theme);
		}
		throw new IllegalArgumentException("Unknown style " + style);
	}

	public static IRenderer createGhostRenderer(RenderingStyle style, Ghost ghost, Theme theme) {
		if (style == RenderingStyle.ARCADE) {
			return new de.amr.games.pacman.view.render.sprite.GhostRenderer(ghost, theme);
		} else if (style == RenderingStyle.BLOCK) {
			return new de.amr.games.pacman.view.render.block.GhostRenderer(ghost, theme);
		}
		throw new IllegalArgumentException("Unknown style " + style);
	}

	protected World world;
	protected Theme theme;
	protected Game game;
	protected int width;
	protected int height;

	protected String[] messageTexts = new String[2];
	protected Color[] messageColors = new Color[2];

	public RenderingStyle style;

	protected IWorldRenderer worldRenderer;
	protected ScoreRenderer scoreRenderer;
	protected TextRenderer textRenderer;
	protected IRenderer pacManRenderer;
	protected Map<Ghost, IRenderer> ghostRenderer = new HashMap<>();

	private boolean showingScores;

	public SimplePlayView(World world, Theme theme, Game game, int width, int height) {
		this.world = world;
		this.theme = theme;
		this.game = game;
		this.width = width;
		this.height = height;
		showingScores = true;
		scoreRenderer = new ScoreRenderer(world, theme);
		textRenderer = new TextRenderer(world, theme);
		style = RenderingStyle.ARCADE;
		updateRenderers(world, theme);
	}

	public void updateRenderers(World world, Theme theme) {
		worldRenderer = createWorldRenderer(style, world, theme);
		pacManRenderer = createPacManRenderer(style, world.population().pacMan(), theme);
		world.population().ghosts().forEach(ghost -> ghostRenderer.put(ghost, createGhostRenderer(style, ghost, theme)));
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

	public void turnEnergizerBlinkingOn() {
		worldRenderer.letEnergizersBlink(true);
	}

	public void turnEnergizerBlinkingOff() {
		worldRenderer.letEnergizersBlink(false);
	}

	protected void drawWorld(Graphics2D g) {
		worldRenderer.draw(g);
	}

	protected void drawMessages(Graphics2D g) {
		if (messageTexts[0] != null) {
			textRenderer.setRow(15);
			textRenderer.setTextColor(messageColors[0]);
			textRenderer.draw(g, messageTexts[0]);
		}
		if (messageTexts[1] != null) {
			textRenderer.setRow(21);
			textRenderer.setTextColor(messageColors[1]);
			textRenderer.draw(g, messageTexts[1]);
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
		if (game != null && showingScores) {
			scoreRenderer.draw(g, game);
		}
	}
}