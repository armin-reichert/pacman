package de.amr.games.pacman.view.dashboard.util;

import java.awt.Color;
import java.awt.Component;
import java.util.function.BiFunction;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CellHilighted extends DefaultTableCellRenderer {

	static final Color HILIGHT_COLOR = new Color(255, 0, 0, 100);

	private final BiFunction<Integer, Integer, Boolean> fnHilightCondition;

	public CellHilighted(BiFunction<Integer, Integer, Boolean> fnHilightCondition) {
		this.fnHilightCondition = fnHilightCondition;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
		setBackground(fnHilightCondition.apply(row, column) ? HILIGHT_COLOR : bg);
		return this;
	}
}