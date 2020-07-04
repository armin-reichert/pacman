package de.amr.games.pacman.view.render;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.view.theme.Theme;

public class GhostRenderer extends CreatureRenderer {

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
		showColored();
	}

	public void showColored() {
		selectSprite("color-" + ghost.moveDir());
	}

	public void showFrightened() {
		selectSprite("frightened");
	}

	public void showEyes() {
		selectSprite("eyes-" + ghost.moveDir());
	}

	public void showFlashing() {
		selectSprite("flashing");
	}

	@Override
	public void draw(Graphics2D g) {
		drawCreature(g, ghost);
	}
}