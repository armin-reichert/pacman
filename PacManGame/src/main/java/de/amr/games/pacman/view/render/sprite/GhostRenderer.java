package de.amr.games.pacman.view.render.sprite;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.view.render.api.IRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class GhostRenderer extends CreatureRenderer implements IRenderer {

	private final Ghost ghost;

	public GhostRenderer(Ghost ghost, Theme theme) {
		this.ghost = ghost;
		Direction.dirs().forEach(dir -> {
			sprites.set("color-" + dir, theme.spr_ghostColored(ghost.color, dir));
			sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir));
		});
		sprites.set("frightened", theme.spr_ghostFrightened());
		sprites.set("flashing", theme.spr_ghostFlashing());
		for (int points : Game.POINTS_GHOST) {
			sprites.set("points-" + points, theme.spr_number(points));
		}
	}

	@Override
	public void draw(Graphics2D g) {
		GhostState state = ghost.getState();
		switch (state) {
		case CHASING:
		case SCATTERING:
		case LEAVING_HOUSE:
			selectSprite("color-" + ghost.moveDir());
			break;
		case LOCKED:
			if (ghost.world().population().pacMan().power > 0) {
				selectSprite("frightened");
			} else {
				selectSprite("color-" + ghost.moveDir());
			}
			break;
		case FRIGHTENED:
			selectSprite(ghost.flashing ? "flashing" : "frightened");
			break;
		case DEAD:
			selectSprite(ghost.points == 0 ? "eyes-" + ghost.moveDir() : "points-" + ghost.points);
			break;
		case ENTERING_HOUSE:
			selectSprite("eyes-" + ghost.moveDir());
			break;
		default:
		}
		drawEntity(g, ghost);
	}
}