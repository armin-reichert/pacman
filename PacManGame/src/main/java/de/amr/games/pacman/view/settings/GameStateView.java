package de.amr.games.pacman.view.settings;

import static de.amr.games.pacman.view.settings.Formatting.seconds;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.view.settings.GameStateTableModel.Column;
import net.miginfocom.swing.MigLayout;

/**
 * Displays information (state, timer values, directions, speed) about the actors and the global
 * game controller.
 * 
 * @author Armin Reichert
 */
public class GameStateView extends JPanel {

	public GameStateTableModel tableModel;

	Action actionShowRoutes = new AbstractAction("Show Routes") {

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox cb = (JCheckBox) e.getSource();
			gameController.setShowingActorRoutes(cb.isSelected());
		}
	};

	Action actionShowStates = new AbstractAction("Show States and Counters") {

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox cb = (JCheckBox) e.getSource();
			gameController.setShowingStates(cb.isSelected());
		}
	};

	private GameController gameController;
	private JTable table;
	private JLabel lblGameControllerState;
	private JCheckBox cbShowRoutes;
	private JCheckBox cbShowStates;

	public GameStateView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[][grow][][grow]"));

		lblGameControllerState = new JLabel("Game Controller State");
		lblGameControllerState.setForeground(Color.BLUE);
		lblGameControllerState.setFont(new Font("SansSerif", Font.BOLD, 16));
		content.add(lblGameControllerState, "cell 0 0,alignx center");

		JScrollPane scrollPane = new JScrollPane();
		content.add(scrollPane, "cell 0 1,grow");

		table = new JTable();
		scrollPane.setViewportView(table);
		table.setModel(GameStateTableModel.SAMPLE_DATA);

		cbShowRoutes = new JCheckBox("Show Routes");
		cbShowRoutes.setAction(actionShowRoutes);
		content.add(cbShowRoutes, "flowx,cell 0 2");

		cbShowStates = new JCheckBox("Show States and Counters");
		cbShowStates.setAction(actionShowStates);
		content.add(cbShowStates, "cell 0 2");
	}

	/**
	 * Attaches this view to the game controller.
	 * 
	 * @param gameController the game controller
	 */
	public void createModel(GameController gameController) {
		this.gameController = gameController;
		tableModel = new GameStateTableModel(gameController);
		table.setModel(tableModel);
		column(Column.Tile).setCellRenderer(new TileCellRenderer());
		column(Column.Speed).setCellRenderer(new SpeedCellRenderer());
		column(Column.Remaining).setCellRenderer(new TicksCellRenderer());
		column(Column.Duration).setCellRenderer(new TicksCellRenderer());
		updateViewState();
	}

	private TableColumn column(Column column) {
		return table.getColumnModel().getColumn(column.ordinal());
	}

	public void updateViewState() {
		String stateText = gameController.getState().name();
		if (gameController.state().getDuration() != Integer.MAX_VALUE) {
			stateText = String.format("%s (%s sec of %s sec remaining)", gameController.getState(),
					seconds(gameController.state().getTicksRemaining()), seconds(gameController.state().getDuration()));
		}
		lblGameControllerState.setText(stateText);
		cbShowRoutes.setSelected(gameController.isShowingActorRoutes());
		cbShowStates.setSelected(gameController.isShowingStates());
	}
}