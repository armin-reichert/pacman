package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.sprite.AnimationType;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.statemachine.StateMachine;

public class IntroPanel implements ViewController {

	private final int width;
	private final int height;
	
	private final StateMachine<Integer, Void> timeline;
	
	private final Image titleImage;
	private final Sprite startText;

	public IntroPanel(int width, int height) {
		this.width = width;
		this.height = height;
		timeline = buildStateMachine();
		titleImage = Assets.image("title.png");
		startText = createBlinkingText("Press SPACE to start");
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
				})
		.transitions()
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
	
	private Sprite createBlinkingText(String text) {
		BufferedImage image = new BufferedImage(width, 32, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.YELLOW);
		g.setFont(PacManTheme.ASSETS.textFont().deriveFont(16f));
		int w = g.getFontMetrics().stringWidth(text);
		g.drawString(text, (width - w) / 2, 16f);
		g.dispose();
		return new Sprite(image, null).animate(AnimationType.BACK_AND_FORTH, 1000);
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(titleImage, (width - titleImage.getWidth(null)) / 2, 0, null);
		g.translate(0, getHeight()/2);
		startText.draw(g);
		g.translate(0, -getHeight()/2);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}