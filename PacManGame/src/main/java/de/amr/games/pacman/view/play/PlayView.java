package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.ui.widgets.FrameRateWidget;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.game.GhostCommand;
import de.amr.games.pacman.controller.ghosthouse.DoorMan;
import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.controller.world.arcade.ArcadeWorldFolks;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.games.pacman.view.theme.arcade.GridRenderer;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.games.pacman.view.theme.common.Rendering;
import de.amr.games.pacman.view.theme.common.RoutesRenderer;
import de.amr.games.pacman.view.theme.common.StatesRenderer;

/**
 * View where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayView implements PacManGameView {

	static class Message {

		String text;
		Color color;
		int row;

		Message(int row) {
			this.row = row;
			text = null;
			color = Color.LIGHT_GRAY;
		}
	}

	private final World world;
	private final ArcadeWorldFolks folks;
	private Game game;

	private final Message[] messages;

	private Theme theme;
	private IWorldRenderer worldRenderer;
	private IRenderer scoreRenderer;
	private IRenderer liveCounterRenderer;
	private IRenderer levelCounterRenderer;
	private MessagesRenderer messagesRenderer;

	private final GridRenderer gridRenderer;
	private final IRenderer routesRenderer;
	private final IRenderer statesRenderer;
	private final FrameRateWidget frameRateDisplay;

	private boolean showingScores = true;
	private boolean showingFrameRate;
	private boolean showingGrid;
	private boolean showingRoutes;
	private boolean showingStates;

	public PlayView(World world, Theme theme, ArcadeWorldFolks folks, Game game, GhostCommand ghostCommand,
			DoorMan doorMan) {
		this.world = world;
		this.folks = folks;
		this.game = game;
		messages = new Message[] { new Message(15), new Message(21) };
		gridRenderer = new GridRenderer(world);
		routesRenderer = new RoutesRenderer(world, folks);
		statesRenderer = new StatesRenderer(folks, ghostCommand);
		frameRateDisplay = new FrameRateWidget();
		setTheme(theme);
	}

	@Override
	public void setTheme(Theme theme) {
		if (this.theme != theme) {
			this.theme = theme;
			worldRenderer = theme.createWorldRenderer(world);
			scoreRenderer = theme.createScoreRenderer(world, game);
			liveCounterRenderer = theme.createLiveCounterRenderer(world, game);
			levelCounterRenderer = theme.createLevelCounterRenderer(world, game);
			messagesRenderer = theme.createMessagesRenderer();
			folks.pacMan.setTheme(theme);
			folks.ghosts().forEach(ghost -> ghost.setTheme(theme));
		}
	}

	@Override
	public Theme getTheme() {
		return theme;
	}

	@Override
	public void init() {
		clearMessages();
		world.setChanging(false);
	}

	@Override
	public void update() {
		folks.ghosts().filter(ghost -> ghost.steering() instanceof PathProvidingSteering).forEach(ghost -> {
			PathProvidingSteering pathProvider = (PathProvidingSteering) ghost.steering();
			pathProvider.setPathComputed(showingRoutes);
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

	public void showGameReady() {
		showMessage(2, "Ready!", Color.YELLOW);
	}

	public void showGameOver() {
		showMessage(2, "Game Over!", Color.RED);
	}

	/**
	 * 
	 * @param number message number (1 or 2)
	 * @param text   message text
	 * @param color  message color
	 */
	public void showMessage(int number, String text, Color color) {
		messages[number - 1].text = text;
		messages[number - 1].color = color;
	}

	public void clearMessages() {
		clearMessage(1);
		clearMessage(2);
	}

	/**
	 * Clears the message with the given number.
	 * 
	 * @param number message number (1 or 2)
	 */
	public void clearMessage(int number) {
		messages[number - 1].text = null;
	}

	public void enableGhostAnimations(boolean enabled) {
		folks.ghosts().map(Ghost::renderer).forEach(r -> r.enableAnimation(enabled));
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
		worldRenderer.setEatenFoodColor(showingGrid ? Rendering::patternColor : tile -> Color.BLACK);
		worldRenderer.render(g);
	}

	private void drawGrid(Graphics2D g) {
		if (showingGrid) {
			gridRenderer.render(g);
		}
	}

	private void drawOneWayTiles(Graphics2D g) {
		if (showingGrid) {
			gridRenderer.drawOneWayTiles(g);
		}
	}

	private void drawMessages(Graphics2D g) {
		for (Message message : messages) {
			if (message.text != null) {
				messagesRenderer.setRow(message.row);
				messagesRenderer.setTextColor(message.color);
				messagesRenderer.drawCentered(g, message.text, world.width());
			}
		}
	}

	private void drawActors(Graphics2D g) {
		folks.pacMan.renderer().render(g);
		folks.ghostsInsideWorld().filter(ghost -> ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> ghost.renderer().render(g));
		folks.ghostsInsideWorld().filter(ghost -> !ghost.is(DEAD, ENTERING_HOUSE))
				.forEach(ghost -> ghost.renderer().render(g));
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

	private void drawFrameRate(Graphics2D g) {
		if (showingFrameRate) {
			frameRateDisplay.tf.setPosition(0, 18 * Tile.SIZE);
			frameRateDisplay.draw(g);
		}
	}

	private void drawStates(Graphics2D g) {
		if (showingStates) {
			statesRenderer.render(g);
		}
	}

	private void drawRoutes(Graphics2D g) {
		if (showingRoutes) {
			routesRenderer.render(g);
		}
	}
}