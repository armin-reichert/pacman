/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.view.dashboard.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class UniversalFormatter extends DefaultTableCellRenderer {

	public static class Context {
		public JTable table;
		public Object value;
		public boolean isSelected;
		public boolean hasFocus;
		public int row;
		public int column;
	}

	private Context context = new Context();
	public Color hilightColor = new Color(255, 0, 0, 100);
	public Predicate<Context> fnHilightCondition = ctx -> false;
	public Function<Context, String> fnTextFormat = ctx -> ctx.value == null ? "" : String.valueOf(ctx.value);
	public Predicate<Context> fnBoldCondition = ctx -> false;
	public int horizontalAlignment = SwingConstants.LEADING;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		context.table = table;
		context.value = value;
		context.isSelected = isSelected;
		context.hasFocus = hasFocus;
		context.row = row;
		context.column = column;
		Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
		setBackground(fnHilightCondition.test(context) ? hilightColor : bg);
		setHorizontalAlignment(horizontalAlignment);
		setText(fnTextFormat.apply(context));
		if (fnBoldCondition.test(context)) {
			setFont(new Font(getFont().getFamily(), Font.BOLD, getFont().getSize()));
		}
		return this;
	}
}