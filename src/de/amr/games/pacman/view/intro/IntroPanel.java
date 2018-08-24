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

public class IntroPanel implements ViewController {

	private final int width;
	private final int height;
	private final StateMachine<Integer, Void> animation;
	private Set<GameEntity> entities = new HashSet<>();
	private PacManLogo logo;
	private BlinkingStartText startText;
	private GhostsChasingPacMan ghostsChasingPacMan;
	private PacManGhostsChasing pacManGhostsChasing;
	private PacManGhostsPoints pacManGhostPoints;

	public IntroPanel(int width, int height) {
		this.width = width;
		this.height = height;
		animation = buildStateMachine();
	}

	private StateMachine<Integer, Void> buildStateMachine() {
		return
		/*@formatter:off*/
		StateMachine.define(Integer.class, Void.class)
			.description("IntroPanel")
			.initialState(0)
	
			.states()
			
					.state(0) // Scroll Pac-Man logo into view
						.onEntry(() -> {
							entities.add(logo = new PacManLogo());
							logo.start();
						})
						
					.state(1) // Show ghosts chasing Pac-Man
						.onEntry(() -> {
							entities.add(ghostsChasingPacMan = new GhostsChasingPacMan());
							ghostsChasingPacMan.tf.setY(100);
							ghostsChasingPacMan.start();
						})
						.onExit(() -> {
							ghostsChasingPacMan.stop();
							ghostsChasingPacMan.init();
							ghostsChasingPacMan.hCenter(width);
						})
						
					.state(2) // Show Pac-Man chasing ghosts
						.onEntry(() -> {
							entities.add(pacManGhostsChasing = new PacManGhostsChasing());
							pacManGhostsChasing.tf.setY(200);
							pacManGhostsChasing.start();
						})
						.onExit(() -> {
							entities.remove(pacManGhostsChasing);
							entities.add(pacManGhostPoints = new PacManGhostsPoints());
							pacManGhostPoints.tf.setY(200);
							pacManGhostPoints.hCenter(width);
							pacManGhostPoints.start();
						})
						
					.state(3) // Show blinking text
						.onEntry(() -> {
							entities.add(startText = new BlinkingStartText("Press   SPACE   to   start!", 16));
							startText.tf.setY(150);
							startText.hCenter(width);
						})
					
			.transitions()

					.when(0).then(1)
						.condition(() -> logo.isCompleted())
						.act(() -> logo.stop())
						
					.when(1).then(2).condition(() -> ghostsChasingPacMan.isComplete())
						
					.when(2).then(3).condition(() -> pacManGhostsChasing.isComplete())
				
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
		if (animation.currentState() == 3) {
			String url = "https://github.com/armin-reichert/pacman";
			g.setColor(Color.LIGHT_GRAY);
			g.setFont(new Font("Arial Narrow", Font.BOLD, 8));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(url, (getWidth() - g.getFontMetrics().stringWidth(url)) / 2, getHeight() - 16);
		}
	}

	@Override
	public void init() {
		animation.init();
	}

	@Override
	public void update() {
		animation.update();
		entities.forEach(GameEntity::update);
	}
}