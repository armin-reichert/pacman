/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.PacManApp.appSettings;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.CHASING_ANIMATIONS;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.READY_TO_PLAY;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.SCROLLING_LOGO_ANIMATION;
import static de.amr.games.pacman.view.intro.IntroView.IntroState.WAITING_FOR_INPUT;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.widgets.ImageWidget;
import de.amr.easy.game.ui.widgets.LinkWidget;
import de.amr.easy.game.view.Pen;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.game.Timing;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.model.world.core.EmptyWorld;
import de.amr.games.pacman.theme.api.MessagesRenderer;
import de.amr.games.pacman.theme.api.Theme;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.intro.IntroView.IntroState;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The intro screen displays different animations and waits for starting the game.
 * 
 * @author Armin Reichert
 */
public class IntroView extends StateMachine<IntroState, Void> implements PacManGameView {

	public enum IntroState {
		SCROLLING_LOGO_ANIMATION, CHASING_ANIMATIONS, WAITING_FOR_INPUT, READY_TO_PLAY
	}

	private static final String GITHUB_URL = "https://github.com/armin-reichert/pacman";
	private static final Color ORANGE = new Color(255, 163, 71);
	private static final Color RED = new Color(171, 19, 0);

	private final TiledWorld world;
	private final int width;
	private final int height;
	private final ImageWidget pacManLogo;
	private final ChasePacManAnimation chasePacMan;
	private final ChaseGhostsAnimation chaseGhosts;
	private final GhostPointsAnimation ghostPointsAnimation;
	private final LinkWidget gitHubLink;

	private Theme theme;
	private MessagesRenderer messagesRenderer;

	public IntroView(Theme theme) {
		super(IntroState.class);
		this.theme = theme;
		width = appSettings.width;
		height = appSettings.height;
		world = new EmptyWorld(width / Tile.SIZE, height / Tile.SIZE);
		messagesRenderer = theme.messagesRenderer();
		pacManLogo = new ImageWidget(Assets.readImage("images/logo.png"));
		chasePacMan = new ChasePacManAnimation(theme, world);
		chaseGhosts = new ChaseGhostsAnimation(theme, world);
		ghostPointsAnimation = new GhostPointsAnimation(theme, world);
		gitHubLink = LinkWidget.create()
		/*@formatter:off*/
			.text(GITHUB_URL)
			.url(GITHUB_URL)
			.font(new Font(Font.MONOSPACED, Font.BOLD, 6))
			.color(Color.LIGHT_GRAY)
			.build();
		/*@formatter:on*/
		createController();
		init();
	}

