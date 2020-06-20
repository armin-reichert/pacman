package de.amr.games.pacman.view.settings;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TileColumnRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		GameStateViewModel model = (GameStateViewModel) table.getModel();
		if (model.data[row].pacManCollision) {
			setBackground(Color.RED);
		} else {
			setBackground(table.getBackground());
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}