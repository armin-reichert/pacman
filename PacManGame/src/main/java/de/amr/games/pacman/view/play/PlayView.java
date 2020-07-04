package de.amr.games.pacman.view.play;

import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

import de.amr.easy.game.ui.widgets.FrameRateWidget;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.ghosthouse.GhostHouseAccessControl;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.ActorRoutesRenderer;
import de.amr.games.pacman.view.render.ActorStatesRenderer;
import de.amr.games.pacman.view.theme.Theme;

/**
 * An extended play view that can visualize actor states, the ghost house pellet counters, ghost
 * routes, the grid background, ghost seats and the current framerate.
 * 
 * @author Armin Reichert
 */
public class PlayView extends SimplePlayView {

	public boolean showingFrameRate = false;

	/** Optional ghost house control */
	public GhostCommand optionalGhostCommand;

	/** Optional ghost house reference */
	public GhostHouseAccessControl optionalHouseAccessControl;

	private FrameRateWidget frameRateDisplay;

	private boolean showingRoutes;
	private boolean showingStates;

	private final ActorRoutesRenderer actorRoutesRenderer;
	private final ActorStatesRenderer actorStatesRenderer;

	public PlayView(World world, Theme theme, Game game, int width, int height) {
		super(world, theme, game, width, height);
		actorRoutesRenderer = new ActorRoutesRenderer(world, theme);
		actorStatesRenderer = new ActorStatesRenderer(world, theme);
		frameRateDisplay = new FrameRateWidget();
		frameRateDisplay.tf.setPosition(0, 18 * Tile.SIZE);
		frameRateDisplay.font = new Font(Font.MONOSPACED, Font.BOLD, 8);
	}

	@Override
	public void draw(Graphics2D g) {
		actorStatesRenderer.setGhostCommand(optionalGhostCommand);
		actorStatesRenderer.setHouseAccessControl(optionalHouseAccessControl);
		drawWorld(g);
		if (showingFrameRate) {
			frameRateDisplay.draw(g);
		}
		drawMessages(g);
		drawActors(g);
		if (showingRoutes) {
			actorRoutesRenderer.draw(g);
		}
		if (showingStates) {
			actorStatesRenderer.draw(g);
		}
//		if (showingGrid) {
//			drawActorOffTrack(g);
//		}
		drawScores(g);
	}

	public void turnGridOn() {
		worldRenderer.setShowingGrid(true);
	}

	public void turnGridOff() {
		worldRenderer.setShowingGrid(false);
	}

	public void turnRoutesOn() {
		showingRoutes = true;
	}

	public void turnRoutesOff() {
		showingRoutes = false;
	}

	public void turnStatesOn() {
		showingStates = true;
	}

	public void turnStatesOff() {
		showingStates = false;
	}

	public void turnScoresOn() {
		worldRenderer.setShowingScores(true);
	}

	public void turnScoresOff() {
		worldRenderer.setShowingScores(false);
	}

}