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
package de.amr.games.pacman.view.dashboard.states;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;

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
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.controller.ghosthouse.DoorMan;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.theme.arcade.ArcadeSpritesheet;
import de.amr.games.pacman.theme.arcade.ArcadeSpritesheet.GhostColor;
import de.amr.games.pacman.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.dashboard.util.Formatting;
import net.miginfocom.swing.MigLayout;

/**
 * Displays the ghost house counters that control which ghost can leave the house.
 * 
 * @author Armin Reichert
 */
public class GhostHouseStateView extends JPanel implements Lifecycle {

	private GameController gameController;
	private Folks folks;

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
		lblPinkyDots.setFont(new Font("Monospaced", Font.BOLD, 14));
		add(lblPinkyDots, "cell 1 1,alignx right,aligny center");

		tfPinkyDots = new JTextField();
		tfPinkyDots.setEditable(false);
		add(tfPinkyDots, "flowx,cell 2 1,alignx left,aligny center");
		tfPinkyDots.setColumns(8);

		lblInkyDots = new JLabel("Inky");
		lblInkyDots.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInkyDots.setFont(new Font("Monospaced", Font.BOLD, 14));
		add(lblInkyDots, "cell 3 1,alignx right,aligny center");

		tfInkyDots = new JTextField();
		tfInkyDots.setEditable(false);
		add(tfInkyDots, "cell 4 1,alignx left");
		tfInkyDots.setColumns(8);

		lblClydeDots = new JLabel("Clyde");
		lblClydeDots.setHorizontalAlignment(SwingConstants.RIGHT);
		lblClydeDots.setFont(new Font("Monospaced", Font.BOLD, 14));
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
		lblPacManStarving.setFont(new Font("Monospaced", Font.BOLD, 14));
		lblPacManStarving.setHorizontalTextPosition(SwingConstants.RIGHT);
		add(lblPacManStarving, "cell 1 2,alignx right,aligny center");

		tfPacManStarvingTime = new JTextField();
		tfPacManStarvingTime.setEditable(false);
		add(tfPacManStarvingTime, "cell 2 2,alignx left");
		tfPacManStarvingTime.setColumns(8);
	}

	public void attachTo(GameController gameController, Folks folks) {
		this.gameController = gameController;
		this.folks = folks;
		init();
	}

	@Override
	public void init() {
		ArcadeSpritesheet sprites = ArcadeTheme.THEME.asValue("sprites");
		Image pinkyImage = sprites.makeSpritGhostColored(GhostColor.PINK, RIGHT).frame(0);
		Image inkyImage = sprites.makeSpritGhostColored(GhostColor.CYAN, RIGHT).frame(0);
		Image clydeImage = sprites.makeSpritGhostColored(GhostColor.ORANGE, RIGHT).frame(0);
		Image pacManImage = sprites.makeSpritePacManWalking(RIGHT).frame(0);
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
		if (PacManGame.started()) {
			DoorMan doorMan = gameController.doorMan;
			tfPinkyDots.setText(formatDots(doorMan, folks.pinky));
			tfPinkyDots.setEnabled(!doorMan.isGlobalDotCounterEnabled());
			updateTrafficLight(trafficPinky, doorMan, folks.pinky);

			tfInkyDots.setText(formatDots(doorMan, folks.inky));
			tfInkyDots.setEnabled(!doorMan.isGlobalDotCounterEnabled());
			updateTrafficLight(trafficInky, doorMan, folks.inky);

			tfClydeDots.setText(formatDots(doorMan, folks.clyde));
			tfClydeDots.setEnabled(!doorMan.isGlobalDotCounterEnabled());
			updateTrafficLight(trafficClyde, doorMan, folks.clyde);

			tfGlobalDots.setText(String.format("%d", doorMan.globalDotCount()));
			tfGlobalDots.setEnabled(doorMan.isGlobalDotCounterEnabled());

			tfPacManStarvingTime.setText(Formatting.ticksAndSeconds(doorMan.pacManStarvingTicks()));
		}
	}

	private void setLabelIconOnly(JLabel label, Icon icon) {
		label.setText("");
		label.setIcon(icon);
	}

	private Color trafficLightColor(DoorMan house, Ghost ghost) {
		if (!ghost.isInsideHouse()) {
			return null;
		}
		if (!ghost.ai.is(LOCKED)) {
			return Color.GREEN;
		}
		Ghost next = house.preferredLockedGhost().orElse(null);
		return ghost == next ? Color.YELLOW : Color.RED;
	}

	private void updateTrafficLight(TrafficLightsWidget trafficLight, DoorMan house, Ghost ghost) {
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

	private String formatDots(DoorMan house, Ghost ghost) {
		return String.format("%d p:%d g:%d", house.ghostDotCount(ghost), house.personalDotLimit(ghost),
				house.globalDotLimit(ghost));
	}
}