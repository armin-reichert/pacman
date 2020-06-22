package de.amr.games.pacman.view.dashboard.states;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.amr.games.pacman.view.dashboard.Formatting;

class TicksRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setText(Formatting.ticksAndSeconds((int) value));
		return this;
	}
}