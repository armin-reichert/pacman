package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.Direction.dirs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.easy.game.view.Pen;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.Theme;

/**
 * Base class of all views in the game.
 * 
 * @author Armin Reichert
 */
public abstract class PacManGameView implements Lifecycle, View {

	public class Message {

		public String text;
		public Color color;
		public int fontSize;
		public int row;

		public Message() {
			text = "";
			color = Color.YELLOW;
			fontSize = 11;
			row = 21;
		}

		public void draw(Graphics2D g) {
			if (text != null && text.trim().length() > 0) {
				try (Pen pen = new Pen(g)) {
					pen.font(theme.fnt_text(fontSize));
					pen.color(color);
					pen.hcenter(text, width(), row, Tile.SIZE);
				}
			}
		}
	}

	public final Game game; // optional
	public final Theme theme;
	public final Message message;

	public int width() {
		return app().settings().width;
	}

	public int height() {
		return app().settings().height;
	}

	protected PacManGameView(Theme theme) {
		this.game = null;
		this.theme = Objects.requireNonNull(theme);
		message = new Message();
	}

	protected PacManGameView(Game game, Theme theme) {
		this.game = Objects.requireNonNull(game);
		this.theme = Objects.requireNonNull(theme);
		message = new Message();
		dressPacMan();
		dressGhost(game.blinky, GhostColor.RED);
		dressGhost(game.pinky, GhostColor.PINK);
		dressGhost(game.inky, GhostColor.CYAN);
		dressGhost(game.clyde, GhostColor.ORANGE);
	}

	@Override
	public void init() {
		message.text = "";
	}

	private void dressPacMan() {
		dirs().forEach(dir -> game.pacMan.sprites.set("walking-" + dir, theme.spr_pacManWalking(dir.ordinal())));
		game.pacMan.sprites.set("dying", theme.spr_pacManDying());
		game.pacMan.sprites.set("full", theme.spr_pacManFull());
	}

	private void dressGhost(Ghost ghost, GhostColor color) {
		dirs().forEach(dir -> {
			ghost.sprites.set("color-" + dir, theme.spr_ghostColored(color, dir.ordinal()));
			ghost.sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir.ordinal()));
		});
		ghost.sprites.set("frightened", theme.spr_ghostFrightened());
		ghost.sprites.set("flashing", theme.spr_ghostFlashing());
		for (int points : Game.POINTS_GHOST) {
			ghost.sprites.set("points-" + points, theme.spr_number(points));
		}
	}

	protected void fillBackground(Graphics2D g, Color color) {
		g.setColor(color);
		g.fillRect(0, 0, width(), height());
	}

	protected void drawActor(Graphics2D g, Entity actor, SpriteMap sprites) {
		if (actor.visible) {
			sprites.current().ifPresent(sprite -> {
				Vector2f center = actor.tf.getCenter();
				float x = center.x - sprite.getWidth() / 2;
				float y = center.y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	protected void drawMessage(Graphics2D g) {
		message.fontSize=8;
		message.draw(g);
	}
}