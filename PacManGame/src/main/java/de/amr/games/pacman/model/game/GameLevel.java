package de.amr.games.pacman.model.game;

import static de.amr.easy.game.Application.loginfo;

import java.util.List;

import de.amr.games.pacman.model.world.api.Symbol;

/**
 * Data structure storing the level-specific values.
 * 
 * @author Armin Reichert
 */
public class GameLevel {

	private static float percentage(Object value) {
		return ((int) value) / 100f;
	}

	private static int integer(Object value) {
		return (int) value;
	}

	// constant values from table
	public Symbol bonusSymbol;
	public int bonusValue;
	public float pacManSpeed;
	public float pacManDotsSpeed;
	public float ghostSpeed;
	public float ghostTunnelSpeed;
	public int elroy1DotsLeft;
	public float elroy1Speed;
	public int elroy2DotsLeft;
	public float elroy2Speed;
	public float pacManPowerSpeed;
	public float pacManPowerDotsSpeed;
	public float ghostFrightenedSpeed;
	public int pacManPowerSeconds;
	public int numFlashes;

	public int number;
	public int totalFoodCount;
	public int eatenFoodCount;
	public int ghostsKilledByEnergizer;
	public int ghostsKilled;
	public int lives;
	public int score;
	public Hiscore hiscore;
	public List<Symbol> counter;

	public GameLevel(int n, int totalFoodCount, Object[] parameters) {
		this.number = n;
		this.totalFoodCount = totalFoodCount;

		int i = 0;
		bonusSymbol = Symbol.valueOf((String) parameters[i++]);
		bonusValue = integer(parameters[i++]);
		pacManSpeed = percentage(parameters[i++]);
		pacManDotsSpeed = percentage(parameters[i++]);
		ghostSpeed = percentage(parameters[i++]);
		ghostTunnelSpeed = percentage(parameters[i++]);
		elroy1DotsLeft = integer(parameters[i++]);
		elroy1Speed = percentage(parameters[i++]);
		elroy2DotsLeft = integer(parameters[i++]);
		elroy2Speed = percentage(parameters[i++]);
		pacManPowerSpeed = percentage(parameters[i++]);
		pacManPowerDotsSpeed = percentage(parameters[i++]);
		ghostFrightenedSpeed = percentage(parameters[i++]);
		pacManPowerSeconds = integer(parameters[i++]);
		numFlashes = integer(parameters[i++]);
	}

	public int remainingFoodCount() {
		return totalFoodCount - eatenFoodCount;
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
	 * Score points for finding an energizer.
	 * 
	 * @return points scored
	 */
	public int scoreEnergizerFound() {
		eatenFoodCount += 1;
		ghostsKilledByEnergizer = 0;
		return score(Game.POINTS_ENERGIZER);
	}

	/**
	 * Score points for finding a simple pellet
	 * 
	 * @return points scored
	 */
	public int scoreSimplePelletFound() {
		eatenFoodCount += 1;
		return score(Game.POINTS_SIMPLE_PELLET);
	}

	/**
	 * Scores for killing a ghost. Value of a killed ghost doubles if killed in series using the same
	 * energizer.
	 * 
	 * @param ghostName killed ghost's name
	 */
	public int scoreGhostKilled(String ghostName) {
		ghostsKilledByEnergizer += 1;
		ghostsKilled += 1;
		if (ghostsKilled == 16) {
			score(Game.POINTS_KILLED_ALL_GHOSTS);
		}
		int points = killedGhostPoints();
		loginfo("Scored %d points for killing %s (%s ghost in sequence)", points, ghostName,
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