package de.amr.games.pacman.view.dashboard.fsm;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

public class FsmDashboard extends JFrame {

	static int WIDTH = 1024, HEIGHT = 700;

	class FsmSelectionModel extends DefaultComboBoxModel<String> {

		public FsmSelectionModel() {
			model.data().forEach(data -> {
				addElement(data.getFsm().getDescription());
			});
		}
	}

	private final FsmModel model;
	private MultiPanel multiPanel;
	private FsmGraphView[] view = new FsmGraphView[4];

	public FsmDashboard(FsmModel model) {
		this.model = model;
		setTitle("Pac-Man State Machines Dashboard");
		multiPanel = new MultiPanel();
		multiPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		getContentPane().add(multiPanel);

		multiPanel.getComboBox(0).setModel(new FsmSelectionModel());
		multiPanel.getComboBox(0).addItemListener(this::onFsmSelection);
		view[0] = new FsmGraphView();
		multiPanel.getPanel(0).add(view[0]);

		multiPanel.getComboBox(1).setModel(new FsmSelectionModel());
		multiPanel.getComboBox(1).addItemListener(this::onFsmSelection);
		view[1] = new FsmGraphView();
		multiPanel.getPanel(1).add(view[1]);

		multiPanel.getComboBox(2).setModel(new FsmSelectionModel());
		multiPanel.getComboBox(2).addItemListener(this::onFsmSelection);
		view[2] = new FsmGraphView();
		multiPanel.getPanel(2).add(view[2]);

		multiPanel.getComboBox(3).setModel(new FsmSelectionModel());
		multiPanel.getComboBox(3).addItemListener(this::onFsmSelection);
		view[3] = new FsmGraphView();
		multiPanel.getPanel(3).add(view[3]);

		pack();
	}

	@SuppressWarnings("unchecked")
	private void onFsmSelection(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			JComboBox<String> combo = (JComboBox<String>) e.getSource();
			int comboIndex = comboIndex(combo);
			List<FsmData> dataList = model.data().collect(Collectors.toList());
			view[comboIndex].setData(dataList.get(combo.getSelectedIndex()));
		}
	}

	private int comboIndex(JComboBox<String> combo) {
		if (combo == multiPanel.getComboBox(0)) {
			return 0;
		}
		if (combo == multiPanel.getComboBox(1)) {
			return 1;
		}
		if (combo == multiPanel.getComboBox(2)) {
			return 2;
		}
		if (combo == multiPanel.getComboBox(3)) {
			return 3;
		}
		throw new IllegalArgumentException();
	}

	public void rebuild() {
		int fsmCount = (int) model.data().count();
		for (int i = 0; i < 4; ++i) {
			multiPanel.getComboBox(i).setModel(new FsmSelectionModel());
			int selection = Math.min(i, fsmCount);
			multiPanel.getComboBox(i).setSelectedIndex(selection);
			if (selection >= 0) {
				view[i].update();
			}
		}
	}

	public void update() {
		for (int i = 0; i < 4; ++i) {
			int selection = multiPanel.getComboBox(i).getSelectedIndex();
			if (selection >= 0) {
				view[i].update();
			}
		}
	}
}