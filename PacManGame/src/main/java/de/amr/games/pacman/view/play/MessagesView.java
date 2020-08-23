package de.amr.games.pacman.view.play;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.common.MessagesRenderer;

/**
 * Renders the messages inside the play view.
 * 
 * @author Armin Reichert
 */
public class MessagesView {

	public final List<Message> messages;
	private final World world;
	private Theme theme;

	public MessagesView(Theme theme, World world, int... rows) {
		this.world = world;
		this.messages = new ArrayList<>();
		for (int row : rows) {
			messages.add(new Message(row));
		}
		setTheme(theme);
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	/**
	 * @param messageNumber message number
	 * @param text          message text
	 * @param color         message color
	 */
	public void showMessage(int messageNumber, String text, Color color) {
		messages.get(messageNumber - 1).text = text;
		messages.get(messageNumber - 1).color = color;
	}

	public void clearMessages() {
		clearMessage(1);
		clearMessage(2);
	}

	/**
	 * Clears the message with the given number.
	 * 
	 * @param number message number
	 */
	public void clearMessage(int number) {
		messages.get(number - 1).text = null;
	}

	public void draw(Graphics2D g) {
		MessagesRenderer renderer = theme.messagesRenderer();
		for (Message message : messages) {
			if (message.text != null) {
				renderer.setRow(message.row);
				renderer.setTextColor(message.color);
				renderer.drawCentered(g, message.text, world.width());
			}
		}
	}
}