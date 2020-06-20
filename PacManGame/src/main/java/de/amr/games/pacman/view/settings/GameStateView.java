package de.amr.games.pacman.view.settings;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import de.amr.games.pacman.controller.GameController;
import net.miginfocom.swing.MigLayout;

public class GameStateView extends JPanel {

	private JTable table;
	public GameStateViewModel model;

	public GameStateView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[grow][grow]"));

		JScrollPane scrollPane = new JScrollPane();
		content.add(scrollPane, "cell 0 0,grow");

		table = new JTable();
		scrollPane.setViewportView(table);
	}

	public void createModel(GameController gameController) {
		model = new GameStateViewModel(gameController);
		table.setModel(model);
		TableColumnModel columns = table.getColumnModel();
		columns.getColumn(GameStateViewModel.Columns.Tile.ordinal()).setCellRenderer(new TileColumnRenderer());
		columns.getColumn(GameStateViewModel.Columns.Remaining.ordinal()).setCellRenderer(new TicksColumnRenderer());
		columns.getColumn(GameStateViewModel.Columns.Duration.ordinal()).setCellRenderer(new TicksColumnRenderer());
	}
}