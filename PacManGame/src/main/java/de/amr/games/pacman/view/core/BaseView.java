package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.Direction.dirs;

import java.awt.Graphics2D;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.view.theme.Theme;

/**
 * Base class of all views in the game.
 * 
 * @author Armin Reichert
 */
public abstract class BaseView implements Lifecycle, View {

	public final Theme theme;
	public final int width, height;

	public BaseView(Theme theme) {
		this.theme = theme;
		width = app().settings().width;
		height = app().settings().height;
	}

	public void dressPacMan(PacMan pacMan) {
		dirs().forEach(dir -> pacMan.sprites.set("walking-" + dir, theme.spr_pacManWalking(dir)));
		pacMan.sprites.set("dying", theme.spr_pacManDying());
		pacMan.sprites.set("full", theme.spr_pacManFull());
	}

	public void dressGhost(Ghost ghost, int color) {
		dirs().forEach(dir -> {
			ghost.sprites.set("color-" + dir, theme.spr_ghostColored(color, dir));
			ghost.sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir));
		});
		ghost.sprites.set("frightened", theme.spr_ghostFrightened());
		ghost.sprites.set("flashing", theme.spr_ghostFlashing());
		for (int points : Game.POINTS_GHOST) {
			ghost.sprites.set("points-" + points, theme.spr_number(points));
		}
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
}