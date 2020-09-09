package de.amr.games.pacman.view.theme.arcade;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.PacManGame;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.view.api.IGameRenderer;
import de.amr.games.pacman.view.api.IGhostRenderer;
import de.amr.games.pacman.view.api.IPacManRenderer;
import de.amr.games.pacman.view.api.IWorldRenderer;
import de.amr.games.pacman.view.api.PacManSounds;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.common.MessagesRenderer;
import de.amr.games.pacman.view.common.PointsCounterRenderer;
import de.amr.games.pacman.view.core.ThemeParameters;

/**
 * This theme mimics the original Arcade version.
 * 
 * @author Armin Reichert
 */
public class ArcadeTheme extends ThemeParameters implements Theme {

	public static final ArcadeTheme THEME = new ArcadeTheme();

	private ArcadeSprites sprites = new ArcadeSprites();
	private Map<World, WorldSpriteMap> worldSprites = new HashMap<>();
	private Map<PacMan, SpriteMap> pacManSprites = new HashMap<>();
	private Map<Ghost, SpriteMap> ghostSprites = new HashMap<>();
	private MessagesRenderer messagesRenderer;

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
			for (GhostPersonality personality : GhostPersonality.values()) {
				map.set(ghostSpriteKeyColor(personality, dir), sprites.makeSprite_ghostColored(personality, dir));
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

	String ghostSpriteKeyColor(GhostPersonality personality, Direction dir) {
		return String.format("colored-%s-%s", personality, dir);
	}

	String ghostSpriteKeyEyes(Direction dir) {
		return String.format("eyes-%s", dir);
	}

	String ghostSpriteKeyPoints(int points) {
		return String.format("points-%d", points);
	}

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

	@Override
	public String name() {
		return "ARCADE";
	}

	@Override
	public IWorldRenderer worldRenderer(World world) {
		WorldSpriteMap spriteMap = worldSprites.get(world);
		if (spriteMap == null) {
			spriteMap = new WorldSpriteMap(world);
			worldSprites.put(world, spriteMap);
		}
		return new WorldRenderer(spriteMap);
	}

	@Override
	public IPacManRenderer pacManRenderer(PacMan pacMan) {
		return new PacManRenderer();
	}

	@Override
	public IGhostRenderer ghostRenderer(Ghost ghost) {
		return new GhostRenderer();
	}

	@Override
	public MessagesRenderer messagesRenderer() {
		if (messagesRenderer == null) {
			messagesRenderer = new MessagesRenderer();
			messagesRenderer.setFont($font("font"));
		}
		return messagesRenderer;
	}

	@Override
	public IGameRenderer levelCounterRenderer() {
		return new LevelCounterRenderer();
	}

	@Override
	public IGameRenderer livesCounterRenderer() {
		return new LivesCounterRenderer();
	}

	@Override
	public IGameRenderer pointsCounterRenderer() {
		return new PointsCounterRenderer($font("font"));
	}

	@Override
	public PacManSounds sounds() {
		return ArcadeSounds.SOUNDS;
	}
}