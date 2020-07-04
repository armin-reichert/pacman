package de.amr.games.pacman.view.render.api;

import java.awt.Color;
import java.util.function.Function;

import de.amr.games.pacman.model.world.core.Tile;

public interface IWorldRenderer extends IRenderer {

	void letEnergizersBlink(boolean b);

	void turnMazeFlashingOn();

	void turnMazeFlashingOff();

	void turnFullMazeOn();

	void setEatenFoodColor(Function<Tile, Color> fnEatenFoodColor);
}