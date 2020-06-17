package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.PacManApp.settings;
import static de.amr.games.pacman.PacManApp.texts;
import static de.amr.games.pacman.model.Game.sec;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.READY_TO_PLAY;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.SCROLLING_LOGO;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.SHOWING_ANIMATIONS;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.WAITING_FOR_INPUT;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.widgets.ImageWidget;
import de.amr.easy.game.ui.widgets.LinkWidget;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.core.BaseView;
import de.amr.games.pacman.view.intro.IntroView.IntroState;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.api.Fsm;
import de.amr.statemachine.api.FsmContainer;
import de.amr.statemachine.core.StateMachine;

/**
 * Intro screen with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroView extends BaseView implements FsmContainer<IntroState, Void> {

	public enum IntroState {
		SCROLLING_LOGO, SHOWING_ANIMATIONS, WAITING_FOR_INPUT, READY_TO_PLAY
	};

	private final String name;
	private final Fsm<IntroState, Void> fsm;

	private ImageWidget pacManLogo;
	private LinkWidget gitHubLink;
	private ChasePacManAnimation chasePacMan;
	private ChaseGhostsAnimation chaseGhosts;
	private GhostPointsAnimation ghostPointsAnimation;

	private Color orange = new Color(255, 163, 71);
	// private Color pink = new Color(248, 120, 88);
	private Color red = new Color(171, 19, 0);

	public IntroView(Theme theme) {
		super(theme);
		this.name = "IntroView";
		fsm = buildStateMachine();
		fsm.getTracer().setLogger(PacManStateMachineLogging.LOGGER);
	}

	private void createUIComponents() {
		pacManLogo = new ImageWidget(theme.img_logo());
		pacManLogo.tf.centerX(width);
		pacManLogo.tf.y = (20);
		chasePacMan = new ChasePacManAnimation(theme);
		chasePacMan.tf.centerX(width);
		chasePacMan.tf.y = (100);
		chaseGhosts = new ChaseGhostsAnimation(theme);
		chaseGhosts.tf.setPosition(width, 200);
		ghostPointsAnimation = new GhostPointsAnimation(theme);
		gitHubLink = LinkWidget.create()
		/*@formatter:off*/
			.text("https://github.com/armin-reichert/pacman")
			.url("https://github.com/armin-reichert/pacman")
			.font(new Font(Font.MONOSPACED, Font.BOLD, 6))
			.color(Color.LIGHT_GRAY)
			.build();
		/*@formatter:on*/
		gitHubLink.tf.y = (height - 16);
		gitHubLink.tf.centerX(width);
	}

	@Override
	public Fsm<IntroState, Void> fsm() {
		return fsm;
	}

	private StateMachine<IntroState, Void> buildStateMachine() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(IntroState.class, Void.class)
			.description(String.format("[%s]", name))
			.initialState(SCROLLING_LOGO)

			.states()
	
				.state(SCROLLING_LOGO)
					.onEntry(() -> {
						theme.snd_insertCoin().play();
						pacManLogo.tf.y = height;
						pacManLogo.tf.vy = -2f;
						pacManLogo.setCompletion(() -> pacManLogo.tf.y <= 20);
						pacManLogo.visible = true; 
						pacManLogo.start(); 
					})
					.onTick(() -> {
						pacManLogo.update();
					})
	
				.state(SHOWING_ANIMATIONS)
					.onEntry(() -> {
						chasePacMan.setStartPosition(width, 100);
						chasePacMan.setEndPosition(-chasePacMan.tf.width, 100);
						chaseGhosts.setStartPosition(-chaseGhosts.tf.width, 200);
						chaseGhosts.setEndPosition(width, 200);
						chasePacMan.start();
						chaseGhosts.start();
					})
					.onTick(() -> {
						chasePacMan.update();
						chaseGhosts.update();
					})
					.onExit(() -> {
						chasePacMan.stop();
						chaseGhosts.stop();
						chasePacMan.tf.centerX(width);
					})
					
				.state(WAITING_FOR_INPUT)
					.timeoutAfter(sec(10))
					.onEntry(() -> {
						ghostPointsAnimation.tf.y=(200);
						ghostPointsAnimation.tf.centerX(width);
						ghostPointsAnimation.start();
						gitHubLink.visible = true;
					})
					.onTick(() -> {
						ghostPointsAnimation.update();
						gitHubLink.update();
					})
					.onExit(() -> {
						ghostPointsAnimation.stop();
						ghostPointsAnimation.visible = false;
						gitHubLink.visible = false;
					})
					
				.state(READY_TO_PLAY)
					
			.transitions()
			
				.when(SCROLLING_LOGO).then(SHOWING_ANIMATIONS)
					.condition(() -> pacManLogo.isComplete())
				
				.when(SHOWING_ANIMATIONS).then(WAITING_FOR_INPUT)
					.condition(() -> chasePacMan.isComplete() && chaseGhosts.isComplete())
				
				.when(WAITING_FOR_INPUT).then(SHOWING_ANIMATIONS)
					.onTimeout()
				
				.when(WAITING_FOR_INPUT).then(READY_TO_PLAY)
					.condition(() -> Keyboard.keyPressedOnce(" "))
	
		.endStateMachine();
	  /*@formatter:on*/
	}

	@Override
	public boolean isComplete() {
		return is(READY_TO_PLAY);
	}

	@Override
	public void init() {
		createUIComponents();
		fsm().init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			setState(READY_TO_PLAY); // shortcut for skipping intro
		}
		fsm().update();
	}

	@Override
	public void draw(Graphics2D g) {
		g = (Graphics2D) g.create();
		g.setColor(new Color(0, 23, 61));
		g.fillRect(0, 0, width, height);
		try (Pen pen = new Pen(g)) {
			switch (getState()) {
			case SCROLLING_LOGO:
				pacManLogo.draw(g);
				break;
			case SHOWING_ANIMATIONS:
				pacManLogo.draw(g);
				chaseGhosts.draw(g);
				chasePacMan.draw(g);
				drawFullScreenMode(g, 31);
				break;
			case WAITING_FOR_INPUT:
				pacManLogo.draw(g);
				chasePacMan.draw(g);
				ghostPointsAnimation.draw(g);
				gitHubLink.draw(g);
				if (app().clock().getTotalTicks() % sec(1) < sec(0.5f)) {
					drawPressSpaceToStart(g, 18);
				}
				drawSpeedSelection(g, 22);
				drawFullScreenMode(g, 31);
				break;
			case READY_TO_PLAY:
				break;
			default:
				throw new IllegalStateException();
			}
		}
		g.dispose();
	}

	private void drawPressSpaceToStart(Graphics2D g, int row) {
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());
			pen.color(Color.WHITE);
			pen.hcenter(texts.getString("press_space_to_start"), width, row, Tile.SIZE);
		}
	}

	private void drawFullScreenMode(Graphics2D g, int row) {
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());
			pen.color(orange);
			if (app().inFullScreenMode()) {
				pen.hcenter("F11 - " + texts.getString("window_mode"), width, row, Tile.SIZE);
			} else {
				pen.hcenter("F11 - " + texts.getString("fullscreen_mode"), width, row, Tile.SIZE);
			}
		}
	}

	private void drawSpeedSelection(Graphics2D g, int row) {
		String t1 = "1 - " + texts.getString("normal");
		String t2 = "2 - " + texts.getString("fast");
		String t3 = "3 - " + texts.getString("insane");
		int selectedSpeed = Arrays.asList(60, 70, 80).indexOf(app().clock().getTargetFramerate()) + 1;
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());
			pen.fontSize(6);
			FontMetrics fm = pen.getFontMetrics();
			int w1 = fm.stringWidth(t1), w2 = fm.stringWidth(t2), w3 = fm.stringWidth(t3);
			float s = (settings.width - (w1 + w2 + w3)) / 4f;
			float x1 = s, x2 = x1 + w1 + s, x3 = x2 + w2 + s;
			int y = row * Tile.SIZE;
			pen.color(selectedSpeed == 1 ? orange : red);
			pen.draw(t1, x1, y);
			pen.color(selectedSpeed == 2 ? orange : red);
			pen.draw(t2, x2, y);
			pen.color(selectedSpeed == 3 ? orange : red);
			pen.draw(t3, x3, y);
		}
	}
}