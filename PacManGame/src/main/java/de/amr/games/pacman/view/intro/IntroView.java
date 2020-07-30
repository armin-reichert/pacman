package de.amr.games.pacman.view.intro;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.game.Game.sec;
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

import de.amr.easy.game.controller.StateMachineRegistry;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.widgets.ImageWidget;
import de.amr.easy.game.ui.widgets.LinkWidget;
import de.amr.easy.game.view.Pen;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.view.Localized;
import de.amr.games.pacman.view.api.PacManGameView;
import de.amr.games.pacman.view.intro.IntroView.IntroState;
import de.amr.games.pacman.view.theme.api.PacManSounds;
import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.theme.arcade.ArcadeThemeSprites;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

/**
 * The intro screen displays different animations and waits for starting the game.
 * 
 * @author Armin Reichert
 */
public class IntroView extends StateMachine<IntroState, Void> implements PacManGameView {

	static final String GITHUB_URL = "https://github.com/armin-reichert/pacman";

	public enum IntroState {
		SCROLLING_LOGO_ANIMATION, CHASING_ANIMATIONS, WAITING_FOR_INPUT, READY_TO_PLAY
	};

	private static final Color ORANGE = new Color(255, 163, 71);
	private static final Color RED = new Color(171, 19, 0);
//	PINK = (248, 120, 88);

	private final World world;
	private final Folks folks;
	private final PacManSounds sounds;
	private final int width;
	private final int height;

	private Theme theme;
	private ImageWidget pacManLogo;
	private LinkWidget gitHubLink;
	private ChasePacManAnimation chasePacMan;
	private ChaseGhostsAnimation chaseGhosts;
	private GhostPointsAnimation ghostPointsAnimation;

	private MessagesRenderer messagesRenderer;

	public IntroView(World world, Folks folks, Theme theme, int width, int height) {
		super(IntroState.class);
		this.world = world;
		this.folks = folks;
		this.theme = theme;
		this.messagesRenderer = theme.messagesRenderer();
		this.sounds = theme.sounds();
		this.width = width;
		this.height = height;
		/*@formatter:off*/
		beginStateMachine()
			.description("IntroView")
			.initialState(SCROLLING_LOGO_ANIMATION)
			.states()
				
				.state(SCROLLING_LOGO_ANIMATION)
					.customState(new ScrollingLogoAnimation())
				
				.state(CHASING_ANIMATIONS)
					.customState(new ChasingAnimation())
				
				.state(WAITING_FOR_INPUT)
					.customState(new WaitingForInput())
					.timeoutAfter(sec(15))
					
				.state(READY_TO_PLAY)
					
			.transitions()
			
				.when(SCROLLING_LOGO_ANIMATION).then(CHASING_ANIMATIONS)
					.condition(() -> pacManLogo.isComplete())
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

	@Override
	public void init() {
		ArcadeWorld world = new ArcadeWorld();
		ArcadeThemeSprites arcadeSprites = ArcadeTheme.THEME.$value("sprites");
		pacManLogo = new ImageWidget(arcadeSprites.image_logo());
		pacManLogo.tf.centerHorizontally(0, width);
		pacManLogo.tf.y = 20;
		chasePacMan = new ChasePacManAnimation(theme, world, new Folks(world, world.house(0)));
		chasePacMan.tf.centerHorizontally(0, width);
		chasePacMan.tf.y = 100;
		chaseGhosts = new ChaseGhostsAnimation(theme, world, new Folks(world, world.house(0)));
		ghostPointsAnimation = new GhostPointsAnimation(theme, new Folks(world, world.house(0)));
		gitHubLink = LinkWidget.create()
		/*@formatter:off*/
			.text(GITHUB_URL)
			.url(GITHUB_URL)
			.font(new Font(Font.MONOSPACED, Font.BOLD, 6))
			.color(Color.LIGHT_GRAY)
			.build();
		/*@formatter:on*/
		gitHubLink.tf.y = (height - 16);
		gitHubLink.tf.centerHorizontally(0, width);
		super.init();
		StateMachineRegistry.REGISTRY.register(Stream.of(this));
	}

	@Override
	public void exit() {
		StateMachineRegistry.REGISTRY.unregister(Stream.of(this));
		theme.sounds().stopAll();
	}

	@Override
	public Theme getTheme() {
		return theme;
	}

	@Override
	public void setTheme(Theme theme) {
		this.theme = theme;
		chaseGhosts.setTheme(theme);
		chasePacMan.setTheme(theme);
		ghostPointsAnimation.setTheme(theme);
		messagesRenderer = theme.messagesRenderer();
	}

	private class ScrollingLogoAnimation extends State<IntroState> implements View {

		@Override
		public void onEntry() {
			sounds.clipInsertCoin().play();
			pacManLogo.tf.y = height;
			pacManLogo.tf.vy = -2f;
			pacManLogo.setCompletion(() -> pacManLogo.tf.y <= 20);
			pacManLogo.visible = true;
			pacManLogo.start();
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
			drawScreenModeText(g, 31);
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
			folks.all().forEach(creature -> creature.entity.tf.vx = 0);
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
			if (app().clock().getTotalTicks() % sec(1) < sec(0.5f)) {
				messagesRenderer.setRow(18);
				messagesRenderer.setTextColor(Color.WHITE);
				messagesRenderer.drawCentered(g, Localized.texts.getString("press_space_to_start"), world.width());
			}
			drawSpeedSelectionTexts(g, 22);
			drawScreenModeText(g, 31);
		}
	}

	@Override
	public boolean isComplete() {
		return is(READY_TO_PLAY);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			setState(READY_TO_PLAY); // shortcut for skipping intro
		}
		super.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(new Color(0, 23, 61));
		g.fillRect(0, 0, width, height);
		if (state() instanceof View) {
			((View) state()).draw(g);
		}
	}

	private void drawScreenModeText(Graphics2D g, int row) {
		String text = "F11-" + Localized.texts.getString(app().inFullScreenMode() ? "window_mode" : "fullscreen_mode");
		messagesRenderer.setRow(row);
		messagesRenderer.setTextColor(Color.ORANGE);
		messagesRenderer.drawCentered(g, text, world.width());
	}

	private void drawSpeedSelectionTexts(Graphics2D g, int row) {
		String[] texts = {
			//@formatter:off
			"1-" + Localized.texts.getString("normal"),
			"2-" + Localized.texts.getString("fast"),
			"3-" + Localized.texts.getString("insane")
			//@formatter:on
		};
		try (Pen pen = new Pen(g)) {
			pen.font(messagesRenderer.getFont());
			FontMetrics fm = pen.getFontMetrics();
			int[] w = { fm.stringWidth(texts[0]), fm.stringWidth(texts[1]), fm.stringWidth(texts[2]) };
			float s = (width - (w[0] + w[1] + w[2])) / 4f;
			float[] x = { s, s + w[0] + s, s + w[0] + s + w[1] + s };
			int selectedSpeed = Arrays.asList(60, 70, 80).indexOf(app().clock().getTargetFramerate());
			for (int i = 0; i < 3; ++i) {
				pen.color(selectedSpeed == i ? ORANGE : RED);
				pen.draw(texts[i], x[i], row * Tile.SIZE);
			}
		}
	}
}