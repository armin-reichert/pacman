package de.amr.games.pacman.view.theme.arcade;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.view.api.IGameRenderer;
import de.amr.games.pacman.view.api.IGhostRenderer;
import de.amr.games.pacman.view.api.IMessagesRenderer;
import de.amr.games.pacman.view.api.IPacManRenderer;
import de.amr.games.pacman.view.api.IPacManSounds;
import de.amr.games.pacman.view.api.IWorldRenderer;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.common.MessagesRenderer;
import de.amr.games.pacman.view.common.PointsCounterRenderer;
import de.amr.games.pacman.view.core.ThemeParameters;
import de.amr.games.pacman.view.theme.arcade.ArcadeSpritesheet.GhostColor;

/**
 * This theme mimics the original Arcade version.
 * 
 * @author Armin Reichert
 */
public class ArcadeTheme extends ThemeParameters implements Theme {

	public static final ArcadeTheme THEME = new ArcadeTheme();

	private ArcadeSpritesheet sprites = new ArcadeSpritesheet();
	private Map<PacMan, SpriteMap> pacManSprites = new HashMap<>();
	private Map<Ghost, SpriteMap> ghostSprites = new HashMap<>();

	private ArcadeTheme() {
		set("font", Assets.storeTrueTypeFont("PressStart2P", "themes/arcade/PressStart2P-Regular.ttf", Font.PLAIN, 8));
		set("maze-flash-sec", 0.4f);
		for (ArcadeBonus symbol : ArcadeBonus.values()) {
			set("symbol-" + symbol.name(), sprites.makeSprite_bonusSymbol(symbol.name()).frame(0));
		}
		for (int points : PacManGame.POINTS_BONUS) {
			set("points-" + points, sprites.makeSprite_number(points).frame(0));
		}
		set("sprites", sprites);
	}

	private SpriteMap makePacManSpriteMap() {
		SpriteMap map = new SpriteMap();
		Direction.dirs().forEach(dir -> {
			map.set("walking-" + dir, sprites.makeSprite_pacManWalking(dir));
			map.set("blocked-" + dir, sprites.makeSprite_pacManBlocked(dir));
		});
		map.set("collapsing", sprites.makeSprite_pacManCollapsing());
		map.set("full", sprites.makeSprite_pacManFull());
		return map;
	}

	private SpriteMap makeGhostSpriteMap(Ghost ghost) {
		SpriteMap map = new SpriteMap();
		for (Direction dir : Direction.values()) {
			for (GhostColor color : GhostColor.values()) {
				map.set(ghostSpriteKeyColor(color, dir), sprites.makeSprite_ghostColored(color, dir));
			}
			map.set(ghostSpriteKeyEyes(dir), sprites.makeSprite_ghostEyes(dir));
		}
		map.set("frightened", sprites.makeSprite_ghostFrightened());
		map.set("flashing", sprites.makeSprite_ghostFlashing());
		for (int bounty : PacManGame.POINTS_GHOSTS) {
			map.set(ghostSpriteKeyPoints(bounty), sprites.makeSprite_number(bounty));
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
	public IWorldRenderer worldRenderer() {
		return new WorldRenderer(sprites);
	}

	@Override
	public IPacManRenderer pacManRenderer() {
		return new PacManRenderer();
	}

	@Override
	public IGhostRenderer ghostRenderer() {
		return new GhostRenderer();
	}

	@Override
	public IMessagesRenderer messagesRenderer() {
		MessagesRenderer messagesRenderer = new MessagesRenderer();
		messagesRenderer.setFont($font("font"));
		return messagesRenderer;
	}

	@Override
	public IGameRenderer levelCounterRenderer() {
		return (Graphics2D g, PacManGame game) -> {
			int max = 7;
			int first = Math.max(0, game.levelCounter.size() - max);
			int n = Math.min(max, game.levelCounter.size());
			int width = 2 * Tile.SIZE;
			for (int i = 0, x = -2 * width; i < n; ++i, x -= width) {
				ArcadeBonus symbol = ArcadeBonus.valueOf(game.levelCounter.get(first + i));
				g.drawImage(sprites.imageBonusSymbol(symbol.ordinal()), x, 0, width, width, null);
			}
		};
	}

	@Override
	public IGameRenderer livesCounterRenderer() {
		return (Graphics2D g, PacManGame game) -> {
			for (int i = 0, x = Tile.SIZE; i < game.lives; ++i, x += 2 * Tile.SIZE) {
				g.drawImage(sprites.imageLivesCounter(), x, 0, null);
			}
		};
	}

	@Override
	public IGameRenderer pointsCounterRenderer() {
		PointsCounterRenderer r = new PointsCounterRenderer();
		r.setFont($font("font"));
		return r;
	}

	@Override
	public IPacManSounds sounds() {
		return ArcadeSounds.SOUNDS;
	}
}