package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.render.GhostRenderer;
import de.amr.games.pacman.view.render.PacManRenderer;
import de.amr.games.pacman.view.render.ScoreRenderer;
import de.amr.games.pacman.view.render.TextRenderer;
import de.amr.games.pacman.view.render.WorldRenderer;
import de.amr.games.pacman.view.theme.Theme;

/**
 * Simple play view providing the core functionality for playing.
 * 
 * @author Armin Reichert
 */
public class SimplePlayView implements LivingView {

	protected World world;
	protected Theme theme;
	protected Game game;
	protected int width;
	protected int height;

	protected String[] messageTexts = new String[2];
	protected Color[] messageColors = new Color[2];

	protected WorldRenderer worldRenderer;
	protected ScoreRenderer scoreRenderer;
	protected TextRenderer textRenderer;

	private boolean showingScores;

	public SimplePlayView(World world, Theme theme, Game game, int width, int height) {
		this.world = world;
		this.theme = theme;
		this.game = game;
		this.width = width;
		this.height = height;
		worldRenderer = new WorldRenderer(world, theme);
		scoreRenderer = new ScoreRenderer(world, theme);
		textRenderer = new TextRenderer(world, theme);
		world.population().pacMan().setRenderer(new PacManRenderer(world.population().pacMan(), theme));
		world.population().ghosts().forEach(ghost -> ghost.setRenderer(new GhostRenderer(ghost, theme)));
		showingScores = true;
	}

	@Override
	public void init() {
		clearMessages();
		turnFullMazeOn();

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
		world.population().ghosts().map(ghost -> ghost.getRenderer())
				.forEach(renderer -> renderer.enableSpriteAnimation(enabled));
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

	public void turnMazeFlashingOn() {
		worldRenderer.selectSprite("maze-flashing");
	}

	public void turnMazeFlashingOff() {
		worldRenderer.selectSprite("maze-empty");
	}

	public void turnFullMazeOn() {
		worldRenderer.selectSprite("maze-full");
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
		world.population().pacMan().getRenderer().draw(g);
		// draw dead ghosts (as number or eyes) under living ghosts
		world.population().ghosts().filter(world::included).filter(ghost -> ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> ghost.getRenderer().draw(g));
		world.population().ghosts().filter(world::included).filter(ghost -> !ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> ghost.getRenderer().draw(g));
	}

	protected void drawScores(Graphics2D g) {
		if (game != null && showingScores) {
			scoreRenderer.draw(g, game);
		}
	}
}