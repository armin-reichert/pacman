package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.LOADING_MUSIC;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.READY_TO_PLAY;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.SCROLLING_LOGO;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.SHOWING_ANIMATIONS;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.WAITING_FOR_INPUT;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.widgets.ImageWidget;
import de.amr.easy.game.ui.widgets.LinkWidget;
import de.amr.easy.game.view.AnimationLifecycle;
import de.amr.easy.game.view.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.core.AbstractPacManGameView;
import de.amr.games.pacman.view.core.Pen;
import de.amr.games.pacman.view.intro.IntroView.IntroState;
import de.amr.statemachine.client.FsmComponent;
import de.amr.statemachine.client.FsmContainer;
import de.amr.statemachine.client.FsmControlled;
import de.amr.statemachine.core.StateMachine;

/**
 * Intro screen with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroView extends AbstractPacManGameView implements FsmContainer<IntroState, Void> {

	public enum IntroState {
		LOADING_MUSIC, SCROLLING_LOGO, SHOWING_ANIMATIONS, WAITING_FOR_INPUT, READY_TO_PLAY
	};

	private final String name;
	private final PacManTheme theme;
	private final FsmComponent<IntroState, Void> fsm;
	private final Set<View> activeAnimations = new HashSet<>();
	private ImageWidget scrollingLogo;
	private ChasePacManAnimation chasePacMan;
	private ChaseGhostsAnimation chaseGhosts;
	private GhostPointsAnimation ghostPointsAnimation;
	private LinkWidget gitHubLink;
	private CompletableFuture<Void> musicLoading;

	private int textAlpha = -1;
	private int textAlphaInc;

	public IntroView(PacManTheme theme, int width, int height) {
		super(width, height);
		this.theme = theme;
		this.name = "IntroView";
		scrollingLogo = new ImageWidget(Assets.image("logo.png"));
		scrollingLogo.tf.centerX(width);
		scrollingLogo.tf.setY(20);
		chasePacMan = new ChasePacManAnimation(theme);
		chasePacMan.tf.centerX(width);
		chasePacMan.tf.setY(100);
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
		gitHubLink.tf.setY(height - 16);
		gitHubLink.tf.centerX(width);
		fsm = buildFsmComponent();
	}

	@Override
	public PacManTheme theme() {
		return theme;
	}

	@Override
	public void updateTheme(PacManTheme theme) {
	}

	@Override
	public FsmControlled<IntroState, Void> fsmComponent() {
		return fsm;
	}

	private FsmComponent<IntroState, Void> buildFsmComponent() {
		StateMachine<IntroState, Void> fsm = buildStateMachine();
		fsm.traceTo(PacManGame.FSM_LOGGER, () -> 60);
		return new FsmComponent<>(fsm);
	}

	private StateMachine<IntroState, Void> buildStateMachine() {
		return StateMachine.
		/*@formatter:off*/
		beginStateMachine(IntroState.class, Void.class)
			.description(String.format("[%s]", name))
			.initialState(LOADING_MUSIC)
			.states()
	
			  .state(LOADING_MUSIC)
			  	.onEntry(() -> {
			  		musicLoading = CompletableFuture.runAsync(() -> {
			  			theme().music_playing();
			  			theme().music_gameover();
			  		});
			  	})
		
				.state(SCROLLING_LOGO)
					.onEntry(() -> {
						scrollingLogo.tf.setY(height);
						scrollingLogo.tf.setVelocityY(-2f);
						scrollingLogo.setCompletion(() -> scrollingLogo.tf.getY() <= 20);
						show(scrollingLogo); 
						scrollingLogo.start(); 
						theme().snd_insertCoin().play();
					})
					.onTick(() -> {
						scrollingLogo.update();
					})
	
				.state(SHOWING_ANIMATIONS)
					.onEntry(() -> {
						chasePacMan.setStartPosition(width, 100);
						chasePacMan.setEndPosition(-chasePacMan.tf.getWidth(), 100);
						chaseGhosts.setStartPosition(-chaseGhosts.tf.getWidth(), 200);
						chaseGhosts.setEndPosition(width, 200);
						show(chasePacMan, chaseGhosts);
						start(chasePacMan, chaseGhosts);
					})
					.onTick(() -> {
						activeAnimations.forEach(animation -> ((Lifecycle) animation).update());
					})
					.onExit(() -> {
						stop(chasePacMan, chaseGhosts);
						chasePacMan.tf.centerX(width);
					})
					
				.state(WAITING_FOR_INPUT)
					.timeoutAfter(sec(10))
					.onEntry(() -> {
						ghostPointsAnimation.tf.setY(200);
						ghostPointsAnimation.tf.centerX(width);
						ghostPointsAnimation.start();
						show(ghostPointsAnimation, gitHubLink);
					})
					.onTick(() -> {
						activeAnimations.forEach(animation -> ((Lifecycle) animation).update());
					})
					.onExit(() -> {
						ghostPointsAnimation.stop();
						hide(ghostPointsAnimation, gitHubLink);
					})
					
				.state(READY_TO_PLAY)
					
			.transitions()
			
				.when(LOADING_MUSIC).then(WAITING_FOR_INPUT)
					.condition(() -> musicLoading.isDone() && app().settings.getAsBoolean("skipIntro"))
			
				.when(LOADING_MUSIC).then(SCROLLING_LOGO)
					.condition(() -> musicLoading.isDone())
				
				.when(SCROLLING_LOGO).then(SHOWING_ANIMATIONS)
					.condition(() -> scrollingLogo.complete())
				
				.when(SHOWING_ANIMATIONS).then(WAITING_FOR_INPUT)
					.condition(() -> chasePacMan.complete() && chaseGhosts.complete())
				
				.when(WAITING_FOR_INPUT).then(SHOWING_ANIMATIONS)
					.onTimeout()
				
				.when(WAITING_FOR_INPUT).then(READY_TO_PLAY)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
	
		.endStateMachine();
	  /*@formatter:on*/
	}

	private void show(View... views) {
		Arrays.stream(views).forEach(activeAnimations::add);
	}

	private void hide(View... views) {
		Arrays.stream(views).forEach(activeAnimations::remove);
	}

	private void start(AnimationLifecycle... animations) {
		Arrays.stream(animations).forEach(AnimationLifecycle::start);
	}

	private void stop(AnimationLifecycle... animations) {
		Arrays.stream(animations).forEach(AnimationLifecycle::stop);
	}

	public boolean isComplete() {
		return is(READY_TO_PLAY);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void init() {
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
		g.fillRect(0, 0, width, height);

		// use colors from logo image
		Color orange = new Color(255, 163, 71);
		// Color pink = new Color(248, 120, 88);
		Color red = new Color(171, 19, 0);

		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());
			switch (getState()) {
			case LOADING_MUSIC:
				if (textAlpha > 160) {
					textAlphaInc = -2;
					textAlpha = 160;
				} else if (textAlpha < 0) {
					textAlphaInc = 2;
					textAlpha = 0;
				}
				pen.color(new Color(255, 255, 255, textAlpha));
				pen.fontSize(16);
				pen.hcenter("Loading...", width, 18);
				textAlpha += textAlphaInc;
				break;
			case SCROLLING_LOGO:
				activeAnimations.forEach(animation -> animation.draw(g));
				break;
			case SHOWING_ANIMATIONS:
				scrollingLogo.draw(g);
				activeAnimations.forEach(animation -> animation.draw(g));
				break;
			case WAITING_FOR_INPUT:
				scrollingLogo.draw(g);
				chasePacMan.draw(g);
				if (app().clock.getTicks() % sec(1) < sec(0.5f)) {
					pen.color(Color.WHITE);
					pen.fontSize(14);
					pen.hcenter("Press SPACE to start!", width, 18);
				}
				pen.color(orange);
				pen.fontSize(10);
				pen.hcenter("F11 - Fullscreen Mode", width, 22);
				int selectedSpeed = Arrays.asList(60, 70, 80).indexOf(app().clock.getFrequency()) + 1;
				pen.color(selectedSpeed == 1 ? orange : red);
				pen.draw("1 - Normal", 1, 31);
				pen.color(selectedSpeed == 2 ? orange : red);
				pen.draw("2 - Fast", 11, 31);
				pen.color(selectedSpeed == 3 ? orange : red);
				pen.draw("3 - Insane", 19, 31);
				activeAnimations.forEach(animation -> animation.draw(g));
				break;
			case READY_TO_PLAY:
				break;
			default:
				throw new IllegalStateException();
			}
		}
	}
}