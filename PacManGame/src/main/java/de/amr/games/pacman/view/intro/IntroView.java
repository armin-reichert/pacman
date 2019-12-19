package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.Timing.sec;
import static de.amr.games.pacman.view.intro.IntroView.IntroViewState.CHASING;
import static de.amr.games.pacman.view.intro.IntroView.IntroViewState.COMPLETE;
import static de.amr.games.pacman.view.intro.IntroView.IntroViewState.LOADING_MUSIC;
import static de.amr.games.pacman.view.intro.IntroView.IntroViewState.LOGO_SCROLLING_IN;
import static de.amr.games.pacman.view.intro.IntroView.IntroViewState.READY;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.widgets.ImageWidget;
import de.amr.easy.game.ui.widgets.LinkWidget;
import de.amr.easy.game.view.AnimationController;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.Timing;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.Pen;
import de.amr.games.pacman.view.intro.IntroView.IntroViewState;
import de.amr.statemachine.StateMachine;

/**
 * Intro screen with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroView extends StateMachine<IntroViewState, Void> implements View, Controller {

	public enum IntroViewState {
		LOADING_MUSIC, LOGO_SCROLLING_IN, CHASING, READY, COMPLETE
	};

	public final PacManTheme theme;
	public final int width;
	public final int height;

	private long ticks;
	private final Set<View> animations = new HashSet<>();
	private final ImageWidget logoScrollingAnimation;
	private final ChasePacManAnimation chasePacManAnimation;
	private final ChaseGhostsAnimation chaseGhostsAnimation;
	private final GhostPointsAnimation ghostPointsAnimation;
	private final LinkWidget gitHubLink;

	private int loadingAlpha;

	public IntroView(PacManTheme theme) {
		super(IntroViewState.class);
		this.theme = theme;

		width = app().settings.width;
		height = app().settings.height;

		logoScrollingAnimation = new ImageWidget(Assets.image("logo.png"));
		logoScrollingAnimation.tf.centerX(width);
		logoScrollingAnimation.tf.setY(height);
		logoScrollingAnimation.tf.setVelocityY(-2f);
		logoScrollingAnimation.setCompletion(() -> logoScrollingAnimation.tf.getY() <= 20);

		chasePacManAnimation = new ChasePacManAnimation(theme);
		chasePacManAnimation.setStartPosition(width, 100);
		chasePacManAnimation.setEndPosition(-chasePacManAnimation.tf.getWidth(), 100);

		chaseGhostsAnimation = new ChaseGhostsAnimation(theme);
		chaseGhostsAnimation.setStartPosition(-chaseGhostsAnimation.tf.getWidth(), 200);
		chaseGhostsAnimation.setEndPosition(width, 200);

		ghostPointsAnimation = new GhostPointsAnimation(theme);
		ghostPointsAnimation.tf.setY(200);
		ghostPointsAnimation.tf.centerX(width);

		gitHubLink = LinkWidget.create()
		/*@formatter:off*/
			.text("Visit me on GitHub!")
			.url("https://github.com/armin-reichert/pacman")
			.font(new Font(Font.SANS_SERIF, Font.BOLD, 8))
			.color(Color.LIGHT_GRAY)
			.build();
		/*@formatter:on*/
		gitHubLink.tf.setY(height - 16);
		gitHubLink.tf.centerX(width);

		buildStateMachine();
		traceTo(Logger.getLogger("StateMachineLogger"), () -> Timing.FPS);
	}

	private void buildStateMachine() {
		/*@formatter:off*/
		beginStateMachine()
			.description("[IntroViewAnimation]")
			.initialState(LOADING_MUSIC)
			.states()
	
			  .state(LOADING_MUSIC)
			  	.onEntry(() -> {
			  		CompletableFuture.runAsync(() -> {
			  			LOGGER.info("Loading music...");
			  			theme.music_playing();
			  			theme.music_gameover();
			  		}).thenAccept(result -> {
			  			LOGGER.info("Music loaded.");
			  			setState(LOGO_SCROLLING_IN);
			  		});
			  	})
		
				.state(LOGO_SCROLLING_IN)
					.onEntry(() -> {
						theme.snd_insertCoin().play();
						show(logoScrollingAnimation); 
						logoScrollingAnimation.startAnimation(); 
					})
					.onExit(logoScrollingAnimation::stopAnimation)
	
				.state(CHASING)
					.onEntry(() -> {
						show(chasePacManAnimation, chaseGhostsAnimation);
						start(chasePacManAnimation, chaseGhostsAnimation);
					})
					.onExit(() -> {
						stop(chasePacManAnimation, chaseGhostsAnimation);
						chasePacManAnimation.tf.centerX(width);
					})
					
				.state(READY)
					.timeoutAfter(sec(6))
					.onEntry(() -> {
						show(ghostPointsAnimation, gitHubLink);
						ghostPointsAnimation.startAnimation();
					})
					.onExit(() -> {
						ghostPointsAnimation.stopAnimation();
						hide(ghostPointsAnimation);
					})
					
				.state(COMPLETE)
					
			.transitions()
				
				.when(LOGO_SCROLLING_IN).then(CHASING)
					.condition(() -> logoScrollingAnimation.isAnimationCompleted())
				
				.when(CHASING).then(READY)
					.condition(() -> chasePacManAnimation.isAnimationCompleted() && chaseGhostsAnimation.isAnimationCompleted())
				
				.when(READY).then(CHASING)
					.onTimeout()
				
				.when(READY).then(COMPLETE)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
	
		.endStateMachine();
	  /*@formatter:on*/
	}

	private void show(View... views) {
		Arrays.stream(views).forEach(animations::add);
	}

	private void hide(View... views) {
		Arrays.stream(views).forEach(animations::remove);
	}

	private void start(AnimationController... animations) {
		Arrays.stream(animations).forEach(AnimationController::startAnimation);
	}

	private void stop(AnimationController... animations) {
		Arrays.stream(animations).forEach(AnimationController::stopAnimation);
	}

	public boolean isComplete() {
		return is(COMPLETE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			setState(COMPLETE); // exit shortcut
		}
		super.update();
		animations.forEach(animation -> ((Controller) animation).update());
		++ticks;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(new Color(0, 23, 61));
		g.fillRect(0, 0, width, height);
		animations.forEach(animation -> animation.draw(g));
		drawTexts(g);
	}

	private void drawTexts(Graphics2D g) {
		Pen pen = new Pen(g);
		pen.font(theme.fnt_text());
		switch (getState()) {
		case LOADING_MUSIC:
			loadingAlpha = Math.min(loadingAlpha + 1, 255);
			pen.color(new Color(255, 255, 255, loadingAlpha));
			pen.fontSize(16);
			pen.draw("Loading...", 8, 18);
			break;
		case LOGO_SCROLLING_IN:
			break;
		case CHASING:
			break;
		case READY:
			if (ticks % sec(1) < sec(0.5f)) {
				pen.color(Color.RED);
				pen.fontSize(14);
				pen.draw("Press SPACE to start!", 2, 18);
			}
			pen.color(Color.PINK);
			pen.fontSize(10);
			pen.draw("F11 - Fullscreen Mode", 6, 22);
			int selectedSpeed = Arrays.asList(60, 70, 80).indexOf(app().clock.getFrequency()) + 1;
			pen.color(selectedSpeed == 1 ? Color.YELLOW : Color.PINK);
			pen.draw("1 Normal", 2, 32);
			pen.color(selectedSpeed == 2 ? Color.YELLOW : Color.PINK);
			pen.draw("2 Fast", 12, 32);
			pen.color(selectedSpeed == 3 ? Color.YELLOW : Color.PINK);
			pen.draw("3 Insane", 20, 32);
			break;
		case COMPLETE:
			break;
		}
	}
}