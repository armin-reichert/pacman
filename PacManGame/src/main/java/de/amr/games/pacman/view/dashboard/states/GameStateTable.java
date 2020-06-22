package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.view.dashboard.Formatting.pixelsPerSec;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import de.amr.games.pacman.controller.actor.Ghost.Sanity;
import de.amr.games.pacman.view.dashboard.Formatting;
import de.amr.games.pacman.view.dashboard.states.GameStateTableModel.ActorRow;
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
			if (gstm.record(row).pacManCollision) {
				setBackground(HILIGHT_COLOR);
			} else {
				setBackground(table.getBackground());
			}
			return this;
		}
	}

	static class TicksCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setText(Formatting.ticksAndSeconds((int) value));
			return this;
		}
	}

	static class SpeedCellRenderer extends DefaultTableCellRenderer {

		private float pacManSpeed(GameStateTableModel model) {
			return model.record(ActorRow.PacMan).speed;
		}

		private boolean isBlinkyInsane(GameStateTableModel model) {
			Sanity sanity = model.record(ActorRow.Blinky).ghostSanity;
			return sanity == Sanity.CRUISE_ELROY1 || sanity == Sanity.CRUISE_ELROY2;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			GameStateTableModel model = (GameStateTableModel) table.getModel();
			float speed = (float) value;
			setText(pixelsPerSec(speed));
			if (row == ActorRow.Blinky.ordinal() && isBlinkyInsane(model) && speed >= pacManSpeed(model)) {
				setBackground(HILIGHT_COLOR);
			} else {
				setBackground(table.getBackground());
			}
			return this;
		}
	}

	public GameStateTable() {
		super(GameStateTableModel.LOREM_IPSUM);
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