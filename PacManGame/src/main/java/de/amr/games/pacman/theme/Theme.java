package de.amr.games.pacman.theme;

import java.awt.Color;
import java.awt.Font;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.Symbol;

/**
 * Common interface for themes used in Pac-Man.
 * 
 * @author Armin Reichert
 */
public interface Theme {

	public static final int MAZE_FLASH_TIME_MILLIS = 400;

	Sprite spr_emptyMaze();

	Sprite spr_fullMaze();

	Sprite spr_flashingMaze();

	Sprite spr_bonusSymbol(Symbol symbol);

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