package de.amr.games.pacman.view.settings;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.amr.games.pacman.model.Game;
import net.miginfocom.swing.MigLayout;

public class GameLevelView extends JPanel {
	private JTable table;
	public GameLevelViewModel tableModel;

	public GameLevelView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[grow]"));

		JScrollPane scrollPane = new JScrollPane();
		content.add(scrollPane, "cell 0 0,grow");

		table = new JTable();
		scrollPane.setViewportView(table);
	}

	public void createModel(Game game) {
		tableModel = new GameLevelViewModel(game);
		table.setModel(tableModel);
	}

}
