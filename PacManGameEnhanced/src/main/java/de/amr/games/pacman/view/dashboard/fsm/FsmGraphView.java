package de.amr.games.pacman.view.dashboard.fsm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import de.amr.easy.game.controller.Lifecycle;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

public class FsmGraphView extends JPanel implements Lifecycle {

	static final double MIN_SCALE = 0.4;
	static final double MAX_SCALE = 3.0;

	private FsmViewTreeNode info;
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
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (info != null) {
			BufferedImage renderedGraph = Graphviz.fromString(info.dotText).scale(scaling).render(Format.PNG).toImage();
			graphDisplay.setIcon(new ImageIcon(renderedGraph));
		} else {
			graphDisplay.setIcon(null);
		}
	}

	public void setScaling(double scaling) {
		this.scaling = scaling;
	}

	public void setFsmInfo(FsmViewTreeNode fsmInfo) {
		this.info = fsmInfo;
		update();
	}

	public void zoomIn() {
		scaling += 0.2;
		scaling = Math.min(MAX_SCALE, scaling);
		update();
	}

	public void zoomOut() {
		scaling -= 0.2;
		scaling = Math.max(MIN_SCALE, scaling);
		update();
	}
}