package de.amr.games.pacman.view.core;

import static de.amr.games.pacman.model.Direction.dirs;

import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.GhostColor;
import de.amr.games.pacman.theme.Theme;

public abstract class AbstractPacManGameView implements PacManGameView {

	public void dress(Theme theme, Cast cast) {
		dress(theme, cast.pacMan);
		dress(theme, cast.blinky, GhostColor.RED);
		dress(theme, cast.pinky, GhostColor.PINK);
		dress(theme, cast.inky, GhostColor.CYAN);
		dress(theme, cast.clyde, GhostColor.ORANGE);
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
}