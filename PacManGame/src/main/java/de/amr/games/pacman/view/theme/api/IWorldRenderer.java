package de.amr.games.pacman.view.theme.api;

import java.awt.Color;
import java.util.function.Function;

import de.amr.games.pacman.model.world.core.Tile;

public interface IWorldRenderer extends IRenderer {

	default void setEatenFoodColor(Function<Tile, Color> fnEatenFoodColor) {

	}
}