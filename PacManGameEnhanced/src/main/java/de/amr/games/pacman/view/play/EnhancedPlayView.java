package de.amr.games.pacman.view.play;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.ui.widgets.FrameRateWidget;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.game.GhostCommand;
import de.amr.games.pacman.controller.ghosthouse.DoorMan;
import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.IWorldRenderer;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.common.Rendering;
import de.amr.games.pacman.view.common.RoutesView;
import de.amr.games.pacman.view.common.StatesView;
import de.amr.games.pacman.view.theme.arcade.GridView;

/**
 * View where the action is.
 * 
 * @author Armin Reichert
 */
public class EnhancedPlayView extends PlayView {

	protected final GridView gridView;
	protected final RoutesView routesView;
	protected final StatesView statesView;
	protected final FrameRateWidget frameRateDisplay;

	protected boolean showingFrameRate;
	protected boolean showingGrid;
	protected boolean showingRoutes;
	protected boolean showingStates;

	public EnhancedPlayView(World world, Theme theme, Folks folks, Game game, GhostCommand ghostCommand,
			DoorMan doorMan) {
		super(world, theme, folks, game, ghostCommand, doorMan);
		gridView = new GridView(world);
		routesView = new RoutesView(world, folks);
		statesView = new StatesView(world, folks, ghostCommand);
		frameRateDisplay = new FrameRateWidget();
		setTheme(theme);
	}

	@Override
	public void update() {
		folks.ghosts().filter(ghost -> ghost.steering() instanceof PathProvidingSteering).forEach(ghost -> {
			PathProvidingSteering<?> pathProvider = (PathProvidingSteering<?>) ghost.steering();
			pathProvider.setPathComputationEnabled(showingRoutes);
		});
	}

	@Override
	public void draw(Graphics2D g) {
		drawGrid(g);
		drawWorld(g);
		drawOneWayTiles(g);
		drawFrameRate(g);
		drawMessages(g);
		drawActors(g);
		drawRoutes(g);
		drawStates(g);
		drawScores(g);
		drawLiveCounter(g);
		drawLevelCounter(g);
	}

	public boolean isShowingFrameRate() {
		return showingFrameRate;
	}

	public void turnFrameRateOn() {
		showingFrameRate = true;
	}

	public void turnFrameRateOff() {
		showingFrameRate = false;
	}

	public boolean isShowingGrid() {
		return showingGrid;
	}

	public void turnGridOn() {
		showingGrid = true;
	}

	public void turnGridOff() {
		showingGrid = false;
	}

	public boolean isShowingRoutes() {
		return showingRoutes;
	}

	public void turnRoutesOn() {
		showingRoutes = true;
	}

	public void turnRoutesOff() {
		showingRoutes = false;
	}

	public boolean isShowingStates() {
		return showingStates;
	}

	public void turnStatesOn() {
		showingStates = true;
	}

	public void turnStatesOff() {
		showingStates = false;
	}

	@Override
	protected void drawWorld(Graphics2D g) {
		IWorldRenderer renderer = theme.worldRenderer(world);
		//TODO this is dubious
		renderer.setEatenFoodColor(showingGrid ? Rendering::patternColor : tile -> Color.BLACK);
		renderer.render(g, world);
	}

	protected void drawGrid(Graphics2D g) {
		if (showingGrid) {
			gridView.draw(g);
		}
	}

	protected void drawOneWayTiles(Graphics2D g) {
		if (showingGrid) {
			gridView.drawOneWayTiles(g);
		}
	}

	protected void drawFrameRate(Graphics2D g) {
		if (showingFrameRate) {
			frameRateDisplay.tf.setPosition(0, 18 * Tile.SIZE);
			frameRateDisplay.draw(g);
		}
	}

	protected void drawStates(Graphics2D g) {
		if (showingStates) {
			statesView.draw(g);
		}
	}

	protected void drawRoutes(Graphics2D g) {
		if (showingRoutes) {
			routesView.draw(g);
		}
	}
}