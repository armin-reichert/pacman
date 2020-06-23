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

	private int d;
	private boolean red;
	private boolean yellow;
	private boolean green;

	public TrafficLights(int d) {
		this.d = d;
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
		Graphics2D g = (Graphics2D) gc.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x, y, getIconWidth(), getIconHeight());
		g.setColor(red ? Color.RED : Color.BLACK);
		g.fillOval(x + 1, y + 1, d - 2, d - 2);
		g.setColor(yellow ? Color.YELLOW : Color.BLACK);
		g.fillOval(x + d + 1, y + 1, d - 2, d - 2);
		g.setColor(green ? Color.GREEN : Color.BLACK);
		g.fillOval(x + 2 * d + 1, y + 1, d - 2, d - 2);
		g.dispose();
	}

	@Override
	public int getIconWidth() {
		return 3 * d;
	}

	@Override
	public int getIconHeight() {
		return d;
	}
}