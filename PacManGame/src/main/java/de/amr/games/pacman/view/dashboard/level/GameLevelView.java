package de.amr.games.pacman.view.dashboard.level;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.function.BiFunction;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.GameController;
import net.miginfocom.swing.MigLayout;

public class GameLevelView extends JPanel implements Lifecycle {

	static class BoldRenderer extends DefaultTableCellRenderer {

		private final BiFunction<Integer, Integer, Boolean> fnBoldCondition;

		public BoldRenderer(BiFunction<Integer, Integer, Boolean> fnBoldCondition) {
			this.fnBoldCondition = fnBoldCondition;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (fnBoldCondition.apply(row, column)) {
				Font bold = new Font(label.getFont().getFamily(), Font.BOLD, label.getFont().getSize());
				label.setFont(bold);
			}
			return this;
		}
	}

	private GameController controller;
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

	public void attachTo(GameController controller) {
		this.controller = controller;
		init();
	}

	@Override
	public void init() {
		if (controller.game != null) {
			table.setModel(new GameLevelTableModel(controller.game));
		} else {
			table.setModel(new GameLevelTableModel());
		}
		TableCellRenderer variableRowsEmphasized = new BoldRenderer((row, col) -> row < 4);
		table.getColumnModel().getColumns().asIterator()
				.forEachRemaining(column -> column.setCellRenderer(variableRowsEmphasized));
		table.getColumnModel().getColumn(0).setPreferredWidth(120);
	}

	@Override
	public void update() {
		if (controller.game != null) {
			GameLevelTableModel tableModel = (GameLevelTableModel) table.getModel();
			if (!tableModel.hasGame()) {
				init();
			}
			tableModel.fireTableDataChanged();
		}
	}
}