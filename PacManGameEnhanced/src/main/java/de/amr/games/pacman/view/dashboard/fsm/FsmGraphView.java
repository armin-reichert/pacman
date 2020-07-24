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

	private StateMachineInfo fsmInfo;
	private JLabel graphDisplay;

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
		if (fsmInfo != null) {
			BufferedImage renderedGraph = Graphviz.fromString(fsmInfo.dotText).scale(fsmInfo.scaling).render(Format.PNG)
					.toImage();
			graphDisplay.setIcon(new ImageIcon(renderedGraph));
		} else {
			graphDisplay.setIcon(null);
		}
	}

	public void setFsmInfo(StateMachineInfo fsmInfo) {
		this.fsmInfo = fsmInfo;
		update();
	}
}