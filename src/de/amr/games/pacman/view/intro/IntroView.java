package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.PULSE;
import static de.amr.games.pacman.theme.PacManThemes.THEME;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.view.core.BlinkingText;
import de.amr.games.pacman.view.core.Link;
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
	private Color background = new Color(0, 23, 61);

	private final StateMachine<Integer, Void> fsm;
	private final Set<View> animations = new HashSet<>();

	private final ScrollingLogo logo;
	private final BlinkingText startText;
	private final ChasePacManAnimation chasePacMan;
	private final ChaseGhostsAnimation chaseGhosts;
	private final GhostPointsAnimation ghostPoints;
	private final Link link;

	public IntroView(int width, int height) {
		this.width = width;
		this.height = height;
		fsm = buildStateMachine();
		logo = new ScrollingLogo(width, height);
		chasePacMan = new ChasePacManAnimation();
		chasePacMan.setStartPosition(width, 100);
		chasePacMan.setEndPosition(-chasePacMan.tf.getWidth(), 100);
		chaseGhosts = new ChaseGhostsAnimation();
		chaseGhosts.setStartPosition(-chaseGhosts.tf.getWidth(), 200);
		chaseGhosts.setEndPosition(width, 200);
		ghostPoints = new GhostPointsAnimation();
		ghostPoints.tf.setY(200);
		ghostPoints.centerHorizontally(width);
		startText = new BlinkingText().set("Press SPACE to start!", THEME.textFont(18), background,
				Color.PINK);
		startText.tf.setY(150);
		startText.centerHorizontally(width);
		link = new Link(LINK_TEXT, THEME.textFont(8), Color.LIGHT_GRAY);
		link.setURL(LINK_URL);
		link.tf.setY(height - 20);
		link.centerHorizontally(width);
	}

	private void show(View view) {
		animations.add(view);
	}

	private void hide(View view) {
		animations.remove(view);
	}

	private StateMachine<Integer, Void> buildStateMachine() {
		return
		/*@formatter:off*/
		StateMachine.define(Integer.class, Void.class)

			.description("IntroAnimation")
			.initialState(0)
	
			.states()
			
				.state(0) // Scroll logo into view
					.onEntry(() -> {
						show(logo);
						logo.start();
					})
					.onExit(() -> logo.stop())
					
				.state(1) // Show ghosts chasing Pac-Man and vice-versa
					.onEntry(() -> {
						hide(startText);
						show(chasePacMan);
						chasePacMan.start();
						show(chaseGhosts);
						chaseGhosts.start();
					})
					.onExit(() -> {
						chasePacMan.stop();
						chasePacMan.init();
						chasePacMan.centerHorizontally(width);
						chaseGhosts.stop();
					})
					
				.state(2) // Show ghost points animation and blinking text
					.timeoutAfter(() -> PULSE.secToTicks(6))
					.onEntry(() -> {
						show(ghostPoints);
						show(startText);
						show(link);
						ghostPoints.start();
					})
					.onExit(() -> {
						ghostPoints.stop();
						hide(ghostPoints);
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
		return fsm.currentState() == COMPLETE;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		animations.forEach(e -> e.draw(g));
	}

	@Override
	public void init() {
		fsm.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			fsm.setState(COMPLETE);
		}
		fsm.update();
		animations.forEach(animation -> ((Controller) animation).update());
	}
}