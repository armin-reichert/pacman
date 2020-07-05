package de.amr.games.pacman.view.theme;

import java.awt.Color;
import java.util.function.Function;

import de.amr.games.pacman.model.world.core.Tile;

public interface IWorldRenderer extends IRenderer {

	void setEatenFoodColor(Function<Tile, Color> fnEatenFoodColor);
}