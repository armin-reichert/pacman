package de.amr.games.pacman.view.settings;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTable;

import de.amr.games.pacman.controller.GhostCommand;
import de.amr.games.pacman.model.Game;
import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;

public class GhostStateView extends JPanel {

	private JTable table;
	public GhostStateModel model;

	public GhostStateView() {
		setLayout(new BorderLayout(0, 0));

		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new MigLayout("", "[grow]", "[grow][grow]"));

		JScrollPane scrollPane = new JScrollPane();
		content.add(scrollPane, "cell 0 0,grow");

		table = new JTable();
		scrollPane.setViewportView(table);
	}

	public void setGame(Game game, GhostCommand ghostCommand) {
		model = new GhostStateModel();
		model.setGame(game, ghostCommand);
		table.setModel(model);
	}
}
