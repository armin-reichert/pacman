package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.model.Direction.dirs;

import java.awt.Graphics2D;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.Theme;

public abstract class PacManGameView implements Lifecycle, View {

	public int width() {
		return app().settings().width;
	}

	public int height() {
		return app().settings().height;
	}

	public void dress(Theme theme, Game game) {
		dress(theme, game.pacMan);
		dress(theme, game.blinky, GhostColor.RED);
		dress(theme, game.pinky, GhostColor.PINK);
		dress(theme, game.inky, GhostColor.CYAN);
		dress(theme, game.clyde, GhostColor.ORANGE);
	}

	private void dress(Theme theme, PacMan pacMan) {
		Direction.dirs().forEach(dir -> pacMan.sprites.set("walking-" + dir, theme.spr_pacManWalking(dir.ordinal())));
		pacMan.sprites.set("dying", theme.spr_pacManDying());
		pacMan.sprites.set("full", theme.spr_pacManFull());
	}

	private void dress(Theme theme, Ghost ghost, GhostColor color) {
		dirs().forEach(dir -> {
			ghost.sprites.set("color-" + dir, theme.spr_ghostColored(color, dir.ordinal()));
			ghost.sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir.ordinal()));
		});
		for (int points : Game.POINTS_GHOST) {
			ghost.sprites.set("points-" + points, theme.spr_number(points));
		}
		ghost.sprites.set("frightened", theme.spr_ghostFrightened());
		ghost.sprites.set("flashing", theme.spr_ghostFlashing());
	}

	protected void drawPacMan(PacMan pacMan, Graphics2D g) {
		if (pacMan.visible) {
			pacMan.sprites.current().ifPresent(sprite -> {
				Vector2f center = pacMan.tf.getCenter();
				float x = center.x - sprite.getWidth() / 2;
				float y = center.y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	protected void drawGhost(Ghost ghost, Graphics2D g) {
		if (ghost.visible) {
			ghost.sprites.current().ifPresent(sprite -> {
				Vector2f center = ghost.tf.getCenter();
				float x = center.x - sprite.getWidth() / 2;
				float y = center.y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	public void drawBonus(Bonus bonus, Graphics2D g) {
		if (bonus.visible) {
			bonus.sprites.current().ifPresent(sprite -> {
				Vector2f center = bonus.tf.getCenter();
				float x = center.x - sprite.getWidth() / 2;
				float y = center.y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}
}