package de.amr.games.pacman.view.dashboard.theme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.theme.api.Theme;
import net.miginfocom.swing.MigLayout;

public class ThemeSelectionView extends JPanel implements Lifecycle {

	static final int THUMBNAIL_SIZE = 100;
	static final int ENTITY_SIZE = 20;

	private boolean initialized = false;
	private Folks folks;

	private GameController gameController;
	private JComboBox<String> comboSelectTheme;
	private JLabel lblPacMan;
	private JLabel lblBlinky;
	private JLabel lblPinky;
	private JLabel lblInky;
	private JLabel lblClyde;

	public void attachTo(GameController gameController) {
		this.gameController = gameController;
		folks = new Folks(gameController.world(), gameController.world().house(0));
	}

	public ThemeSelectionView() {
		setLayout(new MigLayout("", "[][grow]", "[][grow]"));

		JLabel lblSelectTheme = new JLabel("Select Theme");
		add(lblSelectTheme, "cell 0 0,alignx trailing");

		comboSelectTheme = new JComboBox<>();
		comboSelectTheme.setModel(new DefaultComboBoxModel<String>(new String[] { "ARCADE", "LETTERS", "BLOCKS" }));
		add(comboSelectTheme, "cell 1 0,alignx left");

		JPanel panelPreview = new JPanel();
		panelPreview.setBackground(Color.BLACK);
		add(panelPreview, "cell 1 1,grow");
		panelPreview.setLayout(new MigLayout("", "[93px][93px][93px][93px][93px]", "[top]"));

		lblPacMan = new JLabel("Pac-Man");
		lblPacMan.setBorder(new LineBorder(Color.WHITE));
		lblPacMan.setVerticalAlignment(SwingConstants.BOTTOM);
		lblPacMan.setForeground(Color.WHITE);
		lblPacMan.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPacMan.setHorizontalAlignment(SwingConstants.CENTER);
		lblPacMan.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPacMan.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblPacMan, "cell 0 0,alignx center,aligny center");

		lblBlinky = new JLabel("Blinky");
		lblBlinky.setBorder(new LineBorder(Color.WHITE));
		lblBlinky.setVerticalAlignment(SwingConstants.BOTTOM);
		lblBlinky.setPreferredSize(new Dimension(40, 60));
		lblBlinky.setForeground(Color.WHITE);
		lblBlinky.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblBlinky.setHorizontalAlignment(SwingConstants.CENTER);
		lblBlinky.setHorizontalTextPosition(SwingConstants.CENTER);
		lblBlinky.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblBlinky, "cell 1 0,alignx center,aligny center");

		lblPinky = new JLabel("Pinky");
		lblPinky.setBorder(new LineBorder(Color.WHITE));
		lblPinky.setVerticalAlignment(SwingConstants.BOTTOM);
		lblPinky.setPreferredSize(new Dimension(40, 60));
		lblPinky.setForeground(Color.WHITE);
		lblPinky.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPinky.setHorizontalAlignment(SwingConstants.CENTER);
		lblPinky.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPinky.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblPinky, "cell 2 0,alignx center,aligny center");

		lblInky = new JLabel("Inky");
		lblInky.setBorder(new LineBorder(Color.WHITE));
		lblInky.setVerticalAlignment(SwingConstants.BOTTOM);
		lblInky.setPreferredSize(new Dimension(40, 60));
		lblInky.setForeground(Color.WHITE);
		lblInky.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblInky.setHorizontalAlignment(SwingConstants.CENTER);
		lblInky.setHorizontalTextPosition(SwingConstants.CENTER);
		lblInky.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblInky, "cell 3 0,alignx center,aligny center");

		lblClyde = new JLabel("Clyde");
		lblClyde.setBorder(new LineBorder(Color.WHITE));
		lblClyde.setVerticalAlignment(SwingConstants.BOTTOM);
		lblClyde.setPreferredSize(new Dimension(40, 60));
		lblClyde.setForeground(Color.WHITE);
		lblClyde.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblClyde.setHorizontalAlignment(SwingConstants.CENTER);
		lblClyde.setHorizontalTextPosition(SwingConstants.CENTER);
		lblClyde.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblClyde, "cell 4 0,alignx center,aligny center");
		comboSelectTheme.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				String themeName = comboSelectTheme.getModel().getElementAt(comboSelectTheme.getSelectedIndex());
				gameController.selectTheme(themeName);
				updatePreviewLabels();
			}
		});
	}

	private Optional<View> currentView() {
		return Optional.ofNullable(gameController).flatMap(GameController::currentView);
	}

	private void updateSelectionFromTheme() {
		currentView().ifPresent(view -> {
			PacManGameView gameView = (PacManGameView) view;
			if (!comboSelectTheme.getSelectedItem().equals(gameController.getTheme().name())) {
				comboSelectTheme.setSelectedItem(gameView.getTheme().name().toUpperCase());
				updatePreviewLabels();
			}
		});
	}

	private void updatePreviewLabels() {
		Theme theme = gameController.getTheme();
		setPacManLabel(folks, theme);
		setGhostLabels(folks, theme);
	}

	private void setPacManLabel(Folks folks, Theme theme) {
		folks.pacMan.setState(PacManState.AWAKE);
		folks.pacMan.setMoveDir(Direction.RIGHT);
		folks.pacMan.entity.tf.width = folks.pacMan.entity.tf.height = ENTITY_SIZE;
		BufferedImage img = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.translate((THUMBNAIL_SIZE - ENTITY_SIZE) / 2, (THUMBNAIL_SIZE - ENTITY_SIZE) / 2);
		theme.pacManRenderer(folks.pacMan).render(g);
		lblPacMan.setIcon(new ImageIcon(img));
	}

	private void setGhostLabels(Folks folks, Theme theme) {
		folks.ghosts().forEach(ghost -> {
			ghost.setState(GhostState.CHASING);
			ghost.setMoveDir(Direction.random());
			ghost.entity.tf.width = ghost.entity.tf.height = ENTITY_SIZE;
			BufferedImage img = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			g.translate((THUMBNAIL_SIZE - ENTITY_SIZE) / 2, (THUMBNAIL_SIZE - ENTITY_SIZE) / 2);
			theme.ghostRenderer(ghost).render(g);
			ImageIcon icon = new ImageIcon(img);
			switch (ghost.name) {
			case "Blinky":
				lblBlinky.setIcon(icon);
				break;
			case "Pinky":
				lblPinky.setIcon(icon);
				break;
			case "Inky":
				lblInky.setIcon(icon);
				break;
			case "Clyde":
				lblClyde.setIcon(icon);
				break;
			default:
				break;
			}
		});
	}

	@Override
	public void init() {
		// not called!
	}

	@Override
	public void update() {
		if (!initialized) {
			updatePreviewLabels();
			initialized = true;
		}
		updateSelectionFromTheme();
	}
}