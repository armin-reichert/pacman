package de.amr.games.pacman.view.dashboard.states;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

class TrafficLights implements Icon {

	enum Light {
		GREEN, YELLOW, RED
	};

	private int height;
	private boolean red;
	private boolean yellow;
	private boolean green;
	private Color lightOff = Color.DARK_GRAY;
	private Color frameColor = Color.LIGHT_GRAY;
	private int margin = 1;

	public TrafficLights(int height) {
		this.height = height;
	}

	public void set(Light... lights) {
		red = yellow = green = false;
		for (Light light : lights) {
			if (light == Light.RED) {
				red = true;
			}
			if (light == Light.YELLOW) {
				yellow = true;
			}
			if (light == Light.GREEN) {
				green = true;
			}
		}
	}

	@Override
	public synchronized void paintIcon(Component c, Graphics gc, int x, int y) {
		int d = height - 2 * margin; // light diameter
		Graphics2D g = (Graphics2D) gc.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(frameColor);
		g.fillRect(x, y, getIconWidth(), getIconHeight());
		g.setColor(red ? Color.RED : lightOff);
		g.fillOval(x + margin, y + margin, d, d);
		g.setColor(yellow ? Color.YELLOW : lightOff);
		g.fillOval(x + height + margin, y + margin, d, d);
		g.setColor(green ? Color.GREEN : lightOff);
		g.fillOval(x + 2 * height + margin, y + margin, d, d);
		g.dispose();
	}

	@Override
	public int getIconWidth() {
		return 3 * height;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
}