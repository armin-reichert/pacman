package de.amr.games.pacman.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
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

	default BufferedImage changeColor(BufferedImage src, int from, int to) {
		BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		Graphics2D g = copy.createGraphics();
		g.drawImage(src, 0, 0, null);
		for (int x = 0; x < copy.getWidth(); ++x) {
			for (int y = 0; y < copy.getHeight(); ++y) {
				if (copy.getRGB(x, y) == from) {
					copy.setRGB(x, y, to);
				}
			}
		}
		g.dispose();
		return copy;
	}

	default BufferedImage $(int x, int y, int w, int h) {
		return spritesheet().getSubimage(x, y, w, h);
	}

	default BufferedImage $(int x, int y) {
		return $(x, y, 16, 16);
	}

	default BufferedImage[] hstrip(int n, int x, int y) {
		return IntStream.range(0, n).mapToObj(i -> $(x + i * 16, y)).toArray(BufferedImage[]::new);
	}

	BufferedImage spritesheet();

	BufferedImage img_logo();

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

	// Sound

	default Sound sound(String name, String type) {
		return Assets.sound("sfx/" + name + "." + type);
	}

	default Sound mp3(String name) {
		return sound(name, "mp3");
	}

	default Sound wav(String name) {
		return sound(name, "wav");
	}

	Sound music_playing();

	Sound music_gameover();

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