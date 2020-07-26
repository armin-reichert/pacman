package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.game.GhostCommand;
import de.amr.games.pacman.controller.ghosthouse.DoorMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.theme.api.IRenderer;
import de.amr.games.pacman.view.theme.api.IWorldRenderer;
import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;

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

	protected final World world;
	protected final Folks folks;
	protected Game game;

	protected final Message[] messages;

	protected Theme theme;
	protected IWorldRenderer worldRenderer;
	protected IRenderer scoreRenderer;
	protected IRenderer liveCounterRenderer;
	protected IRenderer levelCounterRenderer;
	protected MessagesRenderer messagesRenderer;

	protected boolean showingScores = true;

	public PlayView(World world, Theme theme, Folks folks, Game game, GhostCommand ghostCommand, DoorMan doorMan) {
		this.world = world;
		this.folks = folks;
		this.game = game;
		messages = new Message[] { new Message(15), new Message(21) };
		setTheme(theme);
	}

	@Override
	public void update() {
	}

	public Stream<Ghost> ghostsInsideWorld() {
		return folks.ghosts().filter(world::contains);
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
	}

	@Override
	public void draw(Graphics2D g) {
		drawWorld(g);
		drawMessages(g);
		drawActors(g);
		drawScores(g);
		drawLiveCounter(g);
		drawLevelCounter(g);
	}

	/**
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

	protected void drawWorld(Graphics2D g) {
		worldRenderer.render(g);
	}

	protected void drawMessages(Graphics2D g) {
		for (Message message : messages) {
			if (message.text != null) {
				messagesRenderer.setRow(message.row);
				messagesRenderer.setTextColor(message.color);
				messagesRenderer.drawCentered(g, message.text, world.width());
			}
		}
	}

	protected void drawActors(Graphics2D g) {
		folks.pacMan.draw(g);
		ghostsInsideWorld().filter(ghost -> ghost.is(DEAD, ENTERING_HOUSE)).forEach(ghost -> ghost.draw(g));
		ghostsInsideWorld().filter(ghost -> !ghost.is(DEAD, ENTERING_HOUSE)).forEach(ghost -> ghost.draw(g));
	}

	protected void drawScores(Graphics2D g) {
		if (showingScores) {
			scoreRenderer.render(g);
		}
	}

	protected void drawLiveCounter(Graphics2D g) {
		g.translate(Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		liveCounterRenderer.render(g);
		g.translate(-Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}

	protected void drawLevelCounter(Graphics2D g) {
		g.translate(world.width() * Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		levelCounterRenderer.render(g);
		g.translate(-world.width() * Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}
}