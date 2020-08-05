package de.amr.games.pacman.view.dashboard.fsm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import de.amr.easy.game.ui.widgets.MultiPanel;

/**
 * Shows up to 4 state machines in a window.
 * 
 * @author Armin Reichert
 */
public class FsmDashboard extends JFrame {

	static class FsmSelectionModel extends DefaultComboBoxModel<String> {

		public FsmSelectionModel(FsmModel model) {
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
		setTitle("State Machines Dashboard");
		multiPanel = new MultiPanel();
		multiPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		getContentPane().add(multiPanel);
		for (int i = 0; i < 4; ++i) {
			multiPanel.getComboBox(i).setModel(new FsmSelectionModel(model));
			multiPanel.getComboBox(i).addItemListener(this::onFsmSelection);
			view[i] = new FsmGraphView();
			multiPanel.getPanel(i).add(view[i], BorderLayout.CENTER);
			multiPanel.getToolBar(i).add(view[i].actionZoomIn);
			multiPanel.getToolBar(i).add(view[i].actionZoomOut);
		}
	}

	@SuppressWarnings("unchecked")
	private void onFsmSelection(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			JComboBox<String> combo = (JComboBox<String>) e.getSource();
			int index = comboIndex(combo);
			List<FsmData> dataList = model.data().collect(Collectors.toList());
			view[index].setData(dataList.get(combo.getSelectedIndex()));
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
		List<FsmData> dataList = model.data().collect(Collectors.toList());
		for (int i = 0; i < 4; ++i) {
			multiPanel.getComboBox(i).setModel(new FsmSelectionModel(model));
			if (i < dataList.size()) {
				multiPanel.getComboBox(i).setSelectedIndex(i);
				view[i].setData(dataList.get(i));
			} else {
				multiPanel.getComboBox(i).setSelectedIndex(-1);
				view[i].setData(null);
			}
		}
	}

	public void update() {
		for (int i = 0; i < 4; ++i) {
			if (view[i].getData() != null) {
				view[i].getData().updateGraph();
			}
			view[i].update();
		}
	}
}