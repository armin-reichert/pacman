package de.amr.games.pacman.view.dashboard.util;

import java.awt.Color;
import java.awt.Component;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CellFormatter extends DefaultTableCellRenderer {

	public Color hilightColor = new Color(255, 0, 0, 100);
	public BiFunction<Integer, Integer, Boolean> fnHilightCondition = (row, col) -> false;
	public Function<Object, String> fnTextFormat = value -> String.valueOf(value);

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
		setBackground(fnHilightCondition.apply(row, column) ? hilightColor : bg);
		setText(fnTextFormat.apply(value));
		return this;
	}
}