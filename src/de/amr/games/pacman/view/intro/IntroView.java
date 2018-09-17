package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controls.AnimationController;
import de.amr.easy.game.controls.BlinkingText;
import de.amr.easy.game.controls.Link;
import de.amr.easy.game.controls.ScrollableImage;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.PacManApp;
import de.amr.statemachine.StateMachine;

/**
 * Intro with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroView extends StateMachine<Integer, Void> implements View, Controller {

	private static final String GITHUB_TEXT = "Visit on GitHub!";
	private static final String GITHUB_URL = "https://github.com/armin-reichert/pacman";

	private final int width;
	private final int height;
	private final Color background;
	private final Set<View> animations = new HashSet<>();
	private final ScrollableImage logo;
	private final BlinkingText pressSpace;
	private final BlinkingText f11Hint;
	private final BlinkingText speedHint;
	private final ChasePacManAnimation chasePacMan;
	private final ChaseGhostsAnimation chaseGhosts;
	private final GhostPointsAnimation ghostPoints;
	private final Link visitGitHub;

	public IntroView() {
		super(Integer.class);
		width = app().settings.width;
		height = app().settings.height;
		background = new Color(0, 23, 61);
		logo = new ScrollableImage(Assets.image("logo.png"));
		logo.tf.centerX(width);
		logo.tf.setY(height);
		logo.setSpeedY(-2f);
		logo.setCompletion(() -> logo.tf.getY() <= 20);
		chasePacMan = new ChasePacManAnimation();
		chasePacMan.setStartPosition(width, 100);
		chasePacMan.setEndPosition(-chasePacMan.tf.getWidth(), 100);
		chaseGhosts = new ChaseGhostsAnimation();
		chaseGhosts.setStartPosition(-chaseGhosts.tf.getWidth(), 200);
		chaseGhosts.setEndPosition(width, 200);
		ghostPoints = new GhostPointsAnimation();
		ghostPoints.tf.setY(200);
		ghostPoints.tf.centerX(width);
		pressSpace = BlinkingText.create().text("Press SPACE to start!").spaceExpansion(3).blinkTimeMillis(1000)
				.font(PacManApp.THEME.fnt_text(18)).background(background).color(Color.PINK).build();
		pressSpace.tf.setY(150);
		pressSpace.tf.centerX(width);
		f11Hint = BlinkingText.create().text("F11 Toggle Fullscreen").spaceExpansion(3)
				.blinkTimeMillis(Integer.MAX_VALUE).font(PacManApp.THEME.fnt_text(12)).background(background)
				.color(Color.PINK).build();
		f11Hint.tf.setY(height - 60);
		f11Hint.tf.centerX(width);
		speedHint = BlinkingText.create().text("Normal 1   Fast 2   Insane 3").spaceExpansion(3)
				.blinkTimeMillis(Integer.MAX_VALUE).font(PacManApp.THEME.fnt_text(12)).background(background)
				.color(Color.PINK).build();
		speedHint.tf.setY(height - 40);
		speedHint.tf.centerX(width);
		visitGitHub = Link.create().text(GITHUB_TEXT).url(GITHUB_URL).font(PacManApp.THEME.fnt_text(8))
				.color(Color.LIGHT_GRAY).build();
		visitGitHub.tf.setY(height - 10);
		visitGitHub.tf.centerX(width);
		buildStateMachine();
	}

	private void show(View... views) {
		Arrays.stream(views).forEach(animations::add);
	}

	private void hide(View... views) {
		Arrays.stream(views).forEach(animations::remove);
	}

	private void start(AnimationController... animations) {
		Arrays.stream(animations).forEach(AnimationController::start);
	}

	private void stop(AnimationController... animations) {
		Arrays.stream(animations).forEach(AnimationController::stop);
	}

	private void buildStateMachine() {
		/*@formatter:off*/
		beginStateMachine()
			.description("[Intro]")
			.initialState(0)
			.states()

				.state(0)
					// Scroll logo into view
					.onEntry(() -> { show(logo); logo.start(); })
					.onExit(() -> logo.stop())

				.state(1)
					// Show ghosts chasing Pac-Man and vice-versa
					.onEntry(() -> {
						show(chasePacMan, chaseGhosts);
						start(chasePacMan, chaseGhosts);
					})
					.onExit(() -> {
						stop(chasePacMan, chaseGhosts);
						chasePacMan.tf.centerX(width);
					})
					
				.state(2)
					// Show ghost points animation and blinking text
					.timeoutAfter(() -> app().clock.sec(6))
					.onEntry(() -> {
						show(ghostPoints, pressSpace, f11Hint, speedHint, visitGitHub);
						ghostPoints.start();
					})
					.onExit(() -> {
						ghostPoints.stop();
						hide(ghostPoints, pressSpace);
					})
					
				.state(42)
					
			.transitions()
				.when(0).then(1).condition(() -> logo.isCompleted())
				.when(1).then(2).condition(() -> chasePacMan.isCompleted() && chaseGhosts.isCompleted())
				.when(2).then(1).onTimeout()
				.when(2).then(42).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

		.endStateMachine();
	  /*@formatter:on*/
	}

	public boolean isComplete() {
		return getState() == 42;
	}

	@Override
	public void init() {
		super.init();
		traceTo(Application.LOGGER, Application.app().clock::getFrequency);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			setState(42);
		}
		super.update();
		animations.forEach(animation -> ((Controller) animation).update());
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		animations.forEach(animation -> animation.draw(g));
	}
}