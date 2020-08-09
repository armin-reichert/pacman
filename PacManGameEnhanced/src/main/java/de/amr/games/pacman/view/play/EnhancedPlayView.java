package de.amr.games.pacman.view.play;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.ui.widgets.FrameRateWidget;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.game.GhostCommand;
import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.common.GridRenderer;
import de.amr.games.pacman.view.common.Rendering;
import de.amr.games.pacman.view.common.RoutesRenderer;
import de.amr.games.pacman.view.common.StatesRenderer;

/**
 * View where the action is.
 * 
 * @author Armin Reichert
 */
public class EnhancedPlayView extends PlayView {

	protected final GridRenderer gridRenderer;
	protected final RoutesRenderer routesRenderer;
	protected final StatesRenderer statesRenderer;
	protected final FrameRateWidget frameRateView;
	protected final GhostCommand ghostCommand;

	protected boolean showingFrameRate;
	protected boolean showingGrid;
	protected boolean showingRoutes;
	protected boolean showingStates;

	public EnhancedPlayView(World world, Theme theme, Folks folks, Game game, GhostCommand ghostCommand) {
		super(world, theme, folks, game);
		this.ghostCommand = ghostCommand;
		gridRenderer = new GridRenderer(world.width(), world.height());
		routesRenderer = new RoutesRenderer();
		statesRenderer = new StatesRenderer();
		frameRateView = new FrameRateWidget();
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
		theme.worldRenderer(world).render(g, world);
		if (showingGrid) {
			world.tiles().filter(world::hasEatenFood).forEach(tile -> {
				Color color = Rendering.patternColor(tile);
				g.setColor(color);
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
	}

	protected void drawGrid(Graphics2D g) {
		if (showingGrid) {
			gridRenderer.renderGrid(g, world);
		}
	}

	protected void drawOneWayTiles(Graphics2D g) {
		if (showingGrid) {
			gridRenderer.drawOneWayTiles(g, world);
		}
	}

	protected void drawFrameRate(Graphics2D g) {
		if (showingFrameRate) {
			frameRateView.tf.setPosition(0, 18 * Tile.SIZE);
			frameRateView.draw(g);
		}
	}

	protected void drawStates(Graphics2D g) {
		if (showingStates) {
			statesRenderer.renderStates(g, folks, ghostCommand);
		}
	}

	protected void drawRoutes(Graphics2D g) {
		if (showingRoutes) {
			routesRenderer.renderRoutes(g, folks);
		}
	}
}