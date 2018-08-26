package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.theme.PacManThemes;
import de.amr.games.pacman.view.widgets.Link;
import de.amr.statemachine.StateMachine;

/**
 * Intro with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroView implements ViewController {

	private static final String LINK_TEXT = "Visit on GitHub!";
	private static final String LINK_URL = "https://github.com/armin-reichert/pacman";

	private final int width;
	private final int height;

	private final StateMachine<Integer, Void> fsm;
	private final Set<GameEntity> visible = new HashSet<>();

	private final LogoAnimation logoAnimation;
	private final StartTextAnimation startTextAnimation;
	private final Link link;
	private final ChasePacManAnimation chasePacManAnimation;
	private final ChaseGhostsAnimation chaseGhostsAnimation;
	private final GhostPointsAnimation ghostPointsAnimation;

	private int repeatTimer;

	public IntroView(int width, int height) {
		this.width = width;
		this.height = height;
		fsm = buildStateMachine();
		logoAnimation = new LogoAnimation(width, height, 20);
		chasePacManAnimation = new ChasePacManAnimation(width);
		chaseGhostsAnimation = new ChaseGhostsAnimation(width);
		ghostPointsAnimation = new GhostPointsAnimation();
		startTextAnimation = new StartTextAnimation("Press SPACE to start!", 16);
		link = new Link(LINK_TEXT, PacManThemes.THEME.textFont().deriveFont(8f), Color.LIGHT_GRAY);
		link.setURL(LINK_URL);
	}

	private void show(GameEntity e) {
		visible.add(e);
	}

	private void hide(GameEntity e) {
		visible.remove(e);
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
					.onExit(() -> logoAnimation.stop())
					
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
					
				.state(42) // Complete	
					
			.transitions()

				.when(0).then(1)
					.condition(() -> logoAnimation.isCompleted())
				
				.when(1).then(2)
					.condition(() -> chasePacManAnimation.isComplete() && chaseGhostsAnimation.isComplete())
				
				.when(2).then(1)
					.condition(() -> repeatTimer == 0)
				
				.when(2).then(42)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
				
		.endStateMachine();
	  /*@formatter:on*/
	}

	public boolean isComplete() {
		return fsm.currentState() == 42;
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
		visible.forEach(e -> e.draw(g));
	}

	@Override
	public void init() {
		fsm.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			fsm.setState(4);
		}
		fsm.update();
		visible.forEach(GameEntity::update);
	}
}