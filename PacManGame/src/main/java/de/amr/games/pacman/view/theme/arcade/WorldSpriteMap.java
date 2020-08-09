package de.amr.games.pacman.view.theme.arcade;

import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.model.world.api.World;

public class WorldSpriteMap extends SpriteMap {

	public WorldSpriteMap(World world) {
		ArcadeSprites sprites = ArcadeTheme.THEME.$value("sprites");
		set("maze-full", sprites.makeSprite_fullMaze());
		set("maze-flashing", sprites.makeSprite_flashingMaze());
	}
}