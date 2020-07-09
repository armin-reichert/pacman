package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.actor.GhostState.DEAD;
import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.ui.widgets.FrameRateWidget;
import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.steering.PathProvidingSteering;
import de.amr.games.pacman.controller.ghosthouse.GhostHouseDoorMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.IWorldRenderer;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.Themes;
import de.amr.games.pacman.view.theme.arcade.GridRenderer;
import de.amr.games.pacman.view.theme.common.ActorStatesRenderer;
import de.amr.games.pacman.view.theme.common.GhostRoutesRenderer;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.Rendering;

/**
 * View where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayView implements LivingView {

	private World world;
	private Game game;

	private String[] messageTexts = new String[2];
	private Color[] messageColors = new Color[2];

	private Theme theme;

	private IWorldRenderer worldRenderer;
	private IRenderer scoreRenderer;
	private IRenderer liveCounterRenderer;
	private IRenderer levelCounterRenderer;
	private MessagesRenderer messagesRenderer;
	private IRenderer pacManRenderer;
	private Map<Ghost, IRenderer> ghostRenderer = new HashMap<>();
	private FrameRateWidget frameRateDisplay;

	private boolean showingScores;
	private boolean showingFrameRate;
	private boolean showingGrid;
	private boolean showingRoutes;
	private boolean showingStates;

	private final GridRenderer gridRenderer;
	private final IRenderer actorRoutesRenderer;
	private final IRenderer actorStatesRenderer;

	public PlayView(World world, Game game, GhostCommand ghostCommand, GhostHouseDoorMan doorMan) {
		this.world = world;
		this.game = game;
		showingScores = true;
		showingFrameRate = false;
		showingGrid = false;
		showingRoutes = false;
		showingStates = false;
		gridRenderer = new GridRenderer(world);
		actorRoutesRenderer = new GhostRoutesRenderer(world);
		actorStatesRenderer = new ActorStatesRenderer(world, ghostCommand);
		frameRateDisplay = new FrameRateWidget();
		frameRateDisplay.tf.setPosition(0, 18 * Tile.SIZE);
		frameRateDisplay.font = new Font(Font.MONOSPACED, Font.BOLD, 8);
		setTheme(Themes.ARCADE_THEME);
	}

	public void setTheme(Theme theme) {
		if (this.theme != theme) {
			this.theme = theme;
			worldRenderer = theme.createWorldRenderer(world);
			liveCounterRenderer = theme.createLiveCounterRenderer(world, game);
			levelCounterRenderer = theme.createLevelCounterRenderer(world, game);
			scoreRenderer = theme.createScoreRenderer(world, game);
			pacManRenderer = theme.createPacManRenderer(world.population().pacMan());
			world.population().ghosts().forEach(ghost -> ghostRenderer.put(ghost, theme.createGhostRenderer(ghost)));
			scoreRenderer = theme.createScoreRenderer(world, game);
			messagesRenderer = theme.createMessagesRenderer();
		}
	}

	public Theme getTheme() {
		return theme;
	}

	@Override
	public void init() {
		clearMessages();
		world.setChangingLevel(false);
	}

	@Override
	public void update() {
		world.population().ghosts().forEach(ghost -> {
			if (ghost.steering() instanceof PathProvidingSteering) {
				PathProvidingSteering pathProvider = (PathProvidingSteering) ghost.steering();
				pathProvider.setPathComputed(showingRoutes);
			}
		});
	}

	@Override
	public void draw(Graphics2D g) {
		if (showingGrid) {
			worldRenderer.setEatenFoodColor(Rendering::patternColor);
			gridRenderer.render(g);
		} else {
			worldRenderer.setEatenFoodColor(tile -> Color.BLACK);
		}
		drawWorld(g);
		if (showingGrid) {
			gridRenderer.drawOneWayTiles(g);
		}
		if (showingFrameRate) {
			frameRateDisplay.draw(g);
		}
		drawMessages(g);
		drawActors(g);
		if (showingRoutes) {
			actorRoutesRenderer.render(g);
		}
		if (showingStates) {
			actorStatesRenderer.render(g);
		}
		drawScores(g);
		drawLiveCounter(g);
		drawLevelCounter(g);
	}

	public void showGameReady() {
		showMessage(2, "Ready!", Color.YELLOW);
	}

	public void showGameOver() {
		showMessage(2, "Game Over!", Color.RED);
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

	private void drawWorld(Graphics2D g) {
		worldRenderer.render(g);
	}

	private void drawMessages(Graphics2D g) {
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

	private void drawActors(Graphics2D g) {
		pacManRenderer.render(g);
		// draw dead ghosts (as number or eyes) under living ghosts
		world.population().ghosts().filter(world::included).filter(ghost -> ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> ghostRenderer.get(ghost).render(g));
		world.population().ghosts().filter(world::included).filter(ghost -> !ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> ghostRenderer.get(ghost).render(g));
	}

	private void drawScores(Graphics2D g) {
		if (showingScores) {
			scoreRenderer.render(g);
		}
	}

	private void drawLiveCounter(Graphics2D g) {
		g.translate(Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		liveCounterRenderer.render(g);
		g.translate(-Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}

	private void drawLevelCounter(Graphics2D g) {
		g.translate(world.width() * Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		levelCounterRenderer.render(g);
		g.translate(-world.width() * Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}
}