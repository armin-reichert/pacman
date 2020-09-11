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

import de.amr.games.pacman.model.fsm.FsmData;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

/**
 * Displays a graph stored in GraphViz format.
 * 
 * @author Armin Reichert
 */
public class FsmGraphView extends JPanel {

	static final int GRAPHVIZ_MEMORY = 20_000_000;

	static final double SCALE_MIN = 0.4;
	static final double SCALE_MAX = 3.0;
	static final double SCALE_STEP = 0.2;

	static int rendering_count = 0;

	public Action actionZoomIn = new AbstractAction("Zoom In", new ImageIcon(getClass().getResource("/zoom_in.png"))) {

		@Override
		public void actionPerformed(ActionEvent e) {
			scaling = Math.min(SCALE_MAX, scaling + SCALE_STEP);
			update();
		}
	};

	public Action actionZoomOut = new AbstractAction("Zoom Out", new ImageIcon(getClass().getResource("/zoom_out.png"))) {

		@Override
		public void actionPerformed(ActionEvent e) {
			scaling = Math.max(SCALE_MIN, scaling - SCALE_STEP);
			update();
		}
	};

	private FsmData data;
	private JLabel graphDisplay;
	private double scaling = 0.8;

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
		graphDisplay.setIcon(null);
		if (data != null) {
			try {
				BufferedImage png = Graphviz.fromString(data.getGraphVizText()).totalMemory(GRAPHVIZ_MEMORY).scale(scaling)
						.render(Format.PNG).toImage();
				graphDisplay.setIcon(new ImageIcon(png));
				++rendering_count;
			} catch (Exception x) {
				System.err.println("Graphviz rendering failed for image #" + rendering_count);
				x.printStackTrace(System.err);
			}
		}
	}

	public FsmData getData() {
		return data;
	}

	public void setData(FsmData data) {
		this.data = data;
		update();
	}
}