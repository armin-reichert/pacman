package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.controller.actor.Ghost.Sanity.ELROY1;
import static de.amr.games.pacman.controller.actor.Ghost.Sanity.ELROY2;
import static de.amr.games.pacman.view.dashboard.Formatting.pixelsPerSec;
import static de.amr.games.pacman.view.dashboard.Formatting.ticksAndSeconds;
import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_BLINKY;
import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_PACMAN;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.Ghost.Sanity;
import de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ColumnInfo;

/**
 * Displays detailed information about the actors in the game that is updated at every tick.
 * 
 * @author Armin Reichert
 */
public class GameStateTable extends JTable {

	static final Color HILIGHT_COLOR = new Color(255, 0, 0, 100);

	static class TileCellRenderer extends DefaultTableCellRenderer {

		static void apply(TableColumn column) {
			column.setCellRenderer(new TileCellRenderer());
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			GameStateTableModel gstm = (GameStateTableModel) table.getModel();
			GameStateRecord r = gstm.record(row);
			setBackground(r != null && r.pacManCollision ? HILIGHT_COLOR : table.getBackground());
			return this;
		}
	}

	static class TicksCellRenderer extends DefaultTableCellRenderer {

		static void apply(TableColumn column) {
			column.setCellRenderer(new TicksCellRenderer());
		}

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

	static class SpeedCellRenderer extends DefaultTableCellRenderer {

		static void apply(TableColumn column) {
			column.setCellRenderer(new SpeedCellRenderer());
		}

		private static boolean isBlinkyInsane(GameStateTableModel model) {
			Sanity sanity = model.record(ROW_BLINKY).ghostSanity;
			return sanity == ELROY1 || sanity == ELROY2;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value != null) {
				float speed = (float) value;
				setText(pixelsPerSec(speed));
				GameStateTableModel gstm = (GameStateTableModel) table.getModel();
				setBackground(
						row == ROW_BLINKY && isBlinkyInsane(gstm) && speed >= gstm.record(ROW_PACMAN).speed ? HILIGHT_COLOR
								: table.getBackground());
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
		configureColumns();
	}

	private void configureColumns() {
		TileCellRenderer.apply(column(ColumnInfo.Tile));
		SpeedCellRenderer.apply(column(ColumnInfo.Speed));
		TicksCellRenderer.apply(column(ColumnInfo.Remaining));
		TicksCellRenderer.apply(column(ColumnInfo.Duration));
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
		GameStateTableModel model = getGameStateTableModel();
		if (e.getColumn() == ColumnInfo.OnStage.ordinal()) {
			int row = e.getFirstRow();
			GameStateRecord r = model.records[row];
			if (r.creature instanceof Ghost) {
				model.gameController.game.takePart(r.creature, r.takesPart);
			}
		}
	}

	private GameStateTableModel getGameStateTableModel() {
		return (GameStateTableModel) getModel();
	}

	public void update() {
		getGameStateTableModel().update();
	}

	private TableColumn column(ColumnInfo info) {
		return getColumnModel().getColumn(info.ordinal());
	}
}