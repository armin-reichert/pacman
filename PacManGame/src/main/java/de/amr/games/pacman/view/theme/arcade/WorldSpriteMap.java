package de.amr.games.pacman.view.theme.arcade;

import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.model.world.api.World;

class WorldSpriteMap extends SpriteMap {

	private SpriteAnimation energizerAnimation;

	public WorldSpriteMap(World world) {
		ArcadeSprites sprites = ArcadeTheme.THEME.$value("sprites");
		set("maze-full", sprites.makeSprite_fullMaze());
		set("maze-flashing", sprites.makeSprite_flashingMaze());
		energizerAnimation = new CyclicAnimation(2);
		energizerAnimation.setFrameDuration(150);
	}

	public SpriteAnimation getEnergizerAnimation() {
		return energizerAnimation;
	}
}