package de.amr.games.pacman.controller.game;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.controller.game.GameController.sec;

import java.util.Random;

import de.amr.games.pacman.controller.bonus.BonusFoodController;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.ArcadeBonus;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;

/**
 * Bonus food (fruits, symbols) appears at a dedicated maze position for around 9 seconds. When
 * consumed, the bonus is displayed for 3 seconds as a number representing its value and then it
 * disappears again.
 * 
 * @author Armin Reichert
 */
public class ArcadeBonusControl extends BonusFoodController {

	public ArcadeBonusControl(Game game, World world) {
		super(game, world);
		init();
	}

	@Override
	public long activationTicks() {
		return sec(Game.BONUS_SECONDS + new Random().nextFloat());
	}

	@Override
	public void activateBonus(Game game, World world) {
		ArcadeBonus bonus = ArcadeBonus.valueOf(game.level.bonusSymbol);
		bonus.setValue(game.level.bonusValue);
		world.showTemporaryFood(bonus, ArcadeWorld.BONUS_LOCATION);
		loginfo("Bonus %s activated for %.2f sec", bonus, state().getDuration() / 60f);
	}
}