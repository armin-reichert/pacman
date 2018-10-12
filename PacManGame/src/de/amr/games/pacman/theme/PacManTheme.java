package de.amr.games.pacman.theme;

import java.awt.Font;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.model.BonusSymbol;

public interface PacManTheme {

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

	// Background music
	
	Sound music_playing();
	
	Sound music_gameover();
	
	// Clips
	
	Sound snd_die();

	Sound snd_eatFruit();

	Sound snd_eatGhost();

	Sound snd_eatPill();

	Sound snd_extraLife();

	Sound snd_insertCoin();

	Sound snd_ready();

	Sound snd_siren();
	
	Sound snd_ghost_dead();

	Sound snd_waza();

	Stream<Sound> snd_clips_all();
	
}
