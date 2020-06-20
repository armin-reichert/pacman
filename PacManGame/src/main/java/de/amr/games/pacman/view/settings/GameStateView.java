package de.amr.games.pacman.view.settings;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import de.amr.games.pacman.controller.GameController;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;

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
		table.setModel(model);
		TableColumnModel columns = table.getColumnModel();
		columns.getColumn(GameStateViewModel.Columns.Tile.ordinal()).setCellRenderer(new TileColumnRenderer());
		columns.getColumn(GameStateViewModel.Columns.Remaining.ordinal()).setCellRenderer(new TicksColumnRenderer());
		columns.getColumn(GameStateViewModel.Columns.Duration.ordinal()).setCellRenderer(new TicksColumnRenderer());
		model.addTableModelListener(e -> {
			GameController gc = model.gameController;
			int remaining = gc.state().getTicksRemaining();
			int duration = gc.state().getDuration();
			if (duration != Integer.MAX_VALUE) {
				lblGameControllerState.setText(String.format("%s (%s sec of %s sec remaining)", gc.getState().name(),
						formatTicksAsSeconds(remaining), formatTicksAsSeconds(duration)));
			} else {
				lblGameControllerState.setText(gc.getState().name());
			}
		});
	}

	static final String formatTicksAsSeconds(int ticks) {
		return String.format("%.2f", ticks / 60f);
	}
}