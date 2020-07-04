package de.amr.games.pacman.view.play;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.ui.widgets.FrameRateWidget;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.ghosthouse.GhostHouseAccessControl;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.sprite.ActorRoutesRenderer;
import de.amr.games.pacman.view.render.sprite.ActorStatesRenderer;
import de.amr.games.pacman.view.render.sprite.GhostHouseStateRenderer;
import de.amr.games.pacman.view.render.sprite.GridRenderer;
import de.amr.games.pacman.view.render.sprite.Rendering;
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

	private boolean showingGrid;
	private boolean showingRoutes;
	private boolean showingStates;

	private final GridRenderer gridRenderer;
	private final ActorRoutesRenderer actorRoutesRenderer;
	private final ActorStatesRenderer actorStatesRenderer;
	private final GhostHouseStateRenderer ghostHouseStateRenderer;

	public PlayView(World world, Theme theme, Game game, int width, int height) {
		super(world, theme, game, width, height);
		gridRenderer = new GridRenderer(world, theme);
		actorRoutesRenderer = new ActorRoutesRenderer(world, theme);
		actorStatesRenderer = new ActorStatesRenderer(world, theme);
		ghostHouseStateRenderer = new GhostHouseStateRenderer(world, theme);
		frameRateDisplay = new FrameRateWidget();
		frameRateDisplay.tf.setPosition(0, 18 * Tile.SIZE);
		frameRateDisplay.font = new Font(Font.MONOSPACED, Font.BOLD, 8);
	}

	@Override
	public void draw(Graphics2D g) {
		if (showingGrid) {
			worldRenderer.setEatenFoodColor(Rendering::patternColor);
			gridRenderer.draw(g);
		} else {
			worldRenderer.setEatenFoodColor(tile -> Color.BLACK);
		}
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
			if (optionalGhostCommand != null) {
				actorStatesRenderer.setGhostCommand(optionalGhostCommand);
				actorStatesRenderer.draw(g);
			}
			if (optionalHouseAccessControl != null) {
				ghostHouseStateRenderer.setHouseAccessControl(optionalHouseAccessControl);
				ghostHouseStateRenderer.draw(g);
			}
		}
		drawScores(g);
	}

	public void turnGridOn() {
		showingGrid = true;
	}

	public void turnGridOff() {
		showingGrid = false;
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
}