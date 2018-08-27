package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.theme.PacManThemes;
import de.amr.games.pacman.view.core.BlinkingText;
import de.amr.games.pacman.view.core.Link;
import de.amr.statemachine.StateMachine;

/**
 * Intro with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroView implements ViewController {

	private static final String LINK_TEXT = "Visit on GitHub!";
	private static final String LINK_URL = "https://github.com/armin-reichert/pacman";

	private static final int COMPLETE = 42;

	private final int width;
	private final int height;
	private Color background = new Color(0, 23, 61);

	private final StateMachine<Integer, Void> fsm;
	private final Set<ViewController> visibleViews = new HashSet<>();

	private final ScrollingLogo logoAnimation;
	private final BlinkingText startTextAnimation;
	private final Link link;
	private final ChasePacManAnimation chasePacManAnimation;
	private final ChaseGhostsAnimation chaseGhostsAnimation;
	private final GhostPointsAnimation ghostPointsAnimation;

	private int repeatTimer;

	public IntroView(int width, int height) {
		this.width = width;
		this.height = height;
		fsm = buildStateMachine();
		logoAnimation = new ScrollingLogo(width, height);
		chasePacManAnimation = new ChasePacManAnimation(width);
		chaseGhostsAnimation = new ChaseGhostsAnimation(width);
		ghostPointsAnimation = new GhostPointsAnimation();
		startTextAnimation = new BlinkingText("Press SPACE to start!", 18, background);
		link = new Link(LINK_TEXT, PacManThemes.THEME.textFont().deriveFont(8f), Color.LIGHT_GRAY);
		link.setURL(LINK_URL);
	}

	private void show(ViewController view) {
		visibleViews.add(view);
	}

	private void hide(ViewController view) {
		visibleViews.remove(view);
	}

	private StateMachine<Integer, Void> buildStateMachine() {
		return
		/*@formatter:off*/
		StateMachine.define(Integer.class, Void.class)

			.description("IntroAnimation")
			.initialState(0)
	
			.states()
			
				.state(0) // Scroll image into view
					.onEntry(() -> {
						show(logoAnimation);
						logoAnimation.start();
					})
					
				.state(1) // Show ghosts chasing Pac-Man and vice-versa
					.onEntry(() -> {
						chasePacManAnimation.tf.setY(100);
						show(chasePacManAnimation);
						chaseGhostsAnimation.tf.setY(200);
						show(chaseGhostsAnimation);
						hide(startTextAnimation);
						chaseGhostsAnimation.start();
						chasePacManAnimation.start();
					})
					.onExit(() -> {
						chasePacManAnimation.stop();
						chaseGhostsAnimation.stop();
						chasePacManAnimation.init();
						chasePacManAnimation.hCenter(width);
					})
					
				.state(2) // Show ghost points animation and blinking text
					.onEntry(() -> {
						ghostPointsAnimation.tf.setY(200);
						ghostPointsAnimation.hCenter(width);
						show(ghostPointsAnimation);
						
						startTextAnimation.tf.setY(150);
						startTextAnimation.hCenter(width);
						startTextAnimation.enableAnimation(true);
						show(startTextAnimation);
						
						link.tf.setY(getHeight() - 20);
						link.hCenter(getWidth());
						show(link);
						
						repeatTimer = 1000;
						ghostPointsAnimation.start();
					})
					.onExit(() -> {
						ghostPointsAnimation.stop();
						hide(ghostPointsAnimation);
					})
					.onTick(() -> {
						repeatTimer -= 1;
					})
					
				.state(COMPLETE)
					
			.transitions()

				.when(0).then(1)
					.condition(() -> logoAnimation.isCompleted())
					.act(() -> logoAnimation.tf.setVelocityY(0))
				
				.when(1).then(2)
					.condition(() -> chasePacManAnimation.isCompleted() && chaseGhostsAnimation.isCompleted())
				
				.when(2).then(1)
					.condition(() -> repeatTimer == 0)
				
				.when(2).then(COMPLETE)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
				
		.endStateMachine();
	  /*@formatter:on*/
	}

	public boolean isComplete() {
		return fsm.currentState() == COMPLETE;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(background);
		g.fillRect(0, 0, getWidth(), getHeight());
		visibleViews.forEach(e -> e.draw(g));
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
		visibleViews.forEach(ViewController::update);
	}
}