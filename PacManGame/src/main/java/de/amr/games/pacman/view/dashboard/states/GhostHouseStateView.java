package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.view.dashboard.Formatting.ticksAndSeconds;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GhostHouse;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.view.theme.Theme;
import net.miginfocom.swing.MigLayout;

/**
 * Displays the ghost house counters that control which ghost can leave the house.
 * 
 * @author Armin Reichert
 */
public class GhostHouseStateView extends JPanel implements Lifecycle {

	private GameController gameController;
	private GhostHouse house;

	private JTextField tfPinkyDots;
	private JTextField tfInkyDots;
	private JTextField tfClydeDots;
	private JTextField tfGlobalDots;
	private JLabel lblPacManStarving;
	private JTextField tfPacManStarvingTime;
	private JLabel lblPinkyDots;
	private JLabel lblInkyDots;
	private JLabel lblClydeDots;
	private JLabel lblDotCounters;
	private JLabel lblStarving;

	public GhostHouseStateView() {
		setLayout(new MigLayout("", "[][][][][][][][][]", "[][]"));

		lblDotCounters = new JLabel("Dot counters");
		add(lblDotCounters, "cell 0 0");

		lblPinkyDots = new JLabel("Pinky");
		lblPinkyDots.setFont(new Font("SansSerif", Font.PLAIN, 24));
		add(lblPinkyDots, "cell 1 0,alignx trailing,aligny center");

		tfPinkyDots = new JTextField();
		tfPinkyDots.setEditable(false);
		add(tfPinkyDots, "flowx,cell 2 0,alignx left,aligny center");
		tfPinkyDots.setColumns(8);

		lblInkyDots = new JLabel("Inky");
		lblInkyDots.setFont(new Font("SansSerif", Font.PLAIN, 24));
		add(lblInkyDots, "cell 3 0,alignx trailing,aligny center");

		tfInkyDots = new JTextField();
		tfInkyDots.setEditable(false);
		add(tfInkyDots, "cell 4 0,alignx left");
		tfInkyDots.setColumns(8);

		lblClydeDots = new JLabel("Clyde");
		lblClydeDots.setFont(new Font("SansSerif", Font.PLAIN, 24));
		add(lblClydeDots, "cell 5 0,alignx trailing,aligny center");

		tfClydeDots = new JTextField();
		tfClydeDots.setEditable(false);
		add(tfClydeDots, "cell 6 0,alignx left");
		tfClydeDots.setColumns(8);

		JLabel lblGlobalDots = new JLabel("Global");
		add(lblGlobalDots, "cell 7 0,alignx trailing");

		tfGlobalDots = new JTextField();
		tfGlobalDots.setEditable(false);
		add(tfGlobalDots, "cell 8 0,alignx left");
		tfGlobalDots.setColumns(8);

		lblStarving = new JLabel("Starving Time");
		add(lblStarving, "cell 0 1");

		lblPacManStarving = new JLabel("Pac-Man");
		lblPacManStarving.setFont(new Font("SansSerif", Font.PLAIN, 24));
		lblPacManStarving.setHorizontalTextPosition(SwingConstants.RIGHT);
		add(lblPacManStarving, "cell 1 1,alignx trailing,aligny center");

		tfPacManStarvingTime = new JTextField();
		tfPacManStarvingTime.setEditable(false);
		add(tfPacManStarvingTime, "cell 2 1,alignx left");
		tfPacManStarvingTime.setColumns(8);
	}

	public void attachTo(GameController gameController) {
		this.gameController = gameController;
		house = gameController.ghostHouse;
		init();
	}

	@Override
	public void init() {
		int size = 24;
		setIconOnly(lblPinkyDots, ghost(Theme.PINK_GHOST, size));
		setIconOnly(lblInkyDots, ghost(Theme.CYAN_GHOST, size));
		setIconOnly(lblClydeDots, ghost(Theme.ORANGE_GHOST, size));
		setIconOnly(lblPacManStarving, pacMan(size));
	}

	@Override
	public void update() {
		Game game = gameController.game;
		Ghost nextGhostLeaving = house.preferredLockedGhost().orElse(null);

		tfPinkyDots.setText(formatDots(game.pinky));
		tfPinkyDots.setEnabled(!house.isGlobalDotCounterEnabled());
		markTextField(tfPinkyDots, nextGhostLeaving == game.pinky);

		tfInkyDots.setText(formatDots(game.inky));
		tfInkyDots.setEnabled(!house.isGlobalDotCounterEnabled());
		markTextField(tfInkyDots, nextGhostLeaving == game.inky);

		tfClydeDots.setText(formatDots(game.clyde));
		tfClydeDots.setEnabled(!house.isGlobalDotCounterEnabled());
		markTextField(tfClydeDots, nextGhostLeaving == game.clyde);

		tfGlobalDots.setText(String.format("%d", house.globalDotCount()));
		tfGlobalDots.setEnabled(house.isGlobalDotCounterEnabled());

		tfPacManStarvingTime.setText(ticksAndSeconds(house.pacManStarvingTicks()));
	}

	private void setIconOnly(JLabel label, Icon icon) {
		label.setText("");
		label.setIcon(icon);
	}

	private ImageIcon ghost(int color, int size) {
		Sprite sprite = gameController.theme.spr_ghostColored(color, Direction.RIGHT);
		return new ImageIcon(sprite.frame(0).getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	private ImageIcon pacMan(int size) {
		Sprite sprite = gameController.theme.spr_pacManFull();
		return new ImageIcon(sprite.frame(0).getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	private void markTextField(JTextField tf, boolean hilight) {
		tf.setBackground(hilight ? Color.GREEN : Color.WHITE);
	}

	private String formatDots(Ghost ghost) {
		return String.format("%d p:%d g:%d", house.ghostDotCount(ghost), house.personalDotLimit(ghost),
				house.globalDotLimit(ghost));
	}
}