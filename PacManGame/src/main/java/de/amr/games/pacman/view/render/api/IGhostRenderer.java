package de.amr.games.pacman.view.render.api;

public interface IGhostRenderer extends ICreatureRenderer {

	void showColored();

	void showFrightened();

	void showEyes();

	void showFlashing();

	void showPoints(int points);

}
