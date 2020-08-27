package de.amr.games.pacman.model.game;

import static de.amr.easy.game.Application.loginfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure storing the level-specific values.
 * 
 * @author Armin Reichert
 */
public class GameLevel {

	private static float percent(Object value) {
		return (int) value / 100f;
	}

	private static int integer(Object value) {
		return (int) value;
	}

	public final String bonusSymbol;
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
	public int ghostsKilledInLevel;
	public int lives;
	public int score;
	public Hiscore hiscore;
	public List<String> counter;

	private ScoreResult scored = new ScoreResult(0, false);

	public GameLevel(int levelNumber, int foodCount, int lives, List<Object> data) {
		this(levelNumber, foodCount, lives, 0, new Hiscore(), new ArrayList<>(), data);
		hiscore.load();
	}

	public GameLevel(int levelNumber, int foodCount, GameLevel previous, List<Object> data) {
		this(levelNumber, foodCount, previous.lives, previous.score, previous.hiscore, previous.counter, data);
	}

	private GameLevel(int levelNumber, int foodCount, int lives, int score, Hiscore hiscore, List<String> counter,
			List<Object> data) {
		this.number = levelNumber;
		this.foodCount = foodCount;
		this.lives = lives;
		this.score = score;
		this.hiscore = hiscore;
		this.counter = counter;
		int i = 0;
		bonusSymbol = (String) data.get(i++);
		bonusValue = integer(data.get(i++));
		pacManSpeed = percent(data.get(i++));
		pacManDotsSpeed = percent(data.get(i++));
		ghostSpeed = percent(data.get(i++));
		ghostTunnelSpeed = percent(data.get(i++));
		elroy1DotsLeft = integer(data.get(i++));
		elroy1Speed = percent(data.get(i++));
		elroy2DotsLeft = integer(data.get(i++));
		elroy2Speed = percent(data.get(i++));
		pacManPowerSpeed = percent(data.get(i++));
		pacManPowerDotsSpeed = percent(data.get(i++));
		ghostFrightenedSpeed = percent(data.get(i++));
		pacManPowerSeconds = integer(data.get(i++));
		numFlashes = integer(data.get(i++));
		counter.add(bonusSymbol);
	}

	public int remainingFoodCount() {
		return foodCount - eatenFoodCount;
	}

	/**
	 * Score the given number of points and handles high score and extra life.
	 * 
	 * @param points points to score
	 * @return score result
	 */
	private ScoreResult score(int points) {
		scored.points = points;
		scored.extraLife = score < Game.POINTS_EXTRA_LIFE && score + points >= Game.POINTS_EXTRA_LIFE;
		score += scored.points;
		lives += scored.extraLife ? 1 : 0;
		hiscore.check(number, score);
		return scored;
	}

	/**
	 * Score eaten bonus.
	 * 
	 * @return score result
	 */
	public ScoreResult scoreBonus() {
		return score(bonusValue);
	}

	/**
	 * Score points for eating an energizer.
	 * 
	 * @return points scored
	 */
	public ScoreResult scoreEnergizerEaten() {
		eatenFoodCount += 1;
		ghostsKilledByEnergizer = 0;
		return score(Game.POINTS_ENERGIZER);
	}

	/**
	 * Score points for eating a simple pellet
	 * 
	 * @return points scored
	 */
	public ScoreResult scoreSimplePelletEaten() {
		eatenFoodCount += 1;
		return score(Game.POINTS_PELLET);
	}

	/**
	 * Scores for killing a ghost. Value of a killed ghost doubles if killed in series using the same
	 * energizer.
	 */
	public ScoreResult scoreGhostKilled() {
		int points = 0;
		ghostsKilledByEnergizer += 1;
		ghostsKilledInLevel += 1;
		if (ghostsKilledInLevel == 16) {
			points += Game.POINTS_ALL_GHOSTS;
		}
		int ghostBounty = ghostBounty();
		loginfo("Got %d points for killing %s ghost", ghostBounty,
				new String[] { "", "first", "2nd", "3rd", "4th" }[ghostsKilledByEnergizer]);
		points += ghostBounty;
		return score(points);
	}

	/**
	 * @return value of killed ghost. Value doubles for each ghost killed by the same energizer.
	 */
	public int ghostBounty() {
		return Game.POINTS_GHOSTS[ghostsKilledByEnergizer > 0 ? ghostsKilledByEnergizer - 1 : 0];
	}

	/**
	 * @return {@code true} if the number of eaten pellets causes the bonus to get active
	 */
	public boolean isBonusDue() {
		return eatenFoodCount == Game.BONUS_ACTIVATION_1 || eatenFoodCount == Game.BONUS_ACTIVATION_2;
	}
}