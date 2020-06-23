package de.amr.games.pacman.view.dashboard.states;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

public class TrafficLightsWidget extends JComponent {

	private int lightDiameter;
	private int longSide;
	private int shortSide;
	private boolean red;
	private boolean yellow;
	private boolean green;
	private Color lightOff = Color.DARK_GRAY;
	private Color frameColor = Color.LIGHT_GRAY;
	private int margin = 2;

	public TrafficLightsWidget() {
		this(8);
	}

	public TrafficLightsWidget(int diameter) {
		lightDiameter = diameter;
		shortSide = lightDiameter + 2 * margin;
		longSide = 3 * shortSide;
	}

	public void setGreen(boolean green) {
		this.green = green;
		repaint();
	}

	public void setYellow(boolean yellow) {
		this.yellow = yellow;
		repaint();
	}

	public void setRed(boolean red) {
		this.red = red;
		repaint();
	}

	public boolean getGreen() {
		return green;
	}

	public boolean getYellow() {
		return yellow;
	}

	public boolean getRed() {
		return red;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(longSide, shortSide);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(longSide, shortSide);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawLights((Graphics2D) g);
	}

	private void drawLights(Graphics2D g) {
		if (isVisible()) {
			g = (Graphics2D) g.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			drawLight(g, red ? Color.RED : lightOff);
			g.translate(shortSide, 0);
			drawLight(g, yellow ? Color.YELLOW : lightOff);
			g.translate(shortSide, 0);
			drawLight(g, green ? Color.GREEN : lightOff);
			g.dispose();
		}
	}

	private void drawLight(Graphics2D g, Color color) {
		g.setColor(frameColor);
		g.fillRect(0, 0, shortSide, shortSide);
		g.translate(margin, margin);
		g.setColor(color);
		g.fillOval(0, 0, lightDiameter, lightDiameter);
		g.translate(-margin, -margin);
	}
}