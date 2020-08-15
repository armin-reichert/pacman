package de.amr.games.pacman.model.game;

import static de.amr.easy.game.Application.loginfo;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.model.world.api.Symbol;

/**
 * Data structure storing the level-specific values.
 * 
 * @author Armin Reichert
 */
public class GameLevel {

	private static Object[] levelData(int n) { // 1-based
		return Game.LEVEL_DATA[n <= Game.LEVEL_DATA.length ? n - 1 : Game.LEVEL_DATA.length - 1];
	}

	private static float percentage(Object value) {
		return ((int) value) / 100f;
	}

	private static int integer(Object value) {
		return (int) value;
	}

	// constant values from table
	public final Symbol bonusSymbol;
	public final int bonusValue;
	public final float pacManSpeed;
	public final float pacManDotsSpeed;
	public final float ghostSpeed;
	public final float ghostTunnelSpeed;
	public final int elroy1DotsLeft;
	public final float elroy1Speed;
	public final int elroy2DotsLeft;
	public final float elroy2Speed;
	public final float pacManPowerSpeed;
	public final float pacManPowerDotsSpeed;
	public final float ghostFrightenedSpeed;
	public final int pacManPowerSeconds;
	public final int numFlashes;

	public final int number;
	public final int foodCount;

	public int eatenFoodCount;
	public int ghostsKilledByEnergizer;
	public int ghostsKilled;
	public int lives;
	public int score;
	public Hiscore hiscore;
	public List<Symbol> counter;

	public GameLevel(int number, int lives, int score, int foodCount) {
		this(number, foodCount, levelData(number));
		this.lives = lives;
		this.score = score;
		counter = new ArrayList<>();
		hiscore = new Hiscore();
		hiscore.load();
	}

	public GameLevel(int number, GameLevel previous) {
		this(number, previous.foodCount, levelData(number));
		lives = previous.lives;
		score = previous.score;
		counter = previous.counter;
		hiscore = previous.hiscore;
	}

	private GameLevel(int number, int foodCount, Object[] data) {
		this.number = number;
		this.foodCount = foodCount;
		int i = 0;
		bonusSymbol = Symbol.valueOf((String) data[i++]);
		bonusValue = integer(data[i++]);
		pacManSpeed = percentage(data[i++]);
		pacManDotsSpeed = percentage(data[i++]);
		ghostSpeed = percentage(data[i++]);
		ghostTunnelSpeed = percentage(data[i++]);
		elroy1DotsLeft = integer(data[i++]);
		elroy1Speed = percentage(data[i++]);
		elroy2DotsLeft = integer(data[i++]);
		elroy2Speed = percentage(data[i++]);
		pacManPowerSpeed = percentage(data[i++]);
		pacManPowerDotsSpeed = percentage(data[i++]);
		ghostFrightenedSpeed = percentage(data[i++]);
		pacManPowerSeconds = integer(data[i++]);
		numFlashes = integer(data[i++]);
	}

	public int remainingFoodCount() {
		return foodCount - eatenFoodCount;
	}

	/**
	 * Score the given number of points and handles high score, extra life etc.
	 * 
	 * @param points points to score
	 * @return points scored
	 */
	public int score(int points) {
		int oldScore = score;
		score += points;
		if (oldScore < Game.POINTS_EXTRA_LIFE && Game.POINTS_EXTRA_LIFE <= score) {
			lives += 1;
		}
		hiscore.checkNewHiscore(this, score);
		return points;
	}

	/**
	 * Score points for eating an energizer.
	 * 
	 * @return points scored
	 */
	public int scoreEnergizerEaten() {
		eatenFoodCount += 1;
		ghostsKilledByEnergizer = 0;
		return score(Game.POINTS_ENERGIZER);
	}

	/**
	 * Score points for eating a simple pellet
	 * 
	 * @return points scored
	 */
	public int scoreSimplePelletEaten() {
		eatenFoodCount += 1;
		return score(Game.POINTS_SIMPLE_PELLET);
	}

	/**
	 * Scores for killing a ghost. Value of a killed ghost doubles if killed in series using the same
	 * energizer.
	 */
	public int scoreGhostKilled() {
		ghostsKilledByEnergizer += 1;
		ghostsKilled += 1;
		if (ghostsKilled == 16) {
			score(Game.POINTS_KILLED_ALL_GHOSTS);
		}
		int points = killedGhostPoints();
		loginfo("Scored %d points for killing %s ghost", points,
				new String[] { "", "first", "2nd", "3rd", "4th" }[ghostsKilledByEnergizer]);
		return score(points);
	}

	/**
	 * @return current value of a killed ghost. Value doubles for each ghost killed by the same
	 *         energizer.
	 */
	public int killedGhostPoints() {
		return Game.GHOST_BOUNTIES[ghostsKilledByEnergizer > 0 ? ghostsKilledByEnergizer - 1 : 0];
	}

	/**
	 * @return {@code true} if the number of eaten pellets causes the bonus to get active
	 */
	public boolean isBonusDue() {
		return eatenFoodCount == Game.BONUS_ACTIVATION_1 || eatenFoodCount == Game.BONUS_ACTIVATION_2;
	}
}