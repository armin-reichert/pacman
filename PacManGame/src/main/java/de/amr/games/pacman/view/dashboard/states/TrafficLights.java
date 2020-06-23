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

	private int lightDiameter;
	private int longSide;
	private int shortSide;
	private boolean red;
	private boolean yellow;
	private boolean green;
	private Color lightOff = Color.DARK_GRAY;
	private Color frameColor = Color.LIGHT_GRAY;
	private int margin = 2;

	public TrafficLights(int diameter) {
		lightDiameter = diameter;
		shortSide = lightDiameter + 2 * margin;
		longSide = 3 * shortSide;
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
		g.setColor(frameColor);
		g.fillRect(x, y, getIconWidth(), getIconHeight());
		g.setColor(red ? Color.RED : lightOff);
		g.fillOval(x + margin, y + margin, lightDiameter, lightDiameter);
		g.setColor(yellow ? Color.YELLOW : lightOff);
		g.fillOval(x + shortSide + margin, y + margin, lightDiameter, lightDiameter);
		g.setColor(green ? Color.GREEN : lightOff);
		g.fillOval(x + 2 * shortSide + margin, y + margin, lightDiameter, lightDiameter);
		g.dispose();
	}

	@Override
	public int getIconWidth() {
		return longSide;
	}

	@Override
	public int getIconHeight() {
		return shortSide;
	}
}