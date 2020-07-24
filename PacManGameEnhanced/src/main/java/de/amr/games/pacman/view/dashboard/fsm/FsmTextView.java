package de.amr.games.pacman.view.dashboard.fsm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.amr.easy.game.controller.Lifecycle;

public class FsmTextView extends JPanel implements Lifecycle {

	static final String HINT_TEXT = "This area shows the Graphviz representation of the selected finite-state machine";

	private FsmViewNodeInfo fsmInfo;
	private JTextArea textArea;

	public FsmTextView() {
		setBackground(Color.WHITE);
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setTabSize(4);
		textArea.setText(HINT_TEXT);

		scrollPane.setViewportView(textArea);
	}

	public void setFsmInfo(FsmViewNodeInfo fsmInfo) {
		this.fsmInfo = fsmInfo;
		update();
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (fsmInfo != null) {
			textArea.setText(fsmInfo.dotText);
			textArea.setCaretPosition(0);
		} else {
			textArea.setText(HINT_TEXT);
		}
	}
}