package de.amr.games.pacman.theme;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.BonusSymbol;

public interface PacManTheme {

	Sprite mazeEmpty();

	Sprite mazeFull();

	Sprite mazeFlashing();

	Sprite symbol(BonusSymbol symbol);

	BufferedImage symbolImage(BonusSymbol symbol);

	Sprite pacManFull();

	Sprite pacManWalking(int dir);

	Sprite pacManDying();

	Sprite ghostColored(GhostColor color, int direction);

	Sprite ghostFrightened();

	Sprite ghostFlashing();

	Sprite ghostEyes(int dir);

	Sprite greenNumber(int i);

	Sprite pinkNumber(int i);

	Font textFont();

	default Font textFont(int size) {
		return textFont().deriveFont((float) size);
	}

	Sound soundDie();

	Sound soundEatFruit();

	Sound soundEatGhost();

	Sound soundEatPill();

	Sound soundEating();

	Sound soundExtraLife();

	Sound soundInsertCoin();

	Sound soundReady();

	Sound soundSiren();

	Sound soundWaza();

	Stream<Sound> allSounds();
}
