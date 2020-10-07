package de.amr.games.pacman.theme.arcade;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.theme.api.GameRenderer;
import de.amr.games.pacman.theme.api.GhostRenderer;
import de.amr.games.pacman.theme.api.MessagesRenderer;
import de.amr.games.pacman.theme.api.PacManRenderer;
import de.amr.games.pacman.theme.api.Theme;
import de.amr.games.pacman.theme.api.WorldRenderer;
import de.amr.games.pacman.theme.arcade.ArcadeSpritesheet.GhostColor;
import de.amr.games.pacman.theme.core.ThemeParameterMap;
import de.amr.games.pacman.view.api.PacManGameSounds;
import de.amr.games.pacman.view.common.DefaultGameScoreRenderer;
import de.amr.games.pacman.view.common.DefaultMessagesRenderer;

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
			set("symbol-" + symbol.name(), spriteSheet.makeSprite_bonusSymbol(symbol.name()).frame(0));
		}
		for (int points : List.of(100, 300, 500, 700, 1000, 2000, 3000, 5000)) {
			set("points-" + points, spriteSheet.imageNumber(points));
		}
		set("sprites", spriteSheet);
	}

	private SpriteMap makePacManSpriteMap() {
		SpriteMap map = new SpriteMap();
		Direction.dirs().forEach(dir -> {
			map.set("walking-" + dir, spriteSheet.makeSprite_pacManWalking(dir));
			map.set("blocked-" + dir, spriteSheet.makeSprite_pacManBlocked(dir));
		});
		map.set("collapsing", spriteSheet.makeSprite_pacManCollapsing());
		map.set("full", spriteSheet.makeSprite_pacManFull());
		return map;
	}

	private SpriteMap makeGhostSpriteMap(Ghost ghost) {
		SpriteMap map = new SpriteMap();
		for (Direction dir : Direction.values()) {
			for (GhostColor color : GhostColor.values()) {
				map.set(ghostSpriteKeyColor(color, dir), spriteSheet.makeSprite_ghostColored(color, dir));
			}
			map.set(ghostSpriteKeyEyes(dir), spriteSheet.makeSprite_ghostEyes(dir));
		}
		map.set("frightened", spriteSheet.makeSprite_ghostFrightened());
		map.set("flashing", spriteSheet.makeSprite_ghostFlashing());
		for (int bounty : List.of(200, 400, 800, 1600)) {
			map.set(ghostSpriteKeyPoints(bounty), Sprite.of(spriteSheet.imageNumber(bounty)));
		}
		return map;
	}

	SpriteMap getSpriteMap(Ghost ghost) {
		SpriteMap spriteMap = ghostSprites.get(ghost);
		if (spriteMap == null) {
			spriteMap = makeGhostSpriteMap(ghost);
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
		return new ArcadeWorldRenderer(spriteSheet);
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
		messagesRenderer.setFont($font("font"));
		return messagesRenderer;
	}

	@Override
	public GameRenderer levelCounterRenderer() {
		return (Graphics2D g, PacManGame game) -> {
			int max = 7;
			int first = Math.max(0, game.levelCounter.size() - max);
			int n = Math.min(max, game.levelCounter.size());
			int width = 2 * Tile.SIZE;
			for (int i = 0, x = -2 * width; i < n; ++i, x -= width) {
				ArcadeBonus.Symbol symbol = ArcadeBonus.Symbol.valueOf(game.levelCounter.get(first + i));
				g.drawImage(spriteSheet.imageBonusSymbol(symbol.ordinal()), x, 0, width, width, null);
			}
		};
	}

	@Override
	public GameRenderer livesCounterRenderer() {
		return (Graphics2D g, PacManGame game) -> {
			for (int i = 0, x = Tile.SIZE; i < game.lives; ++i, x += 2 * Tile.SIZE) {
				g.drawImage(spriteSheet.imageLivesCounter(), x, 0, null);
			}
		};
	}

	@Override
	public GameRenderer gameScoreRenderer() {
		DefaultGameScoreRenderer r = new DefaultGameScoreRenderer();
		r.setFont($font("font"));
		return r;
	}

	@Override
	public PacManGameSounds sounds() {
		return ArcadeSounds.SOUNDS;
	}
}