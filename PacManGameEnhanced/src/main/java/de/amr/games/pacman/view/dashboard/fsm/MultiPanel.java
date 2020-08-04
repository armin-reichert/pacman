package de.amr.games.pacman.view.dashboard.fsm;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import net.miginfocom.swing.MigLayout;

public class MultiPanel extends JPanel {

	private JSplitPane horizontalSplitBottom;
	private JComboBox<String> comboBox0;
	private JComboBox<String> comboBox1;
	private JComboBox<String> comboBox2;
	private JComboBox<String> comboBox3;
	private JPanel container0;
	private JPanel container1;
	private JPanel container2;
	private JPanel container3;
	private JToolBar toolBar0;
	private JToolBar toolBar1;
	private JToolBar toolBar2;
	private JToolBar toolBar3;
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

		container0 = new JPanel();
		horizontalSplitTop.setLeftComponent(container0);
		container0.setLayout(new MigLayout("", "[grow][trailing]", "[22px][grow]"));

		comboBox0 = new JComboBox<>();
		comboBox0.setMaximumRowCount(20);
		container0.add(comboBox0, "cell 0 0,alignx left,aligny top");

		toolBar0 = new JToolBar();
		toolBar0.setFloatable(false);
		container0.add(toolBar0, "cell 1 0");

		panel0 = new JPanel();
		container0.add(panel0, "cell 0 1 2 1,grow");
		panel0.setLayout(new BorderLayout(0, 0));

		container1 = new JPanel();
		horizontalSplitTop.setRightComponent(container1);
		container1.setLayout(new MigLayout("", "[grow][trailing]", "[22px][grow]"));

		comboBox1 = new JComboBox<>();
		comboBox1.setMaximumRowCount(20);
		container1.add(comboBox1, "cell 0 0,alignx left,aligny top");

		toolBar1 = new JToolBar();
		toolBar1.setFloatable(false);
		container1.add(toolBar1, "cell 1 0");

		panel1 = new JPanel();
		container1.add(panel1, "cell 0 1 2 1,grow");
		panel1.setLayout(new BorderLayout(0, 0));

		horizontalSplitBottom = new JSplitPane();
		horizontalSplitBottom.setResizeWeight(0.5);
		verticalSplitPane.setRightComponent(horizontalSplitBottom);

		container2 = new JPanel();
		horizontalSplitBottom.setLeftComponent(container2);
		container2.setLayout(new MigLayout("", "[grow][trailing]", "[][grow]"));

		comboBox2 = new JComboBox<>();
		comboBox2.setMaximumRowCount(20);
		container2.add(comboBox2, "cell 0 0,alignx left,aligny top");

		toolBar2 = new JToolBar();
		toolBar2.setFloatable(false);
		container2.add(toolBar2, "cell 1 0");

		panel2 = new JPanel();
		container2.add(panel2, "cell 0 1 2 1,grow");
		panel2.setLayout(new BorderLayout(0, 0));

		container3 = new JPanel();
		horizontalSplitBottom.setRightComponent(container3);
		container3.setLayout(new MigLayout("", "[grow][trailing]", "[][grow]"));

		comboBox3 = new JComboBox<>();
		comboBox3.setMaximumRowCount(20);
		container3.add(comboBox3, "cell 0 0,alignx left,aligny top");

		toolBar3 = new JToolBar();
		toolBar3.setFloatable(false);
		container3.add(toolBar3, "cell 1 0");

		panel3 = new JPanel();
		container3.add(panel3, "cell 0 1 2 1,grow");
		panel3.setLayout(new BorderLayout(0, 0));
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

	public JToolBar getToolBar(int i) {
		switch (i) {
		case 0:
			return toolBar0;
		case 1:
			return toolBar1;
		case 2:
			return toolBar2;
		case 3:
			return toolBar3;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}