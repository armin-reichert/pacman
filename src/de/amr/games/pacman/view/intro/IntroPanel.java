package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.Set;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.view.ViewController;
import de.amr.statemachine.StateMachine;

/**
 * Intro panel with different animations.
 * 
 * @author Armin Reichert
 */
public class IntroPanel implements ViewController {

	private final String url = "Visit https://github.com/armin-reichert/pacman";
	private final int width;
	private final int height;

	private final StateMachine<Integer, Void> fsm;
	private Set<GameEntity> entities = new HashSet<>();

	private LogoAnimation logo;
	private StartTextAnimation startText;
	private ChasePacManAnimation chasePacManAnimation;
	private ChaseGhostsAnimation chaseGhostsAnimation;
	private GhostPointsAnimation ghostPointsAnimation;

	public IntroPanel(int width, int height) {
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
							entities.add(logo = new LogoAnimation(width, height, 20));
							logo.start();
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
							entities.add(startText = new StartTextAnimation("Press   SPACE   to   start!", 16));
							startText.tf.setY(150);
							startText.hCenter(width);
							startText.enableAnimation(true);
						})
					
			.transitions()

					.when(0).then(1).condition(() -> logo.isCompleted()).act(() -> logo.stop())
						
					.when(1).then(2).condition(() -> chasePacManAnimation.isComplete())
						
					.when(2).then(3).condition(() -> chaseGhostsAnimation.isComplete())
				
		.endStateMachine();
	  /*@formatter:on*/
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
			g.setColor(Color.LIGHT_GRAY);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 8));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(url, (getWidth() - g.getFontMetrics().stringWidth(url)) / 2, getHeight() - 16);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
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