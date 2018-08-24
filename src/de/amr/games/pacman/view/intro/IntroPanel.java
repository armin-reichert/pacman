package de.amr.games.pacman.view.intro;

import java.awt.Graphics2D;
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
	private Title title;
	private Text startText;
	private GhostsChasingPacMan ghostsChasingPacMan;
	private PacManChasingGhosts pacManChasingGhosts;

	public IntroPanel(int width, int height) {
		this.width = width;
		this.height = height;
		animation = buildStateMachine();
	}

	private StateMachine<Integer, Void> buildStateMachine() {
		return
		/*@formatter:off*/
		StateMachine.define(Integer.class, Void.class)
			.description("")
			.initialState(0)
	
			.states()
			
				.state(0) // scroll title image from bottom into view
					.onEntry(() -> {
						entities.add(title = new Title());
						title.tf.setVelocityY(-0.8f);
						title.tf.setY(getHeight());
						title.hCenter(getWidth());
					})
					
					
				.state(1) // show blinking start text and chasing
					.onEntry(() -> {
						entities.add(startText = new Text("Press SPACE to start!", 16));
						entities.add(ghostsChasingPacMan = new GhostsChasingPacMan());
						entities.add(pacManChasingGhosts = new PacManChasingGhosts());
						startText.center(width, height);
						ghostsChasingPacMan.tf.moveTo(width, 100);
						ghostsChasingPacMan.tf.setVelocityX(-0.8f);
						pacManChasingGhosts.tf.moveTo(-80,  200);
						pacManChasingGhosts.tf.setVelocityX(0);
					})
					
			.transitions()

			.when(0).then(1)
				.condition(() -> title.tf.getY() < 10)
				.act(() -> title.tf.setVelocityY(0))
				
			.stay(1)
				.condition(() -> ghostsChasingPacMan.tf.getX() < -80)
				.act(() -> {
					ghostsChasingPacMan.tf.moveTo(width, 100);
					ghostsChasingPacMan.tf.setVelocityX(0);
					pacManChasingGhosts.tf.moveTo(-80,  200);
					pacManChasingGhosts.tf.setVelocityX(0.8f);
				})
				
			.stay(1)
				.condition(() -> pacManChasingGhosts.tf.getX() > width)
				.act(() -> {
					pacManChasingGhosts.tf.moveTo(-80,  200);
					pacManChasingGhosts.tf.setVelocityX(0);
					ghostsChasingPacMan.tf.moveTo(width, 100);
					ghostsChasingPacMan.tf.setVelocityX(-0.8f);
				})
				
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