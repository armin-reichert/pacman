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
import de.amr.games.pacman.controller.ghosthouse.GhostHouseDoorMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.core.LivingView;
import de.amr.games.pacman.view.theme.IRenderer;
import de.amr.games.pacman.view.theme.IWorldRenderer;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.games.pacman.view.theme.Theming;
import de.amr.games.pacman.view.theme.Theming.ThemeName;
import de.amr.games.pacman.view.theme.arcade.GhostHouseStateRenderer;
import de.amr.games.pacman.view.theme.arcade.GridRenderer;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.Rendering;

/**
 * View where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayView implements LivingView {

	protected World world;
	protected Game game;
	protected int width;
	protected int height;

	protected String[] messageTexts = new String[2];
	protected Color[] messageColors = new Color[2];

	protected Theme theme;

	protected IWorldRenderer worldRenderer;
	protected IRenderer scoreRenderer;
	protected IRenderer liveCounterRenderer;
	protected IRenderer levelCounterRenderer;
	protected MessagesRenderer messagesRenderer;
	protected IRenderer pacManRenderer;
	protected Map<Ghost, IRenderer> ghostRenderer = new HashMap<>();

	protected GhostCommand ghostCommand;
	protected GhostHouseDoorMan doorMan;

	private FrameRateWidget frameRateDisplay;

	private boolean showingScores;
	private boolean showingFrameRate;
	private boolean showingGrid;
	private boolean showingRoutes;
	private boolean showingStates;

	private final GridRenderer gridRenderer;
	private final IRenderer actorRoutesRenderer;
	private final IRenderer actorStatesRenderer;
	private final IRenderer ghostHouseStateRenderer;

	public PlayView(World world, Game game, GhostCommand ghostCommand, GhostHouseDoorMan ghostHouseAccessControl,
			int width, int height) {
		this.world = world;
		this.game = game;
		this.width = width;
		this.height = height;
		showingScores = true;
		showingFrameRate = false;
		showingGrid = false;
		showingRoutes = false;
		showingStates = false;
		gridRenderer = new GridRenderer(world);
		actorRoutesRenderer = new ActorRoutesRenderer(world);
		actorStatesRenderer = new ActorStatesRenderer(world, ghostCommand);
		ghostHouseStateRenderer = new GhostHouseStateRenderer(world, ghostHouseAccessControl);
		frameRateDisplay = new FrameRateWidget();
		frameRateDisplay.tf.setPosition(0, 18 * Tile.SIZE);
		frameRateDisplay.font = new Font(Font.MONOSPACED, Font.BOLD, 8);
		setTheme(ThemeName.ARCADE);
	}

	public void setTheme(ThemeName themeName) {
		theme = Theming.getTheme(themeName);
		worldRenderer = theme.createWorldRenderer(world);
		liveCounterRenderer = theme.createLiveCounterRenderer(game);
		levelCounterRenderer = theme.createLevelCounterRenderer(game);
		scoreRenderer = theme.createScoreRenderer(game);
		pacManRenderer = theme.createPacManRenderer(world, world.population().pacMan());
		world.population().ghosts().forEach(ghost -> ghostRenderer.put(ghost, theme.createGhostRenderer(ghost)));
		scoreRenderer = theme.createScoreRenderer(game);
		messagesRenderer = theme.createMessagesRenderer();
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
		if (showingGrid) {
			gridRenderer.drawOneWayTiles(g);
		}
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
			ghostHouseStateRenderer.draw(g);
		}
		drawScores(g);
		drawLiveCounter(g);
		drawLevelCounter(g);
	}

	public void showGameReady() {
		showMessage(1, "Ready!", Color.YELLOW);
	}

	public void showGameOver() {
		showMessage(1, "Game Over!", Color.RED);
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
		g.translate(0, world.height() * Tile.SIZE);
		liveCounterRenderer.draw(g);
		g.translate(0, -world.height() * Tile.SIZE);
	}

	protected void drawLevelCounter(Graphics2D g) {
		g.translate(world.width() * Tile.SIZE, world.height() * Tile.SIZE);
		levelCounterRenderer.draw(g);
		g.translate(-world.width() * Tile.SIZE, -world.height() * Tile.SIZE);
	}
}