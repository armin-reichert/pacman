package de.amr.games.pacman.view.theme.arcade;

import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;

class GhostSpriteMap extends SpriteMap {

	static String keyColor(GhostPersonality personality, Direction dir) {
		return String.format("color-%s-%s", personality, dir);
	}

	static String keyEyes(Direction dir) {
		return String.format("eyes-%s", dir);
	}

	static String keyPoints(int points) {
		return String.format("points-%d", points);
	}

	GhostSpriteMap(Ghost ghost) {
		ArcadeSprites arcadeSprites = ArcadeTheme.THEME.$value("sprites");
		for (Direction dir : Direction.values()) {
			for (GhostPersonality personality : GhostPersonality.values()) {
				set(keyColor(personality, dir), arcadeSprites.makeSprite_ghostColored(personality, dir));
			}
			set(keyEyes(dir), arcadeSprites.makeSprite_ghostEyes(dir));
		}
		set("frightened", arcadeSprites.makeSprite_ghostFrightened());
		set("flashing", arcadeSprites.makeSprite_ghostFlashing());
		for (int bounty : Game.GHOST_BOUNTIES) {
			set(keyPoints(bounty), arcadeSprites.makeSprite_number(bounty));
		}
	}
}