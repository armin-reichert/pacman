package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.CLOCK;
import static de.amr.games.pacman.theme.PacManThemes.THEME;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controls.AnimationController;
import de.amr.easy.game.controls.BlinkingText;
import de.amr.easy.game.controls.Link;
import de.amr.easy.game.controls.ScrollableImage;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.statemachine.StateMachine;

/**
 * Intro with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroView implements View, Controller {

	private static final String LINK_TEXT = "Visit on GitHub!";
	private static final String LINK_URL = "https://github.com/armin-reichert/pacman";

	private static final int COMPLETE = 42;

	private final int width;
	private final int height;
	private final Color background;

	private final StateMachine<Integer, Void> fsm;
	private final Set<View> animations = new HashSet<>();

	private final ScrollableImage logo;
	private final BlinkingText pressSpace;
	private final ChasePacManAnimation chasePacMan;
	private final ChaseGhostsAnimation chaseGhosts;
	private final GhostPointsAnimation ghostPoints;
	private final Link link;

	public IntroView(int width, int height) {
		this.width = width;
		this.height = height;
		background = new Color(0, 23, 61);
		fsm = buildStateMachine();
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
				.font(THEME.textFont(18)).background(background).color(Color.PINK).build();
		pressSpace.tf.setY(150);
		pressSpace.tf.centerX(width);
		link = Link.create().text(LINK_TEXT).url(LINK_URL).font(THEME.textFont(8)).color(Color.LIGHT_GRAY)
				.build();
		link.tf.setY(height - 20);
		link.tf.centerX(width);
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

	private StateMachine<Integer, Void> buildStateMachine() {
		return
		/*@formatter:off*/
		StateMachine.define(Integer.class, Void.class)
			.description("IntroAnimation")
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
					.timeoutAfter(() -> CLOCK.sec(6))
					.onEntry(() -> {
						show(ghostPoints, pressSpace, link);
						ghostPoints.start();
					})
					.onExit(() -> {
						ghostPoints.stop();
						hide(ghostPoints, pressSpace);
					})
					
				.state(COMPLETE)
					
			.transitions()
				.when(0).then(1).condition(() -> logo.isCompleted())
				.when(1).then(2).condition(() -> chasePacMan.isCompleted() && chaseGhosts.isCompleted())
				.when(2).then(1).onTimeout()
				.when(2).then(COMPLETE).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

		.endStateMachine();
	  /*@formatter:on*/
	}

	public boolean isComplete() {
		return fsm.getState() == COMPLETE;
	}

	@Override
	public void init() {
		fsm.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			fsm.setState(COMPLETE);
			return;
		}
		fsm.update();
		animations.forEach(animation -> ((Controller) animation).update());
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		animations.forEach(animation -> animation.draw(g));
	}
}