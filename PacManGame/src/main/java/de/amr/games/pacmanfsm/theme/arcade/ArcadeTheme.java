/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.theme.arcade;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacmanfsm.controller.creatures.ghost.Ghost;
import de.amr.games.pacmanfsm.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacmanfsm.controller.creatures.pacman.PacMan;
import de.amr.games.pacmanfsm.lib.Direction;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.game.PacManGame;
import de.amr.games.pacmanfsm.model.world.arcade.ArcadeBonus;
import de.amr.games.pacmanfsm.theme.api.GameRenderer;
import de.amr.games.pacmanfsm.theme.api.GhostRenderer;
import de.amr.games.pacmanfsm.theme.api.MessagesRenderer;
import de.amr.games.pacmanfsm.theme.api.PacManRenderer;
import de.amr.games.pacmanfsm.theme.api.Theme;
import de.amr.games.pacmanfsm.theme.api.WorldRenderer;
import de.amr.games.pacmanfsm.theme.arcade.ArcadeSpritesheet.GhostColor;
import de.amr.games.pacmanfsm.theme.core.ThemeParameterMap;
import de.amr.games.pacmanfsm.view.api.PacManGameSounds;
import de.amr.games.pacmanfsm.view.common.DefaultGameScoreRenderer;
import de.amr.games.pacmanfsm.view.common.DefaultMessagesRenderer;

/**
 * This theme mimics the original Arcade version.
 * 
 * @author Armin Reichert
 */
public class ArcadeTheme extends ThemeParameterMap implements Theme {

	public static final ArcadeTheme THEME = new ArcadeTheme();

	private ArcadeSpritesheet spriteSheet = new ArcadeSpritesheet();
	private Map<PacMan, SpriteMap> pacManSprites = new HashMap<>();
	private Map<Ghost, SpriteMap> ghostSprites = new HashMap<>();

	private ArcadeTheme() {
		set("font", Assets.storeTrueTypeFont("PressStart2P", "themes/arcade/PressStart2P-Regular.ttf", Font.PLAIN, 8));
		set("maze-flash-sec", 0.4f);
		for (ArcadeBonus.Symbol symbol : ArcadeBonus.Symbol.values()) {
			set("symbol-" + symbol.name(), spriteSheet.makeSpriteBonusSymbol(symbol.name()).frame(0));
		}
		for (int points : List.of(100, 300, 500, 700, 1000, 2000, 3000, 5000)) {
			set("points-" + points, spriteSheet.imageNumber(points));
		}
		set("sprites", spriteSheet);
	}

	private SpriteMap makePacManSpriteMap() {
		SpriteMap map = new SpriteMap();
		Direction.dirs().forEach(dir -> {
			map.set("walking-" + dir, spriteSheet.makeSpritePacManWalking(dir));
			map.set("blocked-" + dir, spriteSheet.makeSpritePacManBlocked(dir));
		});
		map.set("collapsing", spriteSheet.makeSpritePacManCollapsing());
		map.set("full", spriteSheet.makeSpritePacManFull());
		return map;
	}

	private SpriteMap makeGhostSpriteMap() {
		SpriteMap map = new SpriteMap();
		for (Direction dir : Direction.values()) {
			for (GhostColor color : GhostColor.values()) {
				map.set(ghostSpriteKeyColor(color, dir), spriteSheet.makeSpritGhostColored(color, dir));
			}
			map.set(ghostSpriteKeyEyes(dir), spriteSheet.makeSpriteGhostEyes(dir));
		}
		map.set("frightened", spriteSheet.makeSpriteGhostFrightened());
		map.set("flashing", spriteSheet.makeSpriteGhostFlashing());
		for (int bounty : List.of(200, 400, 800, 1600)) {
			map.set(ghostSpriteKeyPoints(bounty), Sprite.of(spriteSheet.imageNumber(bounty)));
		}
		return map;
	}

	SpriteMap getSpriteMap(Ghost ghost) {
		SpriteMap spriteMap = ghostSprites.get(ghost);
		if (spriteMap == null) {
			spriteMap = makeGhostSpriteMap();
			ghostSprites.put(ghost, spriteMap);
		}
		return spriteMap;
	}

	SpriteMap getSpriteMap(PacMan pacMan) {
		SpriteMap spriteMap = pacManSprites.get(pacMan);
		if (spriteMap == null) {
			spriteMap = makePacManSpriteMap();
			pacManSprites.put(pacMan, spriteMap);
		}
		return spriteMap;
	}

	String ghostSpriteKeyColor(GhostColor color, Direction dir) {
		return String.format("colored-%s-%s", color, dir);
	}

	String ghostSpriteKeyEyes(Direction dir) {
		return String.format("eyes-%s", dir);
	}

	String ghostSpriteKeyPoints(int points) {
		return String.format("points-%d", points);
	}

	GhostColor color(GhostPersonality personality) {
		switch (personality) {
		case SHADOW:
			return GhostColor.RED;
		case SPEEDY:
			return GhostColor.PINK;
		case BASHFUL:
			return GhostColor.CYAN;
		case POKEY:
			return GhostColor.ORANGE;
		default:
			throw new IllegalArgumentException("Illegal ghost personality: " + personality);
		}
	}

	@Override
	public String name() {
		return "ARCADE";
	}

	@Override
	public WorldRenderer worldRenderer() {
		return new ArcadeWorldRenderer();
	}

	@Override
	public PacManRenderer pacManRenderer() {
		return new ArcadePacManRenderer();
	}

	@Override
	public GhostRenderer ghostRenderer() {
		return new ArcadeGhostRenderer();
	}

	@Override
	public MessagesRenderer messagesRenderer() {
		DefaultMessagesRenderer messagesRenderer = new DefaultMessagesRenderer();
		messagesRenderer.setFont(asFont("font"));
		return messagesRenderer;
	}

	@Override
	public GameRenderer levelCounterRenderer() {
		return (Graphics2D g, PacManGame game) -> {
			int max = 7;
			int first = Math.max(0, game.levelCounter.size() - max);
			int n = Math.min(max, game.levelCounter.size());
			int width = 2 * Tile.TS;
			for (int i = 0, x = -2 * width; i < n; ++i, x -= width) {
				ArcadeBonus.Symbol symbol = ArcadeBonus.Symbol.valueOf(game.levelCounter.get(first + i));
				g.drawImage(spriteSheet.imageBonusSymbol(symbol.ordinal()), x, 0, width, width, null);
			}
		};
	}

	@Override
	public GameRenderer livesCounterRenderer() {
		return (Graphics2D g, PacManGame game) -> {
			for (int i = 0, x = Tile.TS; i < game.lives; ++i, x += 2 * Tile.TS) {
				g.drawImage(spriteSheet.imageLivesCounter(), x, 0, null);
			}
		};
	}

	@Override
	public GameRenderer gameScoreRenderer() {
		DefaultGameScoreRenderer r = new DefaultGameScoreRenderer();
		r.setFont(asFont("font"));
		return r;
	}

	@Override
	public PacManGameSounds sounds() {
		return ArcadeSounds.SOUNDS;
	}
}