package de.amr.games.pacman.view.settings;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TicksCellRenderer extends DefaultTableCellRenderer {

	private static String formatTicks(int ticks) {
		if (ticks == Integer.MAX_VALUE) {
			return Character.toString('\u221E');
		}
		return String.format("%d (%.2fs)", ticks, ticks / 60f);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setText(formatTicks((int) value));
		return this;
	}
}