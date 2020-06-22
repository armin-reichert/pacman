package de.amr.games.pacman.view.dashboard;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TileRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		GameStateTableModel model = (GameStateTableModel) table.getModel();
		if (model != null) {
			if (model.records[row].pacManCollision) {
				setBackground(new Color(255, 0, 0, 100));
			} else {
				setBackground(table.getBackground());
			}
		}
		return this;
	}
}