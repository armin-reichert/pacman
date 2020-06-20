package de.amr.games.pacman.view.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.view.settings.GameStateViewModel.Column;
import net.miginfocom.swing.MigLayout;

public class GameStateView extends JPanel {

	private JTable table;
	public GameStateViewModel model;
	private JLabel lblGameControllerState;

	public GameStateView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[][grow][grow]"));

		lblGameControllerState = new JLabel("Game Controller State");
		lblGameControllerState.setForeground(Color.BLUE);
		lblGameControllerState.setFont(new Font("SansSerif", Font.BOLD, 16));
		content.add(lblGameControllerState, "cell 0 0,alignx center");

		JScrollPane scrollPane = new JScrollPane();
		content.add(scrollPane, "cell 0 1,grow");

		table = new JTable();
		scrollPane.setViewportView(table);
		table.setModel(GameStateViewModel.SAMPLE_DATA);
	}

	public void createModel(GameController gameController) {
		model = new GameStateViewModel(gameController);
		model.addTableModelListener(e -> {
			int remaining = gameController.state().getTicksRemaining();
			int duration = gameController.state().getDuration();
			if (duration != Integer.MAX_VALUE) {
				lblGameControllerState.setText(String.format("%s (%s sec of %s sec remaining)",
						gameController.getState().name(), formatTicksAsSeconds(remaining), formatTicksAsSeconds(duration)));
			} else {
				lblGameControllerState.setText(gameController.getState().name());
			}
		});
		table.setModel(model);
		column(Column.Tile).setCellRenderer(new TileCellRenderer());
		column(Column.Remaining).setCellRenderer(new TicksCellRenderer());
		column(Column.Duration).setCellRenderer(new TicksCellRenderer());
	}

	private TableColumn column(Column column) {
		return table.getColumnModel().getColumn(column.ordinal());
	}

	private static String formatTicksAsSeconds(int ticks) {
		return String.format("%.2f", ticks / 60f);
	}
}