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
public class LoadingView extends AbstractPacManGameView {

	private PacManTheme theme;
	private int alpha = -1;
	private int alphaInc;

	public LoadingView(PacManTheme theme) {
		this.theme = theme;
	}

	@Override
	public PacManTheme theme() {
		return theme;
	}

	@Override
	public void onThemeChanged(PacManTheme theme) {
		this.theme = theme;
	}

	@Override
	public void draw(Graphics2D g) {
		try (Pen pen = new Pen(g)) {
			pen.font(theme.fnt_text());
			if (alpha > 160) {
				alphaInc = -2;
				alpha = 160;
			}
			else if (alpha < 0) {
				alphaInc = 2;
				alpha = 0;
			}
			pen.color(new Color(255, 255, 255, alpha));
			pen.fontSize(16);
			pen.hcenter("Loading music...", width(), 18);
			alpha += alphaInc;
		}
		fpsView.draw(g);
	}
}