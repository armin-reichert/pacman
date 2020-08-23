package de.amr.games.pacman.view.play;

import java.awt.Color;

class Message {

	public String text;
	public Color color;
	public int row;

	public Message(int row) {
		this.row = row;
		text = null;
		color = Color.LIGHT_GRAY;
	}
}