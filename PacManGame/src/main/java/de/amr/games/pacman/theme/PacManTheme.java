package de.amr.games.pacman.theme;

import static de.amr.games.pacman.model.Maze.NESW;

import java.awt.Color;
import java.awt.Font;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.BonusSymbol;

public interface PacManTheme {

	default void apply(PacMan pacMan) {
		pacMan.theme = this;
		NESW.dirs().forEach(dir -> pacMan.sprites.set("walking-" + dir, spr_pacManWalking(dir)));
		pacMan.sprites.set("dying", spr_pacManDying());
		pacMan.sprites.set("full", spr_pacManFull());
		pacMan.sprites.select("full");
	}

	default void apply(Ghost ghost, GhostColor color) {
		ghost.theme = this;
		NESW.dirs().forEach(dir -> {
			ghost.sprites.set("color-" + dir, spr_ghostColored(color, dir));
			ghost.sprites.set("eyes-" + dir, spr_ghostEyes(dir));
		});
		for (int i = 0; i < 4; ++i) {
			ghost.sprites.set("value-" + i, spr_greenNumber(i));
		}
		ghost.sprites.set("frightened", spr_ghostFrightened());
		ghost.sprites.set("flashing", spr_ghostFlashing());
	}

	Sprite spr_emptyMaze();

	Sprite spr_fullMaze();

	Sprite spr_flashingMaze();

	Sprite spr_bonusSymbol(BonusSymbol symbol);

	Sprite spr_pacManFull();

	Sprite spr_pacManWalking(int dir);

	Sprite spr_pacManDying();

	Sprite spr_ghostColored(GhostColor color, int direction);

	Sprite spr_ghostFrightened();

	Sprite spr_ghostFlashing();

	Sprite spr_ghostEyes(int dir);

	Sprite spr_greenNumber(int i);

	Sprite spr_pinkNumber(int i);

	Font fnt_text();

	default Font fnt_text(int size) {
		return fnt_text().deriveFont((float) size);
	}

	default Color color_mazeBackground() {
		return Color.BLACK;
	}

	// Background music

	Sound music_playing();

	Sound music_gameover();

	void loadMusic();

	// Clips

	Sound snd_die();

	Sound snd_eatFruit();

	Sound snd_eatGhost();

	Sound snd_eatPill();

	Sound snd_extraLife();

	Sound snd_insertCoin();

	Sound snd_ready();

	Sound snd_ghost_chase();

	Sound snd_ghost_dead();

	Sound snd_waza();

	Stream<Sound> snd_clips_all();

}
