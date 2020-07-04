package de.amr.games.pacman.view.render.api;

public interface IWorldRenderer extends IRenderer {

	void letEnergizersBlink(boolean b);

	void turnMazeFlashingOn();

	void turnMazeFlashingOff();

	void turnFullMazeOn();

}
