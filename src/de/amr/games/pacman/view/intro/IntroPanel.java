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
			
				.state(0)
					.onEntry(() -> {
						title = new Title();
						title.tf.setVelocityY(-0.8f);
						title.tf.setY(getHeight());
						title.hCenter(getWidth());
						entities.add(title);
					})
					.onTick(() -> title.update())
					
					
				.state(1)
					.onEntry(() -> {
						startText = new Text("Press SPACE to start!", 16);
						startText.center(width, height);
						entities.add(startText);
					})
					
			.transitions()

			.when(0).then(1).condition(() -> title.tf.getY() < 10)

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
	}
}