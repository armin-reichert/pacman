package de.amr.games.pacman.view.dashboard.fsm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

public class FsmGraphView extends JPanel {

	static final double SCALE_MIN = 0.4;
	static final double SCALE_MAX = 3.0;
	static final double SCALE_STEP = 0.2;

	public Action actionZoomIn = new AbstractAction("Zoom In") {
		@Override
		public void actionPerformed(ActionEvent e) {
			zoomIn();
		};
	};

	public Action actionZoomOut = new AbstractAction("Zoom Out") {
		@Override
		public void actionPerformed(ActionEvent e) {
			zoomOut();
		};
	};

	private FsmData data;
	private JLabel graphDisplay;
	private double scaling = 1.0;

	public FsmGraphView() {
		setBackground(Color.WHITE);
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		graphDisplay = new JLabel("");
		graphDisplay.setOpaque(true);
		graphDisplay.setBackground(Color.WHITE);
		graphDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane.setViewportView(graphDisplay);

		getInputMap().put(KeyStroke.getKeyStroke('+'), actionZoomIn);
		getActionMap().put(actionZoomIn, actionZoomIn);
		getInputMap().put(KeyStroke.getKeyStroke('-'), actionZoomOut);
		getActionMap().put(actionZoomOut, actionZoomOut);
	}

	public void update() {
		if (data != null) {
			BufferedImage png = Graphviz.fromString(data.getGraph()).totalMemory(20_000_000).scale(scaling).render(Format.PNG)
					.toImage();
			graphDisplay.setIcon(new ImageIcon(png));
		} else {
			graphDisplay.setIcon(null);
		}
	}
	
	public FsmData getData() {
		return data;
	}

	public void setData(FsmData data) {
		this.data = data;
		update();
	}

	public void zoomIn() {
		scaling = largerScaling();
		update();
	}

	public void zoomOut() {
		scaling = smallerScaling();
		update();
	}

	private double scaling() {
		return scaling;
	}

	private double largerScaling() {
		return Math.min(SCALE_MAX, scaling() + SCALE_STEP);
	}

	private double smallerScaling() {
		return Math.max(SCALE_MIN, scaling() - SCALE_STEP);
	}
}