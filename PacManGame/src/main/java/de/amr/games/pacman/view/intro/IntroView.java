package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.READY_TO_PLAY;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.SCROLLING_LOGO;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.SHOWING_ANIMATIONS;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.WAITING_FOR_INPUT;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.widgets.ImageWidget;
import de.amr.easy.game.ui.widgets.LinkWidget;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.core.PacManGameView;
import de.amr.games.pacman.view.core.Pen;
import de.amr.games.pacman.view.intro.IntroView.IntroState;
import de.amr.statemachine.client.FsmComponent;
import de.amr.statemachine.client.FsmContainer;
import de.amr.statemachine.core.StateMachine;

/**
 * Intro screen with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroView extends PacManGameView implements FsmContainer<IntroState, Void> {

	public enum IntroState {
		SCROLLING_LOGO, SHOWING_ANIMATIONS, WAITING_FOR_INPUT, READY_TO_PLAY
	};

	private final String name;
	private final Theme theme;
	private final FsmComponent<IntroState, Void> fsm;

	private ImageWidget pacManLogo;
	private LinkWidget gitHubLink;
	private ChasePacManAnimation chasePacMan;
	private ChaseGhostsAnimation chaseGhosts;
	private GhostPointsAnimation ghostPointsAnimation;

	public IntroView(Theme theme) {
		this.theme = theme;
		this.name = "IntroView";
		fsm = buildFsmComponent();
	}

	private void createUIComponents() {
		pacManLogo = new ImageWidget(Assets.image("images/logo.png"));
		pacManLogo.tf.centerX(width());
		pacManLogo.tf.setY(20);
		chasePacMan = new ChasePacManAnimation(theme);
		chasePacMan.tf.centerX(width());
		chasePacMan.tf.setY(100);
		chaseGhosts = new ChaseGhostsAnimation(theme);
		chaseGhosts.tf.setPosition(width(), 200);
		ghostPointsAnimation = new GhostPointsAnimation(theme);
		gitHubLink = LinkWidget.create()
		/*@formatter:off*/
			.text("https://github.com/armin-reichert/pacman")
			.url("https://github.com/armin-reichert/pacman")
			.font(new Font(Font.MONOSPACED, Font.BOLD, 6))
			.color(Color.LIGHT_GRAY)
			.build();
		/*@formatter:on*/
		gitHubLink.tf.setY(height() - 16);
		gitHubLink.tf.centerX(width());
	}

	@Override
	public Theme theme() {
		return theme;
	}

	@Override
	public void onThemeChanged(Theme theme) {
	}

	@Override
	public FsmComponent<IntroState, Void> fsmComponent() {
		return fsm;
	}

	private FsmComponent<IntroState, Void> buildFsmComponent() {
		StateMachine<IntroState, Void> fsm = buildStateMachine();
		fsm.traceTo(Game.FSM_LOGGER, () -> 60);
		return new FsmComponent<>(fsm);
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
						pacManLogo.tf.setY(height());
						pacManLogo.tf.setVelocityY(-2f);
						pacManLogo.setCompletion(() -> pacManLogo.tf.getY() <= 20);
						pacManLogo.show(); 
						pacManLogo.start(); 
					})
					.onTick(() -> {
						pacManLogo.update();
					})
	
				.state(SHOWING_ANIMATIONS)
					.onEntry(() -> {
						chasePacMan.setStartPosition(width(), 100);
						chasePacMan.setEndPosition(-chasePacMan.tf.getWidth(), 100);
						chaseGhosts.setStartPosition(-chaseGhosts.tf.getWidth(), 200);
						chaseGhosts.setEndPosition(width(), 200);
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
						chasePacMan.tf.centerX(width());
					})
					
				.state(WAITING_FOR_INPUT)
					.timeoutAfter(sec(10))
					.onEntry(() -> {
						ghostPointsAnimation.tf.setY(200);
						ghostPointsAnimation.tf.centerX(width());
						ghostPointsAnimation.start();
						gitHubLink.show();
					})
					.onTick(() -> {
						ghostPointsAnimation.update();
					})
					.onExit(() -> {
						ghostPointsAnimation.stop();
						ghostPointsAnimation.hide();
						gitHubLink.hide();
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
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
	
		.endStateMachine();
	  /*@formatter:on*/
	}

	@Override
	public boolean isComplete() {
		return is(READY_TO_PLAY);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void init() {
		createUIComponents();
		super.init();
		fsmComponent().init();
	}

	@Override
	public void update() {
		super.update();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			setState(READY_TO_PLAY); // exit shortcut
		}
		fsmComponent().update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(new Color(0, 23, 61));
		g.fillRect(0, 0, width(), height());

		// use colors from logo image
		Color orange = new Color(255, 163, 71);
		// Color pink = new Color(248, 120, 88);
		Color red = new Color(171, 19, 0);

		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());
			switch (getState()) {
			case SCROLLING_LOGO:
				pacManLogo.draw(g);
				break;
			case SHOWING_ANIMATIONS:
				pacManLogo.draw(g);
				chaseGhosts.draw(g);
				chasePacMan.draw(g);
				break;
			case WAITING_FOR_INPUT:
				if (app().clock.getTicks() % sec(1) < sec(0.5f)) {
					pen.color(Color.WHITE);
					pen.fontSize(14);
					pen.hcenter("Press SPACE to start!", width(), 18);
				}
				pen.color(orange);
				pen.fontSize(10);
				pen.hcenter("F11 - Fullscreen Mode", width(), 22);
				int selectedSpeed = Arrays.asList(Game.SPEED_1_FPS, Game.SPEED_2_FPS, Game.SPEED_3_FPS)
						.indexOf(app().clock.getFrequency()) + 1;
				pen.color(selectedSpeed == 1 ? orange : red);
				pen.drawAtTilePosition(1, 31, "1 - Normal");
				pen.color(selectedSpeed == 2 ? orange : red);
				pen.drawAtTilePosition(11, 31, "2 - Fast");
				pen.color(selectedSpeed == 3 ? orange : red);
				pen.drawAtTilePosition(19, 31, "3 - Insane");
				pacManLogo.draw(g);
				chasePacMan.draw(g);
				ghostPointsAnimation.draw(g);
				break;
			case READY_TO_PLAY:
				break;
			default:
				throw new IllegalStateException();
			}
		}
	}
}