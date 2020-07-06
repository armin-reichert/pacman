package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.controller.actor.GhostState.LOCKED;
import static de.amr.games.pacman.model.world.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Population.CYAN_GHOST;
import static de.amr.games.pacman.model.world.api.Population.ORANGE_GHOST;
import static de.amr.games.pacman.model.world.api.Population.PINK_GHOST;
import static de.amr.games.pacman.view.dashboard.util.Formatting.ticksAndSeconds;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.ghosthouse.GhostHouseDoorMan;
import de.amr.games.pacman.model.world.api.Habitat;
import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;
import net.miginfocom.swing.MigLayout;

/**
 * Displays the ghost house counters that control which ghost can leave the house.
 * 
 * @author Armin Reichert
 */
public class GhostHouseStateView extends JPanel implements Lifecycle {

	private GameController gameController;
	private Habitat world;

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
	private TrafficLightsWidget trafficPinky;
	private TrafficLightsWidget trafficInky;
	private TrafficLightsWidget trafficClyde;

	public GhostHouseStateView() {
		setBorder(new TitledBorder(null, "Ghost House", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new MigLayout("", "[][][][][][][][][]", "[10px:10px:10px][][]"));

		trafficPinky = new TrafficLightsWidget();
		add(trafficPinky, "cell 2 0,alignx center");

		trafficInky = new TrafficLightsWidget();
		add(trafficInky, "cell 4 0,alignx center");

		trafficClyde = new TrafficLightsWidget();
		add(trafficClyde, "cell 6 0,alignx center");

		lblDotCounters = new JLabel("Dot counters");
		add(lblDotCounters, "cell 0 1");

		lblPinkyDots = new JLabel("Pinky");
		lblPinkyDots.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPinkyDots.setFont(new Font("Monospaced", Font.BOLD, 24));
		add(lblPinkyDots, "cell 1 1,alignx right,aligny center");

		tfPinkyDots = new JTextField();
		tfPinkyDots.setEditable(false);
		add(tfPinkyDots, "flowx,cell 2 1,alignx left,aligny center");
		tfPinkyDots.setColumns(8);

		lblInkyDots = new JLabel("Inky");
		lblInkyDots.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInkyDots.setFont(new Font("Monospaced", Font.BOLD, 24));
		add(lblInkyDots, "cell 3 1,alignx right,aligny center");

		tfInkyDots = new JTextField();
		tfInkyDots.setEditable(false);
		add(tfInkyDots, "cell 4 1,alignx left");
		tfInkyDots.setColumns(8);

		lblClydeDots = new JLabel("Clyde");
		lblClydeDots.setHorizontalAlignment(SwingConstants.RIGHT);
		lblClydeDots.setFont(new Font("Monospaced", Font.BOLD, 24));
		add(lblClydeDots, "cell 5 1,alignx right,aligny center");

		tfClydeDots = new JTextField();
		tfClydeDots.setEditable(false);
		add(tfClydeDots, "cell 6 1,alignx left");
		tfClydeDots.setColumns(8);

		JLabel lblGlobalDots = new JLabel("Global");
		add(lblGlobalDots, "cell 7 1,alignx trailing");

		tfGlobalDots = new JTextField();
		tfGlobalDots.setEditable(false);
		add(tfGlobalDots, "cell 8 1,alignx left");
		tfGlobalDots.setColumns(8);

		lblStarving = new JLabel("Starving Time");
		add(lblStarving, "cell 0 2");

		lblPacManStarving = new JLabel("Pac-Man");
		lblPacManStarving.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPacManStarving.setFont(new Font("Monospaced", Font.BOLD, 24));
		lblPacManStarving.setHorizontalTextPosition(SwingConstants.RIGHT);
		add(lblPacManStarving, "cell 1 2,alignx right,aligny center");

		tfPacManStarvingTime = new JTextField();
		tfPacManStarvingTime.setEditable(false);
		add(tfPacManStarvingTime, "cell 2 2,alignx left");
		tfPacManStarvingTime.setColumns(8);
	}

	public void attachTo(GameController gameController) {
		this.gameController = gameController;
		world = gameController.world();
		init();
	}

	@Override
	public void init() {
		Image pinkyImage = ArcadeTheme.ASSETS.makeSprite_ghostColored(PINK_GHOST, RIGHT).frame(0);
		Image inkyImage = ArcadeTheme.ASSETS.makeSprite_ghostColored(CYAN_GHOST, RIGHT).frame(0);
		Image clydeImage = ArcadeTheme.ASSETS.makeSprite_ghostColored(ORANGE_GHOST, RIGHT).frame(0);
		Image pacManImage = ArcadeTheme.ASSETS.makeSprite_pacManWalking(RIGHT).frame(0);
		setLabelIconOnly(lblPinkyDots, scaledIcon(pinkyImage, 30));
		setLabelIconOnly(lblInkyDots, scaledIcon(inkyImage, 30));
		setLabelIconOnly(lblClydeDots, scaledIcon(clydeImage, 30));
		setLabelIconOnly(lblPacManStarving, scaledIcon(pacManImage, 30));
	}

	private Icon scaledIcon(Image image, int size) {
		return new ImageIcon(image.getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	@Override
	public void update() {
		gameController.game().ifPresent(game -> {
			gameController.ghostHouseAccess().ifPresent(ghostHouseAccess -> {
				tfPinkyDots.setText(formatDots(ghostHouseAccess, world.population().pinky()));
				tfPinkyDots.setEnabled(!ghostHouseAccess.isGlobalDotCounterEnabled());
				updateTrafficLight(trafficPinky, ghostHouseAccess, world.population().pinky());

				tfInkyDots.setText(formatDots(ghostHouseAccess, world.population().inky()));
				tfInkyDots.setEnabled(!ghostHouseAccess.isGlobalDotCounterEnabled());
				updateTrafficLight(trafficInky, ghostHouseAccess, world.population().inky());

				tfClydeDots.setText(formatDots(ghostHouseAccess, world.population().clyde()));
				tfClydeDots.setEnabled(!ghostHouseAccess.isGlobalDotCounterEnabled());
				updateTrafficLight(trafficClyde, ghostHouseAccess, world.population().clyde());

				tfGlobalDots.setText(String.format("%d", ghostHouseAccess.globalDotCount()));
				tfGlobalDots.setEnabled(ghostHouseAccess.isGlobalDotCounterEnabled());

				tfPacManStarvingTime.setText(ticksAndSeconds(ghostHouseAccess.pacManStarvingTicks()));
			});
		});
	}

	private void setLabelIconOnly(JLabel label, Icon icon) {
		label.setText("");
		label.setIcon(icon);
	}

	private Color trafficLightColor(GhostHouseDoorMan house, Ghost ghost) {
		if (!ghost.isInsideHouse()) {
			return null;
		}
		if (!ghost.is(LOCKED)) {
			return Color.GREEN;
		}
		Ghost next = house.preferredLockedGhost().orElse(null);
		return ghost == next ? Color.YELLOW : Color.RED;
	}

	private void updateTrafficLight(TrafficLightsWidget trafficLight, GhostHouseDoorMan house, Ghost ghost) {
		Color color = trafficLightColor(house, ghost);
		if (color != null) {
			trafficLight.setVisible(true);
			trafficLight.setRed(color == Color.RED);
			trafficLight.setYellow(color == Color.YELLOW);
			trafficLight.setGreen(color == Color.GREEN);
		} else {
			trafficLight.setVisible(false);
		}
	}

	private String formatDots(GhostHouseDoorMan house, Ghost ghost) {
		return String.format("%d p:%d g:%d", house.ghostDotCount(ghost), house.personalDotLimit(ghost),
				house.globalDotLimit(ghost));
	}
}