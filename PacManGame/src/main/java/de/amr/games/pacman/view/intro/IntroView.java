package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.view.intro.IntroView.IntroViewState.CHASING_EACH_OTHER;
import static de.amr.games.pacman.view.intro.IntroView.IntroViewState.LEAVING_INTRO;
import static de.amr.games.pacman.view.intro.IntroView.IntroViewState.LOGO_SCROLLING_IN;
import static de.amr.games.pacman.view.intro.IntroView.IntroViewState.READY_TO_PLAY;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.widgets.ImageWidget;
import de.amr.easy.game.ui.widgets.LinkWidget;
import de.amr.easy.game.view.AnimationController;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
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
		LOGO_SCROLLING_IN, CHASING_EACH_OTHER, READY_TO_PLAY, LEAVING_INTRO
	};

	private static final String GITHUB_TEXT = "Visit me on GitHub!";
	private static final String GITHUB_URL = "https://github.com/armin-reichert/pacman";

	public final PacManTheme theme;

	private final int width;
	private final int height;
	private long ticks;
	private boolean showStaticTexts;
	private boolean showReadyToPlay;

	private final Color background;
	private final Set<View> animations = new HashSet<>();
	private final ImageWidget logo;
	private final ChasePacManAnimation chasePacMan;
	private final ChaseGhostsAnimation chaseGhosts;
	private final GhostPointsAnimation ghostPoints;
	private final LinkWidget visitGitHub;

	public IntroView(PacManTheme theme) {
		super(IntroViewState.class);
		this.theme = theme;
		width = app().settings.width;
		height = app().settings.height;
		background = new Color(0, 23, 61);

		logo = new ImageWidget(Assets.image("logo.png"));
		logo.tf.centerX(width);
		logo.tf.setY(height);
		logo.tf.setVelocityY(-2f);
		logo.setCompletion(() -> logo.tf.getY() <= 20);

		chasePacMan = new ChasePacManAnimation(theme);
		chasePacMan.setStartPosition(width, 100);
		chasePacMan.setEndPosition(-chasePacMan.tf.getWidth(), 100);

		chaseGhosts = new ChaseGhostsAnimation(theme);
		chaseGhosts.setStartPosition(-chaseGhosts.tf.getWidth(), 200);
		chaseGhosts.setEndPosition(width, 200);

		ghostPoints = new GhostPointsAnimation(theme);
		ghostPoints.tf.setY(200);
		ghostPoints.tf.centerX(width);

		visitGitHub = LinkWidget.create().text(GITHUB_TEXT).url(GITHUB_URL)
				.font(new Font(Font.SANS_SERIF, Font.BOLD, 8)).color(Color.LIGHT_GRAY).build();
		visitGitHub.tf.setY(height - 16);
		visitGitHub.tf.centerX(width);

		buildStateMachine();
		traceTo(Logger.getLogger("StateMachineLogger"), app().clock::getFrequency);
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

	private void buildStateMachine() {
		/*@formatter:off*/
		beginStateMachine()
			.description("[Intro]")
			.initialState(LOGO_SCROLLING_IN)
			.states()

				.state(LOGO_SCROLLING_IN)
					.onEntry(() -> { show(logo); logo.startAnimation(); })
					.onExit(() -> logo.stopAnimation())

				.state(CHASING_EACH_OTHER)
					// Show ghosts chasing Pac-Man and vice-versa
					.onEntry(() -> {
						show(chasePacMan, chaseGhosts);
						start(chasePacMan, chaseGhosts);
					})
					.onExit(() -> {
						stop(chasePacMan, chaseGhosts);
						chasePacMan.tf.centerX(width);
					})
					
				.state(READY_TO_PLAY)
					// Show ghost points animation and blinking text
					.timeoutAfter(() -> app().clock.sec(6))
					.onEntry(() -> {
						showStaticTexts = true;
						showReadyToPlay = true;
						show(ghostPoints, visitGitHub);
						ghostPoints.startAnimation();
					})
					.onExit(() -> {
						showReadyToPlay = false;
						showStaticTexts = false;
						ghostPoints.stopAnimation();
						hide(ghostPoints);
					})
					
				.state(LEAVING_INTRO)
					
			.transitions()
				
				.when(LOGO_SCROLLING_IN).then(CHASING_EACH_OTHER)
					.condition(() -> logo.isAnimationCompleted())
				
				.when(CHASING_EACH_OTHER).then(READY_TO_PLAY)
					.condition(() -> chasePacMan.isAnimationCompleted() && chaseGhosts.isAnimationCompleted())
				
				.when(READY_TO_PLAY).then(CHASING_EACH_OTHER)
					.onTimeout()
				
				.when(READY_TO_PLAY).then(LEAVING_INTRO)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

		.endStateMachine();
	  /*@formatter:on*/
	}

	public boolean isComplete() {
		return is(LEAVING_INTRO);
	}

	@Override
	public void update() {
		++ticks;
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			setState(LEAVING_INTRO);
		}
		super.update();
		animations.forEach(animation -> ((Controller) animation).update());
	}

	@Override
	public void draw(Graphics2D g) {
		Pen pen = new Pen(g);
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		animations.forEach(animation -> animation.draw(g));
		if (showReadyToPlay) {
			if (ticks % 60 < 30) {
				pen.color = Color.RED;
				pen.font = theme.fnt_text(14);
				pen.text("Press SPACE to start!", 2, 18);
			}
		}
		if (showStaticTexts) {
			pen.color = Color.PINK;
			pen.font = theme.fnt_text(10);
			pen.text("F11 - Fullscreen Mode", 6, 22);
			int selectedSpeed = Arrays.asList(60, 70, 80).indexOf(app().clock.getFrequency()) + 1;
			pen.color = selectedSpeed == 1 ? Color.YELLOW : Color.PINK;
			pen.text("1 Normal", 2, 32);
			pen.color = selectedSpeed == 2 ? Color.YELLOW : Color.PINK;
			pen.text("2 Fast", 12, 32);
			pen.color = selectedSpeed == 3 ? Color.YELLOW : Color.PINK;
			pen.text("3 Insane", 20, 32);
		}
	}
}