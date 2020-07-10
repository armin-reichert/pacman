package de.amr.games.pacman.view.loading;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.actor.ArcadeGameFolks;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.Localized;
import de.amr.games.pacman.view.core.PacManGameView;
import de.amr.games.pacman.view.core.Theme;
import de.amr.games.pacman.view.theme.common.MessagesRenderer;

/**
 * View displayed while the music files are loaded.
 * 
 * @author Armin Reichert
 */
public class LoadingView implements PacManGameView {

	private final World world = new ArcadeWorld();
	private final ArcadeGameFolks folks = new ArcadeGameFolks();
	private final PacMan pacMan = folks.pacMan();
	private final List<Ghost> ghosts = folks.ghosts().collect(Collectors.toList());
	private MessagesRenderer messagesRenderer;

	private final int width;
	private final int height;
	private int alpha;
	private int alphaInc;
	private int ghostCount;
	private int ghostInc;
	private Random rnd = new Random();

	public LoadingView(Theme theme, int width, int height) {
		this.width = width;
		this.height = height;
		setTheme(theme);
		folks.populate(world);
	}

	@Override
	public void setTheme(Theme theme) {
		folks.all().forEach(c -> c.setTheme(theme));
		messagesRenderer = theme.createMessagesRenderer();
	}

	@Override
	public void init() {
		ghostCount = 0;
		ghostInc = 1;
		ghosts.forEach(ghost -> {
			ghost.setMoveDir(Direction.values()[rnd.nextInt(4)]);
		});
		pacMan.init();
		pacMan.startRunning();
	}

	@Override
	public void update() {
		float x = pacMan.tf.getCenter().x;
		if (x > 0.9f * width || x < 0.1 * width) {
			pacMan.setMoveDir(pacMan.moveDir().opposite());
			ghostCount += ghostInc;
			if (ghostCount == 9 || ghostCount == 0) {
				ghostInc = -ghostInc;
			}
		}
		pacMan.tf.setVelocity(Vector2f.smul(2.5f, pacMan.moveDir().vector()));
		pacMan.tf.move();

		alpha += alphaInc;
		if (alpha >= 160) {
			alphaInc = -2;
			alpha = 160;
		} else if (alpha <= 0) {
			alphaInc = 2;
			alpha = 0;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(new Color(0, 23, 61));
		g.fillRect(0, 0, width, height);
		messagesRenderer.setRow(18);
		messagesRenderer.setTextColor(new Color(255, 0, 0, alpha));
		messagesRenderer.drawCentered(g, Localized.texts.getString("loading_music"), width);
		pacMan.draw(g);
		float x = width / 2 - (ghostCount / 2) * 20 - Tile.SIZE / 2, y = pacMan.tf.y + 20;
		for (int i = 0; i < ghostCount; ++i) {
			Ghost ghost = ghosts.get(rnd.nextInt(4));
			ghost.tf.x = x;
			ghost.tf.y = y;
			ghost.draw(g);
			x += 20;
		}
	}
}