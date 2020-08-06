package de.amr.games.pacman.view.dashboard.theme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.Random;

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
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.theme.api.Theme;
import net.miginfocom.swing.MigLayout;

public class ThemeSelectionView extends JPanel implements Lifecycle {

	static final int THUMBNAIL_SIZE = 80;
	static final int ENTITY_SIZE = 16;

	private boolean initialized = false;
	private Folks folks;

	private GameController gameController;
	private JComboBox<String> comboSelectTheme;
	private JLabel lblPacMan;
	private JLabel lblBlinky;
	private JLabel lblPinky;
	private JLabel lblInky;
	private JLabel lblClyde;
	private JLabel lblGhostFrightened;
	private JLabel lblGhostDead;
	private JLabel lblGhostDeadBounty;

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
		panelPreview.setBackground(new Color(0, 23, 61));
		add(panelPreview, "cell 1 1,grow");
		panelPreview.setLayout(new MigLayout("", "[][][][][]", "[][]"));

		lblPacMan = new JLabel("Pac-Man");
		lblPacMan.setPreferredSize(new Dimension(100, 100));
		lblPacMan.setBorder(new LineBorder(Color.WHITE));
		lblPacMan.setVerticalAlignment(SwingConstants.BOTTOM);
		lblPacMan.setForeground(Color.WHITE);
		lblPacMan.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPacMan.setHorizontalAlignment(SwingConstants.CENTER);
		lblPacMan.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPacMan.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblPacMan, "cell 0 0,alignx left,aligny top");

		lblBlinky = new JLabel("Blinky");
		lblBlinky.setBorder(new LineBorder(Color.WHITE));
		lblBlinky.setVerticalAlignment(SwingConstants.BOTTOM);
		lblBlinky.setPreferredSize(new Dimension(100, 100));
		lblBlinky.setForeground(Color.WHITE);
		lblBlinky.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblBlinky.setHorizontalAlignment(SwingConstants.CENTER);
		lblBlinky.setHorizontalTextPosition(SwingConstants.CENTER);
		lblBlinky.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblBlinky, "cell 1 0,alignx left,aligny top");

		lblPinky = new JLabel("Pinky");
		lblPinky.setBorder(new LineBorder(Color.WHITE));
		lblPinky.setVerticalAlignment(SwingConstants.BOTTOM);
		lblPinky.setPreferredSize(new Dimension(100, 100));
		lblPinky.setForeground(Color.WHITE);
		lblPinky.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPinky.setHorizontalAlignment(SwingConstants.CENTER);
		lblPinky.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPinky.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblPinky, "cell 2 0,alignx left,aligny top");

		lblInky = new JLabel("Inky");
		lblInky.setBorder(new LineBorder(Color.WHITE));
		lblInky.setVerticalAlignment(SwingConstants.BOTTOM);
		lblInky.setPreferredSize(new Dimension(100, 100));
		lblInky.setForeground(Color.WHITE);
		lblInky.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblInky.setHorizontalAlignment(SwingConstants.CENTER);
		lblInky.setHorizontalTextPosition(SwingConstants.CENTER);
		lblInky.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblInky, "cell 3 0,alignx left,aligny top");

		lblClyde = new JLabel("Clyde");
		lblClyde.setBorder(new LineBorder(Color.WHITE));
		lblClyde.setVerticalAlignment(SwingConstants.BOTTOM);
		lblClyde.setPreferredSize(new Dimension(100, 100));
		lblClyde.setForeground(Color.WHITE);
		lblClyde.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblClyde.setHorizontalAlignment(SwingConstants.CENTER);
		lblClyde.setHorizontalTextPosition(SwingConstants.CENTER);
		lblClyde.setVerticalTextPosition(SwingConstants.BOTTOM);
		panelPreview.add(lblClyde, "cell 4 0,alignx left,aligny top");

		lblGhostFrightened = new JLabel("Frightened");
		lblGhostFrightened.setForeground(Color.WHITE);
		lblGhostFrightened.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblGhostFrightened.setVerticalAlignment(SwingConstants.BOTTOM);
		lblGhostFrightened.setVerticalTextPosition(SwingConstants.BOTTOM);
		lblGhostFrightened.setHorizontalTextPosition(SwingConstants.CENTER);
		lblGhostFrightened.setBorder(new LineBorder(Color.WHITE));
		lblGhostFrightened.setPreferredSize(new Dimension(100, 100));
		lblGhostFrightened.setHorizontalAlignment(SwingConstants.CENTER);
		panelPreview.add(lblGhostFrightened, "cell 1 1,alignx right,aligny top");

		lblGhostDead = new JLabel("Dead");
		lblGhostDead.setForeground(Color.WHITE);
		lblGhostDead.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblGhostDead.setHorizontalTextPosition(SwingConstants.CENTER);
		lblGhostDead.setHorizontalAlignment(SwingConstants.CENTER);
		lblGhostDead.setVerticalAlignment(SwingConstants.BOTTOM);
		lblGhostDead.setVerticalTextPosition(SwingConstants.BOTTOM);
		lblGhostDead.setPreferredSize(new Dimension(100, 100));
		lblGhostDead.setBorder(new LineBorder(Color.WHITE));
		panelPreview.add(lblGhostDead, "cell 2 1,alignx left,aligny top");

		lblGhostDeadBounty = new JLabel("Bounty");
		lblGhostDeadBounty.setVerticalTextPosition(SwingConstants.BOTTOM);
		lblGhostDeadBounty.setVerticalAlignment(SwingConstants.BOTTOM);
		lblGhostDeadBounty.setPreferredSize(new Dimension(100, 100));
		lblGhostDeadBounty.setHorizontalTextPosition(SwingConstants.CENTER);
		lblGhostDeadBounty.setHorizontalAlignment(SwingConstants.CENTER);
		lblGhostDeadBounty.setForeground(Color.WHITE);
		lblGhostDeadBounty.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblGhostDeadBounty.setBorder(new LineBorder(Color.WHITE));
		panelPreview.add(lblGhostDeadBounty, "cell 3 1");

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
		lblPacMan.setIcon(createPacManIcon(theme, folks.pacMan));
		lblBlinky.setIcon(createGhostIcon(theme, folks.blinky, GhostState.CHASING, Direction.random()));
		lblPinky.setIcon(createGhostIcon(theme, folks.pinky, GhostState.CHASING, Direction.random()));
		lblInky.setIcon(createGhostIcon(theme, folks.inky, GhostState.CHASING, Direction.random()));
		lblClyde.setIcon(createGhostIcon(theme, folks.clyde, GhostState.CHASING, Direction.random()));
		lblGhostFrightened.setIcon(createGhostIcon(theme, folks.blinky, GhostState.FRIGHTENED, Direction.random()));
		lblGhostDead.setIcon(createGhostIcon(theme, folks.blinky, GhostState.DEAD, Direction.random()));
		int[] bounties = { 200, 400, 800, 1600 };
		folks.blinky.setBounty(bounties[new Random().nextInt(4)]);
		lblGhostDeadBounty.setIcon(createGhostIcon(theme, folks.blinky, GhostState.DEAD, Direction.random()));
		folks.blinky.setBounty(0);
	}

	private ImageIcon createPacManIcon(Theme theme, PacMan pacMan) {
		pacMan.setState(PacManState.AWAKE);
		pacMan.setMoveDir(Direction.RIGHT);
		pacMan.entity.tf.width = folks.pacMan.entity.tf.height = ENTITY_SIZE;
		BufferedImage img = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.translate((THUMBNAIL_SIZE - ENTITY_SIZE) / 2, (THUMBNAIL_SIZE - ENTITY_SIZE) / 2);
		theme.pacManRenderer(pacMan).render(g, pacMan);
		return new ImageIcon(img);
	}

	private ImageIcon createGhostIcon(Theme theme, Ghost ghost, GhostState state, Direction moveDir) {
		ghost.setState(state);
		ghost.setMoveDir(moveDir);
		ghost.entity.tf.width = ghost.entity.tf.height = ENTITY_SIZE;
		BufferedImage img = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.translate((THUMBNAIL_SIZE - ENTITY_SIZE) / 2, (THUMBNAIL_SIZE - ENTITY_SIZE) / 2);
		theme.ghostRenderer(ghost).render(g, ghost);
		return new ImageIcon(img);
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