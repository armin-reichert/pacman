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
package de.amr.games.pacman.view.dashboard.level;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.view.dashboard.util.UniversalFormatter;
import net.miginfocom.swing.MigLayout;

public class GameLevelView extends JPanel implements Lifecycle {

	private JTable table;

	public GameLevelView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[grow]"));

		JScrollPane scrollPane = new JScrollPane();
		content.add(scrollPane, "cell 0 0,grow");

		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setRowHeight(17);
		table.setRowSelectionAllowed(false);
		scrollPane.setViewportView(table);
	}

	@Override
	public void init() {
		table.setModel(new GameLevelTableModel());
		UniversalFormatter fmt = new UniversalFormatter();
		fmt.fnBoldCondition = c -> c.row < 6;
		table.getColumnModel().getColumns().asIterator().forEachRemaining(column -> column.setCellRenderer(fmt));
		table.getColumnModel().getColumn(0).setMaxWidth(180);
		table.getColumnModel().getColumn(0).setMinWidth(180);
	}

	@Override
	public void update() {
		if (!GameController.isGameStarted()) {
			init();
		} else {
			GameLevelTableModel tableModel = (GameLevelTableModel) table.getModel();
			tableModel.fireTableDataChanged();
		}
	}
}