package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.render.MessageRenderer;
import de.amr.games.pacman.view.render.ScoreRenderer;
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

	public boolean showingGrid = false;
	
	protected boolean mazeEmpty;
	protected boolean mazeFlashing;
	protected String messageText;

	protected WorldRenderer worldRenderer;
	protected ScoreRenderer scoreRenderer;
	protected MessageRenderer messageRenderer;

	public SimplePlayView(World world, Theme theme, Game game, int width, int height) {
		this.world = world;
		this.theme = theme;
		this.game = game;
		this.width = width;
		this.height = height;
		worldRenderer = new WorldRenderer(world, theme);
		scoreRenderer = new ScoreRenderer(world, theme);
		messageRenderer = new MessageRenderer(world, theme);
	}

	@Override
	public void init() {
		clearMessage();
		setEmptyMaze(false);
		setMazeFlashing(false);
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		drawScores(g);
		drawWorld(g);
		drawMessage(g, messageText);
		drawActors(g);
	}

	public void showMessage(String text, Color color, int fontSize) {
		messageText = text;
		messageRenderer.setTextColor(color);
		messageRenderer.setFontSize(fontSize);
	}

	public void showMessage(String text, Color color) {
		showMessage(text, color, 8);
	}

	public void clearMessage() {
		messageText = "";
	}

	public void enableGhostAnimations(boolean enabled) {
		world.population().ghosts().map(ghost -> ghost.getRenderer())
				.forEach(renderer -> renderer.enableSpriteAnimation(enabled));
	}

	public void letEnergizersBlink(boolean enabled) {
		worldRenderer.letEnergizersBlink(enabled);
	}

	protected Color tileColor(Tile tile) {
		return Color.BLACK;
	}

	public void setMazeFlashing(boolean flashing) {
		mazeFlashing = flashing;
	}

	public void setEmptyMaze(boolean empty) {
		mazeEmpty = empty;
	}

	protected void drawWorld(Graphics2D g) {
		worldRenderer.setShowingGrid(showingGrid);
		if (!mazeEmpty) {
			worldRenderer.selectSprite("maze-full");
		} else if (mazeFlashing) {
			worldRenderer.selectSprite("maze-flashing");
		} else {
			worldRenderer.selectSprite("maze-empty");
		}
		worldRenderer.draw(g);
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
		if (game != null) {
			scoreRenderer.draw(g, game);
		}
	}

	protected void drawMessage(Graphics2D g, String messageText) {
		messageRenderer.draw(g, messageText);
	}

}