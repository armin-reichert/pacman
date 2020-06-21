package de.amr.games.pacman.view.settings;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GhostHouse;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.Game;
import net.miginfocom.swing.MigLayout;

public class GhostHouseStateView extends JPanel {

	private GameController gameController;
	private GhostHouse house;

	private JTextField tfPinkyDots;
	private JTextField tfInkyDots;
	private JTextField tfClydeDots;
	private JTextField tfGlobalDots;
	private JLabel lblNewLabel_4;
	private JTextField tfPacManStarvingTime;

	public GhostHouseStateView() {
		setLayout(new MigLayout("", "[][][][][][][][]", "[][]"));

		JLabel lblNewLabel = new JLabel("Pinky dots");
		add(lblNewLabel, "cell 0 0,alignx trailing");

		tfPinkyDots = new JTextField();
		tfPinkyDots.setEditable(false);
		add(tfPinkyDots, "flowx,cell 1 0,alignx left");
		tfPinkyDots.setColumns(8);

		JLabel lblNewLabel_1 = new JLabel("Inky dots");
		add(lblNewLabel_1, "cell 2 0,alignx trailing");

		tfInkyDots = new JTextField();
		tfInkyDots.setEditable(false);
		add(tfInkyDots, "cell 3 0,alignx left");
		tfInkyDots.setColumns(8);

		JLabel lblNewLabel_2 = new JLabel("Clyde dots");
		add(lblNewLabel_2, "cell 4 0,alignx trailing");

		tfClydeDots = new JTextField();
		tfClydeDots.setEditable(false);
		add(tfClydeDots, "cell 5 0,alignx left");
		tfClydeDots.setColumns(8);

		JLabel lblNewLabel_3 = new JLabel("Global dots");
		add(lblNewLabel_3, "cell 6 0,alignx trailing");

		tfGlobalDots = new JTextField();
		tfGlobalDots.setEditable(false);
		add(tfGlobalDots, "cell 7 0,alignx left");
		tfGlobalDots.setColumns(8);

		lblNewLabel_4 = new JLabel("Pac-Man starving");
		add(lblNewLabel_4, "cell 0 1,alignx trailing");

		tfPacManStarvingTime = new JTextField();
		tfPacManStarvingTime.setEditable(false);
		add(tfPacManStarvingTime, "cell 1 1,alignx left");
		tfPacManStarvingTime.setColumns(8);
	}

	public void attachTo(GameController gameController) {
		this.gameController = gameController;
		house = gameController.ghostHouse;
	}

	public void updateViewState() {
		Game game = gameController.game;
		tfPinkyDots.setText(formatDots(game.pinky));
		tfInkyDots.setText(formatDots(game.inky));
		tfClydeDots.setText(formatDots(game.clyde));
		tfPacManStarvingTime.setText(Formatting.ticksAndSeconds(house.pacManStarvingTicks()));
		tfGlobalDots.setText(String.format("%d", house.globalDotCount()));
	}

	private String formatDots(Ghost ghost) {
		return String.format("%d-%d-%d", house.ghostDotCount(ghost), house.globalDotLimit(ghost),
				house.personalDotLimit(ghost));
	}
}