/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.view.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacmanfsm.model.world.api.TiledWorld;
import de.amr.games.pacmanfsm.theme.api.MessagesRenderer;
import de.amr.games.pacmanfsm.theme.api.Theme;

/**
 * Renders the messages inside the play view.
 * 
 * @author Armin Reichert
 */
public class MessagesView {

	public final List<Message> messages;
	private final TiledWorld world;
	private MessagesRenderer renderer;

	public MessagesView(Theme theme, TiledWorld world, int... rows) {
		this.world = world;
		this.messages = new ArrayList<>();
		for (int row : rows) {
			messages.add(new Message(row));
		}
		setTheme(theme);
	}

	public void setTheme(Theme theme) {
		renderer = theme.messagesRenderer();
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
		for (Message message : messages) {
			if (message.text != null) {
				renderer.setRow(message.row);
				renderer.setTextColor(message.color);
				renderer.draw(g, message.text, world.width());
			}
		}
	}
}