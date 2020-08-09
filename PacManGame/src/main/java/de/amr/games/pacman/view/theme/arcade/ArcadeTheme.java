package de.amr.games.pacman.view.theme.arcade;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Symbol;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.api.IGameScoreRenderer;
import de.amr.games.pacman.view.api.IGhostRenderer;
import de.amr.games.pacman.view.api.IPacManRenderer;
import de.amr.games.pacman.view.api.IWorldRenderer;
import de.amr.games.pacman.view.api.PacManSounds;
import de.amr.games.pacman.view.common.MessagesRenderer;
import de.amr.games.pacman.view.common.PointsCounterRenderer;
import de.amr.games.pacman.view.core.AbstractTheme;
import de.amr.games.pacman.view.theme.PacManSpriteMap;

public class ArcadeTheme extends AbstractTheme {

	public static final ArcadeTheme THEME = new ArcadeTheme();

	private ArcadeSprites sprites = new ArcadeSprites();
	private Map<World, WorldSpriteMap> worldSprites = new HashMap<>();
	private Map<PacMan, PacManSpriteMap> pacManSprites = new HashMap<>();
	private Map<Ghost, GhostSpriteMap> ghostSprites = new HashMap<>();
	private MessagesRenderer messagesRenderer;

	private ArcadeTheme() {
		super("ARCADE");
		put("font", Assets.storeTrueTypeFont("PressStart2P", "themes/arcade/PressStart2P-Regular.ttf", Font.PLAIN, 8));
		put("maze-flash-sec", 0.4f);
		put("sprites", sprites);
		for (Symbol symbol : Symbol.values()) {
			put("symbol-" + symbol.name(), sprites.makeSprite_bonusSymbol(symbol.name()).frame(0));
		}
		for (int points : Game.POINTS_BONUS) {
			put("points-" + points, sprites.makeSprite_number(points).frame(0));
		}
		put("sounds", ArcadeSounds.SOUNDS);
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
		PacManSpriteMap spriteMap = pacManSprites.get(pacMan);
		if (spriteMap == null) {
			spriteMap = new PacManSpriteMap();
			pacManSprites.put(pacMan, spriteMap);
		}
		return new PacManRenderer(spriteMap);
	}

	@Override
	public IGhostRenderer ghostRenderer(Ghost ghost) {
		GhostSpriteMap spriteMap = ghostSprites.get(ghost);
		if (spriteMap == null) {
			spriteMap = new GhostSpriteMap(ghost);
			ghostSprites.put(ghost, spriteMap);
		}
		return new GhostRenderer(spriteMap);
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
	public IGameScoreRenderer levelCounterRenderer() {
		return new LevelCounterRenderer();
	}

	@Override
	public IGameScoreRenderer livesCounterRenderer() {
		return new LivesCounterRenderer();
	}

	@Override
	public IGameScoreRenderer pointsCounterRenderer() {
		PointsCounterRenderer renderer = new PointsCounterRenderer();
		renderer.setFont($font("font"));
		return renderer;
	}

	@Override
	public PacManSounds sounds() {
		return $value("sounds");
	}
}