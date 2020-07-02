package de.amr.games.pacman.view.play;

import static de.amr.games.pacman.view.play.SimplePlayView.MazeMode.CROWDED;
import static de.amr.games.pacman.view.play.SimplePlayView.MazeMode.EMPTY;
import static de.amr.games.pacman.view.play.SimplePlayView.MazeMode.FLASHING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.function.Function;

import de.amr.easy.game.ui.sprites.CyclicAnimation;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteAnimation;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Door.DoorState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.play.SimplePlayView.MazeMode;
import de.amr.games.pacman.view.theme.Theme;
import de.amr.statemachine.core.StateMachine;

/**
 * Inner class realizing the maze view which can be in any of states EMPTY, CROWDED or FLASHING.
 */
public class MazeView extends StateMachine<MazeMode, Void> implements View {

	private final World world;
	private final Theme theme;
	public final Sprite spriteEmptyMaze, spriteFullMaze, spriteFlashingMaze;
	public final SpriteAnimation energizersBlinking;
	private Function<Tile, Color> fnTileColor;

	public MazeView(World world, Theme theme) {
		super(MazeMode.class);
		this.world = world;
		this.theme = theme;
		fnTileColor = tile -> Color.BLACK;
		spriteFullMaze = theme.spr_fullMaze();
		spriteEmptyMaze = theme.spr_emptyMaze();
		spriteFlashingMaze = theme.spr_flashingMaze();
		energizersBlinking = new CyclicAnimation(2);
		energizersBlinking.setFrameDuration(150);
		//@formatter:off
		beginStateMachine()
			.description("[Maze View]")
			.initialState(CROWDED)
			.states()
				.state(CROWDED)
					.onEntry(() -> energizersBlinking.setEnabled(false))
					.onTick(() -> energizersBlinking.update())
			.transitions()
		.endStateMachine();
		//@formatter:on
		getTracer().setLogger(PacManStateMachineLogging.LOGGER);
	}

	@Override
	public void draw(Graphics2D g) {
		if (getState() == CROWDED) {
			drawMaze(g);
		} else if (getState() == EMPTY) {
			spriteEmptyMaze.draw(g, 0, 3 * Tile.SIZE);
		} else if (getState() == FLASHING) {
			spriteFlashingMaze.draw(g, 0, 3 * Tile.SIZE);
		}
	}

	public void setTileColor(Function<Tile, Color> fnTileColor) {
		this.fnTileColor = fnTileColor;
	}

	private void drawMaze(Graphics2D g) {
		spriteFullMaze.draw(g, 0, 3 * Tile.SIZE);
		// hide eaten food
		world.habitatTiles().filter(world::containsEatenFood).forEach(tile -> {
			g.setColor(fnTileColor.apply(tile));
			g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
		});
		// simulate energizer blinking animation
		if (energizersBlinking.currentFrameIndex() == 1) {
			world.habitatTiles().filter(world::containsEnergizer).forEach(tile -> {
				g.setColor(fnTileColor.apply(tile));
				g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE);
			});
		}
		// draw bonus when active or consumed
		world.getBonus().filter(bonus -> bonus.state != BonusState.INACTIVE).ifPresent(bonus -> {
			Sprite sprite = bonus.state == BonusState.CONSUMED ? theme.spr_number(bonus.value)
					: theme.spr_bonusSymbol(bonus.symbol);
			g.drawImage(sprite.frame(0), world.bonusTile().x(), world.bonusTile().y() - Tile.SIZE / 2, null);
		});
		// draw doors depending on their state
		world.theHouse().doors().filter(door -> door.state == DoorState.OPEN).forEach(door -> {
			g.setColor(Color.BLACK);
			door.tiles.forEach(tile -> g.fillRect(tile.x(), tile.y(), Tile.SIZE, Tile.SIZE));
		});
	}
}