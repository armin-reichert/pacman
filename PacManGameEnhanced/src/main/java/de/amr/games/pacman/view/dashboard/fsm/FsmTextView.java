package de.amr.games.pacman.view.dashboard.fsm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Displays the GraphViz representation of a finite-state machine.
 * 
 * @author Armin Reichert
 */
public class FsmTextView extends JPanel {

	static final String HINT_TEXT = "This area shows the Graphviz representation of the selected finite-state machine";

	private FsmData data;
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

	public void setData(FsmData data) {
		this.data = data;
		update();
	}

	public void update() {
		if (data != null) {
			textArea.setText(data.getGraphVizText());
			textArea.setCaretPosition(0);
		} else {
			textArea.setText(HINT_TEXT);
		}
	}
}