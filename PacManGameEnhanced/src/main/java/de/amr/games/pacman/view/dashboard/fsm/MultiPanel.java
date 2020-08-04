package de.amr.games.pacman.view.dashboard.fsm;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class MultiPanel extends JPanel {

	private JSplitPane horizontalSplitBottom;
	private JComboBox<String> comboBox0;
	private JComboBox<String> comboBox1;
	private JComboBox<String> comboBox2;
	private JComboBox<String> comboBox3;
	private JPanel panel0;
	private JPanel panel1;
	private JPanel panel2;
	private JPanel panel3;

	public MultiPanel() {
		setLayout(new BorderLayout(0, 0));

		JSplitPane verticalSplitPane = new JSplitPane();
		verticalSplitPane.setResizeWeight(0.5);
		verticalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(verticalSplitPane, BorderLayout.CENTER);

		JSplitPane horizontalSplitTop = new JSplitPane();
		horizontalSplitTop.setResizeWeight(0.5);
		verticalSplitPane.setLeftComponent(horizontalSplitTop);

		panel0 = new JPanel();
		horizontalSplitTop.setLeftComponent(panel0);
		panel0.setLayout(new BorderLayout(0, 0));

		comboBox0 = new JComboBox<>();
		panel0.add(comboBox0, BorderLayout.NORTH);

		panel1 = new JPanel();
		horizontalSplitTop.setRightComponent(panel1);
		panel1.setLayout(new BorderLayout(0, 0));

		comboBox1 = new JComboBox<>();
		panel1.add(comboBox1, BorderLayout.NORTH);

		horizontalSplitBottom = new JSplitPane();
		horizontalSplitBottom.setResizeWeight(0.5);
		verticalSplitPane.setRightComponent(horizontalSplitBottom);

		panel2 = new JPanel();
		horizontalSplitBottom.setLeftComponent(panel2);
		panel2.setLayout(new BorderLayout(0, 0));

		comboBox2 = new JComboBox<>();
		panel2.add(comboBox2, BorderLayout.NORTH);

		panel3 = new JPanel();
		horizontalSplitBottom.setRightComponent(panel3);
		panel3.setLayout(new BorderLayout(0, 0));

		comboBox3 = new JComboBox<>();
		panel3.add(comboBox3, BorderLayout.NORTH);
	}

	public JComboBox<String> getComboBox(int i) {
		switch (i) {
		case 0:
			return comboBox0;
		case 1:
			return comboBox1;
		case 2:
			return comboBox2;
		case 3:
			return comboBox3;
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	public JPanel getPanel(int i) {
		switch (i) {
		case 0:
			return panel0;
		case 1:
			return panel1;
		case 2:
			return panel2;
		case 3:
			return panel3;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}