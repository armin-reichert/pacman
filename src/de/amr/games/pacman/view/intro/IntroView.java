package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.ViewController;
import de.amr.statemachine.StateMachine;

/**
 * Intro with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroView implements ViewController {

	private static final String LINK = "Visit https://github.com/armin-reichert/pacman";

	private final int width;
	private final int height;

	private final StateMachine<Integer, Void> fsm;
	private final Set<GameEntity> entities = new HashSet<>();

	private LogoAnimation logoAnimation;
	private StartTextAnimation startTextAnimation;
	private ChasePacManAnimation chasePacManAnimation;
	private ChaseGhostsAnimation chaseGhostsAnimation;
	private GhostPointsAnimation ghostPointsAnimation;

	public IntroView(int width, int height) {
		this.width = width;
		this.height = height;
		fsm = buildStateMachine();
	}

	private StateMachine<Integer, Void> buildStateMachine() {
		return
		/*@formatter:off*/
		StateMachine.define(Integer.class, Void.class)

			.description("IntroAnimation")
			.initialState(0)
	
			.states()
			
				.state(0) // Scroll Pac-Man logo into view
					.onEntry(() -> {
						entities.add(logoAnimation = new LogoAnimation(width, height, 20));
						logoAnimation.start();
					})
					.onExit(() -> {
						logoAnimation.stop();
					})
					
				.state(1) // Show ghosts chasing Pac-Man
					.onEntry(() -> {
						entities.add(chasePacManAnimation = new ChasePacManAnimation(width));
						chasePacManAnimation.tf.setY(100);
						chasePacManAnimation.start();
					})
					.onExit(() -> {
						chasePacManAnimation.stop();
						chasePacManAnimation.init();
						chasePacManAnimation.hCenter(width);
					})
					
				.state(2) // Show Pac-Man chasing ghosts
					.onEntry(() -> {
						entities.add(chaseGhostsAnimation = new ChaseGhostsAnimation(width));
						chaseGhostsAnimation.tf.setY(200);
						chaseGhostsAnimation.start();
					})
					.onExit(() -> {
						chaseGhostsAnimation.stop();
						entities.remove(chaseGhostsAnimation);
					})
					
				.state(3) // Show ghost points animation and blinking text
					.onEntry(() -> {
						entities.add(ghostPointsAnimation = new GhostPointsAnimation());
						ghostPointsAnimation.tf.setY(200);
						ghostPointsAnimation.hCenter(width);
						ghostPointsAnimation.start();
						entities.add(startTextAnimation = new StartTextAnimation("Press SPACE to start!", 16));
						startTextAnimation.tf.setY(150);
						startTextAnimation.hCenter(width);
						startTextAnimation.enableAnimation(true);
					})
					
				.state(4) // Complete	
					
			.transitions()

				.when(0).then(1).condition(() -> logoAnimation.isCompleted())
				.when(1).then(2).condition(() -> chasePacManAnimation.isComplete())
				.when(2).then(3).condition(() -> chaseGhostsAnimation.isComplete())
				.when(3).then(4).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
				
		.endStateMachine();
	  /*@formatter:on*/
	}

	public boolean isComplete() {
		return fsm.currentState() == 4;
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
		entities.forEach(e -> e.draw(g));
		if (fsm.currentState() == 3) {
			drawLinkText(g);
		}
	}

	protected void drawLinkText(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawString(LINK, (getWidth() - g.getFontMetrics().stringWidth(LINK)) / 2, getHeight() - 6);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	@Override
	public void init() {
		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
		entities.forEach(GameEntity::update);
	}
}