	@Override
	public void init() {
		pacManLogo.tf.centerHorizontally(0, width);
		pacManLogo.tf.y = 20;
		chasePacMan.tf.centerHorizontally(0, width);
		chasePacMan.tf.y = 100;
		gitHubLink.tf.centerHorizontally(0, width);
		gitHubLink.tf.y = -16 + height;
		super.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			setState(READY_TO_PLAY); // shortcut for skipping intro
		}
		super.update();
	}

	@Override
	public boolean isComplete() {
		return is(READY_TO_PLAY);
	}

	@Override
	public void exit() {
		theme.sounds().stopAll();
	}

	@Override
	public Stream<StateMachine<?, ?>> machines() {
		return Stream.of(this);
	}

	@Override
	public Theme getTheme() {
		return theme;
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		messagesRenderer = theme.messagesRenderer();
		chaseGhosts.setTheme(theme);
		chasePacMan.setTheme(theme);
		ghostPointsAnimation.setTheme(theme);
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(new Color(0, 23, 61));
		g.fillRect(0, 0, width, height);
		if (state() instanceof View) {
			((View) state()).draw(g);
		}
	}

	private void drawToggleScreenModeText(Graphics2D g, int row) {
		boolean isFullscreen = app().inFullScreenMode();
		String text = "F11-" + texts.getString(isFullscreen ? "window_mode" : "fullscreen_mode");
		messagesRenderer.setRow(row);
		messagesRenderer.setTextColor(Color.ORANGE);
		messagesRenderer.draw(g, text, world.width());
	}

	private void createController() {
		/*@formatter:off*/
		beginStateMachine()
			.description("IntroViewController")
			.initialState(SCROLLING_LOGO_ANIMATION)
			.states()
				
				.state(SCROLLING_LOGO_ANIMATION)
					.customState(new ScrollingLogoAnimation())
				
				.state(CHASING_ANIMATIONS)
					.customState(new ChasingAnimation())
				
				.state(WAITING_FOR_INPUT)
					.customState(new WaitingForInput())
					.timeoutAfter(Timing.sec(10))
					
				.state(READY_TO_PLAY)
					
			.transitions()
			
				.when(SCROLLING_LOGO_ANIMATION).then(CHASING_ANIMATIONS)
					.condition(pacManLogo::isComplete)
					.annotation("Pac-Man logo at top")
				
				.when(CHASING_ANIMATIONS).then(WAITING_FOR_INPUT)
					.condition(() -> chasePacMan.isComplete() && chaseGhosts.isComplete())
					.annotation("Chasing animations complete")
				
				.when(WAITING_FOR_INPUT).then(CHASING_ANIMATIONS)
					.onTimeout()
				
				.when(WAITING_FOR_INPUT).then(READY_TO_PLAY)
					.condition(() -> Keyboard.keyPressedOnce("space"))
					.annotation("SPACE pressed")
	
		.endStateMachine();
	  /*@formatter:on*/
	}

	private class ScrollingLogoAnimation extends State<IntroState> implements View {

		@Override
		public void onEntry() {
			pacManLogo.tf.y = height;
			pacManLogo.tf.vy = -2f;
			pacManLogo.setCompletion(() -> pacManLogo.tf.y <= 20);
			pacManLogo.visible = true;
			pacManLogo.start();
			theme.sounds().clipInsertCoin().play();
		}

		@Override
		public void onTick(State<IntroState> state, long passed, long remaining) {
			pacManLogo.update();
		}

		@Override
		public void draw(Graphics2D g) {
			pacManLogo.draw(g);
		}
	}

	private class ChasingAnimation extends State<IntroState> implements View {

		@Override
		public void onEntry() {
			chasePacMan.tf.width = 88;
			chasePacMan.tf.height = 16;
			chaseGhosts.tf.y = 200;
			chaseGhosts.init();
			chasePacMan.start();
			chaseGhosts.start();
		}

		@Override
		public void onTick(State<IntroState> state, long consumed, long remaining) {
			chasePacMan.update();
			chaseGhosts.update();
		}

		@Override
		public void onExit() {
			chasePacMan.stop();
			chaseGhosts.stop();
			chasePacMan.tf.centerHorizontally(0, width);
		}

		@Override
		public void draw(Graphics2D g) {
			pacManLogo.draw(g);
			chaseGhosts.draw(g);
			chasePacMan.draw(g);
			drawToggleScreenModeText(g, 31);
		}
	}

	private class WaitingForInput extends State<IntroState> implements View {

		@Override
		public void onEntry() {
			ghostPointsAnimation.tf.y = 200;
			ghostPointsAnimation.tf.centerHorizontally(0, width);
			ghostPointsAnimation.start();
			chasePacMan.tf.centerHorizontally(0, width);
			chasePacMan.initPositions(width / 2 + 5 * Tile.SIZE);
			chasePacMan.guys().forEach(guy -> guy.tf.vx = 0);
			gitHubLink.visible = true;
		}

		@Override
		public void onTick(State<IntroState> state, long consumed, long remaining) {
			ghostPointsAnimation.update();
			chasePacMan.update();
			gitHubLink.update();
		}

		@Override
		public void onExit() {
			ghostPointsAnimation.stop();
			ghostPointsAnimation.visible = false;
			gitHubLink.visible = false;
		}

		@Override
		public void draw(Graphics2D g) {
			pacManLogo.draw(g);
			chasePacMan.draw(g);
			ghostPointsAnimation.draw(g);
			gitHubLink.draw(g);
			if (app().clock().getTotalTicks() % Timing.sec(1) < Timing.sec(0.5f)) {
				messagesRenderer.setRow(18);
				messagesRenderer.setTextColor(Color.WHITE);
				messagesRenderer.draw(g, texts.getString("press_space_to_start"), world.width());
			}
			drawSpeedSelectionTexts(g, 22);
			drawToggleScreenModeText(g, 31);
		}

		private void drawSpeedSelectionTexts(Graphics2D g, int row) {
			String[] speedTexts = { "1-" + texts.getString("normal"), "2-" + texts.getString("fast"),
					"3-" + texts.getString("insane") };
			try (Pen pen = new Pen(g)) {
				pen.font(messagesRenderer.getFont());
				FontMetrics fm = pen.getFontMetrics();
				int[] w = { fm.stringWidth(speedTexts[0]), fm.stringWidth(speedTexts[1]), fm.stringWidth(speedTexts[2]) };
				float s = (width - (w[0] + w[1] + w[2])) / 4f;
				float[] x = { s, s + w[0] + s, s + w[0] + s + w[1] + s };
				int selectedSpeed = Arrays.asList(60, 70, 80).indexOf(app().clock().getTargetFramerate());
				for (int i = 0; i < 3; ++i) {
					pen.color(selectedSpeed == i ? ORANGE : RED);
					pen.draw(speedTexts[i], x[i], row * Tile.SIZE);
				}
			}
		}
	}
}