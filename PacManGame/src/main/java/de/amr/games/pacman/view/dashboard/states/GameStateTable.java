package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_BLINKY;
import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_PACMAN;
import static de.amr.games.pacman.view.dashboard.util.Formatting.ticksAndSeconds;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ColumnInfo;
import de.amr.games.pacman.view.dashboard.util.CellHilighted;

/**
 * Displays detailed information about the actors in the game that is updated at every tick.
 * 
 * @author Armin Reichert
 */
public class GameStateTable extends JTable {

	static class TicksCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value != null) {
				setText(ticksAndSeconds((int) value));
			}
			return this;
		}
	}

	public GameStateTable() {
		super(new GameStateTableModel());
		setRowSelectionAllowed(false);
	}

	@Override
	public void setModel(TableModel model) {
		super.setModel(model);
		configureRenderers();
	}

	public void update() {
		getGameStateTableModel().update();
	}

	private void configureRenderers() {
		renderer(ColumnInfo.Tile, new CellHilighted((row, col) -> record(row) != null && record(row).pacManCollision));
		renderer(ColumnInfo.Speed,
				new CellHilighted((row, col) -> row == ROW_BLINKY && record(ROW_PACMAN).speed <= record(ROW_BLINKY).speed));
		renderer(ColumnInfo.Remaining, new TicksCellRenderer());
		renderer(ColumnInfo.Duration, new TicksCellRenderer());
	}

	private void renderer(ColumnInfo columnInfo, TableCellRenderer renderer) {
		getColumnModel().getColumn(columnInfo.ordinal()).setCellRenderer(renderer);
	}

	private GameStateTableModel getGameStateTableModel() {
		return (GameStateTableModel) getModel();
	}

	private GameStateRecord record(int row) {
		return getGameStateTableModel().record(row);
	}
}