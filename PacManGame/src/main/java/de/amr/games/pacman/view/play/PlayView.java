package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.common.MessagesRenderer;

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
	protected MessagesRenderer messagesRenderer;

	protected boolean showingScores = true;

	public PlayView(World world, Theme theme, Folks folks, Game game) {
		this.world = world;
		this.folks = folks;
		this.game = game;
		messages = new Message[] { new Message(15), new Message(21) };
		setTheme(theme);
	}

	@Override
	public void init() {
		clearMessages();
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
			messagesRenderer = theme.messagesRenderer();
			folks.pacMan.setTheme(theme);
			folks.ghosts().forEach(ghost -> ghost.setTheme(theme));
		}
	}

	@Override
	public Theme getTheme() {
		return theme;
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
		theme.worldRenderer(world).render(g, world);
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

	protected void drawPacMan(Graphics2D g, PacMan pacMan) {
		theme.pacManRenderer(pacMan).render(g, pacMan);
	}

	protected void drawGhost(Graphics2D g, Ghost ghost) {
		theme.ghostRenderer(ghost).render(g, ghost);
	}

	protected void drawActors(Graphics2D g) {
		drawPacMan(g, folks.pacMan);
		ghostsInsideWorld().filter(ghost -> ghost.is(DEAD, ENTERING_HOUSE)).forEach(ghost -> drawGhost(g, ghost));
		ghostsInsideWorld().filter(ghost -> !ghost.is(DEAD, ENTERING_HOUSE)).forEach(ghost -> drawGhost(g, ghost));
	}

	protected void drawScores(Graphics2D g) {
		if (showingScores) {
			theme.pointsCounterRenderer().render(g, game);
		}
	}

	protected void drawLiveCounter(Graphics2D g) {
		g.translate(Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		theme.livesCounterRenderer().render(g, game);
		g.translate(-Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}

	protected void drawLevelCounter(Graphics2D g) {
		g.translate(world.width() * Tile.SIZE, (world.height() - 2) * Tile.SIZE);
		theme.levelCounterRenderer().render(g, game);
		g.translate(-world.width() * Tile.SIZE, -(world.height() - 2) * Tile.SIZE);
	}
}