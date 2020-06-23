package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.controller.actor.Ghost.Sanity.CRUISE_ELROY1;
import static de.amr.games.pacman.controller.actor.Ghost.Sanity.CRUISE_ELROY2;
import static de.amr.games.pacman.view.dashboard.Formatting.pixelsPerSec;
import static de.amr.games.pacman.view.dashboard.Formatting.ticksAndSeconds;
import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_BLINKY;
import static de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ROW_PACMAN;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import de.amr.games.pacman.controller.actor.Ghost.Sanity;
import de.amr.games.pacman.view.dashboard.states.GameStateTableModel.Field;

/**
 * Displays detailed information about the actors in the game that is updated at every tick.
 * 
 * @author Armin Reichert
 */
public class GameStateTable extends JTable {

	static final Color HILIGHT_COLOR = new Color(255, 0, 0, 100);

	static class TileCellRenderer extends DefaultTableCellRenderer {

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

		private static boolean isBlinkyInsane(GameStateTableModel model) {
			Sanity sanity = model.record(ROW_BLINKY).ghostSanity;
			return sanity == CRUISE_ELROY1 || sanity == CRUISE_ELROY2;
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
	}

	@Override
	public void setModel(TableModel model) {
		super.setModel(model);
		column(Field.Tile).setCellRenderer(new TileCellRenderer());
		column(Field.Speed).setCellRenderer(new SpeedCellRenderer());
		column(Field.Remaining).setCellRenderer(new TicksCellRenderer());
		column(Field.Duration).setCellRenderer(new TicksCellRenderer());
	}

	public void update() {
		((GameStateTableModel) getModel()).update();
	}

	private TableColumn column(Field f) {
		return getColumnModel().getColumn(f.ordinal());
	}
}