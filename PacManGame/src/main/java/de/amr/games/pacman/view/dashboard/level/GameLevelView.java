package de.amr.games.pacman.view.dashboard.level;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.GameController;
import net.miginfocom.swing.MigLayout;

public class GameLevelView extends JPanel implements Lifecycle {

	private GameController controller;
	private JTable table;
	public GameLevelTableModel tableModel;

	public GameLevelView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[grow]"));

		JScrollPane scrollPane = new JScrollPane();
		content.add(scrollPane, "cell 0 0,grow");

		table = new JTable();
		table.setRowSelectionAllowed(false);
		scrollPane.setViewportView(table);
	}

	public void attachTo(GameController controller) {
		this.controller = controller;
		init();
	}

	@Override
	public void init() {
		if (controller.game != null) {
			tableModel = new GameLevelTableModel(controller.game);
		} else {
			tableModel = new GameLevelTableModel();
		}
		table.setModel(tableModel);
	}

	@Override
	public void update() {
		if (!tableModel.hasGame()) {
			init();
		} else {
			tableModel.fireTableDataChanged();
		}
	}
}
