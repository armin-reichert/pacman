package de.amr.games.pacman.view.loading;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.core.AbstractPacManGameView;
import de.amr.games.pacman.view.core.Pen;

/**
 * View displayed while the music files are loaded.
 * 
 * @author Armin Reichert
 */
public class LoadingMusicView extends AbstractPacManGameView {

	private PacManTheme theme;
	private int textAlpha = -1;
	private int textAlphaInc;

	public LoadingMusicView(PacManTheme theme, int width, int height) {
		super(width, height);
		this.theme = theme;
	}

	@Override
	public void draw(Graphics2D g) {
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());
			if (textAlpha > 160) {
				textAlphaInc = -2;
				textAlpha = 160;
			} else if (textAlpha < 0) {
				textAlphaInc = 2;
				textAlpha = 0;
			}
			pen.color(new Color(255, 255, 255, textAlpha));
			pen.fontSize(16);
			pen.hcenter("Loading music...", width, 18);
			textAlpha += textAlphaInc;
		}
	}

	@Override
	public PacManTheme theme() {
		return theme;
	}

	@Override
	public void updateTheme(PacManTheme theme) {
	}
}